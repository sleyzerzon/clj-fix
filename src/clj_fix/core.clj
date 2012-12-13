(ns clj-fix.core
  (:use clj-fix.connection.protocol)
  (:use fix-translator.core)
  (:use clj-fix-oms.core)
  (:require (clojure [string :as s])
            (lamina [core :as l])
            (aleph [tcp :as a])
            (gloss [core :as g])
            (clj-time [core :as t] [format :as f])))

(def ^:const msg-delimiter #"10=\d{3}\u0001")
(def ^:const msg-identifier #"(?<=10=\d{3}\u0001)")
(def ^:const seq-num-tag 34)

(defrecord Conn [venue host port sender target channel in-seq-num
                out-seq-num msg-handler next-msg msg-fragment translate?])

(defn timestamp
  ; Returns a UTC timestamp in a specified format.
  ([]
    (timestamp "yyyyMMdd-HH:mm:ss"))
  ([format]
    (f/unparse (f/formatter format) (t/now))))

(def sessions (atom {}))

(defn- error [msg]
  (throw (Exception. msg)))

(defn get-session [id]
  ((:id id) @sessions))

(defn get-channel [session]
  @@(:channel session))

(defn open-channel? [session]
  (and (not= nil @(:channel session)) (not (l/closed? (get-channel session)))))

(defn- create-channel [session]
  (if (not (open-channel? session))
    (do
      (reset! (:channel session) (a/tcp-client {:host (:host session),
                                                :port (:port session),
                                                :frame (g/string :ascii)}))
      (try (get-channel session)
        (catch java.net.ConnectException e
          (reset! (:channel session) nil)
          (println (.getMessage e)))))
    (error "Channel already open.")))

(defn segment-msg
  ; Takes a TCP segment and splits it into individual FIX messages. If the last
  ; message in the collection is complete, appends a blank
  [msg]
  (let [segments (s/split msg msg-identifier)]
    (if (re-find msg-delimiter (peek segments))
      (conj segments "")
      segments)))

(defn gen-msg-sig [session]
  (let [{:keys [out-seq-num sender target]} session]
    [:msg-seq-num (inc @out-seq-num)
     :sender-comp-id sender
     :target-comp-id target
     :sending-time (timestamp)]))

(defn send-msg [session msg-type msg-body]
  (let [msg (reduce #(apply conj % %2) [[:msg-type msg-type]
                                         (gen-msg-sig session) msg-body])
       encoded-msg (encode-msg (:venue session) msg)]
    (do
      (println "clj-fix sending" encoded-msg)
      (l/enqueue (get-channel session) encoded-msg)
      (swap! (:out-seq-num session) inc))))
    

(defn update-next-msg [old-msg new-msg] new-msg)

(defn update-user [session payload]
  (send (:next-msg session) update-next-msg payload))

(defn transmit-heartbeat
  ([session]
    (send-msg session :heartbeat [[]]))
  ([session test-request-id]
    (send-msg session :heartbeat [:test-request-id test-request-id])))

(defn msg-handler [id msg]
  ; Need to:
  ; 1) Update inbound sequence number.
  ; 2) Get more useful information for unknown order message types.
  ; 3) Significant refactoring.
  (let [session (get-session id)
        msg-fragment (:msg-fragment session)
        segments (segment-msg msg)
        msgs (assoc segments 0 (s/join "" [@msg-fragment (first segments)]))
        inbound-seq-num (Integer/parseInt (extract-tag-value seq-num-tag
                                                             (first msgs)))
        cur-seq-num (inc @(:in-seq-num session))]

    ; This makes an assumption that, in a block of messages, if the
    ; sequence number of the first message is as expected, then the rest will
    ; be as well.
    (if (= cur-seq-num inbound-seq-num)
      (if-let [msgs-to-proc (butlast msgs)]
        (doseq [m msgs-to-proc]
          (let [venue (:venue session)
                msg-type (get-msg-type venue m)
                _ (swap! (:in-seq-num session) inc)]
            (println "clj-fix received" m)
            (case msg-type
              :logon (update-user session {:msg-type msg-type
                                           :sender-comp-id (:sender-comp-id
                                                             (decode-msg venue
                                                              msg-type m))})
              :heartbeat (transmit-heartbeat session)

              :test-request (transmit-heartbeat session (:test-request-id
                              (decode-msg venue msg-type m)))

              :reject (println "SESSION REJECT")

              :execution-report (if @(:translate? session)
                                  (update-user session (merge 
                                                        {:msg-type msg-type}
                                                        (decode-msg venue
                                                         msg-type m)))
                                  (update-user session {:msg-type msg-type
                                                        :report m}))

              :order-cancel-reject (println "ORDER CANCEL REJECT")

              :indication-of-interest (println "INDICATION OF INTEREST")

              :logout (update-user session {:msg-type msg-type
                                            :sender-comp-id (:sender-comp-id
                                                              (decode-msg venue
                                                              msg-type m))})

              :resend-request (println "RESEND REQUEST")
            
              :seq-reset (println "SEQUENCE RESET")
              :unknown-msg-type (println "UNKNOWN MSG TYPE"))
  
            (reset! msg-fragment (peek msgs)))))
       ;(println [inbound-seq-num cur-seq-num]))))

      (send-msg session :resend-request [:begin-seq-num cur-seq-num
                                         :ending-seq-num 0]))))

(defn gen-msg-handler [id]
  (fn [msg]
    (msg-handler id msg)))

(defn replace-with-map-val [tag-value-vec tag-value-map]
  (for [e (partition 2 tag-value-vec)]
    (if-let [shared-key (find tag-value-map (first e))]
      shared-key
      e)))

(defn merge-params [required-tags-values additional-params]
  (let [reqd-keys (set (take-nth 2 required-tags-values))
        ts (apply concat (replace-with-map-val required-tags-values
                                               additional-params))]
    (reduce
      (fn [lst [tag value]]
        (if (contains? reqd-keys tag)
          lst
          (conj lst tag value))) (vec ts) (vec additional-params))))

(defn connect 
  ([id translate-returning-msgs]
  (if-let [session (get-session id)]
    (do
      (reset! (:translate? session) translate-returning-msgs)
      (create-channel session)
      (l/receive-all (get-channel session) #((gen-msg-handler id) %)))
    (error (str "Session " (:id id) " not found. Please create it first.")))))

(defrecord FixConn [id]
  Connection
  (logon [id msg-handler heartbeat-interval reset-seq-num
          translate-returning-msgs]
    
    (let [session (get-session id)]
      (if (not (open-channel? session))
        (connect id translate-returning-msgs))
      (if (= reset-seq-num :yes) 
        (do (reset! (:out-seq-num session) 0)
            (reset! (:in-seq-num session) 0)))
      (add-watch (:next-msg session) :user-callback msg-handler)
      (send-msg session :logon [:heartbeat-interval 0
                                :reset-seq-num reset-seq-num
                                :encrypt-method :none])))

  (new-order [id side size instrument-symbol price]
    (new-order id side size instrument-symbol price nil))
    
  (new-order [id side size instrument-symbol price additional-params]
    (let [session (get-session id)
          required-tags-values [:client-order-id (str (gensym (name (:id id))))
                                :hand-inst :private :order-qty size
                                :order-type :limit :price price
                                :side side :symbol instrument-symbol
                                :transact-time (timestamp)]]
      (if-let [addns additional-params]
        (send-msg session :new-order-single (merge-params required-tags-values
                                                          additional-params))
        (send-msg session :new-order-single required-tags-values))))

  (cancel [id order]
    (if (not= nil order)
      (let [session (get-session id)
            orig-client-order-id (:client-order-id order)
            required-tags-values (into (vec (mapcat
                                     #(find order %)
              [:client-order-id :order-qty :side :symbol
               :transact-time])) [:orig-client-order-id orig-client-order-id])]
        (send-msg session :order-cancel-request required-tags-values))))

  (cancel-replace [id order]
    (cancel-replace id order nil))

  (cancel-replace [id order additional-params]
    (let [session (get-session id)
          orig-client-order-id (:client-order-id order)
          required-tags-values (into
            [:client-order-id (str (gensym (name (:id id))))
             :orig-client-order-id orig-client-order-id]
            (vec (mapcat #(find order %)
              [:hand-inst :order-qty :order-type :price :side :symbol
               :transact-time])))]
      (if-let [addns additional-params]
        (send-msg session :order-cancel-replace-request (merge-params
                                                   required-tags-values
                                                   additional-params))
        (send-msg session :order-cancel-replace-request required-tags-values))))

  (order-status [id order])

  (logout [id reason]
    (send-msg (get-session id) :logout [:text reason])))

(defn new-fix-session [venue host port sender target]
  {:pre [(keyword? venue) (every? string? [host sender target])
         (integer? port)]}
  (if (load-spec venue)
    (let [id (keyword (str sender "-" target))]
      (if (not (contains? @sessions id))
        (do
          (swap! sessions assoc id (Conn. venue host port sender target
                                          (atom nil) (atom 0) (atom 0)
                                          (atom nil) (agent {}) (atom "")
                                          (atom nil)))
        (FixConn. id))
      (error (str "Session " id " already exists. Please close it first."))))
    (error (str "Spec for " venue " failed to load."))))

(defn disconnect [id]
  (if-let [session (get-session id)]
    (if (open-channel? session)
      (l/close (get-channel session)))
  (error (str "Session " id " not found."))))

; This should output the session's details to a file first to aid message
; recovery.
(defn end-session [id]
  (if-let [session (get-session id)]
    (do
      (disconnect id)
      (swap! sessions dissoc (:id id))
      true)
    (error (str "Session " id " not found."))))

; This is strictly for utility
(defn close-all []
    (reset! sessions {}))

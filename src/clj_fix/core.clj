(ns clj-fix.core
  (:use clj-fix.connection.protocol)
  (:use fix-translator.core)
  (:require (clojure [string :as s])
            (lamina [core :as l])
            (aleph [tcp :as a])
            (gloss [core :as g])
            (clj-time [core :as t] [format :as f])))

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

(defn gen-msg-sig [session]
  (let [{:keys [out-seq-num sender target]} session]
    {:msg-seq-num (swap! out-seq-num inc)
     :sender-comp-id sender
     :target-comp-id target
     :transact-time (timestamp)}))

(defn send-msg [session msg-type msg-body]
  (let [msg (into {:msg-type msg-type} [(gen-msg-sig session) msg-body])]
    (encode-msg (:venue session) msg)))

(defn gen-msg-handler [id]
  (fn [msg] (println msg)))

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
      (send-msg session :logon {:heartbeat-interval 0
                                :reset-seq-num reset-seq-num
                                :encrypt-method :none})))

  (buy [id size instrument-symbol price & additional-params])

  (sell [id size instrument-symbol price & additional-params])

  (cancel [id order])

  (cancel-replace [id order & additional-params])

  (order-status [id order])

  (logout [id reason]))

(defn new-fix-session [venue host port sender target]
  {:pre [(keyword? venue) (every? string? [host sender target])
         (integer? port)]}
  (if (load-spec venue)
    (let [id (keyword (str sender "-" target))]
      (if (not (contains? @sessions id))
        (do
          (swap! sessions assoc id (Conn. venue host port sender target
                                          (atom nil) (atom 0) (atom 0)
                                          (atom nil) (agent "") (atom "")
                                          (atom nil)))
        (FixConn. id))
      (error (str "Session " id " already exists. Please close it first."))))
    (error (str "Spec for " venue " failed to load."))))

; This should output the session's details to a file first to aid message
; recovery.
(defn end-session [id]
  (if-let [session (get-session id)]
    (do
      (if (open-channel? session)
        (l/close (get-channel session)))
      (swap! sessions dissoc (:id id))
       true)
    (error (str "Session " id " not found."))))

; This is strictly for utility
(defn close-all []
    (reset! sessions {}))

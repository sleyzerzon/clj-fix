(ns clj-fix.core
  (:use clj-fix.connection.protocol)
  (:use fix-translator.core)
  (:require (clojure [string :as s])
            (lamina [core :as l])
            (aleph [tcp :as a])
            (gloss [core :as g])))

(defrecord Conn [venue host port sender target channel in-seq-num
                out-seq-num msg-handler next-msg msg-fragment translate?])

(def sessions (atom {}))

(defn- error [msg]
  (throw (Exception. msg)))

(defn get-session [id]
  ((:id id) @sessions))

(defn get-channel [session]
  @@(:channel session))

(defn open-channel? [session]
  (and (not nil? @(:channel session)) (not (l/closed? (get-channel session)))))

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

(defn gen-msg-handler [id]
  (fn [msg] (println msg)))

(defn connect 
  ([id]
  (connect id false))

  ([id translate-returning-msgs]
  (if-let [session (get-session id)]
    (do
      (reset! (:translate? session) translate-returning-msgs)
      (create-channel session)
      (l/receive-all (get-channel session) #((gen-msg-handler id) %)))
    (error (str "Session " (:id id) " not found. Please create it first.")))))

(defrecord FixConn [id]
  Connection
  (logon [id msg-handler heartbeat-interval])

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

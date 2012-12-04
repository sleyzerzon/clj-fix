(ns clj-fix.core
  (:use clj-fix.connection.protocol)
  (:use fix-translator.core)
  (:require (clojure [string :as s])
            (lamina [core :as l])
            (aleph [tcp :as a])
            (gloss [core :as g])))

(defrecord Conn [venue host port sender target channel in-seq-num
                out-seq-num msg-handler next-msg msg-fragment])

(def sessions (atom {}))

(defn- error [msg]
  (throw (Exception. msg)))

(defn get-session [id]
  ((:id id) @sessions))

(defn get-channel [session]
  @@(:channel session))

(defn open-channel? [session]
  (and (not= nil @(:channel session)) (not (l/closed? (get-channel session)))))

(defrecord FixConn [id]
  Connection
  (connect [id])

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
                                          (atom nil) (agent "") (atom "")))
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

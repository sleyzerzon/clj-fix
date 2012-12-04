(ns clj-fix.test.core
  (:use [clj-fix.core])
  (:use [clojure.test]))

(defn call [f] (try f (catch Exception e (type e))))

(def ^:const port-s 7777)

(deftest session-management
  
  ; Create a new session.]
  (let [nyse-session (call (new-fix-session :test-market "local" port-s "me"
                                            "you"))]
    (is (= (:id nyse-session) :me-you)))

  ; Create a session with the identical sender and target arguments.
  (is (thrown? Exception 
      (call (new-fix-session :test-market "local" port-s "me" "you"))))

  ; Create another session.
  (let [nasdaq-session (call (new-fix-session :test-market "foreign" port-s
                                              "him" "her"))]
    (is (= (:id nasdaq-session) :him-her)))

  ; Check there are two sessions.
  (is (= (set (keys @sessions)) #{:me-you :him-her}))

    ; Remove a valid session.
  (let [cme-session (call (new-fix-session :test-market "galactic" port-s "up"
                                           "down"))]
    (is (= (:id cme-session) :up-down))
    (is (= (set (keys @sessions)) #{:me-you :him-her :up-down}))
    (is (= true (end-session cme-session)))
    (is (= (set (keys @sessions)) #{:me-you :him-her})))

    ; Remove a non-existant session.
  (is (thrown? Exception (end-session {:id :non-existant})))

  ; Re-create the previously removed session.
  (let [cme-session (call (new-fix-session :test-market "galactic" port-s "up"
                                           "down"))]
    (is (= (set (keys @sessions)) #{:me-you :him-her :up-down})))

  (close-all))





  

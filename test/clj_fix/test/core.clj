(ns clj-fix.test.core
  (:use [clj-fix.core])
  (:use [clojure.test])
  (:require (clojure [string :as s])
            (lamina [core :as l])
            (aleph [tcp :as a])
            (gloss [core :as g]))
  (:import clj_fix.core.FixConn))

(def msgs {
  :logon "8=FIX.4.2\u00019=76\u000135=A\u000134=702\u000149=ABC\u000152=20100130-10:52:40.663\u000156=XYZ\u000195=4\u000196=1234\u000198=0\u0001108=60\u000110=134\u0001"
  :heartbeat "8=FIX.4.2\u00019=51\u000135=0\u000134=703\u000149=ABC\u000152=20100130-10:53:40.830\u000156=XYZ\u000110=249\u0001"
  :test-request "8=FIX.4.2\u00019=076\u000135=1\u000152=20121024-06:25:58\u000149=ABC\u000156=XYZ\u000134=2\u0001112=BI_TEST\u000110=237\u0001"
  :execution-report "8=FIX.4.2\u00019=266\u000135=8\u000149=ABC\u000156=XYZ\u000150=AZ12\u000157=NA\u000134=833\u000152=20100130-08:00:51.992\u000155=GLD\u000148=PL11YA\u0001167=FUT\u0001207=LIFFE\u00011=AA1\u000137=ABC1\u000117=INDNTHDOG\u000158=Fill\u0001200=201009\u0001205=13\u000132=25\u0001151=0\u000114=25\u000154=2\u000140=2\u000177=O\u000159=0\u0001150=2\u000120=0\u000139=2\u0001442=1\u000144=99.06\u000138=25\u000131=99.06\u00016=99.06\u000160=20100130-08:00:52\u000110=136\u0001"})

(defn echo-handler [channel client-info]
  (l/siphon channel channel))

(defn create-test-server
  "Create a simple echo server to connect to."
  [host port]
  (a/start-tcp-server echo-handler {:port port}))

(deftest channel
  (let [conn-a (load-client :test-client-a)
       {:keys [host port]} (get-session conn-a)
        server-a (create-test-server host port)]
  
  (connect conn-a false)

  ; Check if the channel is open.
  (is (= true (open-channel? (get-session conn-a))))

  ; Try to re-create the channel.
  (is (thrown? Exception (create-channel (get-session conn-a))))

  ; Close the channel.
  (disconnect conn-a)
  (is (= false (open-channel? (get-session conn-a))))
 
  ; Re-open the channel.
  (connect conn-a false)
  (is (= true (open-channel? (get-session conn-a))))

  ; Connect with a garbage id.
  (is (thrown? Exception (connect "abc" false)))

  ; Connect with a nonexistant session.
  (let [rogue-session (FixConn. :nonsession)]
    (is (thrown? Exception (connect rogue-session false))))

  (let [conn-b (load-client :test-client-b)]

    ; Connect to a non-existant server.
    (is (thrown? Exception (connect conn-b false)))

    (let [{:keys [host port]} (get-session conn-b)
          server-b (create-test-server host port)]

      (connect conn-b false)
      (is (= true (open-channel? (get-session conn-b))))
      (server-b)))

  (server-a)
  (close-all)))

(deftest segment-msg-t
  (let [msg (:execution-report msgs)
        msg-fragment (apply str (take 100 msg))]
  
    ; Segment a block with only one complete message.
    (is (= [msg ""] (segment-msg msg)))

    ; Segment a block with one complete message and a message fragment.
    (let [test-msg (s/join "" [msg msg-fragment])]
      (is (= [msg msg-fragment] (segment-msg test-msg))))

    ; Segment a block with two complete messages.
    (let [test-msg (s/join "" [msg msg])]
      (is (= [msg msg ""] (segment-msg test-msg))))

    ; Segment a block with one msg-fragment.
    (is (= [msg-fragment] (segment-msg msg-fragment)))))

(deftest session-management
  
  ; Create a new session.
  (let [nyse-session (load-client :test-client-a)]
    (is (= (:id nyse-session) :me-you)))

  ; Create a session with the identical sender and target arguments.
  (is (thrown? Exception (load-client :test-client-a)))

  ; Create another session.
  (let [nasdaq-session (load-client :test-client-b)]
    (is (= (:id nasdaq-session) :him-her)))

  ; Check there are two sessions.
  (is (= (set (keys @sessions)) #{:me-you :him-her}))

  ; Remove a valid session.
  (let [cme-session (load-client :test-client-c)]
    (is (= (:id cme-session) :up-down))
    (is (= (set (keys @sessions)) #{:me-you :him-her :up-down}))
    (is (= true (end-session cme-session)))
    (is (= (set (keys @sessions)) #{:me-you :him-her})))

  ; Remove a non-existant session.
  (is (thrown? Exception (end-session {:id :non-existant})))

  ; Re-create the previously removed session.
  (let [cme-session (load-client :test-client-c)]
    (is (= (set (keys @sessions)) #{:me-you :him-her :up-down})))

  (close-all))

(deftest replace-with-map-val-t
  (let [tag-value-coll [:this :a :that :b :other :c]]

  ; No matches against empty map.
  (is (= '((:this :a) (:that :b) (:other :c))
          (replace-with-map-val tag-value-coll {})))

  ; No matches against populated map.
  (is (= '((:this :a) (:that :b) (:other :c))
          (replace-with-map-val tag-value-coll {:no-match :d})))

  ; One match.
  (is (= '((:this :a) (:that :b) (:other :d))
          (replace-with-map-val tag-value-coll {:other :d})))

  ; Two matches.
  (is (= '((:this :a) (:that :e) (:other :d))
          (replace-with-map-val tag-value-coll {:other :d :that :e})))

  ; All matches.
  (is (= '((:this :f) (:that :e) (:other :d))
          (replace-with-map-val tag-value-coll {:other :d :this :f :that :e})))

  ; Empty tag-value collection.
  (is (= '() (replace-with-map-val [] {:this :a})))

  ; Empty tag-value collection and map.
  (is (= '() (replace-with-map-val [] {})))))

(deftest merge-params-t
  (let [tag-value-coll [:this :a :that :b :other :c]]

    ; No additional params.
    (is (= [:this :a :that :b :other :c] (merge-params tag-value-coll nil)))

    ; Additional params with no match.
    (is (= [:this :a :that :b :other :c :neither :d]
           (merge-params tag-value-coll {:neither :d})))

    ; Additional params with one match.
    (is (= [:this :a :that :b :other :e :neither :d]
           (merge-params tag-value-coll {:neither :d :other :e})))

    ; Additional params with all matches.
    (is (= [:this :g :that :f :other :e :neither :d]
      (merge-params tag-value-coll {:neither :d :other :e :that :f
                                    :this :g})))))

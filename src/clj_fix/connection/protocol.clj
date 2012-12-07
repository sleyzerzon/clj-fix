(ns clj-fix.connection.protocol)

(defprotocol Connection
  (logon [id msg-handler heartbeat-interval reset-seq-num-flag
          translate-returning-msgs])
  (new-order [id side size instrument-symbol price]
             [id side size instrument-symbol price additional-params])
  (cancel [id order])
  (cancel-replace [id order & additional-params])
  (order-status [id order])
  (logout [id reason]))



(ns clj-fix.connection.protocol)

(defprotocol Connection
  (logon [id msg-handler heartbeat-interval])
  (buy [id size instrument-symbol price & additional-params])
  (sell [id size instrument-symbol price & additional-params])
  (cancel [id order])
  (cancel-replace [id order & additional-params])
  (order-status [id order])
  (logout [id reason]))



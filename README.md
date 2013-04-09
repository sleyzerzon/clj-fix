# clj-fix

## What is it?
clj-fix is a toolkit that makes it easy for you to create your own [FIX](http://www.fixprotocol.org/what-is-fix.shtml) client.

From [fixprotocol.org](http://www.fixprotocol.org/what-is-fix.shtml)
>The Financial Information eXchange ("FIX") Protocol is a series of messaging specifications for the electronic communication of trade-related messages. It has been developed through the collaboration of banks, broker-dealers, exchanges, industry utilities and associations, institutional investors, and information technology providers from around the world.


In addition to sending and receiving order messages, a FIX application must handle a number of other tasks including message parsing, heartbeating, dealing with admin messages, forming FIX messages, and more. clj-fix takes care of the following for you:

- Initiating and maintaining a connection to your destination
- Segmenting a stream of bytes into individual FIX messages
- Maintaining inbound and outbound sequence numbers
- Handling most admin messages
- Providing a means to specify an order message in a vendor-neutral, user-friendly manner, which clj-fix will translate into a proper FIX message using your particular destination's specification and vice-versa using [fix-translator](https://github.com/nitinpunjabi/fix-translator).

See [Steps to Create a FIX Client Using clj-fix](https://github.com/nitinpunjabi/clj-fix#steps-to-create-a-fix-client-using-clj-fix) for more information.

## Why does it exist?
clj-fix was written so that different trading scenarios could be generated to test a market surveillance system my company developed (also in Clojure). clj-fix has been used on live exchanges and is released here under the MIT license.

## Related repos
- [fix-translator](https://github.com/nitinpunjabi/fix-translator)
- [clj-fix-oms](https://github.com/nitinpunjabi/clj-fix-oms)

## Installing (Leiningen)
```Clojure

;Include this in your project.clj:
[clj-fix "0.6.2"]

; Example:
(defproject my-project "1.0"
  :dependencies [[clj-fix "0.6.2"]])
```

## Usage
```Clojure

; In your ns statement:
(ns my-project.core
  (:use clj-fix.core)
  (:use clj-fix.connection.protocol))
```

## Steps to create a FIX client using clj-fix
__1__.[Install](https://github.com/nitinpunjabi/clj-fix#installing-leiningen) and [include](https://github.com/nitinpunjabi/clj-fix#usage) clj-fix in your project.

__2__.In your project's root directory, create a directory called _config_.

You should have four pieces of information about the destination you want to communicate with: the host IP, the port, the [sender id](http://www.fixprotocol.org/FIXimate3.0/en/FIX.4.4/tag49.html), and the [target id](http://www.fixprotocol.org/FIXimate3.0/en/FIX.4.4/tag56.html).

Create a _clients.cfg_ file in the config directory and type in the following information (replacing sample data as needed):
```json
{
  "my-fix-client" : {
    "venue" : "the-destination",    ; This is a label you provide.
    "host" : "10.10.10.10",
    "port" : 8888,
    "sender" : "sender-id",
    "target" : "target-id",
    "last-logout" : "20130101",     ; Enter a date from the past here. clj-fix
    "inbound-seq-num" : 0,          ; will update it as needed.
    "outbound-seq-num" : 0
  }
}
```
You can include multiple client configs in this file.

__3__.In your project's root directory, create a directory called _specs_. Place your destination's .spec file here. This is a file you create and is used by [fix-translator](https://github.com/nitinpunjabi/fix-translator). Each .spec file should have a name corresponding to the label you gave to "venue" in the config file. So in the example above, the spec directory should contain a file called _the-destination.spec_. Please refer to [fix-translator](https://github.com/nitinpunjabi/fix-translator) for information on how to create a .spec file for your destination.

__4__.Write a callback function for clj-fix, which will use it to send you client-level responses from your destination. As of this writing, clients must handle three types of messages: logon acknowledgment, execution reports, and logout acknowledgment. Here's an example:
```clojure
; This callback is supplied to an agent.
(defn my-handler [key-id reference last-msg new-msg]
  (case (:msg-type new-msg)
    :logon (println "Logon accepted by" (:sender-comp-id new-msg))
    :execution-report (println "Execution Report: " new-msg)
    :logout (println "Logged out from" (:sender-comp-id new-msg))))
```

__5__.Load the client:
```clojure
(def client (load-client :my-fix-client))
```

__6__.Logon to your destination
```clojure
;; The logon params are [client-var, callback, heartbeat-interval, 
;; whether-to-reset-sequence-numbers, whether-to-translate-inbound-fix-messages]
(logon client my-handler 60 :no true)
```
Selecting _false_ for the last parameter will result in your client receiving FIX messages in their raw format (except for logon and logout acknowledgement messages which are always translated). Please see [fix-translator](https://github.com/nitinpunjabi/fix-translator) for more information.

__7__.Send an order:
```clojure
; The new-order params are [the-client-var, side, quantity, symbol, price]
; This sends a buy order for 100 shares of NESNz at 10.0 of the
; symbol's currency
(new-order client :buy 100 "NESNz" 10.0)
```
Supplying clj-fix's new-order command with minimal parameters results in a simple LIMIT DAY order being sent with the most common default values set. If you want to override a certain default (for example, supply your own client order id) or use a particular order type your destination supports, just include an additional map in the params:
```Clojure
; This sends a buy order for 100 shares of NESNz at 10.0 of the symbol's
; currency with a client specified id and an immediate-or-cancel time-in-force.
(new-order client :buy 100 "NESNz" 10.0 {:client-order-id "my-own-id" :time-in-force :ioc})
```
Canceling an order requires you to track and maintain orders as they change states. You can either write your own manager or use [clj-fix-oms](https://github.com/nitinpunjabi/clj-fix-oms) as a light-weight option.

__8__.Logout of your destination:
```clojure
; The logout params are [client-var, reason] 
(logout client "done")
```

__9__.End your session properly so that clj-fix updates the config file.
```clojure
; The logout param is [client-var] 
(end-session client)
```


## To-Dos
- Provide a means for the client to respond to resend requests from the destination.
- Give client the option to let clj-fix handle resend requests from the destination.
- Pass session reject, and order-cancel reject messages to the client.





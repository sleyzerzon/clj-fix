(defproject clj-fix "0.6.2"
  :description "A Clojure API for FIX communication"
  :url "https://github.com/nitinpunjabi/clj-fix"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [clj-time "0.4.4"]
                 [aleph "0.3.0-beta8"] 
                 [lamina "0.5.0-beta8"]
                 [gloss "0.2.2-beta3"]
                 [edw/ordered "1.3.2"]
                 [fix-translator "1.05"]
                 [cheshire "5.0.0"]]
  :main clj-fix.core)
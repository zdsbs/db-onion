(defproject db-onion "1.0.0-SNAPSHOT"
  :description "FIXME: write"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [com.h2database/h2 "1.2.138"]
                 [ant/ant "1.7.0"]]
  :aot [DBOnionScriptRunner DBOnionInitializer])

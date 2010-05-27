(ns db-onion-test
  (:use clojure.test db-onion)
  (:import (java.io File FilenameFilter)))

(def onion-test-path "db-onion/test-scripts")

;(def script-file-filter (proxy [FilenameFilter] []
;  (accept [dir filename] true)))

;(doseq [file (.listFiles onion script-file-filter)] (println (.getName file)))

(deftest get-scripts-test
         (is (= (list "1-example.sql" "4-example.sql" "123-example.sql" ) (map #(.getName %) (get-scripts onion-test-path))))
         )

(run-tests 'db-onion-test)

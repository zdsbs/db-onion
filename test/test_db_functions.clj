(ns test-db-functions
  (:use clojure.test db-onion clojure.contrib.sql)
  (:import (java.io File FileWriter)))

(defn init-db-ref []
  (dosync (ref-set db
       {:classname   "org.h2.Driver" ; must be in classpath
           :subprotocol "h2"
           :subname "mem:mytest;DB_CLOSE_DELAY=-1"
           :user     "sa"
           :password ""})))

(defn create-script-numbers-table []
  (create-table
    :script_numbers
    [:script_number :integer]
    [:insertion_time :timestamp "default current_timestamp"]))

(defn drop-all-objects []
  (do-commands "drop all objects;"))

(defn create-db []
  (with-connection
    @db
    (transaction
      (initialize-version-table)
      (create-script-numbers-table))))

(defn drop-db []
  (with-connection
    @db
    (transaction
      (drop-all-objects))))

(defn get-ran-script-nums []
  (with-connection
     @db
     (transaction
       (with-query-results rs ["SELECT script_number from script_numbers order by insertion_time"]
          (doall (map #(:script_number %) rs))))))

(defn initialize-version-number [n]
  (with-connection
    @db
    (transaction
      (set-version n))))

(defn db-fixture [test-fn]
  (create-db)
  (test-fn)
  (drop-db))


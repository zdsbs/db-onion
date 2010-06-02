(ns db-onion-db-test
  (:use clojure.test db-onion)
  (:import 
     (java.sql Connection DriverManager Statement)
     (java.io File FileWriter)))

(Class/forName "org.h2.Driver")
(def con (DriverManager/getConnection "jdbc:h2:mem:mytest","sa",""))
(def sst (.createStatement con))

(def db-onion-test-dir "db-onion-test-dir")

(defn create-db-fixture [test-fn]
    (.execute sst "CREATE TABLE version (version INTEGER)")
    (.execute sst "INSERT INTO version values (0)")
    (test-fn)
    (.execute sst "DROP TABLE version")
    (.close sst))

(defn create-file-fixture [test-fn]
  (def test-dir (File. db-onion-test-dir))
  (.mkdir test-dir)

  (def do-nothing-file (File. (str db-onion-test-dir "/1-do-nothing-file.sql")))
  (.createNewFile do-nothing-file)

  (def writer (FileWriter. do-nothing-file))

  (.write writer "--")
  (.close writer)
  
  (test-fn)

  (.delete do-nothing-file)
  (.delete test-dir))

(deftest apply-one-script-version-should-be-1
  (run db-onion-test-dir con)
    (let [rs (.executeQuery sst "SELECT version from version")]
      (.next rs)
      (is (= 1 (.getObject rs "version")))))

(use-fixtures :each create-db-fixture create-file-fixture)

(run-tests 'db-onion-db-test)

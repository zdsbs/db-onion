(ns db-onion-db-test
  (:use clojure.test db-onion)
  (:import 
     (java.sql Connection DriverManager Statement)
     (java.io File FileWriter)))

(Class/forName "org.h2.Driver")

(dosync
	(ref-set con (DriverManager/getConnection "jdbc:h2:mem:mytest","sa","")))

(def db-onion-test-dir "db-onion-test-dir")

(defn create-script[name contents]
	  (def script-file (File. (str db-onion-test-dir "/" name)))
	  (.createNewFile script-file)
	  (def writer (FileWriter. script-file))
	  (.write writer contents)
	  (.close writer))



(defn create-db-fixture [test-fn]
	(def sst (.createStatement @con))
	(.execute sst "CREATE TABLE version (version INTEGER)")
	(.execute sst "INSERT INTO version values (0)")
	(.execute sst "CREATE TABLE script_numbers (script_number INTEGER, insertion_time TIMESTAMP default current_timestamp)")
	(test-fn)
	(.execute sst "drop all objects;")
	(.close sst))

(defn create-file-fixture [test-fn]
  (def test-dir (File. db-onion-test-dir))
  (.mkdir test-dir)
	(create-script "1-script.sql" "INSERT INTO script_numbers (script_number) values (1)")
	(create-script "2-script.sql" "INSERT INTO script_numbers (script_number) values (2)")
	(create-script "3-script.sql" "INSERT INTO script_numbers (script_number) values (3)")
	(create-script "4-script.sql" "INSERT INTO script_numbers (script_number) values (4)")
  (test-fn)
	(doseq [test-file (.listFiles test-dir)]
		(.delete test-file))
  (.delete test-dir)
)

(defn initialize-version [number]
	(.execute sst (str "update version set version=" number)))

(defn get-ran-script-nums []
	(let [rs (.executeQuery sst "SELECT script_number from script_numbers order by insertion_time")]
				(loop [results []]
					(if (not (.next rs))
						results
						(recur (conj results (.getObject rs "script_number")))
					))))

(deftest apply-all-scripts
  (run db-onion-test-dir)
	(is (= (get-ran-script-nums) [1 2 3 4])
	(is (= 4 (get-version-number)))))

(deftest apply-scripts-3-and-4-when-version-starts-at-2
	(initialize-version 2)
  (run db-onion-test-dir)
	(is (= (get-ran-script-nums) [3 4]))
	(is (= 4 (get-version-number))))

	

(use-fixtures :each create-db-fixture create-file-fixture)

(run-tests 'db-onion-db-test)

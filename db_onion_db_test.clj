(ns db-onion-db-test
  (:use clojure.test db-onion clojure.contrib.sql)
  (:import 
     (java.io File FileWriter)))

(dosync (ref-set db
	{:classname   "org.h2.Driver" ; must be in classpath
			           :subprotocol "h2"
			           :subname "mem:mytest"
			           ; Any additional keys are passed to the driver
			           ; as driver-specific properties.
			           :user     "sa"
			           :password ""}))

(defn keep-alive
	[]
	(do-commands "set db_close_delay -1"))

(defn create-version-table
  []
  (create-table
    :version
    [:version :integer]))

(defn create-script-numbers-table
  []
  (create-table
    :script_numbers
    [:script_number :integer]
		[:insertion_time :timestamp "default current_timestamp"]))

(defn initialize-version
	[] (insert-values
	   		:version
			  [:version]
			  [0]))

(defn drop-all-objects
	[]
	(do-commands "drop all objects;"))

(def db-onion-test-dir "db-onion-test-dir")






(defn create-script[name contents]
	  (def script-file (File. (str db-onion-test-dir "/" name)))
	  (.createNewFile script-file)
	  (def writer (FileWriter. script-file))
	  (.write writer contents)
	  (.close writer))
	
(defn create-db-fixture [test-fn]
	(with-connection
	  @db
	  (transaction
	    (create-version-table)
			(initialize-version)
			(create-script-numbers-table)
			(test-fn)
		  (drop-all-objects))))

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
  (.delete test-dir))
					
(defn get-ran-script-nums []
	(with-connection 
		@db
		(transaction 
			(with-query-results rs ["SELECT script_number from script_numbers order by insertion_time"]
				(doall (map #(:script_number %) rs))))))
		
(defn print-version []
	(with-connection 
		@db
		(with-query-results rs ["select * from version"]
			(dorun (map #(println (:version %)) rs)))))

(deftest apply-all-scripts
  (run db-onion-test-dir)
	(is (= (get-ran-script-nums) [1 2 3 4])
	(is (= 4 (get-version-number)))))

(deftest apply-scripts-3-and-4-when-version-starts-at-2
	(with-connection
	  @db
	  (transaction
			(set-version 2)))
  (run db-onion-test-dir)
	(is (= (get-ran-script-nums) [3 4]))
	(is (= 4 (get-version-number))))

(use-fixtures :each create-db-fixture create-file-fixture)

(run-tests 'db-onion-db-test)

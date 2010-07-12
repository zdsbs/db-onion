(ns db-onion-db-test
  (:use clojure.test db-onion clojure.contrib.sql test-file-functions test-db-functions)
  (:import 
     (java.io File FileWriter)))

(init-db-ref)

(defn create-happy-path-files []
  (create-script "1-script.sql" "INSERT INTO script_numbers (script_number) values (1)")
  (create-script "2-script.sql" "INSERT INTO script_numbers (script_number) values (2)")
  (create-script "3-script.sql" "INSERT INTO script_numbers (script_number) values (3)")
  (create-script "4-script.sql" "INSERT INTO script_numbers (script_number) values (4)"))

(deftest will-apply-scripts-1-through-4-when-version-is-0
  (run test-dir-name)
	(is (= (get-ran-script-nums) [1 2 3 4])
	(is (= 4 (get-version-number)))))

(deftest will-apply-scripts-3-and-4-when-version-is-2
  (initialize-version-number 2)
  (run test-dir-name)
	(is (= (get-ran-script-nums) [3 4]))
	(is (= 4 (get-version-number))))

(use-fixtures :each db-fixture (files-fixture create-happy-path-files))

(run-tests 'db-onion-db-test)

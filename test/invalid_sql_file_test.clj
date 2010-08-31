(ns invalid-sql-file-test 
  (:use clojure.test db-onion db-onion-internal-db clojure.contrib.sql test-file-functions test-db-functions))

(init-db-ref)

(defn invalid-sql-file-files []
  (create-script "1-script.sql" "INSERT INTO script_numbers (script_number) values (1);")
  (create-script "2-script.sql" "INVALID SQL")
  (create-script "3-script.sql" "INSERT INTO script_numbers (script_number) values (3);"))

(deftest with-unhappy-files-we-will-only-apply-script-1
  (run test-dir-name)
	(is (= (get-ran-script-nums) [1])
	(is (= 1 (get-version-number)))))

(use-fixtures :each db-fixture (files-fixture invalid-sql-file-files))

(run-tests 'invalid-sql-file-test)

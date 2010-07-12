(ns exceptions-test
  (:use clojure.test db-onion clojure.contrib.sql test-file-functions test-db-functions)
  (:import 
     (java.io File FileWriter)))

(init-db-ref)

(defn create-unhappy-files []
  (create-script "1-script.sql" "INSERT INTO script_numbers (script_number) values (1);")
  (create-script "2-script.sql" "INVALID SQL")
  (create-script "3-script.sql" "INSERT INTO script_numbers (script_number) values (3);"))

(deftest with-unhappy-files-we-will-only-apply-script-1
  (run test-dir-name)
	(is (= (get-ran-script-nums) [1])
	(is (= 1 (get-version-number)))))

(use-fixtures :each db-fixture (files-fixture create-unhappy-files))

(run-tests 'exceptions-test)

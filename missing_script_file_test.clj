(ns missing-script-file-test 
  (:use clojure.test db-onion clojure.contrib.sql test-file-functions test-db-functions)
  (:import 
     (java.io File FileWriter)))

(init-db-ref)

(defn create-unhappy-files []
  (create-script "1-script.sql" "INSERT INTO script_numbers (script_number) values (1);")
  (create-script "2-script.sql" "INSERT INTO script_numbers (script_number) values (2);")
  (create-script "4-script.sql" "INSERT INTO script_numbers (script_number) values (4);"))

(deftest with-unhappy-files-we-will-only-apply-script-1
  (initialize-version-number 0)
  (is (thrown? IllegalArgumentException (run test-dir-name)))
  (is (= (get-ran-script-nums) [])
	(is (= 0 (get-version-number)))))

(deftest missing-the-first-file
  (initialize-version-number 2)
  (is (thrown? IllegalArgumentException (run test-dir-name)))
  (is (= (get-ran-script-nums) [])
	(is (= 2 (get-version-number)))))

(deftest when-missing-earlier-numbers-we-dont-care
  (initialize-version-number 3)
  (run test-dir-name)
  (is (= (get-ran-script-nums) [4])
	(is (= 4 (get-version-number)))))


(use-fixtures :each db-fixture (files-fixture create-unhappy-files))

(run-tests 'missing-script-file-test )

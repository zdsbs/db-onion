(ns can-apply-scripts-in-order-test
  (:use clojure.test db-onion clojure.contrib.sql test-file-functions test-db-functions))

(init-db-ref)

(defn can-apply-scripts-in-order-files []
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

(use-fixtures :each db-fixture (files-fixture can-apply-scripts-in-order-files))

(run-tests 'can-apply-scripts-in-order-test)

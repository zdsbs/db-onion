(ns prints-success-message-test 
  (:use clojure.test db-onion clojure.contrib.sql test-file-functions test-db-functions))


(init-db-ref)

(defn prints-success-message-files []
  (create-script "1-script.sql" "INSERT INTO script_numbers (script_number) values (1)")
  (create-script "2-script.sql" "INSERT INTO script_numbers (script_number) values (2)")
  (create-script "3-script.sql" "INSERT INTO script_numbers (script_number) values (3)")
  (create-script "4-script.sql" "INSERT INTO script_numbers (script_number) values (4)"))

(deftest will-print-correct-message-scripts-1-through-4
  (is (= "Yay! We applied 1-4" (run test-dir-name))))

(deftest will-print-up-to-date-message-if-no-scripts-need-to-be-applied
  (initialize-version-number 4)
  (is (= "DB up to date with latest scripts.  Already up to date." (run test-dir-name))))

(use-fixtures :each db-fixture (files-fixture prints-success-message-files))

(run-tests 'prints-success-message-test)
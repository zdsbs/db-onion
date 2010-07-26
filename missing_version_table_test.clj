(ns missing-version-table-test 
  (:use clojure.test db-onion clojure.contrib.sql test-file-functions test-db-functions))

(init-db-ref)

(defn missing-version-table-files []
  (create-script "1-script.sql" "INSERT INTO script_numbers (script_number) values (1);")
  (create-script "2-script.sql" "INSERT INTO script_numbers (script_number) values (2);"))

(deftest no-version-table-throws-exception
  (is (thrown? IllegalStateException (run test-dir-name))))

(use-fixtures :each (files-fixture missing-version-table-files))

(run-tests 'missing-version-table-test)

(ns initialize-with-existing-version-table-test
  (:use clojure.test db-onion clojure.contrib.sql test-file-functions test-db-functions))

(init-db-ref)

(deftest initialize-with-version-table-throws-exception
  (is (thrown? IllegalStateException (initialize-version-table))))

(use-fixtures :each db-fixture)

(run-tests 'initialize-with-existing-version-table-test)
(ns missing-script-dir-test 
  (:use clojure.test db-onion clojure.contrib.sql test-file-functions test-db-functions))

(init-db-ref)

(deftest provide-feedback-when-we-cannot-find-the-script-dir
  (is (thrown? IllegalArgumentException (run "non-existant test dir"))))

(use-fixtures :each db-fixture)

(run-tests 'missing-script-dir-test )
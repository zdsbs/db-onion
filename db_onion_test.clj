(ns db-onion-test
  (:use clojure.test db-onion)
  (:import (java.io File FilenameFilter)))

(def onion-test-path "db-onion/test-scripts")

(deftest test-numeric-sort-comparator
	(let [file1 (File. "1-foo.sql")
		  file2 (File. "2-foo.sql")]
		  (is (= -1 (.compare numeric-sort-comparator file1 file2)))
		  (is (= 1 (.compare numeric-sort-comparator file2 file1)))
		  (is (= 0 (.compare numeric-sort-comparator file1 file1)))))

(deftest script-name-list-has-holes-test
  (is (false? (script-name-list-has-holes? (list "1-foo") 0)))
  (is (false? (script-name-list-has-holes? (list "1-foo" "2-foo") 0)))
  (is (true? (script-name-list-has-holes? (list "1-foo" "3-foo") 0)))
  (is (true? (script-name-list-has-holes? (list "7-foo" "8-foo") 5)))
  )

(run-tests 'db-onion-test)

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

;(deftest test-script-file-filter
;	(let [dir (File. "unused")]
;		(is (.accept script-file-filter dir "1-script.sql"))
;		(is (.accept script-file-filter dir "100-script.sql"))
;		(is (not (.accept script-file-filter dir "1-script.sql.bak")))
;		(is (not (.accept script-file-filter dir "-1-script.sql")))
;		(is (not (.accept script-file-filter dir "abc-script.sql")))
;		))

(deftest get-scripts-test
         (is (= (list "1-example.sql" "4-example.sql" "123-example.sql" ) (map #(.getName %) (get-scripts onion-test-path))))
         )

(run-tests 'db-onion-test)

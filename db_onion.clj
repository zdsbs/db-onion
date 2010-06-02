(ns db-onion
  (:import (java.io File FilenameFilter) (java.util Comparator)))

(def script-file-filter 
  (proxy [FilenameFilter] []
    (accept [_ filename] 
          (if (nil? (re-find #"[0-9]+-.*sql$" filename))
            false
            true))))

(def numeric-sort-comparator 
  (proxy [Comparator] []
	(compare [file1 file2]
		(let [file1-number (Integer/parseInt (re-find #"[0-9]+" (.getName file1)))
			  file2-number (Integer/parseInt (re-find #"[0-9]+" (.getName file2)))]
			  (.compareTo file1-number file2-number)))))

(defn get-scripts [script-dir-path]
    (let [onion (new File script-dir-path)]
          (sort numeric-sort-comparator (seq (.listFiles onion script-file-filter)))))

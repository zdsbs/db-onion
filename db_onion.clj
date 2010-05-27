(ns db-onion
  (:import (java.io File FilenameFilter) (java.util Comparator)))

(def script-file-filter 
  (proxy [FilenameFilter] []
    (accept [dir filename] 
          (if (nil? (re-find #"[0-9]+-.*sql$" filename))
            false
            true))))

(def numeric-sort-comparator 
  (proxy [Comparator] []
	(compare [this that]
		(let [this-number (Integer/parseInt (re-find #"[0-9]+" (.getName this)))
			  that-number (Integer/parseInt (re-find #"[0-9]+" (.getName that)))]
			  (.compareTo this-number that-number)))))

(defn get-scripts [script-dir-path]
    (let [onion (new File script-dir-path)]
          (sort numeric-sort-comparator (seq (.listFiles onion script-file-filter)))))

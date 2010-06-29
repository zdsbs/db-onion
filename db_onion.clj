(ns db-onion
	(:use clojure.contrib.sql)
  (:import (java.sql Connection DriverManager Statement) (java.io File FilenameFilter) (java.util Comparator)))

(def db (ref nil))

(defn get-version-number []
		(with-connection @db
			(transaction 
				(with-query-results rs ["SELECT version from version"]
				(first (doall (map #(:version %) rs)))))))


(def script-file-filter 
  (proxy [FilenameFilter] []
    (accept [_ filename] 
          (if (nil? (re-find #"^[0-9]+-.*sql$" filename))
            false
            (> (Integer/parseInt (first (.split filename "-"))) (get-version-number))))))

(def numeric-sort-comparator 
  (proxy [Comparator] []
	(compare [file1 file2]
		(let [file1-number (Integer/parseInt (re-find #"[0-9]+" (.getName file1)))
			  file2-number (Integer/parseInt (re-find #"[0-9]+" (.getName file2)))]
			  (.compareTo file1-number file2-number)))))

(defn get-scripts [script-dir-path]
    (let [onion (new File script-dir-path)]
         (sort numeric-sort-comparator (seq (.listFiles onion script-file-filter)))))
	
(defn get-script-contents [script-dir-path]
	(map slurp (map #(.getAbsolutePath %) (get-scripts script-dir-path))))

(defn set-version
	[num] (update-values
						:version ["1=?" 1] {:version num}))

(defn inc-version []
	(set-version (inc (get-version-number))))

(defn run [script-dir-path]
  (let [scripts-contents (get-script-contents script-dir-path)]
				(doseq [script scripts-contents]
					(with-connection
						@db
						(transaction
							(do-commands script)
							(inc-version))))))

(ns db-onion
  (:import (java.io File FilenameFilter) (java.util Comparator)))

(def con (ref nil))

(defn get-statement []
	(.createStatement @con))

(defn get-version-number []
	(let [sst (get-statement)
				rs (.executeQuery sst "select version from version")]
				(.next rs)
				(.getObject rs "version")))

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

(defn inc-version []
	(let [sst (get-statement)
				new-version (inc (get-version-number))]
		(.execute sst (str "update version set version=" new-version))))

(defn run [script-dir-path]
  (let [sst (get-statement)
			  scripts-contents (get-script-contents script-dir-path)]
				(doseq [script scripts-contents]
					(.execute sst script)
					(inc-version))
    		(.close sst)))

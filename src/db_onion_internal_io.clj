(ns db-onion-internal-io
  (:import (java.io File FilenameFilter) (java.util Comparator)))


(defn get-version-number-from-filename [filename]
  (Integer/parseInt (first (.split filename "-"))))

(defn is-not-valid-filename? [filename] 
  (nil? (re-find #"^[0-9]+-.*sql$" filename)))

(defn is-valid-filename-over-version? [filename current-version]
  (if (is-not-valid-filename? filename)
    false
    (> (get-version-number-from-filename filename) current-version)))

(defn get-script-file-filter [version-number]
  (proxy [FilenameFilter] []
    (accept [_ filename] 
          (is-valid-filename-over-version? filename version-number))))

(def numeric-sort-comparator 
  (proxy [Comparator] []
	(compare [file1 file2]
		(let [file1-number (Integer/parseInt (re-find #"[0-9]+" (.getName file1)))
			  file2-number (Integer/parseInt (re-find #"[0-9]+" (.getName file2)))]
			  (.compareTo file1-number file2-number)))))

(defn get-scripts [script-dir-path version-number]
    (let [onion (new File script-dir-path)]
         (sort numeric-sort-comparator (seq (.listFiles onion (get-script-file-filter version-number))))))
	
(defn get-script-contents [scripts]
	(map slurp (map #(.getAbsolutePath %) scripts)))

(defn get-file-names [files]
  (map #(.getName %) files))

(defn script-name-list-has-holes? [script-file-names current-version]
  (loop [names script-file-names last-version current-version]
    (if (empty? names)
      false
      (if (not (= (get-version-number-from-filename (first names)) (inc last-version)))
        true
        (recur (rest names) (get-version-number-from-filename (first names)))))))


(defn dir-exists? [path]
  (.exists (File. path)))
    

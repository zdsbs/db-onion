(ns db-onion
  (:use clojure.contrib.sql)
  (:import (java.sql Connection DriverManager Statement ) (java.io File FilenameFilter) (java.util Comparator)))

(def db (ref nil))

(defn get-version-number []
  (with-connection @db
    (transaction 
      (with-query-results rs ["SELECT version from version"]
      (first (doall (map #(:version %) rs)))))))

(defn get-version-number-from-filename [filename]
  (Integer/parseInt (first (.split filename "-"))))

(defn is-not-valid-filename? [filename] 
  (nil? (re-find #"^[0-9]+-.*sql$" filename)))

(defn is-valid-filename-over-version? [filename current-version]
  (if (is-not-valid-filename? filename)
    false
    (> (get-version-number-from-filename filename) current-version)))

(def script-file-filter 
  (proxy [FilenameFilter] []
    (accept [_ filename] 
          (is-valid-filename-over-version? filename (get-version-number)))))

(def numeric-sort-comparator 
  (proxy [Comparator] []
	(compare [file1 file2]
		(let [file1-number (Integer/parseInt (re-find #"[0-9]+" (.getName file1)))
			  file2-number (Integer/parseInt (re-find #"[0-9]+" (.getName file2)))]
			  (.compareTo file1-number file2-number)))))

(defn get-scripts [script-dir-path]
    (let [onion (new File script-dir-path)]
         (sort numeric-sort-comparator (seq (.listFiles onion script-file-filter)))))
	
(defn get-script-contents [scripts]
	(map slurp (map #(.getAbsolutePath %) scripts)))

(defn set-version
	[num] (update-values
          :version ["1=?" 1] {:version num}))

(defn inc-version []
	(set-version (inc (get-version-number))))

(defn apply-single-script [script]
    (with-connection
       @db
       (transaction
         (do-commands script)
         (inc-version))))

(defn script-name-list-has-holes? [script-file-names current-version]
  (loop [names script-file-names last-version current-version]
    (if (empty? names)
      false
      (if (not (= (get-version-number-from-filename (first names)) (inc last-version)))
        true
        (recur (rest names) (get-version-number-from-filename (first names)))))))

(defn get-file-names [files]
  (map #(.getName %) files))

(defn apply-all-scripts[all-scripts]
  (doseq [script all-scripts]
      (apply-single-script script)))

(defn run [script-dir-path]
  (let [scripts (get-scripts script-dir-path)
        scripts-contents (get-script-contents scripts)]
    (if (script-name-list-has-holes? (get-file-names scripts) (get-version-number))
      (throw (IllegalArgumentException. "foo"))
      (try 
        (apply-all-scripts scripts-contents)
        (catch Exception sql )))))

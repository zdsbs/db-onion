(ns db-onion
  (:use clojure.contrib.sql db-onion-internal-io db-onion-internal-db))

(def db (ref nil))

(defn get-version-number []
  (with-connection @db
    (transaction 
      (with-query-results rs ["SELECT version from version"]
      (first (doall (map #(:version %) rs)))))))

(defn version-table-missing? []
  (try 
    (get-version-number)
    false
    (catch Exception sql 
      (do 
        (println sql)
        true))))

(defn set-version [num] 
  (update-values
          :version ["1=?" 1] {:version num}))

(defn inc-version []
	(set-version (inc (get-version-number))))

(defn apply-single-script [script]
    (with-connection
       @db
       (transaction
         (do-commands script)
         (inc-version))))

(defn apply-all-scripts[all-scripts]
  (doseq [script all-scripts]
      (apply-single-script script)))

(defn check-version-table-exists []
  (if (version-table-missing?)
    (throw (IllegalStateException. "Version table missing from database."))))

(defn check-script-dir-exists [path]
  (if (not (dir-exists? path))
    (throw (IllegalArgumentException. "Could not find specified script directory.  Please check your configuration."))))

(defn check-script-names-correct [scripts]
  (if (script-name-list-has-holes? (get-file-names scripts) (get-version-number))
    (throw (IllegalArgumentException. "Script file numbers not contiguous."))))

(defn get-success-message [script-version-numbers]
  (if (empty? script-version-numbers)
    "DB up to date with latest scripts.  Already up to date."
    (str "Yay! We applied " (first script-version-numbers) "-" (last script-version-numbers))))

(defn check-dependencies [script-dir-path]
  (check-version-table-exists)
  (check-script-dir-exists script-dir-path))

(defn run [script-dir-path]
  (check-dependencies script-dir-path)
  (let [scripts (get-scripts script-dir-path (get-version-number))
        script-version-numbers (map get-version-number-from-filename (get-file-names scripts))
        scripts-contents (get-script-contents scripts)]
    (check-script-names-correct scripts)
    (try 
      (apply-all-scripts scripts-contents)
      (catch Exception sql (println sql)))
    (get-success-message script-version-numbers)
    ))

(defn initialize-version-table[]
  (with-connection
    @db
    (transaction
    (create-version-table)
    (initialize-version))))
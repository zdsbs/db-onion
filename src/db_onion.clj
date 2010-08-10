(ns db-onion
  (:use clojure.contrib.sql db-onion-internal-io))

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

(defn check-script-names-correct [scripts]
  (if (script-name-list-has-holes? (get-file-names scripts) (get-version-number))
    (throw (IllegalArgumentException. "Script file numbers not contiguous."))))

(defn get-success-message [script-version-numbers]
  (str "Yay! We applied " (first script-version-numbers) "-" (last script-version-numbers)))

(defn run [script-dir-path]
  (check-version-table-exists)
  (let [scripts (get-scripts script-dir-path (get-version-number))
        script-version-numbers (map get-version-number-from-filename (get-file-names scripts))
        scripts-contents (get-script-contents scripts)]
    (check-script-names-correct scripts)
    (try 
      (apply-all-scripts scripts-contents)
      (catch Exception sql (println sql)))
    (get-success-message script-version-numbers)
    ))

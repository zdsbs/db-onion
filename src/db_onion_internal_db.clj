(ns db-onion-internal-db
  (:use clojure.contrib.sql))

(def db (ref nil))

(defn create-version-table[]
    (create-table
          :version
          [:version :integer]))

(defn initialize-version[] 
  (insert-values
   :version
   [:version]
   [0]))

(defn set-version [num] 
 (update-values
         :version ["1=?" 1] {:version num}))

(defn get-version-number []
  (with-connection @db
    (transaction 
      (with-query-results rs ["SELECT version from version"]
      (first (doall (map #(:version %) rs)))))))

(defn inc-version []
	(set-version (inc (get-version-number))))

(defn version-table-missing? []
 (try 
   (get-version-number)
   false
   (catch Exception sql 
     (do 
       (println sql)
       true))))

(defn throw-exception-if-version-table-is-missing []
 (if (version-table-missing?)
   (throw (IllegalStateException. "Version table missing from database."))))

(defn throw-exception-if-version-table-already-exists []
 (if (not (version-table-missing?))
   (throw (IllegalStateException. "Version table already exists."))))

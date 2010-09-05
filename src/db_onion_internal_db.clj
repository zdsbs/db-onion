(ns db-onion-internal-db
  (:use clojure.contrib.sql))

(defn create-version-table[]
    (create-table
          :version
          [:version :integer]))

(defn initialize-version[] 
  (insert-values
   :version
   [:version]
   [0]))
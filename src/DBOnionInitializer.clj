(ns DBOnionInitializer
  (:use db-onion)
  (:gen-class
    :extends org.apache.tools.ant.Task
    :methods [[setDriverClassName [String] void]
              [setSubprotocol [String] void]
              [setSubname [String] void]
              [setUsername [String] void]
              [setPassword [String] void]
              [setVersionTableName [String] void]]))

(def db-props (ref {}))
(def version-table-name (ref ""))

(defn -setDriverClassName [this db-driver]
  (dosync (alter db-props assoc :classname db-driver)))

(defn -setSubprotocol [this subprotocol]
  (dosync (alter db-props assoc :subprotocol subprotocol)))

(defn -setSubname [this subname]
  (dosync (alter db-props assoc :subname subname)))

(defn -setUsername [this username]
  (dosync (alter db-props assoc :user username)))

(defn -setPassword [this password]
  (dosync (alter db-props assoc :password password)))

(defn -setVersionTableName [this name]
  (dosync (ref-set version-table-name name)))





(defn -execute [this] 
  (println "Initializing database for DB-Onion")
  (dosync (ref-set db @db-props))
  (initialize-version-table))




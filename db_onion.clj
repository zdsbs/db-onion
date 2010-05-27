(ns db-onion
  (:import (java.io File FilenameFilter))
  (:require [clojure.contrib.str-utils2 :as s]))

(def script-file-filter (proxy [FilenameFilter] []
  (accept [dir filename] 
          (if (nil? (re-find #"[0-9]+-.*sql$" filename))
            false
            true))))

(defn get-scripts [script-dir-path]
    (let [onion (new File script-dir-path)]
          (seq (.listFiles onion script-file-filter))))

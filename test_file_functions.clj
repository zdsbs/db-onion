(ns test-file-functions
  (:use clojure.test db-onion clojure.contrib.sql)
  (:import (java.io File FileWriter)))

(def test-dir-name "irrelevant-test-dir-name")

(def test-dir (File. test-dir-name))

(defn create-test-dir []
  (.mkdir test-dir))

(defn create-script[name contents]
  (def script-file (File. (str test-dir-name "/" name)))
  (.createNewFile script-file)
  (def writer (FileWriter. script-file))
  (.write writer contents)
  (.close writer))

(defn delete-test-dir-and-files []
    (doseq [test-file (.listFiles test-dir)]
                      (.delete test-file))
    (.delete test-dir))

(defmacro files-fixture [file-creator-fn]
    `(fn [test-fn#] (do
                      (create-test-dir)
                      (~file-creator-fn)
                      (test-fn#)
                      (delete-test-dir-and-files))))

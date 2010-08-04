(ns AntTask
  (:gen-class
;   :extends java.lang.Exception))
    :extends org.apache.tools.ant.Task))

(defn -execute [this] (println "hello world")) 
(defn -main [args] (println "hi in main" args))

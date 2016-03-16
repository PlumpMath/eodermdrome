(ns eodermdrome.core
  (:require [eodermdrome.parser :as parser]
            [eodermdrome.graph :as eg]
            [loom.graph :refer [graph edges directed? nodes remove-edges
                                remove-nodes add-nodes add-edges] :as graph]
            [loom.io :as lio]
            [clojure.java.io :refer [output-stream]]
            (:gen-class)))

(def ^:dynamic *debug* false)
(def png-counter (atom 0))

(defn input-set-match? [input-set system-input]
  (if input-set
    (= (str (first system-input)) input-set)
    true))

(defn transform-graph [g-sys {:keys [input output g-in g-out]}]
  (let [in-nodes (nodes g-in)
        in-edges (edges g-in)
        out-nodes (nodes g-out)
        out-edges (edges g-out)
        closed-nodes-match (clojure.set/difference in-nodes out-nodes)
        closed-nodes-replacement (clojure.set/difference out-nodes in-nodes)]
    (-> g-sys
        ((fn [g] (apply remove-edges g in-edges)))
        ((fn [g] (apply remove-nodes g closed-nodes-match)))
        ((fn [g] (apply add-nodes g closed-nodes-replacement)))
        ((fn [g] (apply add-edges g out-edges))))))

(defn run-command [g-sys {:keys [input output g-in g-out] :as cmd} system-input system-output]
  (when (and (input-set-match? input system-input)
             (eg/subgraph? g-in g-sys))
    (when output
      (print output))
    {:system-input (if input (apply str (rest system-input)) system-input)
     :system-output (if output (conj system-output output) system-output)
     :g (transform-graph g-sys cmd)}))

(defn run-commands [cmds g input output]
  (reduce (fn [result cmd]
            (let [last-result (last result)
                  rg (:g last-result)
                  rs (:system-input last-result)
                  ro (:system-output last-result)
                  run (run-command rg cmd rs ro)]
              (if run
                (conj result run)
                result)))
          [{:g g :system-input input :system-output output}]
          cmds))

(defn write-png [g]
  (with-open [out (output-stream (str "step-" (swap! png-counter inc) ".png"))]
    (.write out (lio/render-to-bytes g))))

(defn run-program [cmds sys-input]
  (loop [g (eg/make-graph "thequickbrownfoxjumpsoverthelazydog")
         input sys-input
         output nil
         matches? true]
    (if (false? matches?)
      (do
        (flush)
        {:g g :input input :output output})
      (let [run-results (run-commands cmds g input output)
            match (> (count run-results) 1)
            last-run (last run-results)]
        (when *debug*
          (mapv write-png (mapv :g run-results)))
        (recur (:g last-run) (:system-input last-run) (:system-output last-run) match)))))

(defn run [program input]
  (let [cmds (parser/parse program)]
    (run-program cmds input)))

(defn -main [& args]
  (when (not (or (= 3 (count args)) (= 2 (count args))))
    (println "Usage: file input <debug> (Example add.eo \"11100111\"  or  add.eo \"101\" debug)")
    (println "debug mode will create pngs of all the graph states")
    (System/exit 1))
  (do (let [[file input debug] args]
        (println "Running program " file " with input " input)
        (if debug
          (binding [*debug* true]
            (run (slurp file) input))
          (run (slurp file) input))
        (println)
        (System/exit 0))))

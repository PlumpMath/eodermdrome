(ns eodermdrome.graph
  (:require [loom.graph :refer [graph edges directed? nodes] :as graph]))

(defn graph-edges [graph-str]
  (let [nodes (map (comp keyword str) graph-str)]
    (if (= (count nodes) 1)
      (set nodes)
      (into (set (partition 2 nodes)) (partition 2 (rest nodes))))))

(defn make-graph [graph-str]
  (apply graph (graph-edges graph-str)))

;;;
;;; taken from the HEAD version of loom to compare graphs
;;;
(defn subgraph?
  "Returns true iff g1 is a subgraph of g2. An undirected graph is never
  considered as a subgraph of a directed graph and vice versa."
  [g1 g2]
  (and (= (directed? g1) (directed? g2))
       (let [edge-test-fn (if (directed? g1)
                            graph/has-edge?
                            (fn [g x y]
                              (or (graph/has-edge? g x y)
                                  (graph/has-edge? g y x))))]
         (and (every? #(graph/has-node? g2 %) (nodes g1))
              (every? (fn [[x y]] (edge-test-fn g2 x y))
                      (edges g1))))))

(ns eodermdrome.parser
  (:require [eodermdrome.graph :as eg]
            [clojure.set :as set]
            [instaparse.core :as insta]))

  (def parser (insta/parser
               "program = cmdline (<'\n'> cmdline)*
                cmdline = (<comment> <space>*)* cmd (<space>* <comment>)*
                cmd = cmd-part <seperator> cmd-part
                cmd-part = (io <seperator>)* graph
                io =  <'('> #'[A-z0-9\\s]*'+ <')'>
                seperator = space | space comment space
                comment = #'\\,.*\\,'
                space = #'[\\s]'+
                graph = #'[a-z]*'+"))

(def transform-options
  {:program vector
   :cmdline identity
   :cmd-part vector
   :cmd (fn [& v]
          (let [[in out] v
                input (:io (first (filter :io in)))
                output (:io (first (filter :io out)))
                g-in (:g (first (filter :g in)))
                g-out (:g (first (filter :g out)))]
            {:input input
             :output output
             :g-in g-in
             :g-out g-out}))
   :graph (fn [v] {:g (eg/make-graph v)})
   :io (fn [v] {:io (str v)})})

(defn parse [input]
  (->> (parser input) (insta/transform transform-options)))

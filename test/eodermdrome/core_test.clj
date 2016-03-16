(ns eodermdrome.core-test
  (:require [clojure.test :refer :all]
            [eodermdrome.core :refer :all]
            [eodermdrome.graph :as eg]
            [eodermdrome.parser :as parser]
            [loom.graph :refer [edges nodes] :as graph]))


(deftest test-run-comment
  (testing "with two graphs"
    (let [result (run-command (eg/make-graph "abc") (first (parser/parse "bc bcd")) nil nil)]
      (is (=  #{:c :b :d :a} (nodes (:g result))))
      (is (=  #{[:c :b] [:c :d] [:b :c] [:b :a] [:d :c] [:a :b]}
              (set (edges (:g result)))))
      (is (= nil (:system-input result)))))
  (testing "with two graphs and an input with matching system input"
    (let [result (run-command (eg/make-graph "abc") (first (parser/parse "(1) bc bcd")) "111" nil)]
      (is (=  #{:c :b :d :a} (nodes (:g result))))
      (is (=  #{[:c :b] [:c :d] [:b :c] [:b :a] [:d :c] [:a :b]}
              (set (edges (:g result)))))
      (is (= "11" (:system-input result)))))
  (testing "with two graphs and an input with non matching system input"
    (let [result (run-command (eg/make-graph "abc") (first (parser/parse "(0) bc bcd")) "111" nil)]
      (nil? result)))
  (testing "with two graphs and an input and an output with matching system input"
    (let [result (run-command (eg/make-graph "abc") (first (parser/parse "(1) bc (0) bcd")) "111" nil)]
      (is (=  #{:c :b :d :a} (nodes (:g result))))
      (is (=  #{[:c :b] [:c :d] [:b :c] [:b :a] [:d :c] [:a :b]}
              (set (edges (:g result)))))
      (is (= "11" (:system-input result)))
      (is (= ["0"] (:system-output result)))))
  (testing "with two graphs and an output"
    (let [result (run-command (eg/make-graph "abc") (first (parser/parse "bc (0) bcd")) "111" nil)]
      (is (=  #{:c :b :d :a} (nodes (:g result))))
      (is (=  #{[:c :b] [:c :d] [:b :c] [:b :a] [:d :c] [:a :b]}
              (set (edges (:g result)))))
      (is (= "111" (:system-input result)))
      (is (= ["0"] (:system-output result))))))

(def add-program
"thequickbrownfoxjumpsoverthelazydog a
(1) a ab
(0) a a
ab (1) a")

(deftest test-run
  (testing "adding two binary strings together sepearted by zero"
   (let [program add-program
         result (run program "1110111")]
     (is (= "111111" (apply str (:output result))))))
(testing "with multiple zeros "
   (let [program add-program
         result (run program "111000111")]
     (is (= "111111" (apply str (:output result))))))
(testing "with just ones "
   (let [program add-program
         result (run program "111")]
     (is (= "111" (apply str (:output result)))))))

(deftest test-file-programs
  (testing "adder"
    (let [program (slurp "examples/adder.eo")
          result (run program "1110011")]
      (is (= "11111" (apply str (:output result))))))
  (testing "incrementer"
    (let [program (slurp "examples/doubler.eo")
          result (run program "11")]
      (is (= "1111" (apply str (:output result)))))))

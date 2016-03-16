(ns eodermdrome.parser-test
  (:require [eodermdrome.parser :refer :all]
            [clojure.test :refer :all]
            [loom.graph :refer [nodes]]))

(deftest test-parse
  (testing "two graphs and an input and an output"
    (let [cmd  "(2) ab (1) a"]
      (is (= "2" (:input (first (parse cmd)))))
      (is (= "1" (:output (first (parse cmd)))))
      (is (= #{:b :a} (nodes (:g-in (first (parse cmd))))))
      (is (= #{:a} (nodes (:g-out (first (parse cmd))))))))
(testing "two graphs and an input"
    (let [cmd  "(2) ab a"]
      (is (= "2" (:input (first (parse cmd)))))
      (is (nil? (:output (first (parse cmd)))))
      (is (= #{:b :a} (nodes (:g-in (first (parse cmd))))))
      (is (= #{:a} (nodes (:g-out (first (parse cmd))))))))
(testing "two graphs and an output"
    (let [cmd  "ab (1 text) a"]
      (is (nil? (:input (first (parse cmd)))))
      (is (= "1 text" (:output (first (parse cmd)))))
      (is (= #{:b :a} (nodes (:g-in (first (parse cmd))))))
      (is (= #{:a} (nodes (:g-out (first (parse cmd))))))))
(testing "two graphs"
    (let [cmd  "ab a"]
      (is (nil? (:input (first (parse cmd)))))
      (is (nil? (:output (first (parse cmd)))))
      (is (= #{:b :a} (nodes (:g-in (first (parse cmd))))))
      (is (= #{:a} (nodes (:g-out (first (parse cmd)))))))))

(deftest test-parse-with-comments
  (testing "comment at the beginning"
    (let [cmd  ",hi, (2) ab (1) a"]
      (is (= "2" (:input (first (parse cmd)))))
      (is (= "1" (:output (first (parse cmd)))))
      (is (= #{:b :a} (nodes (:g-in (first (parse cmd))))))
      (is (= #{:a} (nodes (:g-out (first (parse cmd))))))))
(testing "comment at the end"
    (let [cmd  "(2) ab (1) a ,comment,"]
      (is (= "2" (:input (first (parse cmd)))))
      (is (= "1" (:output (first (parse cmd)))))
      (is (= #{:b :a} (nodes (:g-in (first (parse cmd))))))
      (is (= #{:a} (nodes (:g-out (first (parse cmd))))))))
(testing "comment in the middel"
    (let [cmd  "(2) ab ,comment, (1) a"]
      (is (= "2" (:input (first (parse cmd)))))
      (is (= "1" (:output (first (parse cmd)))))
      (is (= #{:b :a} (nodes (:g-in (first (parse cmd))))))
      (is (= #{:a} (nodes (:g-out (first (parse cmd)))))))))

(deftest test-parse-with-newlines
  (let [program "(2) ab (1) a\n(3) a b\nd e"
        cmds (parse program)]
    (is (= 3 (count cmds)))))

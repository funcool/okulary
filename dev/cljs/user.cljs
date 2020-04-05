(ns cljs.user
  (:require [okulary.core :as ok]
            [lentes.core :as l]))


;; --- Lentes & Default Atom

(defn prepare-1
  []
  (let [a  (atom {:a 1 :b 1})
        a1 (-> (l/key :a)
               (l/derive a))
        a2 (-> (l/key :b)
               (l/derive a))]
    (dotimes [i 100]
      (add-watch a1 (str "key" i) identity)
      (add-watch a2 (str "key" i) identity))
    a))

(defn ^:export bench-1
  []
  (simple-benchmark
    [a (prepare-1)]
    (swap! a update :b inc)
    10000))


;; --- Lentes & Fast Atom

(defn prepare-2
  []
  (let [a  (ok/atom {:a 1 :b 1})
        a1 (-> (l/key :a)
               (l/derive a))
        a2 (-> (l/key :b)
               (l/derive a))]
    (dotimes [i 100]
      (add-watch a1 (str "key" i) identity)
      (add-watch a2 (str "key" i) identity))
    a))

(defn ^:export bench-2
  []
  (simple-benchmark
    [a (prepare-2)]
    (swap! a update :b inc)
    10000))


;; --- Okulary & Fast Atom

(defn prepare-3
  []
  (let [a  (ok/atom {:a 1 :b 1})
        a1 (-> (ok/key :a)
               (ok/derive a))
        a2 (-> (ok/key :b)
               (ok/derive a))]
    (dotimes [i 100]
      (add-watch a1 (str "key" i) identity)
      (add-watch a2 (str "key" i) identity))
    a))

(defn ^:export bench-3
  []
  (simple-benchmark
    [a (prepare-3)]
    (swap! a update :b inc)
    10000))

(def a (ok/atom {:a 1 :b 1}))
(def b (-> (ok/key :a)
           (ok/derive a)))
(def c (-> (ok/key :b)
           (ok/derive a)))

(add-watch b :foobar #(js/console.log "watch: :a" %3 "=>" %4))
(add-watch c :foobar #(js/console.log "watch: :b" %3 "=>" %4))

(defn ^:export test-swap
  []
  (swap! a update :a inc))

;; --- Other

(defn prepare-atom
  []
  (let [a (atom 0)]
    (dotimes [i 500]
      (add-watch a (str "key" i) identity))
    a))


(defn prepare-fast-atom
  []
  (let [a (ok/atom 0)]
    (dotimes [i 500]
      (add-watch a (str "key" i) identity))
    a))


(defn ^:export bench-atom
  []
  (simple-benchmark
    [a (prepare-atom)]
    (swap! a inc)
    1000))

(defn ^:export bench-fast-atom
  []
  (simple-benchmark
    [a (prepare-fast-atom)]
    (swap! a inc)
    1000))


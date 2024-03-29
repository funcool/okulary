(ns cljs.user
  (:require
   [okulary.core :as ok]
   [okulary.util :as ou]
   [lentes.core :as l]))


;; --- Lentes & Default Atom

(defn prepare-1
  []
  (let [a  (atom {:a 1 :b 1})
        a1 (-> (l/key :a)
               (l/derive a))
        a2 (-> (l/key :b)
               (l/derive a))
        a3 (-> (l/key :a)
               (l/derive a))
        a4 (-> (l/key :b)
               (l/derive a))
        a5 (-> (l/key :a)
               (l/derive a))
        a6 (-> (l/key :b)
               (l/derive a))]
    (dotimes [i 50]
      (add-watch a1 (str "key" i) identity)
      (add-watch a2 (str "key" i) identity)
      (add-watch a3 (str "key" i) identity)
      (add-watch a4 (str "key" i) identity)
      (add-watch a5 (str "key" i) identity)
      (add-watch a6 (str "key" i) identity))
    a))

(defn ^:export bench-1
  []
  (simple-benchmark
    [a (prepare-1)]
    (swap! a update :b inc)
    10000))

;; --- Okulary & Fast Atom

(defn prepare-2
  []
  (let [a  (ok/atom {:a 1 :b 1})
        a1 (ok/derived :a a)
        a2 (ok/derived :b a)
        a3 (ok/derived :a a)
        a4 (ok/derived :b a)
        a5 (ok/derived :a a)
        a6 (ok/derived :b a)]
    (dotimes [i 50]
      (add-watch a1 (str "key" i) identity)
      (add-watch a2 (str "key" i) identity)
      (add-watch a3 (str "key" i) identity)
      (add-watch a4 (str "key" i) identity)
      (add-watch a5 (str "key" i) identity)
      (add-watch a6 (str "key" i) identity))
    a))

(defn ^:export bench-2
  []
  (simple-benchmark
    [a (prepare-2)]
    (swap! a update :b inc)
    10000))

(def x1 (ok/atom {:a 1 :b {:c 2 :d 3}}))

(def x2 (ok/derived
         (fn [v]
           (prn "getter x2")
           (get v :a))
         x1))

(def x3 (ok/derived
         (fn [v]
           (prn "getter x3")
           (get v :b))
         x1))

(def x4 (ok/derived
         (fn [v]
           (prn "getter x4")
           (get v :d))
         x3))

(add-watch x2 :foobar #(js/console.log "watch: x2" %3 "=>" %4))
(add-watch x3 :foobar #(js/console.log "watch: x3" %3 "=>" %4))
(add-watch x4 :foobar #(js/console.log "watch: x4" %3 "=>" %4))

(defn ^:export test-swap
  []
  (swap! x1 update-in [:b :d] inc))

;; --- Other

(defn prepare-atom
  []
  (let [a (atom 0)]
    (dotimes [i 25]
      (add-watch a (str "key" i) identity))
    a))


(defn prepare-fast-atom
  []
  (let [a (ok/atom 0)]
    (dotimes [i 25]
      (add-watch a (str "key" i) identity))
    a))

(defn ^:export bench-atom
  []
  (simple-benchmark
    [a (prepare-atom)]
    (swap! a inc)
    10000))

(defn ^:export bench-fast-atom
  []
  (simple-benchmark
    [a (prepare-fast-atom)]
    (swap! a inc)
    10000))

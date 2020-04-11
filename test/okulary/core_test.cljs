(ns okulary.core-test
  (:require
   [cljs.test :as t]
   [okulary.core :as l]))

(t/deftest atom-basics
  (let [a (l/atom 0)]
    (t/is (= 1 (swap! a inc)))
    (t/is (= 1 (deref a)))
    (t/is (= 2 (reset! a 2)))
    (t/is (= 2 (deref a)))))

(t/deftest atom-watching
  (let [a (l/atom 0)
        b (volatile! 0)]
    (add-watch a "foo" #(vswap! b inc))
    (add-watch a "bar" #(vswap! b inc))
    (swap! a inc)
    (t/is (= @b 2))
    (remove-watch a "foo")
    (remove-watch a "bar")
    (swap! a inc)
    (t/is (= @b 2))
    (t/is (= @a 2))))

(t/deftest derived-atom
  (let [a1 (l/atom {:a 0 :b 0})
        a2 (l/derived :a a1)
        a3 (l/derived :b a1)
        r1 (volatile! 0)
        r2 (volatile! 0)
        r3 (volatile! 0)]
    (add-watch a1 "foo" #(vswap! r1 inc))
    (add-watch a2 "foo" #(vswap! r2 inc))
    (add-watch a3 "foo" #(vswap! r3 inc))
    (swap! a1 update :a inc)
    (swap! a1 update :a inc)
    (swap! a1 update :b inc)

    (t/is (= 3 @r1))
    (t/is (= 2 @r2))
    (t/is (= 1 @r3))

    (t/is (= 2 @a2))
    (t/is (= 1 @a3))

    (remove-watch a2 "foo")
    (remove-watch a3 "foo")

    (swap! a1 update :a inc)
    (swap! a1 update :a inc)
    (swap! a1 update :b inc)

    (t/is (= 6 @r1))
    (t/is (= 2 @r2))
    (t/is (= 1 @r3))

    (t/is (= 4 @a2))
    (t/is (= 2 @a3))))

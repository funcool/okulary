(ns okulary.util
  (:refer-clojure :exclude [next]))

(defn iterator
  "Get iterator object from iterable."
  [iterable]
  (let [iterator-fn (unchecked-get iterable js/Symbol.iterator)]
    (.call ^js iterator-fn iterable)))

(defn next
  [iterator]
  (.next ^js iterator))

(defn ^boolean done?
  [chunk]
  (.-done ^js chunk))

(defn value
  [chunk]
  (.-value ^js chunk))

(defn doiter
  [iterable f]
  (let [iterator (iterator iterable)]
    (loop [chunk (next iterator)]
      (when-not (done? chunk)
        (f (value chunk))
        (recur (next iterator))))))

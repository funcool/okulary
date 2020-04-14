(ns okulary.core
  (:refer-clojure :exclude [key ->Atom Atom atom derive]))

(defn key
  "A key based selector."
  [k]
  (fn [v] (get v k)))

(defn in
  [kv]
  (fn [v] (get-in v kv)))

(deftype Atom [state watches]
  Object
  (equiv [self other]
    (-equiv self other))

  IAtom
  IEquiv
  (-equiv [o other] (identical? o other))

  IDeref
  (-deref [_] state)

  IReset
  (-reset! [self newval]
    (let [oldval state]
      (set! (.-state self) newval)
      (when (> (.-size watches) 0)
        (-notify-watches self oldval newval))
      newval))

  ISwap
  (-swap! [self f]
    (-reset! self (f state)))
  (-swap! [self f x]
    (-reset! self (f state x)))
  (-swap! [self f x y]
    (-reset! self (f state x y)))
  (-swap! [self f x y more]
    (-reset! self (apply f state x y more)))

  IWatchable
  (-notify-watches [self oldval newval]
    (let [a (js/Array.from (.entries watches))
          t (alength a)]
      (loop [i 0]
        (when (< i t)
          (let [nx (aget a i)
                f (aget nx 1)
                k (aget nx 0)]
            (f k self oldval newval)
            (recur (inc i)))))))

  (-add-watch [self key f]
    (.set watches key f)
    self)

  (-remove-watch [self key]
    (.delete watches key))

  IHash
  (-hash [self] (goog/getUid self)))

(defn atom
  "Creates and returns an Atom with an initial value of x."
  [x]
  (Atom. x (js/Map.)))

(def EMPTY (js/Symbol "empty"))

(deftype DerivedAtom [id selector source equals? watchers srccache cache]
  IAtom
  IDeref
  (-deref [self]
    (let [source (deref source)]
      (if (and (not (identical? cache EMPTY))
               (identical? srccache source))
        (.-cache self)
        (let [result (selector source)]
          (set! (.-srccache self) source)
          (set! (.-cache self) result)
          result))))

  IWatchable
  (-add-watch [self key cb]
    (.set watchers key cb)
    (when (identical? (.-size watchers) 1)
      (add-watch source id
                 (fn [_ _ oldv newv]
                   (when-not (identical? oldv newv)
                     (let [new' (selector newv)
                           old' (selector oldv)]
                       (set! (.-srccache self) newv)
                       (set! (.-cache self) new')
                       (when-not ^boolean (equals? old' new')
                         (let [a (js/Array.from (.entries watchers))
                               t (alength a)]
                           (loop [i 0]
                             (when (< i t)
                               (let [nx (aget a i)
                                     f (aget nx 1)
                                     k (aget nx 0)]
                                 (f k self old' new')
                                 (recur (inc i))))))))))))
    self)

  (-remove-watch [self key]
    (.delete watchers key)
    (when (identical? (.-size watchers) 0)
      (remove-watch source id)
      (set! (.-cache self) EMPTY))))

(defn derived
  "Create a derived atom from an other atom with the provided lense.

  The returned atom is lazy, so no code is executed until user
  requires it.

  By default the derived atom does not trigger updates if the data
  does not affects to it (determined by selector), but this behavior
  can be deactivated passing `:equals?` to `false` on the third
  options parameter. You also may pass `=` as `equals?` parameter if
  you want value comparison instead of reference comparison with
  `identical?`."
  ([selector source]
   (derived selector source identical?))
  ([selector source equals?]
   (DerivedAtom. (js/Symbol "okulary") selector source equals? (js/Map.)
                 EMPTY EMPTY)))

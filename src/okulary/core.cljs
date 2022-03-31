(ns okulary.core
  (:refer-clojure :exclude [key ->Atom Atom atom derive])
  (:require [okulary.util :as ou]))

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
    (ou/doiter
     (.entries watches)
     (fn [n]
       (let [f (aget n 1)
             k (aget n 0)]
         (f k self oldval newval)))))

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

(def ^:private EMPTY (js/Symbol "empty"))

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
    (when (= (.-size watchers) 1)
      (add-watch source id
                 (fn [_ _ old-source-value new-source-value]
                   (when-not (identical? old-source-value new-source-value)
                     (let [;; As first step we apply the selector to
                           ;; the new source value.
                           new-value  (selector new-source-value)

                           ;; Retrieve the cached value, if it is
                           ;; empty, execute the selector for the old
                           ;; value.
                           old-cached (.-cache self)
                           old-value  (if (identical? old-cached EMPTY)
                                        (selector old-source-value)
                                        old-cached)]

                       ;; Store the new source value in the instance;
                       ;; this is mainly used by the deref, so this is
                       ;; just a small performance improvement for it.
                       (set! (.-srccache self) new-source-value)

                       ;; Cache the new value in the instance.
                       (set! (.-cache self) new-value)

                       ;; Then proceed to check if the new value and
                       ;; the old value are equals using user provided
                       ;; equals function.
                       (when-not ^boolean (equals? new-value old-value)
                         ;; Iterate over all watchers and run them
                         (ou/doiter (.entries watchers)
                                    (fn [n]
                                      (let [f (aget n 1)
                                            k (aget n 0)]
                                        (f k self old-value new-value))))))))))
    self)

  (-remove-watch [self key]
    (.delete watchers key)
    (when (= (.-size watchers) 0)
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
   (DerivedAtom. (js/Symbol "okulary") selector source equals? (js/Map.) EMPTY EMPTY)))

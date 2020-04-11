# okulary #

A faster Atom and DerivedAtom impl for clojurescript.

```clojure
{:deps {funcool/okulary {:mvn/version "2020.04.11-0"}}}
```


## Atom ##

Is a Faster (mainly related to watcher handling) implementation of
Atom reference type (of clojurescript).

```clojure
(require '[okulary.core :as ol])

(def myatom (ol/atom 0))

@myatom
;; => 0
```

It behaves almost identically to the default builtin atom. Just
dispatches the watchers much faster. being faster don't mean better
than the default one. The implementation of this library has
tradeoffs:

- Only supports keys as strings or JavaScript Symbols. Using anything
  different is considered undefined behaviour.
- Validators are not supported.
- Does not implements IMeta protocol.

The main use case for this implementation is for situations where you
have a huge amount of watchers and you want watcher dispatching very
fast.


## Derived Atom ##

The DerivedAtom is like a read-only cursor, and can derive from the
atom defined in this library or any that implements the IWatchable
protocol (clojurescript builtin Atom as example).

```clojure
(def a1 (ol/atom {:a 1 :b 1}))
(def d1 (ol/derived :a a1))

@a1
;; => {:a 1 :b 1}

@d1
;; => 1
```


The relevant features of DerivedAtom's:

- They don't need resource management. If no watcher is attached the
  selector function will not be executed and this will not add
  overhead.
- If the source value is the same, selector will be omited and a
  cached value will be used.
- Only attaches 1 watcher to the parent atom-like independently of the
  number of watchers this derived atom will have.
- The watchers on derived atom will be called only if the value is
  changed using the `identical?` equality check (you can provide your
  own equality function as third argument to `derived`).
- Selector will be called twice on each change of the parent atom-like
  reference when no local cache is found and once when a cache is
  available.





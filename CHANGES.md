# Changelog #

## 2020.04.14-0

- Use defensive copy of iterators (prevents infinite loops when
  dispatching a watcher adds an other watcher).


## 2020.04.13-0

- Bugfixes.


## 2020.04.11-0

- Bugfixes, tests & documentation.


## 2020.04.05-0

Initial release with:

- Faster `Atom` implementation (uses js native Map to store watches,
  does not support validator and uses the fastest way to iterate over
  watches on changes notification, instead of doseq).

- `DerivedAtom` implementation using same technique that with Atom and
  instead of using generic lenses, uses a simple selector
  functions. DerivedAtom's are read-only.

- Default selectors `key` and `in`.


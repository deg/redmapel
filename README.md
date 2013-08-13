# redmapel

Redmapel is a simple, single-purpose, library designed to work in both
Clojure and ClojureScript. It lets you easily manage a tree of related
values and set triggers to fire when elements of the tree have
changed.

It is in-memory storage, with no built-in persistence to disk or
net. But, its trigger mechanism makes it trivial to add automatic
persistence.

## History and Naming

Redmapel is small state tree library, named after the stunning [red
maple
tree](http://www.publicdomainpictures.net/view-image.php?image=12819&picture=red-maple-tree),
the offical tree of the small state of Rhode Island. (groan!)

The misspelling is deliberate, to make it easier to search for
Redmapel. Pronounciation: whatever you like in the privacy of your own
room, but I favor rhyming with the French m'appelle. [see
video](http://www.youtube.com/watch?v=5H59Py7KApU).

For the latest version, include `[degel.redmapel "0.1.7"]` in your
`project.clj`.

## Background

Redmapel is loosely inspired by the state management ideas of
[Pedestal](http://pedestal.io/). But, I wanted something much simpler,
and without all of Pedestal's learning curve.

The Redmapel library lets you create a tree of persistent values,
stored together in a single clojure atom. Each element in the tree is
named by a *path*, which is a vector of objects, typically keywords.

The API lets you get and set values, and also to set alerts that
trigger when a value changes.

Triggers can be set on a single value or an entire subtree of
values. Multiple triggers can be in place simultaneously, even on the
same or overlapping values or subtrees.

Triggers can be registered to occur before a value is set (where they
can disable the action) or after (typically to trigger side-effects).

## Usage

The primariy API is defined in the redmapel namespace, as a set of
side-effecting actions on an atom.

The redmapel.node namespace contains a parallel, lower-level API that
offers essentially the same behaviors, but on a regular immutable map
structure. (Behind the scenes,the primary API relies on this API, and
is just careful to save results back into the designated atom).

For full API details, see the source for now, or use `lein marg` to
format the comments more readably. The test directory also contains
some simple usage examples.

I'll supply more detailed docs and sample usages here soon, once the
API stabilizes.

## License

Copyright © 2013 David Goldfarb, deg@degel.com

Distributed under the Eclipse Public License, the same as Clojure.

The use and distribution terms for this software are covered by the
[Eclipse Public License
1.0](http://opensource.org/licenses/eclipse-1.0.php) which can be
found in the file epl-v10.html at the root of this distribution.  By
using this software in any fashion, you are agreeing to be bound by
the terms of this license.

You must not remove this notice, or any other, from this software.


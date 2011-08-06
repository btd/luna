/*
 * Copyright (c) 2011 Denis Bardadym
 * Distributed under Apache License.
 */

package com.twitter.querulous.query

import scala.collection.mutable

class QueryClass(val name: String)

object QueryClass {
  val classes = mutable.Map[String, QueryClass]()

  def apply(name: String) = {
    classes(name) = new QueryClass(name)
    lookup(name)
  }

  def lookup(name: String) = classes(name)

  val Select = QueryClass("select")
  val Execute = QueryClass("execute")
}

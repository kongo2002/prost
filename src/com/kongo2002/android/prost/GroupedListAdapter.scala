/*
 * Copyright 2013 Gregor Uhlenheuer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kongo2002.android.prost

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ArrayAdapter
import android.widget.BaseAdapter

import scala.collection.mutable.Map


class GroupedListAdapter(ctx: Context) extends BaseAdapter {

  final val TYPE_HEADER  = 0
  final val TYPE_CONTENT = 1

  private val headers = new ArrayAdapter[String](ctx, R.layout.list_header)
  private val sections = Map[String, Adapter]()

  def addSection(section: String, adapter: Adapter) {
    headers.add(section)
    sections(section) = adapter
  }

  def getItem(position: Int) : Object = {
    var pos = position
    for (sec <- sections.keySet) {
      if (pos == 0) return sec

      val adapter = sections(sec)
      val size = adapter.getCount + 1

      if (pos < size)
        return adapter.getItem(pos - 1)

      pos -= size
    }
    return null
  }

  def getCount = {
    var count = 0
    sections.values.foreach { s => count += s.getCount + 1 }
    count
  }

  override def getViewTypeCount = {
    var count = TYPE_CONTENT
    sections.values.foreach { s => count += s.getViewTypeCount }
    count
  }

  override def getItemViewType(position: Int) : Int = {
    var pos = position
    var t = TYPE_CONTENT

    for (sec <- sections.keySet) {
      if (pos == 0) return TYPE_HEADER

      val adapter = sections(sec)
      val size = adapter.getCount + 1

      if (pos < size)
        return t + adapter.getItemViewType(pos - 1)

      pos -= size
      t += adapter.getViewTypeCount
    }

    return Adapter.IGNORE_ITEM_VIEW_TYPE
  }

  def areAllItemsSelectable = false

  override def isEnabled(position: Int) = {
    getItemViewType(position) != TYPE_HEADER
  }

  override def getView(position: Int, view: View, parent: ViewGroup) : View = {
    var pos = position
    var section = 0

    for (sec <- sections.keySet) {
      if (pos == 0) return headers.getView(section, view, parent)

      val adapter = sections(sec)
      val size = adapter.getCount + 1

      if (pos < size)
        return adapter.getView(pos - 1, view, parent)

      pos -= size
      section += 1
    }
    return null
  }

  override def getItemId(position: Int) = position
}

/* vim: set et sw=2 sts=2: */

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
import android.widget.TextView

import scala.collection.mutable.Map


class IdArrayAdapter[T <: Id](ctx: Context, res: Int, items: Array[T], func: T => String)
  extends ArrayAdapter[T](ctx, res, items) {

  val texts = items.map(func)
  val (map, revMap) = getMaps(items)

  def getPosition(id: Long) = revMap(id)

  override def getItemId(pos: Int) = {
    items(pos).id
  }

  override def getView(position: Int, convertView: View, parent: ViewGroup) = {
    val text = texts(position)
    val (view, holder) = getHolder(convertView)

    holder.text.setText(text)
    view
  }

  private def getHolder(view: View) = {
    if (view == null) {
      val newView = View.inflate(ctx, R.layout.list_row, null)

      val holder = ViewHolder(TR.find(newView, TR.rowText))
      newView.setTag(holder)

      (newView, holder)
    } else {
      (view, view.getTag.asInstanceOf[ViewHolder])
    }
  }

  private def getMaps(items: Traversable[T]) = {
    val map = Map[Long, T]()
    val reverseMap = Map[Long, Int]()
    var index = 0

    for (item <- items) {
      val id = item.id

      map(id) = item
      reverseMap(id) = index

      index += 1
    }

    (map, reverseMap)
  }
}

case class ViewHolder(text: TextView)

/* vim: set et sw=2 sts=2: */

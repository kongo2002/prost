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

import android.app.Activity
import android.content.DialogInterface
import android.view.View
import android.widget.AdapterView


object Implicits {

  implicit def view2Typed(v: View) = new TypedViewHolder { def view = v }

  implicit def activity2Typed(a : Activity) = new TypedActivityHolder { def activity = a }

  implicit def func2OnClickListener(f: View => Unit) : View.OnClickListener = {
    new View.OnClickListener() {
      def onClick(v: View) {
        f(v)
      }
    }
  }

  implicit def func2OnLongClickListener(f: View => Boolean) : View.OnLongClickListener = {
    new View.OnLongClickListener() {
      def onLongClick(v: View) = {
        f(v)
      }
    }
  }

  implicit def func2DialogOnClickListener(f: (DialogInterface, Int) => Unit) : DialogInterface.OnClickListener = {
    new DialogInterface.OnClickListener() {
      override def onClick(di: DialogInterface, i: Int) {
        f(di, i)
      }
    }
  }

  implicit def func2OnItemClickListener(f: (AdapterView[_], View, Int, Long) => Unit) : AdapterView.OnItemClickListener = {
    new AdapterView.OnItemClickListener() {
      override def onItemClick(parent: AdapterView[_], v: View, pos: Int, id: Long) {
        f(parent, v, pos, id)
      }
    }
  }

  implicit def func2Runnable[F](f: () => F): Runnable =
    new Runnable() {
      def run() {
        f()
      }
    }

  implicit def lazy2Runnable[F](f: => F): Runnable =
    new Runnable() {
      def run() {
        f
      }
    }
}

/* vim: set et sw=2 sts=2: */

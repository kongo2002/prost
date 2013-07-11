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

package com.kongo2002.android.prost;

import android.app.Activity
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView


case class TypedResource[T](id : Int)

object TR {
  /* statistics fragment */
  val newBeerBtn = TypedResource[Button](R.id.new_beer)
  val topLeftTile = TypedResource[LinearLayout](R.id.tl_layout)
  val topTile = TypedResource[LinearLayout](R.id.t_layout)
  val topRightTile = TypedResource[LinearLayout](R.id.tr_layout)
  val leftTile = TypedResource[LinearLayout](R.id.l_layout)
  val rightTile = TypedResource[LinearLayout](R.id.r_layout)
  val bottomLeftTile = TypedResource[LinearLayout](R.id.bl_layout)
  val bottomTile = TypedResource[LinearLayout](R.id.b_layout)
  val bottomRightTile = TypedResource[LinearLayout](R.id.br_layout)
  val pager = TypedResource[ViewPager](R.id.pager)

  /* drinks fragment */
  val drinksList = TypedResource[ListView](R.id.drinksList)

  def find[T](v: View, tr: TypedResource[T]) = v.findViewById(tr.id).asInstanceOf[T]
}

trait TypedViewHolder {
  def view : View
  def findView[T](tr : TypedResource[T]) = view.findViewById(tr.id).asInstanceOf[T]
}

trait TypedView extends View with TypedViewHolder {
  def view = this
}

trait TypedActivityHolder {
  def activity : Activity
  def findView[T](tr : TypedResource[T]) = activity.findViewById(tr.id).asInstanceOf[T]
}

trait TypedActivity extends Activity with TypedActivityHolder {
  def activity = this
}

trait TypedFragment extends Fragment with TypedActivityHolder {
  def fragment = this
  def activity = fragment.getActivity
}

trait TypedFragmentActivity extends FragmentActivity with TypedActivity { }

trait Loggable {
  val tag = this.getClass().getSimpleName()
  def TAG = tag

  def logI(msg: String) {
    if (BuildConfig.DEBUG) Log.i(tag, msg)
  }
}

/* vim: set et sw=2 sts=2: */

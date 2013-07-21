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

import android.os.Bundle
import android.support.v4.widget.SimpleCursorAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo

import Implicits._
import DrinksDatabase.BarsCursor

class BarsFragment extends TypedFragment
  with Loggable {

  val OPTION_DELETE_BAR = 1

  lazy val db = new DrinksDatabase.DrinksDatabase(getActivity)
  lazy val barsList = findView(TR.barsList)

  var cursor : BarsCursor = null

  override def onCreateView(inf: LayoutInflater, vg: ViewGroup, b: Bundle) = {
    logI("onCreateView")

    val view = inf.inflate(R.layout.bars_fragment, vg, false)

    setHasOptionsMenu(true)

    view
  }

  override def onViewCreated(view: View, bundle: Bundle) {
    /* create adapter */
    cursor = db.getAllBarsCursor
    val selectedFields = Array(BarsCursor.KEY_NAME)
    val bindResources = Array(R.id.bars_text)
    val adapter = new SimpleCursorAdapter(activity, R.layout.bars_row, cursor, selectedFields, bindResources)

    /* attach adapter */
    barsList.setAdapter(adapter)

    /* hook into list events */
    registerForContextMenu(barsList)

    barsList.setOnItemClickListener((p: AdapterView[_], v: View, pos: Int, id: Long) => {
      /* TODO: edit bar */
      logI("edit bar" + id)
    })
  }

  override def onPause {
    logI("onPause")
    db.close

    super.onPause
  }

  override def onDestroy {
    logI("onDestroy")
    db.close

    super.onDestroy
  }
}

/* vim: set et sw=2 sts=2: */

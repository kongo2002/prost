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
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.CursorAdapter
import android.support.v4.widget.SimpleCursorAdapter
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo

import Implicits._
import DrinksDatabase.BarsCursor


/**
 * Fragment that displays a list of all available bars
 * contained in the database.
 */
class BarsFragment extends TypedFragment
  with Loggable {

  lazy val db = new DrinksDatabase.DrinksDatabase(getActivity)
  lazy val barsList = findView(TR.barsList)

  var cursor : BarsCursor = null

  override def onCreateView(inf: LayoutInflater, vg: ViewGroup, b: Bundle) = {
    logI("onCreateView")

    val view = inf.inflate(R.layout.bars_fragment, vg, false)

    setHasOptionsMenu(true)

    view
  }

  private def refreshView {
    val adapter = barsList.getAdapter.asInstanceOf[CursorAdapter]
    cursor = db.getAllBarsCursor

    adapter.changeCursor(cursor)
    adapter.notifyDataSetChanged
  }

  override def onCreateContextMenu(menu: ContextMenu, view: View, info: ContextMenuInfo) {
    super.onCreateContextMenu(menu, view, info)

    menu.add(0, Options.DELETE_BAR, 0, R.string.delete_bar)
  }

  override def onContextItemSelected(item: MenuItem) = {
    item.getItemId match {
      case Options.DELETE_BAR => {
        val info = item.getMenuInfo.asInstanceOf[AdapterContextMenuInfo]
        val id = info.id

        val title = activity.getString(R.string.delete_bar)
        val name = db.getBarName(info.id)
        val question = activity.getString(R.string.delete_bar_question).format(name.get)

        UI.confirm(activity, title, question,
          (_, _) => {
            db.removeBar(id)
            refreshView
          })
        true
      }
      case _ => super.onContextItemSelected(item)
    }
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
      /* position cursor */
      cursor.moveToPosition(pos)

      /* build intent and start edit activity */
      val intent = getIntent(id)

      startActivityForResult(intent, Activities.EDIT_BAR)
    })
  }

  override def onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.bar, menu)

    super.onCreateOptionsMenu(menu, inflater)
  }

  override def onOptionsItemSelected(item: MenuItem) = {
    item.getItemId match {
      case R.id.menu_add_bar => {
        val intent = new Intent(activity, classOf[EditBarActivity])
        startActivityForResult(intent, Activities.CREATE_BAR)
        true
      }
      case _ => super.onOptionsItemSelected(item)
    }
  }

  override def onActivityResult(request: Int, result: Int, data: Intent) {
    super.onActivityResult(request, result, data)

    if (result == Activity.RESULT_OK) {
      val extras = data.getExtras
      val bar = Bar.fromBundle(extras)

      request match {
        case Activities.CREATE_BAR => db.addBar(bar)
        case Activities.EDIT_BAR   => db.updateBar(bar)
      }

      refreshView
    }
  }

  private def getIntent(id: Long) = {
    val intent = new Intent(activity, classOf[EditBarActivity])

    intent.putExtra(DrinksDatabase.KEY_ID, id)
    intent.putExtra(BarsCursor.KEY_NAME, cursor.getBarName)
    intent.putExtra(BarsCursor.KEY_LONGITUDE, cursor.getBarLongitude)
    intent.putExtra(BarsCursor.KEY_LATITUDE, cursor.getBarLatitude)

    intent
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

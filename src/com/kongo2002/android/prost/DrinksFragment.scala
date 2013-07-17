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
import DrinksDatabase.DrinkTypesCursor


class DrinksFragment extends TypedFragment
  with Loggable {

  val OPTION_DELETE_DRINK = 1

  lazy val db = new DrinksDatabase.DrinksDatabase(getActivity)
  lazy val drinksList = findView(TR.drinksList)

  var cursor : DrinkTypesCursor = null

  override def onCreateView(inf: LayoutInflater, c: ViewGroup, b: Bundle) = {
    logI("onCreateView")

    val view = inf.inflate(R.layout.drinks_fragment, c, false)

    setHasOptionsMenu(true)

    view
  }

  override def onViewCreated(view: View, bundle: Bundle) {
    /* create adapter */
    cursor = db.getAllDrinkTypesCursor
    val selectedFields = Array(DrinkTypesCursor.KEY_NAME)
    val bindResources = Array(R.id.drink_text)
    val adapter = new SimpleCursorAdapter(activity, R.layout.drinks_row, cursor, selectedFields, bindResources)

    /* attach list adapter */
    drinksList.setAdapter(adapter)

    /* hook into list events */
    registerForContextMenu(drinksList)

    drinksList.setOnItemClickListener((p: AdapterView[_], v: View, pos: Int, id: Long) => {
      /* position cursor */
      cursor.moveToPosition(pos)

      /* build intent with its extra contents */
      val intent = new Intent(activity, classOf[EditDrinkActivity])
      intent.putExtra(DrinksDatabase.KEY_ID, id)
      intent.putExtra(DrinkTypesCursor.KEY_NAME, cursor.getTypeName)
      intent.putExtra(DrinkTypesCursor.KEY_TYPE, cursor.getType.id)
      intent.putExtra(DrinkTypesCursor.KEY_UNIT, cursor.getTypeUnit)
      intent.putExtra(DrinkTypesCursor.KEY_PRICE, cursor.getPrice)
      intent.putExtra(DrinkTypesCursor.KEY_BAR, cursor.getDrinkTypeBar)

      /* start edit activity */
      startActivityForResult(intent, Activities.EDIT_DRINK)
    })
  }

  override def onCreateContextMenu(menu: ContextMenu, view: View, info: ContextMenuInfo) {
    super.onCreateContextMenu(menu, view, info)

    menu.add(0, OPTION_DELETE_DRINK, 0, R.string.delete_drink)
  }

  override def onContextItemSelected(item: MenuItem) = {
    item.getItemId match {
      case OPTION_DELETE_DRINK => {
        def getMessage(id: Long) = {
          /* TODO: use resource strings */
          val usage = db.getDrinkTypeUsage(id)
          val msg = "Do you really want to delete the selected drink type?"
          usage match {
            case Some(x) if x > 0 => {
              msg + " The drink type is used " + x + " times."
            }
            case _ => msg
          }
        }

        val info = item.getMenuInfo.asInstanceOf[AdapterContextMenuInfo]
        val id = info.id

        UI.confirm(activity, "Delete drink type", getMessage(id),
            (_, _) => {
              db.removeDrinkType(id)
              refreshView
            })

        true
      }
      case _ => super.onContextItemSelected(item)
    }
  }

  override def onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.drinks, menu)

    super.onCreateOptionsMenu(menu, inflater)
  }

  override def onOptionsItemSelected(item: MenuItem) = {
    item.getItemId match {
      case R.id.menu_add_drink => {
        val intent = new Intent(activity, classOf[EditDrinkActivity])
        startActivityForResult(intent, Activities.CREATE_DRINK)
        true
      }
      case _ => super.onOptionsItemSelected(item)
    }
  }

  override def onActivityResult(request: Int, result: Int, data: Intent) {
    super.onActivityResult(request, result, data)

    if (result == Activity.RESULT_OK) {
      val extras = data.getExtras
      val drinkType = DrinkType.fromBundle(extras)

      request match {
        case Activities.CREATE_DRINK => db.addDrinkType(drinkType)
        case Activities.EDIT_DRINK => db.updateDrinkType(drinkType)
      }

      refreshView
    }
  }

  private def refreshView {
    val adapter = drinksList.getAdapter.asInstanceOf[CursorAdapter]
    cursor = db.getAllDrinkTypesCursor

    adapter.changeCursor(cursor)
    adapter.notifyDataSetChanged
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

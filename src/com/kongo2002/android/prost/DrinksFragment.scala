package com.kongo2002.android.prost

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.view.View
import android.support.v4.widget.SimpleCursorAdapter
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo

class DrinksFragment extends TypedFragment
  with Loggable {

  lazy val db = new DrinksDatabase.DrinksDatabase(getActivity)
  lazy val drinksList = findView(TR.drinksList)

  val OPTION_DELETE_DRINK = 1

  override def onCreateView(inf: LayoutInflater, c: ViewGroup, b: Bundle) = {
    logI("onCreateView")

    val view = inf.inflate(R.layout.drinks_fragment, c, false)

    setHasOptionsMenu(true)

    view
  }

  override def onViewCreated(view: View, bundle: Bundle) {
    /* create adapter */
    val cursor = db.getAllDrinkTypesCursor
    val selectedFields = Array(DrinksDatabase.DrinkTypesCursor.NAME_KEY)
    val bindResources = Array(R.id.drink_text)
    val adapter = new SimpleCursorAdapter(activity, R.layout.drinks_row, cursor, selectedFields, bindResources)

    /* attach list adapter */
    drinksList.setAdapter(adapter)

    registerForContextMenu(drinksList)
  }

  override def onCreateContextMenu(menu: ContextMenu, view: View, info: ContextMenuInfo) {
    super.onCreateContextMenu(menu, view, info)

    menu.add(0, OPTION_DELETE_DRINK, 0, R.string.delete_drink)
  }

  override def onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.drinks, menu)

    super.onCreateOptionsMenu(menu, inflater)
  }

  override def onOptionsItemSelected(item: MenuItem) = {
    item.getItemId match {
      case R.id.menu_add_drink => {
        val intent = new Intent(activity, classOf[EditDrinkActivity])
        startActivityForResult(intent, Activities.EDIT_DRINK)
        true
      }
    }
  }

}
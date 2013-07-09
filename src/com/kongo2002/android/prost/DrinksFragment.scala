package com.kongo2002.android.prost

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup

class DrinksFragment extends TypedFragment
  with Loggable {

  override def onCreateView(inf: LayoutInflater, c: ViewGroup, b: Bundle) = {
    logI("onCreateView")

    val view = inf.inflate(R.layout.drinks_fragment, c, false)

    setHasOptionsMenu(true)

    view
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
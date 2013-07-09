package com.kongo2002.android.prost

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.MenuInflater
import android.view.Menu

class DrinksFragment extends Fragment
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

}
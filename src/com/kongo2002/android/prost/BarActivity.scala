package com.kongo2002.android.prost

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup

class BarActivity extends Fragment {

  override def onCreateView(inf: LayoutInflater, c: ViewGroup, b: Bundle) = {
    Log.i("prost", "onCreateView: BarActivity")
    val view = inf.inflate(R.layout.bar_activity, c, false)
    view
  }

}
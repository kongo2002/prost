package com.kongo2002.android.prost

import android.os.Bundle

class EditDrinkActivity extends TypedActivity
  with Loggable {

  override def onCreate(state: Bundle) {
    super.onCreate(state)

    setContentView(R.layout.edit_drink_activity)

    logI("onCreate")
  }
}
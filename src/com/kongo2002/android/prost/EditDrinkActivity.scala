package com.kongo2002.android.prost

import android.os.Bundle

import DrinksDatabase.DrinkTypesCursor

class EditDrinkActivity extends TypedActivity
  with Loggable {

  lazy val editName = findView(TR.editDrinkName)
  lazy val editUnit = findView(TR.editDrinkUnit)

  override def onCreate(state: Bundle) {
    super.onCreate(state)

    setContentView(R.layout.edit_drink_activity)

    val extras = getIntent.getExtras
    if (extras != null) {
      val name = extras.getString(DrinkTypesCursor.KEY_NAME)
      val unit = extras.getInt(DrinkTypesCursor.KEY_UNIT)

      if (name != null) editName.setText(name)
      if (unit > 0) editUnit.setText(unit.toString)
    }

    logI("onCreate")
  }
}
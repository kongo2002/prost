package com.kongo2002.android.prost

import android.os.Bundle
import DrinksDatabase.DrinkTypesCursor
import android.widget.ArrayAdapter

class EditDrinkActivity extends TypedActivity
  with Loggable {

  lazy val editName = findView(TR.editDrinkName)
  lazy val editUnit = findView(TR.editDrinkUnit)
  lazy val selectType = findView(TR.selectDrinkType)

  override def onCreate(state: Bundle) {
    super.onCreate(state)

    setContentView(R.layout.edit_drink_activity)

    val adapter = ArrayAdapter.createFromResource(this, R.array.drink_types, android.R.layout.simple_spinner_item)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

    selectType.setAdapter(adapter)

    /* load intent contents if specified */
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
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
import android.widget.ArrayAdapter
import android.widget.TextView
import android.view.View
import android.content.Intent
import android.app.Activity

import DrinksDatabase.DrinkTypesCursor
import Implicits._


class EditDrinkActivity extends TypedActivity
  with Loggable {

  lazy val editName = findView(TR.editDrinkName)
  lazy val editUnit = findView(TR.editDrinkUnit)
  lazy val editPrice = findView(TR.editDrinkPrice)
  lazy val selectType = findView(TR.selectDrinkType)
  lazy val submit = findView(TR.submitDrinkType)

  var id = 0L

  override def onCreate(state: Bundle) {
    super.onCreate(state)

    setContentView(R.layout.edit_drink_activity)

    val adapter = ArrayAdapter.createFromResource(this, R.array.drink_types, android.R.layout.simple_spinner_item)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

    selectType.setAdapter(adapter)

    /* load intent contents if specified */
    val extras = getIntent.getExtras
    if (extras != null) {
      id = extras.getLong(DrinksDatabase.KEY_ID)

      val name = extras.getString(DrinkTypesCursor.KEY_NAME)
      val drinkType = extras.getInt(DrinkTypesCursor.KEY_TYPE)
      val unit = extras.getInt(DrinkTypesCursor.KEY_UNIT)
      val price = extras.getInt(DrinkTypesCursor.KEY_PRICE)
      val bar = extras.getLong(DrinkTypesCursor.KEY_BAR)

      if (name != null)
        editName.setText(name)

      editUnit.setText(unit.toString)
      editPrice.setText(price.toString)
      selectType.setSelection(drinkType)
    }

    /* add validation callbacks */
    editName.addTextChangedListener(new EditTextValidator(editName) {
      override def getError(view: TextView, value: String) = {
        if (StringUtils.isEmpty(value))
          Some("invalid drink type name given")
        else
          None
      }
    })

    editUnit.addTextChangedListener(new EditTextValidator(editUnit) {
      override def getError(view: TextView, value: String) = {
        if (StringUtils.isEmpty(value) || value.toInt < 1)
          Some("unit has to be a valid amount in milliliters")
        else
          None
      }
    })

    submit.setOnClickListener((v: View) => {
      val intent = new Intent()
      intent.putExtras(getResultBundle)

      setResult(Activity.RESULT_OK, intent)
      finish
    })

    logI("onCreate")
  }

  private def getResultBundle = {
    val bundle = new Bundle()

    if (id > 0)
      bundle.putLong(DrinksDatabase.KEY_ID, id)

    bundle.putString(DrinkTypesCursor.KEY_NAME, editName.getText.toString)
    bundle.putInt(DrinkTypesCursor.KEY_UNIT, editUnit.getText.toString.toInt)
    bundle.putInt(DrinkTypesCursor.KEY_TYPE, selectType.getSelectedItemPosition)
    bundle.putInt(DrinkTypesCursor.KEY_PRICE, editPrice.getText.toString.toInt)

    /* TODO: add bar */
    bundle.putLong(DrinkTypesCursor.KEY_BAR, 0)

    bundle
  }
}

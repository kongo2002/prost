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
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView

import DrinksDatabase.DrinkTypesCursor
import Implicits._


class EditDrinkActivity extends TypedActivity
  with Loggable {

  lazy val db = new DrinksDatabase.DrinksDatabase(this)

  lazy val editName = findView(TR.editDrinkName)
  lazy val editUnit = findView(TR.editDrinkUnit)
  lazy val editPrice = findView(TR.editDrinkPrice)
  lazy val selectType = findView(TR.selectDrinkType)
  lazy val selectBar = findView(TR.selectDrinkBar)
  lazy val submit = findView(TR.submitDrinkType)

  var id = 0L

  override def onCreate(state: Bundle) {
    super.onCreate(state)

    setContentView(R.layout.edit_drink_activity)

    /* drink types adapter */
    val dtAdapter = ArrayAdapter.createFromResource(this, R.array.drink_types, android.R.layout.simple_spinner_item)
    dtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

    selectType.setAdapter(dtAdapter)

    /* bars adapter */
    val allBars = db.getAllBarMap(getString(R.string.no_bar))
    val barNames = allBars.values.map { b => b.name }
    val barAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, barNames.toArray)
    barAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

    selectBar.setAdapter(barAdapter)

    /* load intent contents if specified */
    val extras = getIntent.getExtras
    if (extras != null) {
      val dt = DrinkType.fromBundle(extras)

      id = dt.id

      if (dt.name != null)
        editName.setText(dt.name)

      editUnit.setText(dt.unit.toString)
      editPrice.setText(dt.price.toString)
      selectType.setSelection(dt.baseType.id)
    }

    /* add validation callbacks */
    editName.addTextChangedListener(new EditTextValidator(editName) {
      override def getError(view: TextView, value: String) = {
        if (StringUtils.isEmpty(value))
          Some("invalid drink name given")
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

/* vim: set et sw=2 sts=2: */

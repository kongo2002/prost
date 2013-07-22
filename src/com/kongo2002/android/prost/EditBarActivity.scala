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


class EditBarActivity extends TypedActivity
  with Loggable {

  lazy val editName = findView(TR.editBarName)
  lazy val submit = findView(TR.submitBar)

  var id = 0L

  override def onCreate(state: Bundle) {
    super.onCreate(state)

    setContentView(R.layout.edit_bar_activity)

    /* TODO: load intent contents if specified */

    /* add validation callbacks */
    editName.addTextChangedListener(new EditTextValidator(editName) {
      override def getError(view: TextView, value: String) = {
        if (StringUtils.isEmpty(value))
          Some("invalid drink type name given")
        else
          None
      }
    })

    submit.setOnClickListener((v: View) => {
      val intent = new Intent()

      /* TODO: build intent extras */

      setResult(Activity.RESULT_OK, intent)
      finish
    })

    logI("onCreate")
  }
}

/* vim: set et sw=2 sts=2: */


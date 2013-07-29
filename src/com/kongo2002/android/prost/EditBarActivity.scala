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

import DrinksDatabase.BarsCursor
import Implicits._


class EditBarActivity extends TypedActivity
  with Loggable {

  lazy val editName = findView(TR.editBarName)
  lazy val submit = findView(TR.submitBar)

  var id = 0L

  override def onCreate(state: Bundle) {
    super.onCreate(state)

    setContentView(R.layout.edit_bar_activity)

    /* load intent contents if specified */
    val extras = getIntent.getExtras
    if (extras != null) {
      val bar = Bar.fromBundle(extras)

      id = bar.id

      if (bar.name != null)
        editName.setText(bar.name)

      /* TODO: longitude, latitude */
    }

    /* add validation callbacks */
    editName.addTextChangedListener(new EditTextValidator(editName) {
      override def getError(view: TextView, value: String) = {
        if (StringUtils.isEmpty(value))
          Some("invalid bar name given")
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

    bundle.putString(BarsCursor.KEY_NAME, editName.getText.toString)

    /* TODO: longitude, latitude */

    bundle
  }
}

/* vim: set et sw=2 sts=2 tw=120: */


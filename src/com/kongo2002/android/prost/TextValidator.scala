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

import android.text.TextWatcher
import android.text.Editable
import android.widget.TextView
import android.widget.EditText


abstract class TextValidator(view: TextView) extends TextWatcher {

  def validate(view: TextView, value: String) : Unit

  override def afterTextChanged(edit: Editable) {
    validate(view, view.getText.toString)
  }

  override def onTextChanged(str: CharSequence, start: Int, before: Int, count: Int) { }

  override def beforeTextChanged(str: CharSequence, start: Int, count: Int, after: Int) { }
}

abstract class EditTextValidator(edit: EditText) extends TextValidator(edit) {

  def getError(view: TextView, value: String) : Option[String]

  override def validate(view: TextView, value: String) {
    getError(view, value) match {
      case Some(error) => edit.setError(error)
      case None => { }
    }
  }
}
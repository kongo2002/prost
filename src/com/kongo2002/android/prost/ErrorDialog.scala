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
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment

import com.google.android.gms.common.GooglePlayServicesUtil


class ErrorDialog(dialog: Dialog) extends DialogFragment {

  override def onCreateDialog(state: Bundle) = dialog

}

object ErrorDialog {

  def fromGooglePlay(activity: Activity, errorCode: Int, requestCode: Int) = {
    val dialog = GooglePlayServicesUtil.getErrorDialog(errorCode, activity, requestCode)
    new ErrorDialog(dialog)
  }
}

/* vim: set et sw=2 sts=2 tw=120: */

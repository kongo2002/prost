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
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.widget.Toast

import Implicits._


object UI {

  /**
   * Create and show a Toast for a specified period of time.
   */
  def toast(ctx: Context, msg: String, duration: Int) {
    Toast.makeText(ctx, msg, duration).show()
  }

  /**
   * Create and show a Toast for a long period of time.
   */
  def longToast(ctx: Context, msg: String) {
    toast(ctx, msg, Toast.LENGTH_LONG)
  }

  /**
   * Create and show a Toast for a short period of time.
   */
  def shortToast(ctx: Context, msg: String) {
    toast(ctx, msg, Toast.LENGTH_SHORT)
  }

  /**
   * Create and show a confirmation dialog and hook into
   * the specified callback function.
   */
  def confirm(ctx: Context, title: String, question: String, ok: (DialogInterface, Int) => Unit) {
    val builder = new AlertDialog.Builder(ctx)

    /* set texts */
    builder.setTitle(title)
    builder.setMessage(question)

    /* add buttons and their callbacks */
    builder.setPositiveButton(R.string.ok, ok)
    builder.setNegativeButton(R.string.cancel, (di: DialogInterface, i: Int) => {})

    /* create and show dialog */
    val dialog = builder.create
    dialog.show
  }

  /**
   * Create and show a dialog with a list selection and hook
   * into the specified callback function.
   */
  def listSelect(ctx: Context, title: Int, items: Int, choice: Int, ok: (DialogInterface, Int) => Unit) {
    val builder = new AlertDialog.Builder(ctx)

    /* set title and items to select from */
    builder.setTitle(title)
    builder.setSingleChoiceItems(items, choice, ok)

    /* create and show dialog */
    val dialog = builder.create
    dialog.show
  }

  /**
   * Create and show a dialog with a list selection and hook
   * into the specified callback function.
   */
  def listSelect(ctx: Context, title: Int, itemsCursor: Cursor, label: String, choice: Int, ok: (DialogInterface, Int) => Unit) {
    val builder = new AlertDialog.Builder(ctx)

    /* set title and items to select from */
    builder.setTitle(title)
    builder.setSingleChoiceItems(itemsCursor, choice, label, ok)

    /* create and show dialog */
    val dialog = builder.create
    dialog.show
  }

  /**
   * Determine whether the given activity supports
   * at least OpenGL ES 2.0
   * @param activity  Activity to check the OpenGL support for
   */
  def supportsOpenGlES2(activity: Activity) = {
    val actManager = activity.getSystemService(Context.ACTIVITY_SERVICE).asInstanceOf[ActivityManager]
    val configInfo = actManager.getDeviceConfigurationInfo

    configInfo.reqGlEsVersion >= 0x20000
  }
}

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

import android.os.AsyncTask
import scala.collection.mutable.ListBuffer

class UpdateTask(tile: Tile, cmd: Command)
  extends AsyncTask[AnyRef, Unit, String] {

  /* sadly I have to use 'AnyRef' as the input type because of the
   * following scala bug: <https://issues.scala-lang.org/browse/SI-1459>
   */

  override def doInBackground(d: AnyRef*) = {
    val drinks = d.head.asInstanceOf[ListBuffer[Drink]]
    val result = cmd.getResult(drinks)
    cmd.format(result)
  }

  override def onPreExecute {
    tile.labelTextView.setText(cmd.unit)
  }

  override def onPostExecute(result: String) {
    tile.textView.setText(result)
  }
}

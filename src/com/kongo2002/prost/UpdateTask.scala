package com.kongo2002.prost

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
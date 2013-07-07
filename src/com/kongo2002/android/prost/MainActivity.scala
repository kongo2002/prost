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

package com.kongo2002.android.prost;

import android.os.Bundle
import android.widget.Toast


/**
 * Main activity of the 'prost' application
 */
class MainActivity extends TypedFragmentActivity
  with Loggable {

  lazy val pager = findView(TR.pager)
  val settingsActivity = 7

  override def onCreate(state: Bundle) {
    super.onCreate(state)

    /* load view */
    setContentView(R.layout.main_activity)

    val adapter = new TabsAdapter(this, pager)

    adapter.addTab(classOf[StatisticsFragment].getName, "Prost", null)
    adapter.addTab(classOf[DrinksFragment].getName, "Drinks", null)
    adapter.addTab(classOf[BarsFragment].getName, "Bars", null)

    logI("onCreate")
  }

  override def onBackPressed() {
    val currentItem = pager.getCurrentItem
    if (currentItem == 0) {
      super.onBackPressed
    } else {
      pager.setCurrentItem(currentItem - 1)
    }
  }


  override def onRestart {
    super.onRestart
    logI("onRestart")
  }

  override def onResume {
    super.onResume
    logI("onResume")
  }

  override def onPause {
    super.onPause
    logI("onPause")
  }

  override def onStop {
    super.onStop
    logI("onStop")
  }

  override def onDestroy {
    super.onDestroy
    logI("onDestroy")
  }

  /**
   * Create and show a Toast for a specified period of time.
   */
  private def toast(duration: Int)(msg: String) {
    Toast.makeText(this, msg, duration).show()
  }

  /**
   * Create and show a Toast for a long period of time.
   */
  private def longToast(msg: String) = toast(Toast.LENGTH_LONG) _

  /**
   * Create and show a Toast for a short period of time.
   */
  private def shortToast(msg: String) = toast(Toast.LENGTH_SHORT) _

}

/* vim: set et sw=2 sts=2: */

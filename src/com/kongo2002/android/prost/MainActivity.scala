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

import com.google.android.gms.maps.SupportMapFragment


/**
 * Main activity of the 'prost' application
 */
class MainActivity extends TypedFragmentActivity
  with Loggable {

  lazy val pager = findView(TR.pager)

  override def onCreate(state: Bundle) {
    super.onCreate(state)

    /* load view */
    setContentView(R.layout.main_activity)

    /* create adapter and initialize fragments */
    val adapter = new TabsAdapter(this, pager)

    adapter.addTab(classOf[StatisticsFragment], R.string.fragment_title_statistics)
    adapter.addTab(classOf[DrinksFragment], R.string.fragment_title_drinks)
    adapter.addTab(classOf[BarsFragment], R.string.fragment_title_bars)
    adapter.addTab(classOf[SupportMapFragment], "Location")

    logI("onCreate")
  }

  override def onBackPressed {
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
}

/* vim: set et sw=2 sts=2: */

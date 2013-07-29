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

import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng


class BarsMapActivity extends TypedFragmentActivity
  with Loggable {

  var map : GoogleMap = null

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)

    /* load maps view */
    setContentView(R.layout.bars_map_activity)
    prepareMap

    logI("onCreate")
  }

  override def onResume {
    super.onResume

    prepareMap
  }

  private def prepareMap {
    if (map == null) {
      /* Try to obtain the map from the SupportMapFragment */
      val manager = getSupportFragmentManager
      val fragment = manager.findFragmentById(R.id.map).asInstanceOf[SupportMapFragment]
      map = fragment.getMap

      /* Check if we were successful in obtaining the map */
      if (map != null) {
        Maps.setUpMap(map)
      }
    }
  }

  override def onDestroy {
    super.onDestroy

    logI("onDestroy")
  }

  override def onPause {
    super.onPause

    logI("onPause")
  }
}

/* vim: set et sw=2 sts=2 tw=120: */

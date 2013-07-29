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
import android.view.LayoutInflater
import android.view.ViewGroup

import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng


class ProstMapFragment extends SupportMapFragment
  with Loggable {

  var map : GoogleMap = null

  override def onCreate(state: Bundle) {
    super.onCreate(state)

    prepareMap
    logI("onCreate")
  }

  override def onCreateView(inf: LayoutInflater, vg: ViewGroup, state: Bundle) = {
    val view = super.onCreateView(inf, vg, state)

    prepareMap
    logI("onCreateView")

    view
  }

  override def onDestroy {
    super.onDestroy

    logI("onDestroy")
  }

  override def onPause {
    super.onPause

    logI("onPause")
  }

  override def onResume {
    super.onResume

    prepareMap
    logI("onResume")
  }

  private def prepareMap {
    /* the map was not initialized yet */
    if (map == null) {
      /* try to acquire map instance */
      map = super.getMap

      /* map setup if we successfully got the map */
      if (map != null) {
        Maps.setUpMap(map)
      }
    }
  }
}

/* vim: set et sw=2 sts=2 tw=120: */

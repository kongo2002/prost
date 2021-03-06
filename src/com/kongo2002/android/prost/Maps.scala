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

import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng


object Maps {

  val INITIAL_POS = new LatLng(51.4926, 7.4518)
  val DEFAULT_ZOOM = 15f

  def setUpMap(map: GoogleMap) {
    /* update initial position */
    val camera = CameraUpdateFactory.newLatLngZoom(INITIAL_POS, DEFAULT_ZOOM)
    map.moveCamera(camera)

    /* adjust some settings */
    val settings = map.getUiSettings

    settings.setZoomControlsEnabled(true)      // zoom        ON
    settings.setScrollGesturesEnabled(true)    // scrolling   ON
    settings.setCompassEnabled(false)          // compass     OFF
    settings.setMyLocationButtonEnabled(false) // my location OFF
    settings.setTiltGesturesEnabled(false)     // tilt        OFF
    settings.setRotateGesturesEnabled(false)   // rotating    OFF

    /* adjust map itself */
    map.setMapType(GoogleMap.MAP_TYPE_NORMAL)
    map.setMyLocationEnabled(false)            // my location OFF
    map.setIndoorEnabled(false)                // indoors     OFF
    map.setTrafficEnabled(false)               // traffic     OFF
  }
}

/* vim: set et sw=2 sts=2 tw=120: */

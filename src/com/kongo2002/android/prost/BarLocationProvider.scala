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

import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

import com.google.android.gms.maps.LocationSource


class BarLocationProvider(ctx: Context) extends LocationSource
  with LocationListener {

  private val MILLISECONDS_PER_SECOND = 1000
  private val INTERVAL_IN_SECONDS     = 30
  private val MIN_DISTANCE_METERS     = 5

  private val INTERVAL = INTERVAL_IN_SECONDS * MILLISECONDS_PER_SECOND

  val manager = ctx.getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]

  var listener : LocationSource.OnLocationChangedListener = null

  override def activate(listener: LocationSource.OnLocationChangedListener) {
    this.listener = listener

    /* build criteria for selecting the best location provider */
    val criteria = new Criteria()
    criteria.setPowerRequirement(Criteria.POWER_LOW)
    criteria.setAccuracy(Criteria.ACCURACY_COARSE)

    /* try to find a provider */
    val providerName = manager.getBestProvider(criteria, true)
    if (providerName != null) {
      val provider = manager.getProvider(providerName)
      if (provider != null) {
        /* request location updates */
        manager.requestLocationUpdates(providerName, INTERVAL, MIN_DISTANCE_METERS, this)
      }
    }
  }

  override def deactivate {
    manager.removeUpdates(this)
  }

  override def onLocationChanged(location: Location) {
    if (listener != null) {
      listener.onLocationChanged(location)
    }
  }

  override def onProviderDisabled(provider: String) { }

  override def onProviderEnabled(provider: String) { }

  override def onStatusChanged(provider: String, status: Int, state: Bundle) { }
}

/* vim: set et sw=2 sts=2 tw=120: */

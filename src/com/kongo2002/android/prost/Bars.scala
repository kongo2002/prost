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

import DrinksDatabase.BarsCursor


/**
 * Case class representing a bar.
 * @param id         ID of the bar
 * @param name       Name of the bar
 * @param longitude  Longitude position of the bar
 * @param latitude   Latitude position of the bar
 */
case class Bar(id: Long, name: String, longitude: Long, latitude: Long)

/**
 * Some basic convenience functions regarding
 * the Bar case class.
 */
object Bar {
  /**
   * Initialize a Bar instance from a specifed Bundle.
   * @param extras  Bundle to extract the data from
   */
  def fromBundle(extras: Bundle) = {
    val id = extras.getLong(DrinksDatabase.KEY_ID)
    val name = extras.getString(BarsCursor.KEY_NAME)
    val long = extras.getLong(BarsCursor.KEY_LONGITUDE)
    val lat = extras.getLong(BarsCursor.KEY_LATITUDE)

    Bar(id, name, long, lat)
  }
}

/* vim: set et sw=2 sts=2: */

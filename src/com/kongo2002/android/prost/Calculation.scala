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

import java.util.Calendar
import java.util.Date

object Calculation {

  def litersPerHour(milliliters: Double, from: Date, to: Date) = {
    if (milliliters > 0) {
      val liters = milliliters / 1000.0
      val diff = timeDiff(from, to)
      if (diff > 0) {
        val hourDiff = diff / (1000.0 * 60.0 * 60.0)
        liters / hourDiff
      } else {
        liters
      }
    } else {
      0.0
    }
  }

  def minDate(a: Date, b: Date) = {
    if (a.after(b)) b else a
  }

  def maxDate(a: Date, b: Date) = {
    if (a.before(b)) b else a
  }

  def timeDiff(from: Date, to: Date) = {
    val calFrom = Calendar.getInstance()
    val calTo = Calendar.getInstance()

    calFrom.setTime(from)
    calTo.setTime(to)

    calTo.getTimeInMillis() - calFrom.getTimeInMillis()
  }
}

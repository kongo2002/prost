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
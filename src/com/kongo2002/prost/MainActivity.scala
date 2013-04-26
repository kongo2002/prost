package com.kongo2002.prost;

import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.os.Bundle

import scala.Option._
import scala.collection.JavaConversions._
import scala.math.Ordering

import java.util.Date
import java.util.Calendar

import com.kongo2002.prost.ImplicitHelpers._

/**
 * Main activity of the 'prost' application
 */
class MainActivity extends TypedActivity
  with Loggable {

  lazy val newBeerBtn = findView(TR.newBeerBtn)
  lazy val totalTv = findView(TR.totalCountTv)
  lazy val perHour = findView(TR.perHourTv)
  lazy val drinksTv = findView(TR.totalDrinksTv)

  val db = new DrinksDatabase.DrinksDatabase(this)
  val order = Ordering.by[Beer, Date](x => x.bought)
  val drinks = new java.util.TreeSet[Beer](order)

  var currentDrinkType = 0

  override def onCreate(state: Bundle) {
    super.onCreate(state)
    
    /* load view */
    setContentView(R.layout.activity_example)
    
    /* restore state */
    restoreState(state)
    
    /* connect listeners */
    newBeerBtn.setOnClickListener { v: View =>
      /* determine whether a valid drink type is selected */
      if (currentDrinkType > 0) {
        for (name <- db.getDrinkTypeName(currentDrinkType)) {
          val beer = new Pint()
          drinks.add(beer)
          update
          longToast("Added " + name)
        }
      }
    }

    update
    
    logI("onCreate")
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
    db.close
    logI("onDestroy")
  }
  
  override def onSaveInstanceState(state: Bundle) {
    super.onSaveInstanceState(state)
    
    /* save state */
    state.putInt("drinkType", currentDrinkType)
    logI("onSaveInstanceState: stored 'drinkType=" + currentDrinkType + "'")
    
    logI("onSaveInstanceState")
  }
  
  override def onRetainNonConfigurationInstance() = {
    logI("onRetainNonConfigurationInstance")
    
    new Integer(getTaskId())
  }
  
  override def onRestoreInstanceState(state: Bundle) {
    super.onRestoreInstanceState(state)
    
    restoreState(state)
    
    logI("onRestoreInstanceState")
  }
  
  private def longToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
  }
  
  private def restoreState(state: Bundle) {
    /* read last drink type from state */
    if (state != null) {
      currentDrinkType = state.getInt("drinkType")
      logI("restored 'drinkType=" + currentDrinkType + "' from state")
    /* or from database */
    } else {
      currentDrinkType = db.getLastDrinkType.getOrElse(0)
      logI("restored 'drinkType=" + currentDrinkType + "' from database")
    }
  }

  private def getLiters = {
    val liters = drinks.foldLeft(0.0)((a, d) => a + d.unitSize)
    liters / 1000.0
  }

  private def timeDiff(from: Date, to: Date) = {
    val calFrom = Calendar.getInstance()
    val calTo = Calendar.getInstance()

    calFrom.setTime(from)
    calTo.setTime(to)

    calTo.getTimeInMillis() - calFrom.getTimeInMillis()
  }

  private def getHourDiff = {
    val size = drinks.size()
    if (size > 0) {
      val now = new Date()
      val diff = timeDiff(drinks.first.bought, now)
      val hourDiff = diff / (1000.0 * 60)

      hourDiff
    } else {
      1.0
    }
  }

  private def update {
    val count = drinks.size()
    val liters = getLiters
    val avg = liters / count

    totalTv.setText(liters.toString())
    perHour.setText((liters / getHourDiff).toString())
    drinksTv.setText(count.toString())
  }
}

/* vim: set et sw=2 sts=2: */

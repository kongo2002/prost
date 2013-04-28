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

  val db = new DrinksDatabase.DrinksDatabase(this)
  val order = Ordering.by[Drink, Date](x => x.bought)
  val drinks = new java.util.TreeSet[Drink](order)
  val commands = new scala.collection.mutable.HashMap[Tile, Command]()

  var currentDrinkType = 0

  override def onCreate(state: Bundle) {
    super.onCreate(state)

    /* load view */
    setContentView(R.layout.main_activity)

    /* restore state */
    restoreState(state)

    /* connect listeners */
    newBeerBtn.setOnClickListener { v: View =>
      /* determine whether a valid drink type is selected */
      if (currentDrinkType > 0) {
        for (dtype <- db.getDrinkType(currentDrinkType)) {
          val drink = dtype.newDrink

          if (addDrink(drink)) {
            update
            longToast("Added " + dtype.name)
          }
        }
      }
    }

    /* load commands */
    loadCommands

    /* load drinks and update the view */
    loadDrinks
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

  private def addDrink(drink: Drink) = {
    if (db.addDrink(drink.drinkType.id)) {
      drinks.add(drink)
      true
    } else {
      false
    }
  }

  private def loadCommands {
    /* TODO: distribute commands to tiles based on configuration */
    commands += ((LeftTile(this), new TotalDrinksCount()))
    commands += ((RightTile(this), new TotalLiters()))
  }

  private def loadDrinks {
    drinks.clear
    db.iterAllDrinks { d =>
      drinks.add(d)
      logI(d.toString())
    }
  }

  private def getDrinkType(state: Bundle) = {
    /* read last drink type from state */
    if (state != null) {
      val drinkType = state.getInt("drinkType")
      logI("restored 'drinkType=" + drinkType + "' from state")
      drinkType
    /* or from database */
    } else {
      val drinkType = db.getLastDrinkType.getOrElse(db.getFirstDrinkType.getOrElse(0))
      logI("restored 'drinkType=" + drinkType + "' from database")
      drinkType
    }
  }

  private def restoreState(state: Bundle) {
    val drinkType = getDrinkType(state)
    if (drinkType != currentDrinkType) {
      currentDrinkType = drinkType

      db.getDrinkTypeName(drinkType) match {
        case Some(name) => newBeerBtn.setText("Add " + name)
        case None => newBeerBtn.setText("Add drink")
      }
    }
  }

  private def getLiters = {
    val liters = drinks.foldLeft(0.0)((a, d) => a + d.drinkType.unit)
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
    commands.foreach { case (t, c) => {
        val result = c.getResult(drinks)

        t.labelTextView.setText(c.unit)
        t.textView.setText(c.format(result))
      }
    }
  }
}

/* vim: set et sw=2 sts=2: */

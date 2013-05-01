package com.kongo2002.prost;

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
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

  object MenuOptions extends Enumeration {
    type MenuOptions = Value
    val Settings, ClearDatabase, About = Value
  }
  import MenuOptions._

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

  override def onCreateOptionsMenu(menu: Menu) = {
    menu.add(Menu.NONE, MenuOptions.Settings.id, Menu.NONE, R.string.settings)
    menu.add(Menu.NONE, MenuOptions.ClearDatabase.id, Menu.NONE, R.string.clear_database)
    menu.add(Menu.NONE, MenuOptions.About.id, Menu.NONE, R.string.about)

    super.onCreateOptionsMenu(menu)
  }

  override def onOptionsItemSelected(item: MenuItem) = {
    val selection = MenuOptions(item.getItemId)

    selection match {
      case MenuOptions.Settings => {
        val intent = new Intent(this, classOf[SettingsActivity])
        startActivity(intent)
        true
      }
      case MenuOptions.ClearDatabase => {
        confirm("Clear database", "Do you really want to clear the database?",
            (_, _) => {
              db.removeAllDrinks
              drinks.clear
              update
            })
        true
      }
      case MenuOptions.About => {
        /* TODO: about dialog */
        true
      }
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

  private def confirm(title: String, question: String, ok: (DialogInterface, Int) => Unit) {
    val builder = new AlertDialog.Builder(this)

    /* set texts */
    builder.setTitle(title)
    builder.setMessage(question)

    /* add buttons and their callbacks */
    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
      def onClick(di: DialogInterface, i: Int) {
        ok(di, i)
      }
    })
    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
      def onClick(di: DialogInterface, i: Int) {
        /* TODO: do nothing */
      }
    })

    /* create and show dialog */
    val dialog = builder.create
    dialog.show
  }

  private def loadCommands {
    /* TODO: distribute commands to tiles based on configuration */
    commands += ((TopLeftTile(this), new TotalBeersCount()))
    commands += ((TopTile(this), new LitersPerHour()))
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

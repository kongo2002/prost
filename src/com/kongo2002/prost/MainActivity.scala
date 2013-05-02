package com.kongo2002.prost;

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
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
  with SharedPreferences.OnSharedPreferenceChangeListener
  with Loggable {

  /**
   *  Enumeration of all valid context menu options
   */
  object MenuOptions extends Enumeration {
    type MenuOptions = Value
    val Settings, ClearDatabase, About = Value
  }
  import MenuOptions._

  lazy val newBeerBtn = findView(TR.newBeerBtn)
  lazy val tiles = Tiles.values.map(t => Tiles.get(t, this))

  val db = new DrinksDatabase.DrinksDatabase(this)
  val order = Ordering.by[Drink, Date](x => x.bought)
  val drinks = new java.util.TreeSet[Drink](order)
  val commands = new scala.collection.mutable.HashMap[Tiles.Tiles, (Tile, Command)]()

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

    /* add long click handlers to every clickable linear layout */
    addClickHandlers

    /* register to settings changes */
    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
    prefs.registerOnSharedPreferenceChangeListener(this)

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

    /* unregister from settings changes */
    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
    prefs.unregisterOnSharedPreferenceChangeListener(this)

    /* close database handle */
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
   * Preference changes event handler
   * @param prefs   Shared preferences
   * @param key     Configuration key that was changed
   */
  override def onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
    if (Tiles.values.exists(t => Tiles.configKey(t).equals(key))) {
      logI("Config key '" + key + "' was changed. Reloading tile commands")

      /* TODO: selective update */
      loadCommands
      update
    }
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

  private def getCommandIndex(cmds: Array[String], pos: Tiles.Tiles) = {
    commands.get(pos) match {
      case Some((t, c)) => {
        val name = c.getClass.getSimpleName
        cmds.indexOf(name)
      }
      case None => 0
    }
  }

  private def updateConfig(tile: Tile, cmd: String) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
    val editor = prefs.edit
    val key = Tiles.configKey(tile.position)

    /* put and commit changes */
    editor.putString(key, cmd)
    editor.apply
  }

  private def addClickHandlers {
    val cmds = getResources.getStringArray(R.array.commands_values)

    val longClickListener = (t: Tile) => {
      val listener = new View.OnLongClickListener() {
        override def onLongClick(v: View) = {
          val before = getCommandIndex(cmds, t.position)

          listSelect(R.string.select_tile_logic, R.array.commands, before, (di, choice) => {
            if (before != choice) {
              if (choice > 0) {
                Commands.get(cmds(choice)) match {
                  case Some(c) => {
                    setCommand(t, c)
                    updateConfig(t, c.getClass.getSimpleName)
                  }
                  case None => {
                    removeCommand(t)
                    updateConfig(t, "Empty")
                  }
                }
              } else {
                removeCommand(t)
                updateConfig(t, "Empty")
              }

              update
            }

            /* dismiss the dialog on the first selection click
             * by default the dialog is closed on a button click only
             */
            di.dismiss
          })

          true
        }
      }
      t.layout.setOnLongClickListener(listener)
    }

    tiles.foreach(longClickListener)
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

  private def listSelect(title: Int, items: Int, choice: Int, ok: (DialogInterface, Int) => Unit) {
    val builder = new AlertDialog.Builder(this)

    /* set title and items to select from */
    builder.setTitle(title)
    builder.setSingleChoiceItems(items, choice, new DialogInterface.OnClickListener() {
      def onClick(di: DialogInterface, i: Int) {
        ok(di, i)
      }
    })

    /* create and show dialog */
    val dialog = builder.create
    dialog.show
  }

  private def loadCommands {
    val prefs = PreferenceManager.getDefaultSharedPreferences(this)

    tiles.foreach(getCommand(prefs))
  }

  private def getCommand(prefs: SharedPreferences)(tile: Tile) = {
    val key = Tiles.configKey(tile.position)
    val setting = prefs.getString(key, "")

    Commands.get(setting) match {
      case Some(cmd) => setCommand(tile, cmd)
      case None => removeCommand(tile)
    }
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

  private def setCommand(tile: Tile, cmd: Command) {
    commands += ((tile.position, (tile, cmd)))
  }

  private def removeCommand(tile: Tile) {
    /* remove command from hash set */
    commands -= tile.position

    /* clear text values */
    tile.labelTextView.setText("")
    tile.textView.setText("")
  }

  private def update {
    /* TODO: async */
    commands.foreach { case (_, (t, c)) => {
        val result = c.getResult(drinks)

        t.labelTextView.setText(c.unit)
        t.textView.setText(c.format(result))
      }
    }
  }
}

/* vim: set et sw=2 sts=2: */

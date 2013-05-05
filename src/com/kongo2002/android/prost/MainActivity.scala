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

package com.kongo2002.android.prost;

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

import scala.collection.JavaConversions._
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

import java.util.Date
import java.util.Calendar

import ImplicitHelpers._


/**
 * Main activity of the 'prost' application
 */
class MainActivity extends TypedActivity
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
  val drinks = new ListBuffer[Drink]
  val commands = new HashMap[Tiles.Tiles, (Tile, Command)]()
  val settingsActivity = 7

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
    addHandlers

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
        startActivityForResult(intent, settingsActivity)
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

  override def onActivityResult(request: Int, result: Int, data: Intent) {
    /* check whether the result is triggered by a settings change */
    if (request == settingsActivity && result == SettingsActivity.RESULT_TILES_CHANGED) {
      val changedData = data.getIntArrayExtra(SettingsActivity.RESULT_DATA_KEY)
      val changedTiles = changedData.map(Tiles.apply)

      val prefs = PreferenceManager.getDefaultSharedPreferences(this)

      /* adjust the commands of all changed tiles */
      changedTiles.foreach { ct =>
        val tile = Tiles.get(ct, this)
        updateCommand(prefs)(tile)
      }

      /* update the view */
      update
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
      drinks += drink
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
    editor.apply()
  }

  private def addHandlers {
    val res = getResources
    val cmds = res.getStringArray(R.array.commands_values)

    val addListeners = (t: Tile) => {
      val clickListener = new View.OnLongClickListener() {
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

      t.layout.setOnLongClickListener(clickListener)
    }

    tiles.foreach(addListeners)
  }

  private def confirm(title: String, question: String, ok: (DialogInterface, Int) => Unit) {
    val builder = new AlertDialog.Builder(this)

    /* set texts */
    builder.setTitle(title)
    builder.setMessage(question)

    /* add buttons and their callbacks */
    builder.setPositiveButton(R.string.ok, ok)
    builder.setNegativeButton(R.string.cancel, (di: DialogInterface, i: Int) => {})

    /* create and show dialog */
    val dialog = builder.create
    dialog.show
  }

  private def listSelect(title: Int, items: Int, choice: Int, ok: (DialogInterface, Int) => Unit) {
    val builder = new AlertDialog.Builder(this)

    /* set title and items to select from */
    builder.setTitle(title)
    builder.setSingleChoiceItems(items, choice, ok)

    /* create and show dialog */
    val dialog = builder.create
    dialog.show
  }

  private def loadCommands {
    val prefs = PreferenceManager.getDefaultSharedPreferences(this)

    tiles.foreach(updateCommand(prefs))
  }

  private def updateCommand(prefs: SharedPreferences)(tile: Tile) = {
    val key = Tiles.configKey(tile.position)
    val setting = prefs.getString(key, "")

    Commands.get(setting) match {
      case Some(cmd) => setCommand(tile, cmd)
      case None => removeCommand(tile)
    }
  }

  private def loadDrinks {
    drinks.clear
    db.iterAllDrinks { d => drinks += d }
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
    commands.foreach { case (_, (t, c)) => {
        val task = new UpdateTask(t, c)
        task.execute(drinks)
      }
    }
  }
}

/* vim: set et sw=2 sts=2: */

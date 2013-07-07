package com.kongo2002.android.prost

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

import ImplicitHelpers._

class StatisticsActivity extends TypedFragment
  with Loggable {

  lazy val newBeerBtn = findView(TR.newBeerBtn)
  lazy val tiles = Tiles.values.map(t => Tiles.get(t, this))

  val db = new DrinksDatabase.DrinksDatabase(this.getActivity)
  val drinks = new ListBuffer[Drink]
  val commands = new HashMap[Tiles.Tiles, (Tile, Command)]()
  val settingsActivity = 7

  var currentDrinkType = 0

  override def onCreate(b: Bundle) {
    /* restore state */
    restoreState(b)

    super.onCreate(b)
  }

  override def onCreateView(inf: LayoutInflater, c: ViewGroup, b: Bundle) = {
    Log.i("prost", "onCreateView: StatisticsActivity")
    val view = inf.inflate(R.layout.statistics_activity, c, false)

    /* connect listeners */
    newBeerBtn.setOnClickListener { v: View =>
      /* determine whether a valid drink type is selected */
      if (currentDrinkType > 0) {
        for (dtype <- db.getDrinkType(currentDrinkType)) {
          val drink = dtype.newDrink

          if (addDrink(drink)) {
            update
            //longToast("Added " + dtype.name)
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

    view
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

  override def onSaveInstanceState(state: Bundle) {
    super.onSaveInstanceState(state)

    /* save state */
    state.putInt("drinkType", currentDrinkType)
    logI("onSaveInstanceState: stored 'drinkType=" + currentDrinkType + "'")

    logI("onSaveInstanceState")
  }

  /*
  override def onRestoreInstanceState(state: Bundle) {
    super.onRestoreInstanceState(state)

    restoreState(state)

    logI("onRestoreInstanceState")
  }
  */

  override def onActivityResult(request: Int, result: Int, data: Intent) {
    /* check whether the result is triggered by a settings change */
    if (request == settingsActivity && result == SettingsActivity.RESULT_TILES_CHANGED) {
      val changedData = data.getIntArrayExtra(SettingsActivity.RESULT_DATA_KEY)
      val changedTiles = changedData.map(Tiles.apply)

      val prefs = PreferenceManager.getDefaultSharedPreferences(activity)

      /* adjust the commands of all changed tiles */
      changedTiles.foreach { ct =>
        val tile = Tiles.get(ct, this)
        updateCommand(prefs)(tile)
      }

      /* update the view */
      update
    }
  }

  private def addDrink(drink: Drink) = {
    if (db.addDrink(drink.drinkType.id)) {
      drinks += drink
      true
    } else {
      false
    }
  }

  private def updateConfig(tile: Tile, cmd: String) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
    val editor = prefs.edit
    val key = Tiles.configKey(tile.position)

    /* put and commit changes */
    editor.putString(key, cmd)
    editor.apply()
  }

  private def listSelect(title: Int, items: Int, choice: Int, ok: (DialogInterface, Int) => Unit) {
    val builder = new AlertDialog.Builder(activity)

    /* set title and items to select from */
    builder.setTitle(title)
    //builder.setSingleChoiceItems(items, choice, ok)

    /* create and show dialog */
    val dialog = builder.create
    dialog.show
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

  private def loadCommands {
    val prefs = PreferenceManager.getDefaultSharedPreferences(activity)

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
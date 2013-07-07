package com.kongo2002.android.prost

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

import ImplicitHelpers._


class StatisticsFragment extends TypedFragment
  with Loggable {

  lazy val tiles = Tiles.values.map(t => Tiles.get(t, StatisticsFragment.this))
  lazy val button = findView(TR.newBeerBtn)
  lazy val db = new DrinksDatabase.DrinksDatabase(StatisticsFragment.this.getActivity)

  val drinks = new ListBuffer[Drink]
  val commands = new HashMap[Tiles.Tiles, (Tile, Command)]()
  val settingsActivity = 7

  var currentDrinkType = 0

  override def onCreateView(inf: LayoutInflater, c: ViewGroup, b: Bundle) = {
    logI("onCreateView")

    val view = inf.inflate(R.layout.statistics_fragment, c, false)

    setHasOptionsMenu(true)

    view
  }

  override def onViewCreated(v: View, b: Bundle) {
    /* restore state */
    restoreState(b)

    /* connect listeners */
    button.setOnClickListener { v: View =>
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

    super.onViewCreated(v, b)
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
  override def onViewStateRestored(state: Bundle) {
    super.onViewStateRestored(state)

    restoreState(state)

    logI("onViewStateRestored")
  }
  */

  override def onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) = {
    inflater.inflate(R.menu.menu, menu)

    super.onCreateOptionsMenu(menu, inflater)
  }

  override def onOptionsItemSelected(item: MenuItem) = {

    item.getItemId match {
      case R.id.menu_settings => {
        val intent = new Intent(activity, classOf[SettingsActivity])
        startActivityForResult(intent, settingsActivity)
        true
      }
      case R.id.menu_clear_database => {
        confirm("Clear database", "Do you really want to clear the database?",
            (_, _) => {
              db.removeAllDrinks
              drinks.clear
              update
            })
        true
      }
      case R.id.menu_about => {
        /* TODO: about dialog */
        true
      }
    }
  }

  override def onDestroy {
    super.onDestroy
    db.close
  }

  private def confirm(title: String, question: String, ok: (DialogInterface, Int) => Unit) {
    val builder = new AlertDialog.Builder(activity)

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

  override def onActivityResult(request: Int, result: Int, data: Intent) {
    /* check whether the result is triggered by a settings change */
    if (request == settingsActivity && result == SettingsActivity.RESULT_TILES_CHANGED) {
      val changedData = data.getIntArrayExtra(SettingsActivity.RESULT_DATA_KEY)
      val changedTiles = changedData.map(Tiles.apply)

      val prefs = PreferenceManager.getDefaultSharedPreferences(activity)

      /* adjust the commands of all changed tiles */
      changedTiles.foreach { ct =>
        val tile = Tiles.get(ct, StatisticsFragment.this)
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
    editor.commit
  }

  private def listSelect(title: Int, items: Int, choice: Int, ok: (DialogInterface, Int) => Unit) {
    val builder = new AlertDialog.Builder(activity)

    /* set title and items to select from */
    builder.setTitle(title)
    builder.setSingleChoiceItems(items, choice, ok)

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

      if (button != null) {
        db.getDrinkTypeName(drinkType) match {
          case Some(name) => button.setText("Add " + name)
          case None => button.setText("Add drink")
        }
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
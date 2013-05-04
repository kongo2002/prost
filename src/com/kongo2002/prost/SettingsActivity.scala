package com.kongo2002.prost

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceActivity

import scala.collection.mutable.HashSet

object SettingsActivity {
  final val RESULT_TILES_CHANGED = 2
  final val RESULT_DATA_KEY = "changedTiles"
}

class SettingsActivity extends PreferenceActivity
  with SharedPreferences.OnSharedPreferenceChangeListener {

  val changedTiles = new HashSet[Tiles.Tiles]

  override def onCreate(state: Bundle) {
    super.onCreate(state)
    addPreferencesFromResource(R.xml.preferences)
  }

  override def onResume() {
    super.onResume

    /* hook into preference changes */
    val prefs = getPreferenceScreen.getSharedPreferences
    Tiles.values.foreach(t => updateSummary(prefs, Tiles.configKey(t)))

    prefs.registerOnSharedPreferenceChangeListener(this)
  }

  override def onPause() {
    super.onPause

    /* remove preference changes callback */
    val prefs = getPreferenceScreen.getSharedPreferences
    prefs.unregisterOnSharedPreferenceChangeListener(this)
  }

  /**
   * Preference changes event handler
   * @param prefs   Shared preferences
   * @param key     Configuration key that was changed
   */
  override def onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
    val foundTile = Tiles.values.find(t => Tiles.configKey(t).equals(key))
    foundTile match {
      case Some(tile) => {
        /* update list preference summary text */
        updateSummary(prefs, key)

        changedTiles += tile
      }
      case None => ()
    }
  }

  override def finish {
    if (changedTiles.size > 0) {
      val intent = new Intent
      val tilesArray = changedTiles.toArray.map(x => x.id)
      intent.putExtra(SettingsActivity.RESULT_DATA_KEY, tilesArray)

      setResult(SettingsActivity.RESULT_TILES_CHANGED, intent)
    } else {
      setResult(Activity.RESULT_OK)
    }

    super.finish
  }

  private def updateSummary(prefs: SharedPreferences, key: String) {
    val value = prefs.getString(key, "")
    val pref = findPreference(key)

    Commands.get(value) match {
      case Some(cmd) => pref.setSummary(cmd.name)
      case _ => pref.setSummary("")
    }
  }
}
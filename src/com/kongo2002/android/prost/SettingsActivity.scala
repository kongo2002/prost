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

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceActivity

import scala.collection.mutable.HashSet

object SettingsActivity {
  final val RESULT_TILES_CHANGED = 2
  final val RESULT_DATA_KEY = "com.kongo2002.android.prost.ChangedTiles"
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
      case None =>
    }
  }

  override def finish {
    if (changedTiles.size > 0) {
      val tilesArray = changedTiles.toArray.map(x => x.id)
      val intent = new Intent
      intent.putExtra(SettingsActivity.RESULT_DATA_KEY, tilesArray)
      changedTiles.clear

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

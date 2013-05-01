package com.kongo2002.prost

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceActivity

class SettingsActivity extends PreferenceActivity
  with SharedPreferences.OnSharedPreferenceChangeListener {

  override def onCreate(state: Bundle) {
    super.onCreate(state)
    addPreferencesFromResource(R.xml.preferences)
  }

  override def onResume() {
    super.onResume

    val prefs = getPreferenceScreen.getSharedPreferences
    Tiles.values.foreach(t => updateSummary(prefs, Tiles.configKey(t)))

    prefs.registerOnSharedPreferenceChangeListener(this)
  }

  override def onPause() {
    super.onPause

    val prefs = getPreferenceScreen.getSharedPreferences
    prefs.unregisterOnSharedPreferenceChangeListener(this)
  }

  /**
   * Preference changes event handler
   * @param prefs   Shared preferences
   * @param key     Configuration key that was changed
   */
  override def onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
    if (Tiles.values.exists(t => Tiles.configKey(t).equals(key))) {
      /* update list preference summary text */
      updateSummary(prefs, key)
    }
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
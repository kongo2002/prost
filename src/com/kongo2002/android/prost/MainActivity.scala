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

import android.app.ActionBar
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
import android.support.v4.view.ViewPager

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
class MainActivity extends TypedFragmentActivity
  with Loggable {

  val db = new DrinksDatabase.DrinksDatabase(this)
  lazy val pager = findView(TR.pager)
  val settingsActivity = 7

  override def onCreate(state: Bundle) {
    super.onCreate(state)

    /* load view */
    setContentView(R.layout.main_activity)

    val adapter = new TabsAdapter(this, pager)

    adapter.addTab(classOf[StatisticsActivity].getName, null)
    adapter.addTab(classOf[BarActivity].getName, null)

    pager.setAdapter(adapter)
    pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener {
      override def onPageSelected(index: Int) {
        logI("selected: " + index)

        // TODO: throws NoSuchMethodError...
        //invalidateOptionsMenu
      }
    })


    logI("onCreate")
  }

  override def onBackPressed() {
    val currentItem = pager.getCurrentItem
    if (currentItem == 0) {
      super.onBackPressed
    } else {
      pager.setCurrentItem(currentItem - 1)
    }
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    val menuInflater = getMenuInflater
    menuInflater.inflate(R.menu.menu, menu)

    super.onCreateOptionsMenu(menu)
  }

  override def onOptionsItemSelected(item: MenuItem) = {

    item.getItemId match {
      case R.id.menu_settings => {
        val intent = new Intent(this, classOf[SettingsActivity])
        startActivityForResult(intent, settingsActivity)
        pager.setCurrentItem(pager.getCurrentItem + 1)
        true
      }
      case R.id.menu_clear_database => {
        confirm("Clear database", "Do you really want to clear the database?",
            (_, _) => {
              db.removeAllDrinks
              //drinks.clear
              //update
            })
        true
      }
      case R.id.menu_about => {
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

    /* close database handle */
    db.close

    logI("onDestroy")
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



}

/* vim: set et sw=2 sts=2: */

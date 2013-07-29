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

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.ViewPager
import android.util.Log

import java.lang.Class

import scala.collection.mutable.ListBuffer


case class TabInfo(name: String, title: String, args: Bundle)

class TabsAdapter(activity: FragmentActivity, pager: ViewPager)
  extends FragmentStatePagerAdapter(activity.getSupportFragmentManager())
  with ViewPager.OnPageChangeListener {

    private val tabs = new ListBuffer[TabInfo]

    pager.setAdapter(this)
    pager.setOnPageChangeListener(this)

    def addTab[T](name: Class[T], titleId: Int) : Unit = addTab(name, activity.getString(titleId))
    def addTab[T](name: Class[T], title: String) : Unit = addTab(name, title, null)
    def addTab[T](name: Class[T], title: String, args: Bundle) {
      val info = TabInfo(name.getName, title, args)

      tabs += info

      notifyDataSetChanged
    }

    override def getCount = tabs.size

    override def getPageTitle(pos: Int) = {
      val info = tabs(pos)
      info.title
    }

    override def getItem(pos: Int) = {
      val info = tabs(pos)
      Fragment.instantiate(activity, info.name, info.args)
    }

    override def onPageScrolled(arg0: Int, arg1: Float, arg2: Int) { }

    override def onPageSelected(pos: Int) {
      pager.setCurrentItem(pos)
    }

    override def onPageScrollStateChanged(pos: Int) { }
}

/* vim: set et sw=2 sts=2: */

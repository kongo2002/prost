package com.kongo2002.android.prost

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.ViewPager
import android.util.Log
import scala.collection.mutable.ListBuffer

import com.actionbarsherlock.app.SherlockFragmentActivity
import com.actionbarsherlock.app.ActionBar
import com.actionbarsherlock.app.ActionBar.Tab

case class TabInfo(name: String, title: String, args: Bundle)

class TabsAdapter(activity: SherlockFragmentActivity, pager: ViewPager)
  extends FragmentStatePagerAdapter(activity.getSupportFragmentManager())
  with ViewPager.OnPageChangeListener {

        private val tabs = new ListBuffer[TabInfo]

        val actionBar = activity.getSupportActionBar

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS)
        actionBar.setDisplayShowHomeEnabled(false)
        actionBar.setDisplayShowTitleEnabled(false)

        pager.setAdapter(this)
        pager.setOnPageChangeListener(this)

        def addTab(name: String, title: String, args: Bundle) {
          val info = TabInfo(name, title, args)

          tabs += info

          val tab = actionBar.newTab()
          tab.setText(title)
          tab.setTabListener(new ActionBar.TabListener() {
            override def onTabSelected(tab: Tab, ft: FragmentTransaction) {
              pager.setCurrentItem(tab.getPosition)
            }

            override def onTabUnselected(tab: Tab, ft: FragmentTransaction) { }

            override def onTabReselected(tab: Tab, ft: FragmentTransaction) { }
          })

          actionBar.addTab(tab)

          notifyDataSetChanged
        }

        override def getCount = tabs.size

        /* this one would be used by the PagerTitleStrip of the ViewPager */
        override def getPageTitle(pos: Int) = {
          val info = tabs(pos)
          info.name
        }

        override def getItem(pos: Int) = {
          val info = tabs(pos)
          Fragment.instantiate(activity, info.name, info.args)
        }

        override def onPageSelected(pos: Int) {
          actionBar.setSelectedNavigationItem(pos)
        }

        override def onPageScrolled(pos: Int, posOff: Float, posOffPix: Int) { }

        override def onPageScrollStateChanged(state: Int) { }

}
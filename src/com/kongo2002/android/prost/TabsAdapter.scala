package com.kongo2002.android.prost

import android.app.ActionBar
import android.app.FragmentTransaction
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager

import scala.collection.mutable.ListBuffer

case class TabInfo(name: String, args: Bundle)

class TabsAdapter(activity: FragmentActivity, pager: ViewPager)
  extends FragmentPagerAdapter(activity.getSupportFragmentManager())
  with ActionBar.TabListener
  with ViewPager.OnPageChangeListener {

        private val bar = activity.getActionBar
        private val tabs = new ListBuffer[TabInfo]

        pager.setAdapter(this)
        pager.setOnPageChangeListener(this)

        def addTab(tab: ActionBar.Tab, name: String, args: Bundle) {
          val info = TabInfo(name, args)
          tab.setTag(info)
          tab.setTabListener(this)
          tabs += info
          bar.addTab(tab)

          notifyDataSetChanged
        }

        override def getCount = tabs.size

        override def getItem(pos: Int) = {
          val info = tabs(pos)
          Fragment.instantiate(activity, info.name, info.args)
        }

        override def onPageScrolled(pos: Int, posOff: Float, posOffPix: Int) { }

        override def onPageSelected(pos: Int) { }

        override def onPageScrollStateChanged(state: Int) { }

        override def onTabSelected(tab: ActionBar.Tab, ft: FragmentTransaction) {
          val tag = tab.getTag
          val index = tabs.indexWhere(t => t == tag)

          if (index >= 0) {
            pager.setCurrentItem(index)
          }
        }

        override def onTabUnselected(tab: ActionBar.Tab, ft: FragmentTransaction) { }

        override def onTabReselected(tab: ActionBar.Tab, ft: FragmentTransaction) { }
}
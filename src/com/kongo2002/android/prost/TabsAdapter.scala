package com.kongo2002.android.prost

import android.app.FragmentTransaction
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log

import scala.collection.mutable.ListBuffer

case class TabInfo(name: String, args: Bundle)

class TabsAdapter(activity: FragmentActivity, pager: ViewPager)
  extends FragmentStatePagerAdapter(activity.getSupportFragmentManager())
  /*with ViewPager.OnPageChangeListener */ {

        private val tabs = new ListBuffer[TabInfo]

        //pager.setAdapter(this)
        //pager.setOnPageChangeListener(this)

        def addTab(name: String, args: Bundle) {
          val info = TabInfo(name, args)

          tabs += info

          notifyDataSetChanged
        }

        override def getCount = tabs.size

        override def getItem(pos: Int) = {
          Log.i("TabsAdapter", "getItem: " + pos)
          val info = tabs(pos)
          Fragment.instantiate(activity, info.name, info.args)
        }

        /*
        override def onPageScrolled(pos: Int, posOff: Float, posOffPix: Int) { }

        override def onPageSelected(pos: Int) { }

        override def onPageScrollStateChanged(state: Int) { }
        */
}
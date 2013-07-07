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

    def addTab[T](name: Class[T], title: String) : Unit = addTab(name, title, null)
    def addTab[T](name: Class[T], title: String, args: Bundle) {
      val info = TabInfo(name.getName, title, args)

      tabs += info

      notifyDataSetChanged
    }

    override def getCount = tabs.size

    /* this one would be used by the PagerTitleStrip of the ViewPager */
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
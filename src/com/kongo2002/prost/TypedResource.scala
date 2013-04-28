package com.kongo2002.prost;

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView

case class TypedResource[T](id : Int)

object TR {
  val newBeerBtn = TypedResource[Button](R.id.new_beer)
  val totalCountTv = TypedResource[TextView](R.id.totalCountTv)
  val perHourTv = TypedResource[TextView](R.id.PerHourTv)
  val totalDrinksTv = TypedResource[TextView](R.id.totalDrinksTv)
}

trait TypedViewHolder {
  def view : View
  def findView[T](tr : TypedResource[T]) = view.findViewById(tr.id).asInstanceOf[T]
}

trait TypedView extends View with TypedViewHolder {
  def view = this
}

trait TypedActivityHolder {
  def activity : Activity
  def findView[T](tr : TypedResource[T]) = activity.findViewById(tr.id).asInstanceOf[T]
}

trait TypedActivity extends Activity with TypedActivityHolder {
  def activity = this
}

trait Loggable {
  val tag = this.getClass().getSimpleName()
  def TAG = tag
  
  def logI(msg: String) {
    if (BuildConfig.DEBUG) Log.i(tag, msg)
  }
}

/* vim: set et sw=2 sts=2: */

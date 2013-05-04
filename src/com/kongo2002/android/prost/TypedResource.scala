package com.kongo2002.android.prost;

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

case class TypedResource[T](id : Int)

object TR {
  val newBeerBtn = TypedResource[Button](R.id.new_beer)
  val topLeftTile = TypedResource[LinearLayout](R.id.tl_layout)
  val topTile = TypedResource[LinearLayout](R.id.t_layout)
  val topRightTile = TypedResource[LinearLayout](R.id.tr_layout)
  val leftTile = TypedResource[LinearLayout](R.id.l_layout)
  val rightTile = TypedResource[LinearLayout](R.id.r_layout)
  val bottomLeftTile = TypedResource[LinearLayout](R.id.bl_layout)
  val bottomTile = TypedResource[LinearLayout](R.id.b_layout)
  val bottomRightTile = TypedResource[LinearLayout](R.id.br_layout)
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

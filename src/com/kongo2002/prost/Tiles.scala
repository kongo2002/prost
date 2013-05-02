package com.kongo2002.prost

import android.widget.LinearLayout
import android.widget.TextView

object Tiles extends Enumeration {
  type Tiles = Value
  val TopLeft, Top, TopRight, Left, Right, BottomLeft, Bottom, BottomRight = Value

  def get(name: Tiles, activity: TypedActivity) : Tile = {
    name match {
      case TopLeft => new TopLeftTile(activity)
      case Top => new TopTile(activity)
      case TopRight => new TopRightTile(activity)
      case Left => new LeftTile(activity)
      case Right => new RightTile(activity)
      case BottomLeft => new BottomLeftTile(activity)
      case Bottom => BottomTile(activity)
      case BottomRight => BottomRightTile(activity)
    }
  }

  def configKey(name: Tiles) = name.toString.toLowerCase + "_command"
}
import Tiles._

abstract class Tile(pos: Tiles, text: Int, label: Int, lay: Int, activity: TypedActivity) {
  def position = pos
  val layout = activity.findView(TypedResource[LinearLayout](lay))
  val textView = activity.findView(TypedResource[TextView](text))
  val labelTextView = activity.findView(TypedResource[TextView](label))
}

case class TopLeftTile(activity: TypedActivity)
  extends Tile(Tiles.TopLeft, R.id.tlTv, R.id.tlLabelTv, R.id.tl_layout, activity)

case class TopTile(activity: TypedActivity)
  extends Tile(Tiles.Top, R.id.tTv, R.id.tLabelTv, R.id.t_layout, activity)

case class TopRightTile(activity: TypedActivity)
  extends Tile(Tiles.TopRight, R.id.trTv, R.id.trLabelTv, R.id.tr_layout, activity)

case class LeftTile(activity: TypedActivity)
  extends Tile(Tiles.Left, R.id.lTv, R.id.lLabelTv, R.id.l_layout, activity)

case class RightTile(activity: TypedActivity)
  extends Tile(Tiles.Right, R.id.rTv, R.id.rLabelTv, R.id.r_layout, activity)

case class BottomLeftTile(activity: TypedActivity)
  extends Tile(Tiles.BottomLeft, R.id.blTv, R.id.blLabelTv, R.id.bl_layout, activity)

case class BottomTile(activity: TypedActivity)
  extends Tile(Tiles.Bottom, R.id.bTv, R.id.bLabelTv, R.id.b_layout, activity)

case class BottomRightTile(activity: TypedActivity)
  extends Tile(Tiles.BottomRight, R.id.brTv, R.id.brLabelTv, R.id.br_layout, activity)
package com.kongo2002.prost

import android.widget.TextView

object Tiles extends Enumeration {
  type Tiles = Value
  val TopLeft, Top, TopRight, Left, Right, BottomLeft, Bottom, BottomRight = Value
}
import Tiles._

abstract class Tile(pos: Tiles, text: Int, label: Int, activity: TypedActivity) {
  val textView = activity.findView(TypedResource[TextView](text))
  val labelTextView = activity.findView(TypedResource[TextView](label))
}

case class TopLeftTile(activity: TypedActivity) extends Tile(Tiles.TopLeft, R.id.tlTv, R.id.tlLabelTv, activity)
case class TopTile(activity: TypedActivity) extends Tile(Tiles.Top, R.id.tTv, R.id.tLabelTv, activity)
case class TopRightTile(activity: TypedActivity) extends Tile(Tiles.TopRight, R.id.trTv, R.id.trLabelTv, activity)

case class LeftTile(activity: TypedActivity) extends Tile(Tiles.Left, R.id.lTv, R.id.lLabelTv, activity)
case class RightTile(activity: TypedActivity) extends Tile(Tiles.Right, R.id.rTv, R.id.rLabelTv, activity)

case class BottomLeftTile(activity: TypedActivity) extends Tile(Tiles.BottomLeft, R.id.blTv, R.id.blLabelTv, activity)
case class BottomTile(activity: TypedActivity) extends Tile(Tiles.Bottom, R.id.bTv, R.id.bLabelTv, activity)
case class BottomRightTile(activity: TypedActivity) extends Tile(Tiles.BottomRight, R.id.brTv, R.id.brLabelTv, activity)
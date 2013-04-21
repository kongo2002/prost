package com.kongo2002.prost

import java.util.Date

abstract class Beer {
  val date = new Date()
  def unitSize : Int
  def name : String
  def inLiters : Double = {
    return unitSize / 1000.0
  }
  def bought = date
}

class Pint extends Beer {
  override def unitSize = 500
  override def name = "Pint"
}
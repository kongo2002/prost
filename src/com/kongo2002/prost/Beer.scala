package com.kongo2002.prost

import java.util.Date

abstract class Beer(date: Date) { 

  def this() = this(new Date())
  
  def unitSize : Int
  def name : String
  def inLiters : Double = unitSize / 1000.0
  def bought = date
}

class Pint(date: Date) extends Beer(date) {
  
  def this() = this(new Date())
  
  override def unitSize = 500
  override def name = "Pint"
}

/* vim: set et sw=2 sts=2: */

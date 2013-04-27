package com.kongo2002.prost

import java.util.Date

object Drinks extends Enumeration {
  type Drinks = Value
  val Beer, Shot, Cocktail = Value
}
import Drinks._

abstract class Drink(d: Drinks, t: DrinkType, date: Date) {
  def this(d: Drinks, t: DrinkType) = this(d, t, new Date())
  def drinkType = t
  def bought = date
}

case class Beer(t: DrinkType, date: Date) extends Drink(Drinks.Beer, t, date)
case class Shot(t: DrinkType, date: Date) extends Drink(Drinks.Shot, t, date)
case class Cocktail(t: DrinkType, date: Date) extends Drink(Drinks.Cocktail, t, date)

case class DrinkType(id: Long, name: String, unit: Long, baseType: Drinks) {
  def newDrink(date: Date) : Drink = {
    baseType match {
      case Drinks.Beer => Beer(this, date)
      case Drinks.Shot => Shot(this, date)
      case Drinks.Cocktail => Cocktail(this, date)
    }
  }
  def newDrink : Drink = newDrink(new Date())
}

/* vim: set et sw=2 sts=2: */

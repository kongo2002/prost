/*
 * Copyright 2013 Gregor Uhlenheuer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kongo2002.android.prost

import android.os.Bundle

import java.util.Date

import DrinksDatabase.BarsCursor
import DrinksDatabase.DrinkTypesCursor


/**
 * Enumeration of available basic drink types
 */
object Drinks extends Enumeration {
  type Drinks = Value
  val Beer, Shot, Cocktail = Value
}
import Drinks._

/**
 * Class holding the information of one bought drink.
 * @param d     Base drink type
 * @param t     Drink type
 * @param date  Date the drink was bought
 */
abstract class Drink(d: Drinks, t: DrinkType, date: Date) {
  def this(d: Drinks, t: DrinkType) = this(d, t, new Date())
  def drinkType = t
  def bought = date
}

/**
 * Specialized class to represent a 'Beer'
 */
case class Beer(t: DrinkType, date: Date) extends Drink(Drinks.Beer, t, date)

/**
 * Specialized class to represent a 'Shot'
 */
case class Shot(t: DrinkType, date: Date) extends Drink(Drinks.Shot, t, date)

/**
 * Specialized class to represent a 'Cocktail'
 */
case class Cocktail(t: DrinkType, date: Date) extends Drink(Drinks.Cocktail, t, date)

/**
 * Base trait that describes a database related entity with
 * a unique ID.
 * @param id  ID of the item
 */
trait Id {
  def id: Long
}

/**
 * Case class to represent a specific drink type
 * @param id        ID of the drink type
 * @param name      Name of the drink type
 * @param unit      Unit of the drink type (in milliliters)
 * @param baseType  Base type the drink is based on
 * @param price     Price of the drink type (in cents)
 * @param bar       ID of the bar the drink type belongs to
 */
case class DrinkType(override val id: Long, name: String, unit: Int, baseType: Drinks, price: Int, bar: Long)
  extends Id {
  def newDrink(date: Date) : Drink = {
    baseType match {
      case Drinks.Beer => Beer(this, date)
      case Drinks.Shot => Shot(this, date)
      case Drinks.Cocktail => Cocktail(this, date)
    }
  }
  def newDrink : Drink = newDrink(new Date())
}

/**
 * Some basic convenience functions regarding
 * the DrinkType class.
 */
object DrinkType {
  /**
   * Initialize a DrinkType instance from a specified Bundle.
   * @param extras  Bundle to extract the data from
   */
  def fromBundle(extras: Bundle) = {
    val id = extras.getLong(DrinksDatabase.KEY_ID)
    val name = extras.getString(DrinkTypesCursor.KEY_NAME)
    val drink = extras.getInt(DrinkTypesCursor.KEY_TYPE)
    val unit = extras.getInt(DrinkTypesCursor.KEY_UNIT)
    val price = extras.getInt(DrinkTypesCursor.KEY_PRICE)
    val bar = extras.getLong(DrinkTypesCursor.KEY_BAR)

    DrinkType(id, name, unit, Drinks(drink), price, bar)
  }
}

/**
 * Case class representing a bar.
 * @param id         ID of the bar
 * @param name       Name of the bar
 * @param longitude  Longitude position of the bar
 * @param latitude   Latitude position of the bar
 */
case class Bar(override val id: Long, name: String, longitude: Long, latitude: Long)
  extends Id

/**
 * Some basic convenience functions regarding
 * the Bar case class.
 */
object Bar {
  /**
   * Initialize a Bar instance from a specifed Bundle.
   * @param extras  Bundle to extract the data from
   */
  def fromBundle(extras: Bundle) = {
    val id = extras.getLong(DrinksDatabase.KEY_ID)
    val name = extras.getString(BarsCursor.KEY_NAME)
    val long = extras.getLong(BarsCursor.KEY_LONGITUDE)
    val lat = extras.getLong(BarsCursor.KEY_LATITUDE)

    Bar(id, name, long, lat)
  }
}

/* vim: set et sw=2 sts=2: */

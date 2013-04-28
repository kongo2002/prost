package com.kongo2002.prost

import java.util.Calendar
import java.util.Date

abstract class Command {
  def name : String
  def description : String
  def unit : String
  def getResult(drinks: Iterable[Drink]) : Double
  def isInt = false
  def format = {
    if (isInt) {
      formatInt _
    } else {
      formatDouble _
    }
  }

  private def formatDouble(value: Double) = value.formatted("%1.2f")
  private def formatInt(value: Double) = value.formatted("%1.0f")
}

abstract class IntCommand extends Command {
  override def isInt = true
}

class TotalDrinksCount extends IntCommand {
  override def name = "Total drink count"
  override def description = "Calculate the total count of drinks"
  override def unit = "total drinks"
  override def getResult(drinks: Iterable[Drink]) : Double = drinks.size
}

class TotalBeersCount extends IntCommand {
  override def name = "Total beers count"
  override def description = "Calculate the total count of beers"
  override def unit = "total beers"
  override def getResult(drinks: Iterable[Drink]) : Double = {
    drinks.count(d => d match {
      case _: Beer => true
      case _ => false
    })
  }
}

class TotalShotsCount extends IntCommand {
  override def name = "Total shots count"
  override def description = "Calculate the total count of shots"
  override def unit = "total shots"
  override def getResult(drinks: Iterable[Drink]) : Double = {
    drinks.count(d => d match {
      case _: Shot => true
      case _ => false
    })
  }
}

class TotalCocktailsCount extends IntCommand {
  override def name = "Total cocktails count"
  override def description = "Calculate the total count of cocktails"
  override def unit = "total cocktails"
  override def getResult(drinks: Iterable[Drink]) : Double = {
    drinks.count(d => d match {
      case _: Cocktail => true
      case _ => false
    })
  }
}

class TotalLiters extends Command {
  override def name = "Total liters"
  override def description = "Calculate the total liters of drinks"
  override def unit = "total liters"
  override def getResult(drinks: Iterable[Drink]) = {
    drinks.foldLeft(0.0)((a, d) => a + d.drinkType.unit) / 1000.0
  }
}

class LitersPerHour extends Command {
  override def name = "Liters per hour"
  override def description = "Calculate the average liters per hour"
  override def unit = "liters/h"
  override def getResult(drinks: Iterable[Drink]) = {
    val (min, max, milliliters) = drinks.foldLeft(new Date(), new Date(1), 0.0) { case ((min, max, liters), c) => {
        val literSum = liters + c.drinkType.unit
        (minDate(min, c.bought), maxDate(max, c.bought), literSum)
      }
    }

    if (milliliters > 0) {
      val liters = milliliters / 1000.0
      val diff = timeDiff(min, max)
      if (diff > 0) {
        val hourDiff = diff / (1000.0 * 60.0 * 60.0)
        liters / hourDiff
      } else {
        liters
      }
    } else {
      0.0
    }
  }

  private def minDate(a: Date, b: Date) = {
    if (a.after(b)) b else a
  }

  private def maxDate(a: Date, b: Date) = {
    if (a.before(b)) b else a
  }

  private def timeDiff(from: Date, to: Date) = {
    val calFrom = Calendar.getInstance()
    val calTo = Calendar.getInstance()

    calFrom.setTime(from)
    calTo.setTime(to)

    calTo.getTimeInMillis() - calFrom.getTimeInMillis()
  }
}
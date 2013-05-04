package com.kongo2002.android.prost

import java.util.Calendar
import java.util.Date

object Commands {
  def get(name: String) = {
    name.toLowerCase match {
      case "totaldrinkscount" => Some(new TotalDrinksCount)
      case "totalbeerscount" => Some(new TotalBeersCount)
      case "totalshotscount" => Some(new TotalShotsCount)
      case "totalcocktailscount" => Some(new TotalCocktailsCount)
      case "totalliters" => Some(new TotalLiters)
      case "litersperhour" => Some(new LitersPerHour)
      case "beerlitersperhour" => Some(new BeerLitersPerHour)
      case "shotlitersperhour" => Some(new ShotLitersPerHour)
      case "cocktaillitersperhour" => Some(new CocktailLitersPerHour)
      case _ => None
    }
  }
}

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
        (Calculation.minDate(min, c.bought), Calculation.maxDate(max, c.bought), literSum)
      }
    }

    Calculation.litersPerHour(milliliters, min, max)
  }
}

class BeerLitersPerHour extends Command {
  override def name = "Beer liters per hour"
  override def description = "Calculate the average liters of beer per hour"
  override def unit = "beer liters/h"
  override def getResult(drinks: Iterable[Drink]) = {
    val (min, max, milliliters) = drinks.foldLeft(new Date(), new Date(1), 0.0) { case ((min, max, liters), c) => {
        val literSum = c match {
          case _: Beer => liters + c.drinkType.unit
          case _ => liters
        }

        (Calculation.minDate(min, c.bought), Calculation.maxDate(max, c.bought), literSum)
      }
    }

    Calculation.litersPerHour(milliliters, min, max)
  }
}

class ShotLitersPerHour extends Command {
  override def name = "Shot liters per hour"
  override def description = "Calculate the average liters of shots per hour"
  override def unit = "shots liters/h"
  override def getResult(drinks: Iterable[Drink]) = {
    val (min, max, milliliters) = drinks.foldLeft(new Date(), new Date(1), 0.0) { case ((min, max, liters), c) => {
        val literSum = c match {
          case _: Shot => liters + c.drinkType.unit
          case _ => liters
        }

        (Calculation.minDate(min, c.bought), Calculation.maxDate(max, c.bought), literSum)
      }
    }

    Calculation.litersPerHour(milliliters, min, max)
  }
}

class CocktailLitersPerHour extends Command {
  override def name = "Cocktail liters per hour"
  override def description = "Calculate the average liters of coktails per hour"
  override def unit = "cocktails liters/h"
  override def getResult(drinks: Iterable[Drink]) = {
    val (min, max, milliliters) = drinks.foldLeft(new Date(), new Date(1), 0.0) { case ((min, max, liters), c) => {
        val literSum = c match {
          case _: Cocktail => liters + c.drinkType.unit
          case _ => liters
        }

        (Calculation.minDate(min, c.bought), Calculation.maxDate(max, c.bought), literSum)
      }
    }

    Calculation.litersPerHour(milliliters, min, max)
  }
}
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

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteCursor
import android.database.sqlite.SQLiteCursorDriver
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQuery
import android.database.SQLException
import android.util.Log

import java.util.Date
import scala.collection.mutable.Map


/**
 * Main database class that manages all database related
 * operations.
 */
object DrinksDatabase {

  final val DBVERSION = 1
  final val DBNAME = "drinks.db"
  final val LOG_TAG = "Prost.DrinksDatabase"

  final val KEY_ID = "_id"

  private final def DRINKS_TABLE = "CREATE TABLE drinks (" +
    "_id INTEGER PRIMARY KEY AUTOINCREMENT, drink INTEGER, date TIMESTAMP NOT NULL DEFAULT current_timestamp);"

  private final def DRINK_TYPES_TABLE = "CREATE TABLE drink_types (" +
    "_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, unit INTEGER, type INTEGER, price INTEGER, bar INTEGER);"

  private final def BARS_TABLE = "CREATE TABLE bars (" +
    "_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, long INTEGER, lat INTEGER);"

  private final def DEFAULT_PINT = DrinkTypesCursor.QUERY_INSERT.format("Pint", 500, 0, 350, 0)
  private final def DEFAULT_KORN = DrinkTypesCursor.QUERY_INSERT.format("Korn", 200, 1, 150, 0)

  /**
   * Iterate a given SQLite cursor using the specified function.
   * @param cursor  Cursor to iterate over
   * @param func    Function to use for each cursor row
   */
  def iter[T <: SQLiteCursor](cursor: T, func: T => Unit) {
    try {
      while (cursor.moveToNext) {
        func(cursor)
      }
    } finally {
      cursor.close
    }
  }

  /**
   * Inner database class that wraps a sqlite connection helper.
   * @param context   Context the database operates on
   */
  class DrinksDatabase(context: Context)
    extends SQLiteOpenHelper(context, DBNAME, null, DBVERSION) {

    /**
     * Called when the database has to be created.
     * @param db  sqlite database to operate on
     */
    override def onCreate(db: SQLiteDatabase) {
      Log.w(LOG_TAG, "Creating database '" + DBNAME + "'")

      db.beginTransaction()

      try {
        executeSql(db,
          DRINKS_TABLE,
          DRINK_TYPES_TABLE,
          BARS_TABLE,
          DEFAULT_PINT,
          DEFAULT_KORN)

        db.setTransactionSuccessful()
      } catch {
        case sex: SQLException => Log.e("Error creating database '" + DBNAME + "'", sex.toString())
      } finally {
          db.endTransaction()
      }
    }

    /**
     * Called when the database has to be upgraded. Right now all tables are
     * dropped and re-created.
     * @param db    sqlite database to operate on
     * @param oldV  old version number
     * @param newV  new version number
     */
    override def onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
      Log.w(LOG_TAG, "Upgrading database '" + DBNAME + "' from version " + oldV +
        " to " + newV + ", which will currently destroy all existing data!")

      db.beginTransaction()

      try {
        executeSql(db,
          drop("drinks"),
          drop("drink_types"),
          drop("bars"),
          DRINKS_TABLE,
          DRINK_TYPES_TABLE,
          BARS_TABLE,
          DEFAULT_PINT,
          DEFAULT_KORN)

        db.setTransactionSuccessful()
      } catch {
        case sex: SQLException => Log.e("Error updating database '" + DBNAME + "'", sex.toString())
      } finally {
          db.endTransaction()
      }
    }

    /**
     * Add a new drink to the 'drinks' database table.
     * @param drinkId   ID of the respective entry in the 'drink_types' table
     */
    def addDrink(drinkId: Long) = {
      val drinkName = getDrinkTypeName(drinkId)
      if (drinkName.isDefined) {
        val map = new ContentValues()

        /* we don't have to insert the 'date' value in here
         * because the column 'date' defaults to 'current_timestamp'
         * and will therefore be inserted by sqlite
         */
        map.put("drink", java.lang.Long.valueOf(drinkId))

        try {
          getWritableDatabase().insert("drinks", null, map)
          true
        } catch {
          case sex: SQLException => {
            Log.e("Error writing new drink", sex.toString())
            false
          }
        }
      } else {
        false
      }
    }

    /**
     * Get the total count of all drinks in the database.
     */
    def getDrinksCount = {
      scalarInt("SELECT count(_id) FROM drinks;")
    }

    /**
     * Get the type of the last drink that was recorded.
     */
    def getLastDrinkType = {
      scalarInt("SELECT drink FROM drinks ORDER BY date DESC LIMIT 1;")
    }

    /**
     * Get the first available drink type in the database.
     */
    def getFirstDrinkType = {
      scalarInt("SELECT _id FROM drink_types ORDER BY _id ASC LIMIT 1;")
    }

    /**
     * Get the name of the drink type with the specified ID
     * @param id  ID of the 'DrinkType' to get the name for
     */
    def getDrinkTypeName(id: Long) = {
      scalarString("SELECT name FROM drink_types WHERE _id=%d;".format(id))
    }

    /**
     * Get the name of the specified bar.
     * @param id  ID of the bar to get the name of
     */
    def getBarName(id: Long) = {
      scalarString("SELECT name FROM bars WHERE _id=%d;".format(id))
    }

    /**
     * Get a DrinkType with the specified ID.
     * @param id  ID of the 'DrinkType' to retrieve
     */
    def getDrinkType(id: Long) = {
      val query = DrinkTypesCursor.QUERY_ONE.format(id)
      val db = getReadableDatabase
      val cursor = db.rawQueryWithFactory(new DrinkTypesCursor.Factory(), query, null, null).asInstanceOf[DrinkTypesCursor]

      try {
        if (cursor.moveToFirst)
          Some(cursor.get)
        else
          None
      } finally {
        cursor.close
      }
    }

    /**
     * Get a DrinkTypesCursor for a specified bar.
     * @param bar  Bar ID to get all drink types for
     */
    def getDrinkTypesForBar(bar: Long) = {
      val query = DrinkTypesCursor.QUERY_FOR_BAR.format(bar)
      val db = getReadableDatabase
      db.rawQueryWithFactory(new DrinkTypesCursor.Factory(), query, null, null).asInstanceOf[DrinkTypesCursor]
    }

    /**
     * Get the number of usages of the given drink type
     * @param id  ID of the drink type to get the usages of
     */
    def getDrinkTypeUsage(id: Long) = {
      scalarInt(DrinksCursor.QUERY_DRINK_TYPE_COUNT.format(id))
    }

    /**
     * Get a cursor accessing all drinks stored in the database.
     */
    def getAllDrinksCursor = {
      val db = getReadableDatabase()
      db.rawQueryWithFactory(new DrinksCursor.Factory(), DrinksCursor.QUERY, null, null).asInstanceOf[DrinksCursor]
    }

    /**
     * Get all drinks stored in the database.
     */
    def getAllDrinks = {
      val drinks = new scala.collection.mutable.ArrayBuffer[Drink]()
      iterAllDrinks(d => drinks.append(d))
      drinks
    }

    /**
     * Get a cursor accessing all drink types stored in the database.
     */
    def getAllDrinkTypesCursor = {
      val db = getReadableDatabase
      db.rawQueryWithFactory(new DrinkTypesCursor.Factory(), DrinkTypesCursor.QUERY_ALL, null, null).asInstanceOf[DrinkTypesCursor]
    }

    /**
     * Get a cursor accessing all bars stored in the database.
     */
    def getAllBarsCursor = {
      val db = getReadableDatabase
      db.rawQueryWithFactory(new BarsCursor.Factory(), BarsCursor.QUERY_ALL, null, null).asInstanceOf[BarsCursor]
    }

    /**
     * Get a map of all bars available.
     * @param noBar  Name of the 'no bar'
     */
    def getAllBarMap(noBar: String) = {
      val bars = Map(0L -> new Bar(0L, noBar, 0, 0))

      DrinksDatabase.iter(getAllBarsCursor, { (c: BarsCursor) =>
        val bar = c.get
        bars(bar.id) = bar
      })

      bars
    }

    /**
     * Iterate all drinks using the specified function.
     * @param func   Function that should be executed for every single 'Drink'
     */
    def iterAllDrinks(func: Drink => Unit) {
      val cursor = getAllDrinksCursor
      try {
        while (cursor.moveToNext) {
          func(cursor.get)
        }
      } finally {
        cursor.close
      }
    }

    /**
     * Reduce all drinks to a single value using the specified function.
     * @param func      function that is invoked for every drink
     * @param initial   initial value
     */
    def reduceAllDrinks[T](func: (Drink, T) => T, initialValue: T) = {
      val cursor = getAllDrinksCursor
      try {
        var acc = initialValue
        while (cursor.moveToNext) {
          acc = func(cursor.get, acc)
        }
        acc
      } finally {
        cursor.close
      }
    }

    /**
     * Remove the specified bar.
     * @param id  ID of the bar to remove
     */
    def removeBar(id: Long) {
      val db = getWritableDatabase

      val remove = "DELETE FROM bars WHERE _id=%d;".format(id)
      db.execSQL(remove)
    }

    /**
     * Add a new Bar instance into the database.
     * @param bar  Bar instance to insert
     */
    def addBar(bar: Bar) {
      val db = getWritableDatabase
      val insert = BarsCursor.insertQuery(bar.name, bar.longitude, bar.latitude)

      db.execSQL(insert)
    }

    /**
     * Update the given Bar instance.
     * @param bar  Bar instance to update
     */
    def updateBar(bar: Bar) {
      if (bar.id > 0) {
        val db = getWritableDatabase
        val update = BarsCursor.updateQuery(bar.id, bar.name, bar.longitude, bar.latitude)

        db.execSQL(update)
      }
    }

    /**
     * Remove the specified drink type and all drinks of that type.
     * @param id  ID of the drink type to remove
     */
    def removeDrinkType(id: Long) {
      val db = getWritableDatabase

      val removeDrinks = "DELETE FROM drinks WHERE drink=%d;".format(id)
      val removeType = "DELETE FROM drink_types WHERE _id=%d;".format(id)

      executeSql(db, removeDrinks, removeType)
    }

    /**
     * Update the given drink type.
     * @param dt  Drink type to update
     */
    def updateDrinkType(dt: DrinkType) {
      if (dt.id > 0) {
        val db = getWritableDatabase
        val update = DrinkTypesCursor.updateQuery(dt.id, dt.name, dt.unit, dt.baseType.id, dt.price, dt.bar)

        db.execSQL(update)
      }
    }

    /**
     * Add a new drink type to the database.
     * @param dt  Drink type to insert
     */
    def addDrinkType(dt: DrinkType) = {
      val db = getWritableDatabase
      val insert = DrinkTypesCursor.insertQuery(dt.name, dt.unit, dt.baseType.id, dt.price, dt.bar)

      db.execSQL(insert)
    }

    /**
     * Remove all drinks from the database.
     */
    def removeAllDrinks {
      val db = getReadableDatabase
      db.execSQL("DELETE FROM drinks;")
    }

    private def drop(table: String) = "DROP TABLE IF EXISTS %s;".format(table)

    private def executeSql(db: SQLiteDatabase, sql: String*) = {
      sql.foreach(s => db.execSQL(s))
    }

    private def query[T](sql: String, args: String*)(func: Cursor => T) = {
      val cursor = getReadableDatabase().rawQuery(sql, args.toArray[String])
      try {
        if (cursor.moveToFirst)
          Some(func(cursor))
        else
          None
      }
      catch {
        case ex: SQLException => {
          Log.e(LOG_TAG, ex.getMessage(), ex)
          None
        }
      }
      finally {
        cursor.close
      }
    }

    private def scalarInt(sql: String, args: String*) = {
      query(sql, args: _*)(c => c.getInt(0))
    }

    private def scalarString(sql: String, args: String*) = {
      query(sql, args: _*)(c => c.getString(0))
    }
  }

  object DrinksCursor {
    final def QUERY = "SELECT drinks._id AS id,name,unit,type,drink_types._id AS tid," +
      "(strftime('%s', date) * 1000) AS date,price,bar " +
      "FROM drinks INNER JOIN drink_types ON " +
      "drinks.drink=drink_types._id ORDER BY id DESC;"

    final def QUERY_DRINK_TYPE_COUNT = "SELECT COUNT(_id) FROM drinks WHERE drink=%d;"

    /**
     * Factory class to be used for 'rawQueryWithFactory()'
     */
    class Factory extends SQLiteDatabase.CursorFactory {
      override def newCursor(db: SQLiteDatabase, driver: SQLiteCursorDriver, table: String, query: SQLiteQuery) = {
        new DrinksCursor(db, driver, table, query)
      }
    }
  }

  /**
   * Convenience cursor wrapper class to centralize all access to
   * the 'drinks' table.
   */
  class DrinksCursor(db: SQLiteDatabase, driver: SQLiteCursorDriver, table: String, query: SQLiteQuery)
    extends SQLiteCursor(db, driver, table, query) {

    def getDrinkId = getLong(getColumnIndexOrThrow("id"))
    def getDrinkName = getString(getColumnIndexOrThrow("name"))
    def getDrinkUnit = getInt(getColumnIndexOrThrow("unit"))
    def getDrinkTypeId = getLong(getColumnIndexOrThrow("tid"))
    def getDrinkBaseType = Drinks(getInt(getColumnIndexOrThrow("type")))
    def getDrinkDate = new Date(getLong(getColumnIndexOrThrow("date")))
    def getDrinkPrice = getInt(getColumnIndexOrThrow("price"))
    def getDrinkBar = getLong(getColumnIndexOrThrow("bar"))

    def getDrinkType = DrinkType(getDrinkTypeId, getDrinkName, getDrinkUnit, getDrinkBaseType, getDrinkPrice, getDrinkBar)
    def get = getDrinkType.newDrink(getDrinkDate)
  }

  /**
   * Cursor to access drink type information.
   */
  object DrinkTypesCursor {
    final val KEY_NAME = "name"
    final val KEY_UNIT = "unit"
    final val KEY_TYPE = "type"
    final val KEY_PRICE = "price"
    final val KEY_BAR = "bar"

    final val QUERY_ALL = "SELECT _id,name,unit,type,price,bar FROM drink_types ORDER BY name ASC;"
    final val QUERY_FOR_BAR = "SELECT _id,name,unit,type,price,bar FROM drink_types WHERE bar=%d ORDER BY name ASC;"
    final val QUERY_ONE = "SELECT _id,name,unit,type,price,bar FROM drink_types WHERE _id=%d;"
    final val QUERY_UPDATE = "UPDATE drink_types SET name='%s',unit=%d,type=%d,price=%d,bar=%d WHERE _id=%d;"
    final val QUERY_INSERT = "INSERT INTO drink_types (name,unit,type,price,bar) VALUES('%s',%d,%d,%d,%d);"

    /**
     * Build an drink type 'update' query string.
     * @param id        drink type ID
     * @param name      drink type name
     * @param unit      drink type unit (in milliliters)
     * @param baseType  base type (beer, shot, cocktail)
     * @param price     price (in cents)
     * @param bar       ID of the bar the drink type belongs to
     */
    def updateQuery(id: Long, name: String, unit: Int, baseType: Int, price: Int, bar: Long) = {
      QUERY_UPDATE.format(name, unit, baseType, price, bar, id)
    }

    /**
     * Build an drink type 'insert' query string.
     * @param name      drink type name
     * @param unit      drink type unit (in milliliters)
     * @param baseType  base type (beer, shot, cocktail)
     * @param price     price (in cents)
     * @param bar       ID of the bar the drink type belongs to
     */
    def insertQuery(name: String, unit: Int, baseType: Int, price: Int, bar: Long) = {
      QUERY_INSERT.format(name, unit, baseType, price, bar)
    }

    /**
     * Factory class to be used for 'rawQueryWithFactory()'
     */
    class Factory extends SQLiteDatabase.CursorFactory {
      override def newCursor(db: SQLiteDatabase, driver: SQLiteCursorDriver, table: String, query: SQLiteQuery) = {
        new DrinkTypesCursor(db, driver, table, query)
      }
    }
  }

  /**
   * Convenience cursor wrapper class to centralize all access to the
   * 'drink_types' table.
   */
  class DrinkTypesCursor(db: SQLiteDatabase, driver: SQLiteCursorDriver, table: String, query: SQLiteQuery)
    extends SQLiteCursor(db, driver, table, query) {

    def getTypeId = getLong(getColumnIndexOrThrow("_id"))
    def getTypeName = getString(getColumnIndexOrThrow("name"))
    def getTypeUnit = getInt(getColumnIndexOrThrow("unit"))
    def getType = Drinks(getInt(getColumnIndexOrThrow("type")))
    def getPrice = getInt(getColumnIndexOrThrow("price"))
    def getDrinkTypeBar = getLong(getColumnIndexOrThrow("bar"))

    def get = DrinkType(getTypeId, getTypeName, getTypeUnit, getType, getPrice, getDrinkTypeBar)
  }

  /**
   * Cursor to access bars information.
   */
  object BarsCursor {
    final val KEY_NAME = "name"
    final val KEY_LONGITUDE = "long"
    final val KEY_LATITUDE = "lat"

    final val QUERY_ALL = "SELECT _id,name,long,lat FROM bars ORDER BY name ASC;"
    final val QUERY_ONE = "SELECT _id,name,long,lat FROM bars WHERE _id=%d;"
    final val QUERY_UPDATE = "UPDATE bars SET name='%s',long=%d,lat=%d WHERE _id=%d;"
    final val QUERY_INSERT = "INSERT INTO bars (name,long,lat) VALUES ('%s',%d,%d);"

    /**
     * Build a bars 'update' query.
     * @param id         ID of the bar to update
     * @param name       Name of the bar
     * @param longitude  Longitude of location
     * @parma latitude   Latitude of location
     */
    def updateQuery(id: Long, name: String, longitude: Long, latitude: Long) = {
      QUERY_UPDATE.format(name, longitude, latitude, id)
    }

    /**
     * Build a bars 'insert' query.
     * @param name       Name of the bar
     * @param longitude  Longitude of location
     * @parma latitude   Latitude of location
     */
    def insertQuery(name: String, longitude: Long, latitude: Long) = {
      QUERY_INSERT.format(name, longitude, latitude)
    }

    /**
     * Factory class to be used for 'rawQueryWithFactory()'
     */
    class Factory extends SQLiteDatabase.CursorFactory {
      override def newCursor(db: SQLiteDatabase, driver: SQLiteCursorDriver, table: String, query: SQLiteQuery) = {
        new BarsCursor(db, driver, table, query)
      }
    }
  }

  /**
   * Convenience cursor wrapper class to centralize all access to
   * the 'drinks' table.
   */
  class BarsCursor(db: SQLiteDatabase, driver: SQLiteCursorDriver, table: String, query: SQLiteQuery)
    extends SQLiteCursor(db, driver, table, query) {

    def getBarId = getLong(getColumnIndexOrThrow("_id"))
    def getBarName = getString(getColumnIndexOrThrow("name"))
    def getBarLongitude = getLong(getColumnIndexOrThrow("long"))
    def getBarLatitude = getLong(getColumnIndexOrThrow("lat"))

    def get = Bar(getBarId, getBarName, getBarLongitude, getBarLatitude)
  }
}

/* vim: set et sw=2 sts=2: */

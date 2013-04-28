package com.kongo2002.prost

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

/**
 * Main database class that manages all database related
 * operations.
 */
object DrinksDatabase {

  private final def DBVERSION = 1
  private final def DBNAME = "drinks.db"

  private final def DRINKS_TABLE = "CREATE TABLE drinks (" +
    "_id INTEGER PRIMARY KEY AUTOINCREMENT, drink INTEGER, date TIMESTAMP NOT NULL DEFAULT current_timestamp);"

  private final def DRINK_TYPES_TABLE = "CREATE TABLE drink_types (" +
    "_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, unit INTEGER, type INTEGER);"

  private final def DEFAULT_TYPES = "INSERT INTO drink_types " +
    "(name,unit,type) VALUES ('Pint', 500, 1);"

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
      Log.w(Prost.LOG_TAG, "Creating database '" + DBNAME + "'")

      db.beginTransaction()

      try {
        executeSql(db, DRINKS_TABLE, DRINK_TYPES_TABLE, DEFAULT_TYPES)
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
      Log.w(Prost.LOG_TAG, "Upgrading database '" + DBNAME + "' from version " + oldV +
        " to " + newV + ", which will currently destroy all existing data!")

      db.beginTransaction()

      try {
        executeSql(db, drop("drinks"), drop("drink_types"), DRINKS_TABLE, DRINK_TYPES_TABLE, DEFAULT_TYPES)
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
      scalarString("SELECT name FROM drink_types WHERE _id=" + id + ";")
    }
    
    /**
     * Get a DrinkType with the specified ID.
     * @param id  ID of the 'DrinkType' to retrieve
     */
    def getDrinkType(id: Long) = {
      val query = DrinkTypesCursor.QUERY_ONE + id + ";"
      val db = getReadableDatabase()
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

    private def drop(table: String) = "DROP TABLE IF EXISTS " + table + ";"

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
          Log.e(Prost.LOG_TAG, ex.getMessage(), ex)
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
      "(strftime('%s', date) * 1000) AS date " +
      "FROM drinks INNER JOIN drink_types ON " +
      "drinks.drink=drink_types._id ORDER BY id DESC;"
      
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
    def getDrinkUnit = getLong(getColumnIndexOrThrow("unit"))
    def getDrinkTypeId = getLong(getColumnIndexOrThrow("tid"))
    def getDrinkBaseType = Drinks(getInt(getColumnIndexOrThrow("type")))
    def getDrinkDate = new Date(getLong(getColumnIndexOrThrow("date")))
    
    def getDrinkType = DrinkType(getDrinkTypeId, getDrinkName, getDrinkUnit, getDrinkBaseType)
    def get = getDrinkType.newDrink(getDrinkDate)
  }
  
  object DrinkTypesCursor {
    final def QUERY_ALL = "SELECT _id,name,unit,type FROM drink_types ORDER BY name ASC;"
    final def QUERY_ONE = "SELECT _id,name,unit,type FROM drink_types WHERE _id="
      
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
    
    def get = DrinkType(getTypeId, getTypeName, getTypeUnit, getType)
  }
}

/* vim: set et sw=2 sts=2: */

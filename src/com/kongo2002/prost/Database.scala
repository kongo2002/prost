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
import java.lang.Long
import java.util.Date

/**
 * Main database class that manages all database related
 * operations.
 */
object DrinksDatabase {

  private final def DBVERSION = 1
  private final def DBNAME = "drinks"

  private final def DRINKS_TABLE = "CREATE TABLE drinks (" +
    "_id INTEGER PRIMARY KEY AUTOINCREMENT, drink INTEGER, date INTEGER);"

  private final def DRINK_TYPES_TABLE = "CREATE TABLE drink_types (" +
    "_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, unit INTEGER, type INTEGER);"

  private final def DEFAULT_TYPES = "INSERT INTO drink_types " +
    "(name,unit) VALUES ('Pint', 500, 1);"

  /**
   * Inner database class that wraps a sqlite connection helper.
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
    def addDrink(drinkId: Long) {
      val map = new ContentValues()
      val date = new Date()

      map.put("drink", Long.valueOf(drinkId))
      map.put("date", Long.valueOf(date.getSeconds()))

      try {
        getWritableDatabase().insert("drinks", null, map)
      } catch {
        case sex: SQLException => Log.e("Error writing new drink", sex.toString())
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
     * Get the name of the drink type with the specified ID
     */
    def getDrinkTypeName(id: Int) = {
      scalarString("SELECT name FROM drink_types WHERE _id=" + id + ";")
    }
    
    /**
     * Get a DrinkType with the specified ID.
     */
    def getDrinkType(id: Int) = {
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

  /**
   * Convenience cursor wrapper class to centralize all access to
   * the 'drinks' table.
   */
  class DrinksCursor(db: SQLiteDatabase, driver: SQLiteCursorDriver, table: String, query: SQLiteQuery)
    extends SQLiteCursor(db, driver, table, query) {

    final def QUERY = "SELECT _id,name,unit,date FROM drinks,drink_types " +
      "WHERE drinks.drink=drink_types._id ORDER BY date DESC"

    def getDrinkId = getLong(getColumnIndexOrThrow("drinks._id"))
    def getDrinkName = getString(getColumnIndexOrThrow("drink_types.name"))
    def getDrinkUnit = getLong(getColumnIndexOrThrow("drink_types.unit"))
    def getDrinkDate = getLong(getColumnIndexOrThrow("drink.date"))

    /**
     * Factory class to be used for 'rawQueryWithFactory()'
     */
    object Factory extends SQLiteDatabase.CursorFactory {
      override def newCursor(db: SQLiteDatabase, driver: SQLiteCursorDriver, table: String, query: SQLiteQuery) = {
        new DrinksCursor(db, driver, table, query)
      }
    }
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

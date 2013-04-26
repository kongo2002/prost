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
    "_id INTEGER PRIMARY KEY AUTOINCREMENT, drink INTEGER, date INTEGER)"

  private final def DRINK_TYPES_TABLE = "CREATE TABLE drink_types (" +
    "_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, unit INTEGER)"

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
        executeSql(db, List(DRINKS_TABLE, DRINK_TYPES_TABLE))
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
        executeSql(db, List(drop("drinks"), drop("drink_types"), DRINKS_TABLE, DRINK_TYPES_TABLE))
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
      try {
        var c = getReadableDatabase().rawQuery(
            "SELECT count(_id) FROM drinks", null)
        if (c.getCount() < 1)
          0
        c.moveToFirst()
        c.getInt(0)
      }
      catch {
        case ex: SQLException => {}
      }
    }
    
    /**
     * Get the type of the last drink that was recorded.
     */
    def getLastDrinkType = {
      try {
        var c = getReadableDatabase().rawQuery(
            "SELECT drink FROM drinks ORDER BY date DESC LIMIT 1", null)
        if (c.getCount() < 1)
          0
        c.moveToFirst()
        c.getInt(0)
      }
      catch {
        case ex: SQLException => {}
      }
    }

    private def drop(table: String) = "DROP TABLE IF EXISTS " + table

    private def executeSql(db: SQLiteDatabase, sql: Iterable[String]) = {
      sql.foreach(s => db.execSQL(s))
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

  /**
   * Convenience cursor wrapper class to centralize all access to the
   * 'drink_types' table.
   */
  class DrinkTypesCursor(db: SQLiteDatabase, driver: SQLiteCursorDriver, table: String, query: SQLiteQuery)
    extends SQLiteCursor(db, driver, table, query) {

    final def QUERY = "SELECT _id,name,unit FROM drink_types ORDER BY name ASC"

    def getTypeId = getLong(getColumnIndexOrThrow("_id"))
    def getTypeName = getString(getColumnIndexOrThrow("name"))
    def getTypeUnit = getLong(getColumnIndexOrThrow("unit"))

    /**
     * Factory class to be used for 'rawQueryWithFactory()'
     */
    object Factory extends SQLiteDatabase.CursorFactory {
      override def newCursor(db: SQLiteDatabase, driver: SQLiteCursorDriver, table: String, query: SQLiteQuery) = {
        new DrinkTypesCursor(db, driver, table, query)
      }
    }
  }
}

/* vim: set et sw=2 sts=2: */

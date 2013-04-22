package com.kongo2002.prost

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.SQLException
import android.util.Log

object DrinksDatabase {

  private final def DBVERSION = 1
  private final def DBNAME = "drinks"

  private final def DRINKS_TABLE = "CREATE TABLE drinks (" +
    "_id INTEGER PRIMARY KEY AUTOINCREMENT, drink INTEGER, date INTEGER)"

  private final def DRINK_TYPES_TABLE = "CREATE TABLE drink_types (" +
    "_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, unit INTEGER)"

  class DrinksDatabase(context: Context)
    extends SQLiteOpenHelper(context, DBNAME, null, DBVERSION) {

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

    private def drop(table: String) = "DROP TABLE IF EXISTS " + table

    private def executeSql(db: SQLiteDatabase, sql: Iterable[String]) = {
      sql.foreach(s => db.execSQL(s))
    }
  }
}

/* vim: set et sw=2 sts=2: */

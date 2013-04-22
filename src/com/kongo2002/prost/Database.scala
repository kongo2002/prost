package com.kongo2002.prost

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.SQLException
import android.util.Log

object DrinksDatabase {

  private final def DBVERSION = 1
  private final def DBNAME = "drinks"

  class DrinksOpenHelper(context: Context) extends ProstOpenHelper(context, DBNAME, DBVERSION) {

    final def CREATE_TABLE = "CREATE TABLE " + DBNAME + " (" +
      "_id INTEGER PRIMARY KEY AUTOINCREMENT, drink INTEGER, date INTEGER)"

  }
}

object DrinkTypesDatabase {

  private final def DBVERSION = 1
  private final def DBNAME = "drink_types"

  class DrinkTypesDatabase(context: Context) extends ProstOpenHelper(context, DBNAME, DBVERSION) {

   final def CREATE_TABLE = "CREATE TABLE " + DBNAME + " (" +
     "_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, unit INTEGER)"

  }
}

abstract class ProstOpenHelper(context: Context, db: String, version: Int)
  extends SQLiteOpenHelper(context, db, null, version) {

  final def DBNAME = db
  final def DBVERSION = version

  protected final def DROP_TABLE = "DROP TABLE IF EXISTS " + DBNAME

  def CREATE_TABLE : String

  override def onCreate(db: SQLiteDatabase) {
    Log.w(Prost.LOG_TAG, "Creating database '" + DBNAME + "'")

    db.beginTransaction()

    try {
      db.execSQL(CREATE_TABLE)
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
      db.execSQL(DROP_TABLE)
      db.execSQL(CREATE_TABLE)
      db.setTransactionSuccessful()
    } catch {
      case sex: SQLException => Log.e("Error updating database '" + DBNAME + "'", sex.toString())
    } finally {
        db.endTransaction()
    }
  }
}

/* vim: set et sw=2 sts=2: */

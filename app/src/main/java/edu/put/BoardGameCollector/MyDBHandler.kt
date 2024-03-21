package edu.put.BoardGameCollector

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class MyDBHandler(context: Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int): SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
    companion object {
        private val DATABASE_VERSION=1
        private val DATABASE_NAME="database.db"
        val TABLE_GAMES="games"
        val COLUMN_ID="id"
        val COLUMN_TITLE="title"
        val TABLE_EXPANSIONS="expansions"
        val COLUMN_YEAR="year"
        val COLUMN_POS="position"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_GAMES_TABLE = ("CREATE TABLE $TABLE_GAMES($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_TITLE TEXT, $COLUMN_YEAR INTEGER, $COLUMN_POS INTEGER)")
        db.execSQL(CREATE_GAMES_TABLE)
        val CREATE_EXPANSIONS_TABLE = ("CREATE TABLE $TABLE_EXPANSIONS($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_TITLE TEXT, $COLUMN_YEAR INTEGER, $COLUMN_POS INTEGER)")
        db.execSQL(CREATE_EXPANSIONS_TABLE)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPANSIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GAMES")
        onCreate(db)
    }
    fun reset() {
        val db = this.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPANSIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GAMES")
        onCreate(db)
    }
    fun addGame(game: Game){
        val values = ContentValues()
        values.put(COLUMN_ID, game.id)
        values.put(COLUMN_TITLE, game.title)
        values.put(COLUMN_YEAR, game.year)
        values.put(COLUMN_POS, game.pos)
        val db = this.writableDatabase
        if(db.rawQuery("SELECT $COLUMN_ID FROM $TABLE_GAMES WHERE $COLUMN_ID = ${game.id} ", null).count == 0)
            db.insert(TABLE_GAMES,null, values)
        else
            db.update(TABLE_GAMES, values, "$COLUMN_ID = ${game.id}", null)
        db.close()
    }
    fun addExpansion(expansion: Expansion){
        val values = ContentValues()
        values.put(COLUMN_ID, expansion.id)
        values.put(COLUMN_TITLE, expansion.title)
        values.put(COLUMN_YEAR, expansion.year)
        values.put(COLUMN_POS, expansion.pos)
        val db = this.writableDatabase
        if(db.rawQuery("SELECT $COLUMN_ID FROM $TABLE_EXPANSIONS WHERE $COLUMN_ID = ${expansion.id} ", null).count == 0)
            db.insert(TABLE_EXPANSIONS,null, values)
        else
            db.update(TABLE_EXPANSIONS, values, "$COLUMN_ID = ${expansion.id}", null)
        db.close()
    }
    fun deleteGames(list: List<Int>): List<Int>{
        val db=this.writableDatabase
        val deletedList: MutableList<Int> = ArrayList()
        val cursor = db.rawQuery(
            "SELECT $COLUMN_ID FROM $TABLE_GAMES WHERE $COLUMN_ID NOT IN ( ? ) ",
            arrayOf(list.joinToString())
        )
        if (cursor.count == 0) return deletedList.toList()
        cursor.moveToFirst()
        do {
            deletedList.add(cursor.getInt(0))
        } while (cursor.moveToNext())
        cursor.close()

        db.delete(TABLE_GAMES, " $COLUMN_ID  IN ( ? ) ", arrayOf(deletedList.joinToString()))
        db.close()
        Log.d("sd","list: $deletedList")
        return deletedList.toList()
    }
    fun deleteExpansions(list: List<Int>): List<Int>{
        val db=this.writableDatabase
        val deletedList: MutableList<Int> = ArrayList()
        val cursor = db.rawQuery(
            "SELECT $COLUMN_ID FROM $TABLE_EXPANSIONS WHERE $COLUMN_ID NOT IN ( ? )",
            arrayOf(list.toString())
        )
        if (cursor.count == 0) return deletedList.toList()
        cursor.moveToFirst()
        do {
            deletedList.add(cursor.getInt(0))
        } while (cursor.moveToNext())
        cursor.close()

        db.delete(TABLE_GAMES, "$COLUMN_ID IN ( ? ) ", arrayOf(deletedList.toString()))
        db.close()
        return deletedList.toList()
    }
    fun cursorGames(order: String, desc: Boolean = false): Cursor {
        val db = this.writableDatabase
        val orderString: String = when (order) {
            "pos" -> COLUMN_POS
            "title" -> COLUMN_TITLE
            "year" -> COLUMN_YEAR
            "id" -> COLUMN_ID
            else -> COLUMN_POS
        }
        val descString = when (desc) {
            false -> " "
            true -> " DESC "
        }
        return db.rawQuery(
            "SELECT $COLUMN_ID as _id, $COLUMN_TITLE, $COLUMN_POS, $COLUMN_YEAR FROM $TABLE_GAMES ORDER BY $orderString $descString ",
            null
        )
    }
    fun cursorExpansions(order: String, desc: Boolean = false): Cursor {
        val db = this.writableDatabase
        val orderString: String = when (order) {
            "title" -> COLUMN_TITLE
            "year" -> COLUMN_YEAR
            "id" -> COLUMN_ID
            else -> COLUMN_POS
        }
        val descString = when (desc) {
            false -> " "
            true -> " DESC "
        }
        return db.rawQuery(
            "SELECT $COLUMN_ID as _id, $COLUMN_TITLE, $COLUMN_YEAR FROM $TABLE_EXPANSIONS ORDER BY $orderString $descString ",
            null
        )
    }
    fun countGames(): Int{
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT COUNT($COLUMN_ID) FROM $TABLE_GAMES",null)
        cursor.moveToFirst()
        val numGames = cursor.getInt(0)
        cursor.close()
        db.close()
        return numGames
    }
    fun countExpansions(): Int{
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT COUNT($COLUMN_ID) FROM $TABLE_EXPANSIONS",null)
        cursor.moveToFirst()
        val numExpansions = cursor.getInt(0)
        cursor.close()
        db.close()
        return numExpansions
    }
    fun gameDetails(id: Int): Game{
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_TITLE, $COLUMN_YEAR, $COLUMN_POS FROM $TABLE_GAMES WHERE $COLUMN_ID = $id",null)
        cursor.moveToFirst()
        val title = cursor.getString(0)
        val year = cursor.getInt(1)
        val pos = cursor.getInt(2)
        cursor.close()
        db.close()
        return Game(id, title, year, pos)
    }
    fun expansionDetails(id: Int): Expansion{
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_TITLE, $COLUMN_YEAR, $COLUMN_POS FROM $TABLE_EXPANSIONS WHERE $COLUMN_ID = $id",null)
        cursor.moveToFirst()
        val title = cursor.getString(0)
        val year = cursor.getInt(1)
        val pos = cursor.getInt(2)
        cursor.close()
        db.close()
        return Expansion(id, title, year, pos)
    }
}
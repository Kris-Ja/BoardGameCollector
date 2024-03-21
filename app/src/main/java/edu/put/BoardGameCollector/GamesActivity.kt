package edu.put.BoardGameCollector

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File


class GamesActivity : AppCompatActivity() {
    var order: String = "pos"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games)
        val dbHandler = MyDBHandler(this, null, null, 1)
        val cursor = dbHandler.cursorGames(order)
        val adapter = MyCursorAdapter(this, cursor)
        val listView = findViewById<ListView>(R.id.listView)
        listView.addHeaderView(headerView(adapter))
        listView.adapter = adapter
    }
    private fun headerView(adapter: MyCursorAdapter): View {
        val listView = findViewById<ListView>(R.id.listView)
        val row = LayoutInflater.from(this).inflate(R.layout.games_row, listView, false)
        val title: TextView = row.findViewById(R.id.titleView)
        title.text = getString(R.string.column_title)
        val pos: TextView = row.findViewById(R.id.posView)
        pos.text = getString(R.string.column_pos)
        val year: TextView = row.findViewById(R.id.yearView)
        year.text = getString(R.string.column_year)
        title.gravity=Gravity.CENTER_HORIZONTAL
        pos.gravity=Gravity.CENTER_HORIZONTAL
        year.gravity=Gravity.CENTER_HORIZONTAL
        year.setOnClickListener(){
            val dbHandler = MyDBHandler(this, null, null, 1)
            if(order != "year"){
                order = "year"
                adapter.changeCursor(dbHandler.cursorGames("year"))
            }else{
                order = ""
                adapter.changeCursor(dbHandler.cursorGames("year", true))
            }
        }
        title.setOnClickListener(){
            val dbHandler = MyDBHandler(this, null, null, 1)
            if(order != "title"){
                order = "title"
                adapter.changeCursor(dbHandler.cursorGames("title"))
            }else{
                order = ""
                adapter.changeCursor(dbHandler.cursorGames("title", true))
            }
        }
        pos.setOnClickListener(){
            val dbHandler = MyDBHandler(this, null, null, 1)
            if(order != "pos"){
                order = "pos"
                adapter.changeCursor(dbHandler.cursorGames("pos"))
            }else{
                order = ""
                adapter.changeCursor(dbHandler.cursorGames("pos", true))
            }
        }
        return row
    }
    class MyCursorAdapter(context: Context?, cursor: Cursor?) :
        CursorAdapter(context, cursor, 0) {
        override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
            return LayoutInflater.from(context).inflate(R.layout.games_row, parent, false)
        }
        override fun bindView(view: View, context: Context?, cursor: Cursor) {
            val titleView = view.findViewById<TextView>(R.id.titleView)
            val posView = view.findViewById<TextView>(R.id.posView)
            val yearView = view.findViewById<TextView>(R.id.yearView)
            val imageView = view.findViewById<ImageView>(R.id.imageView)
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
            val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
            val pos = cursor.getInt(cursor.getColumnIndexOrThrow("position"))
            val year = cursor.getInt(cursor.getColumnIndexOrThrow("year"))
            titleView.text = title
            if(pos == 0) posView.text = "-"
            else posView.text = pos.toString()
            yearView.text = year.toString()
            val bitmap = getCapturedImage("${context?.filesDir}/thumbnails/$id.jpg")
            if(bitmap != null)imageView.setImageBitmap(bitmap)
            view.setOnClickListener(){
                val i = Intent(context, GameDetailsActivity::class.java)
                i.putExtra("id", id)
                context?.startActivity(i)
            }
        }
        private fun getCapturedImage(filename: String): Bitmap? {
            val file = File(filename)
            if(file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if(bitmap != null)
                    return BitmapScaler.scaleToFitWidth(bitmap, 250)
            }
            return null
        }
    }
}
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


class ExpansionsActivity : AppCompatActivity() {
    var order: String = "title"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expansions)
        val dbHandler = MyDBHandler(this, null, null, 1)
        val cursor = dbHandler.cursorExpansions(order)
        val adapter = MyCursorAdapter(this, cursor)
        val listView = findViewById<ListView>(R.id.listView)
        listView.addHeaderView(headerView(adapter))
        listView.adapter = adapter
    }
    private fun headerView(adapter: MyCursorAdapter): View {
        val listView = findViewById<ListView>(R.id.listView)
        val row = LayoutInflater.from(this).inflate(R.layout.expansions_row, listView, false)
        val title: TextView = row.findViewById(R.id.titleView)
        title.text = getString(R.string.column_title)
        val year: TextView = row.findViewById(R.id.yearView)
        year.text = getString(R.string.column_year)
        title.gravity=Gravity.CENTER_HORIZONTAL
        year.gravity=Gravity.CENTER_HORIZONTAL
        year.setOnClickListener(){
            val dbHandler = MyDBHandler(this, null, null, 1)
            if(order != "year"){
                order = "year"
                adapter.changeCursor(dbHandler.cursorExpansions("year"))
            }else{
                order = ""
                adapter.changeCursor(dbHandler.cursorExpansions("year", true))
            }
        }
        title.setOnClickListener(){
            val dbHandler = MyDBHandler(this, null, null, 1)
            if(order != "title"){
                order = "title"
                adapter.changeCursor(dbHandler.cursorExpansions("title"))
            }else{
                order = ""
                adapter.changeCursor(dbHandler.cursorExpansions("title", true))
            }
        }
        return row
    }
    class MyCursorAdapter(context: Context?, cursor: Cursor?) :
        CursorAdapter(context, cursor, 0) {
        override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
            return LayoutInflater.from(context).inflate(R.layout.expansions_row, parent, false)
        }
        override fun bindView(view: View, context: Context?, cursor: Cursor) {
            val titleView = view.findViewById<TextView>(R.id.titleView)
            val yearView = view.findViewById<TextView>(R.id.yearView)
            val imageView = view.findViewById<ImageView>(R.id.imageView)
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
            val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
            val year = cursor.getInt(cursor.getColumnIndexOrThrow("year"))
            titleView.text = title
            yearView.text = year.toString()
            val bitmap = getCapturedImage("${context?.filesDir}/thumbnails/$id.jpg")
            if(bitmap != null)imageView.setImageBitmap(bitmap)
            view.setOnClickListener(){
                val i = Intent(context, ExpansionDetailsActivity::class.java)
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
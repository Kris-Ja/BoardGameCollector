package edu.put.BoardGameCollector

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText

class FirstTimeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_time)
    }
    fun buttonClick(v: View) {
        finish()
    }
    override fun finish() {
        val data = Intent()
        data.putExtra("username", findViewById<EditText>(R.id.usernameEdit).text.toString())
        setResult(Activity.RESULT_OK, data)
        super.finish()
    }
}
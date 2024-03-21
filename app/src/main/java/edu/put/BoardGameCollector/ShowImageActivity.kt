package edu.put.BoardGameCollector

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import java.io.File

class ShowImageActivity : AppCompatActivity() {
    private var path: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_image)
        val extras = intent.extras ?: return
        path = extras.getString("path") ?: return
        val absolutePath = extras.getString("absolute_path") ?: return
        val image = findViewById<ImageView>(R.id.showImageView)
        val viewTreeObserver: ViewTreeObserver = image.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    image.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val bitmap = getCapturedImage(absolutePath, image.height)
                    image.setImageBitmap(bitmap)
                }
            })
        }
    }
    private fun getCapturedImage(absolutePath: String, height: Int): Bitmap? {
        val bitmap = BitmapFactory.decodeFile(absolutePath)
        if(bitmap != null){
            return BitmapScaler.scaleToFitHeight(bitmap, height)
        }
        return null
    }
    fun deleteClick (v: View){
        if(path != null) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(getString(R.string.confirm_delete))
                .setPositiveButton(getString(R.string.yes)) { dialog, id ->
                    val file = File(path)
                    if (file.exists()) file.delete()
                    finish()
                }
                .setNegativeButton(getString(R.string.no)) { dialog, id ->
                    dialog.dismiss()
                }
            val dialog = builder.create()
            dialog.show()
        }
    }
}
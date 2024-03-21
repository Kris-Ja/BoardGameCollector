package edu.put.BoardGameCollector

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream


class ExpansionDetailsActivity : AppCompatActivity() {
    var id: Int = 0
    private var mGetContent = registerForActivityResult(GetContent()){ result ->
        if(result != null){

            var file = File("$filesDir/$id/${System.currentTimeMillis()}.jpg")
            if(!file.exists()) {
                file.createNewFile()
                contentResolver.openInputStream(result)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)
                val image: ImageView = ImageView(this)
                val width = resources.displayMetrics.widthPixels
                val height = linearLayout.height
                if(width <= 0 || height <= 0) {
                    Log.d("error", "width or height <= 0")
                }else {
                    val bitmap = getCapturedImage(file, width, height)
                    image.setImageBitmap(bitmap)
                    image.setOnClickListener(){
                        val i = Intent(this, ShowImageActivity::class.java)
                        i.putExtra("absolute_path", file.absolutePath)
                        i.putExtra("path", file.path)
                        startActivity(i)
                    }
                    linearLayout.addView(image)
                }
            }
        }
    }
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()){result ->
        if(result == true) populate()
    }
    override fun onResume(){
        super.onResume()
        populate()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expansion_details)
        val extras = intent.extras ?: return
        id = extras.getInt("id")
        val dbHandler = MyDBHandler(this, null, null, 1)
        val expansion = dbHandler.expansionDetails(id)
        findViewById<TextView>(R.id.titleText).text = expansion.title
        val pos = if(expansion.pos == 0) "-"
        else expansion.pos.toString()
        findViewById<TextView>(R.id.posText).text = "${getString(R.string.ranking)}\n$pos"
        findViewById<TextView>(R.id.yearText).text = "${getString(R.string.year)}\n${expansion.year.toString()}"
        findViewById<TextView>(R.id.idText).text = "${getString(R.string.id)}\n$id"
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)
        val viewTreeObserver: ViewTreeObserver = linearLayout.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    linearLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    populate()
                }
            })
        }
    }
    fun takePictureClick(v: View){
        val dir = File(applicationContext.filesDir, getString(R.string.images_dir))
        dir.mkdir()
        File(dir, "$id").mkdir()
        val file = File(dir, "$id/${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(applicationContext, getString(R.string.authorities), file)
        resultLauncher.launch(uri)
    }
    fun galleryClick(v: View){
        mGetContent.launch("image/*")
    }
    private fun populate(){
        val files = File("$filesDir/$id/").listFiles() ?: return
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)
        linearLayout.removeAllViews()
        for(file in files){
            val image: ImageView = ImageView(this)
            val width = resources.displayMetrics.widthPixels
            val height = linearLayout.height
            if(width <= 0 || height <= 0) {
                Log.d("error", "width or height <= 0")
                continue
            }
            val bitmap = getCapturedImage(file, width, height)
            image.setImageBitmap(bitmap)
            image.setOnClickListener(){
                val i = Intent(this, ShowImageActivity::class.java)
                i.putExtra("absolute_path", file.absolutePath)
                i.putExtra("path", file.path)
                startActivity(i)
            }
            linearLayout.addView(image)
        }
    }
    private fun getCapturedImage(file: File, width: Int, height: Int): Bitmap? {
        if(file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if(bitmap != null)
                return BitmapScaler.scaleToFit(bitmap, width, height)
        }
        return null
    }
}
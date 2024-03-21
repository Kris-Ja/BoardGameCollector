package edu.put.BoardGameCollector

import android.graphics.Bitmap

object BitmapScaler{
    fun scaleToFitWidth(b: Bitmap, width: Int): Bitmap {
        val factor = width / b.width.toFloat()
        return Bitmap.createScaledBitmap(b, width, (b.height * factor).toInt(), true)
    }
    fun scaleToFitHeight(b: Bitmap, height: Int): Bitmap {
        val factor = height / b.height.toFloat()
        return Bitmap.createScaledBitmap(b, (b.width * factor).toInt(), height, true)
    }
    fun scaleToFit(b: Bitmap, width: Int, height: Int): Bitmap {
        val factor = height / b.height.toFloat()
        if((b.width * factor).toInt()>width) return scaleToFitWidth(b, width)
        return scaleToFitHeight(b, height)
    }
}
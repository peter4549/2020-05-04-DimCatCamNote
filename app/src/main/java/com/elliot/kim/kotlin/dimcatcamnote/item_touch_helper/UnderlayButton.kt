package com.elliot.kim.kotlin.dimcatcamnote.item_touch_helper

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.elliot.kim.kotlin.dimcatcamnote.activities.MainActivity

class UnderlayButton (private val context: Context,
                      val id: MainActivity.Companion.UnderlayButtonIds,
                      var text: String,
                      private val textSize: Int,
                      var imageResourceId: Int, private val color: Int,
                      private val listener: UnderlayButtonClickListener) {

    private var position = 0
    private var clickRegion: RectF? = null
    private val resources: Resources = context.resources

    fun onClick(x: Float, y: Float): Boolean {
        if(clickRegion != null && clickRegion!!.contains(x, y)) {
            listener.onClick(position)

            return true
        }

        return false
    }

    fun onDraw(c: Canvas, rectF: RectF, position: Int) {
        val paint = Paint()
        paint.color = color
        c.drawRect(rectF, paint)

        // text
        paint.color = Color.WHITE
        paint.textSize = textSize.toFloat()

        val rect = Rect()
        val height = rectF.height()
        val width = rectF.width()

        paint.textAlign = Paint.Align.LEFT
        paint.getTextBounds(text, 0, text.length, rect)

        if(imageResourceId == 0) {
            val x = width / 2F - rect.width() / 2F - rect.left
            val y = height / 2F - rect.height() /2F - rect.bottom
            c.drawText(text, rectF.left + x, rectF.top + y, paint)
        } else {
            val drawable = ContextCompat.getDrawable(context, imageResourceId)
            val bitmap = drawableToBitmap(drawable)
            c.drawBitmap(bitmap, rectF.left + 32,
                (rectF.top + rectF.bottom) / 2, paint)
        }

        clickRegion = rectF
        this.position = position
    }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap {
        if(drawable is BitmapDrawable) return drawable.bitmap

        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }
}
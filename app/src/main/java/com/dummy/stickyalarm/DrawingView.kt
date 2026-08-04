package com.dummy.stickyalarm

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import java.io.ByteArrayOutputStream

class DrawingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private data class Stroke(val path: Path, val color: Int, val width: Float)

    private val strokes = mutableListOf<Stroke>()
    private var currentPath = Path()
    private var currentColor = Color.BLACK
    private var currentWidth = 8f
    private var backgroundBitmap: Bitmap? = null
    private var lastX = 0f
    private var lastY = 0f

    fun setColor(color: Int) { currentColor = color }
    fun setStrokeWidth(w: Float) { currentWidth = w }
    fun undo() { if (strokes.isNotEmpty()) { strokes.removeAt(strokes.size - 1); invalidate() } }
    fun clearAll() { strokes.clear(); backgroundBitmap = null; invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        backgroundBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        for (s in strokes) {
            val paint = Paint().apply {
                color = s.color; strokeWidth = s.width; style = Paint.Style.STROKE
                isAntiAlias = true; strokeJoin = Paint.Join.ROUND; strokeCap = Paint.Cap.ROUND
            }
            canvas.drawPath(s.path, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x; val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> { currentPath = Path(); currentPath.moveTo(x, y); lastX = x; lastY = y }
            MotionEvent.ACTION_MOVE -> {
                currentPath.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
                lastX = x; lastY = y
            }
            MotionEvent.ACTION_UP -> {
                strokes.add(Stroke(currentPath, currentColor, currentWidth))
            }
        }
        invalidate()
        return true
    }

    fun hasContent(): Boolean = strokes.isNotEmpty() || backgroundBitmap != null

    fun exportToBase64(): String {
        val w = if (width > 0) width else 800
        val h = if (height > 0) height else 800
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        draw(c)
        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
    }

    fun loadFromBase64(data: String) {
        val bytes = Base64.decode(data, Base64.DEFAULT)
        backgroundBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        invalidate()
    }
}

package com.example.natha.assignment1

import android.content.Context
import android.graphics.*
import android.widget.TextView
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.view.*

/**
 * Created by Nathan on 9/11/2017.
 */
class Brush : View {
    val paint : Paint = Paint()
    val path : Path = Path()
    lateinit var canvas : Canvas

    constructor(context: Context?) : super(context) {
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 20F
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 20F
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 20F
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 20F
    }

     fun setColor(r : Int, g : Int, b : Int) {
        paint.setARGB(255, r, g, b)
        invalidate()
    }

     fun setWidth(width : Float) {
        paint.strokeWidth = width
         invalidate()
    }

    fun setJoin(join : Paint.Join)
    {
        paint.strokeJoin = join
        invalidate()
    }

    fun setCap(cap : Paint.Cap)
    {
        paint.strokeCap = cap
        invalidate()
    }

    override fun onDraw(canvas : Canvas?)
    {
        super.onDraw(canvas)
        if(canvas !is Canvas) return

        var line1 = object {
            val startX = canvas.width - (canvas.width - canvas.width/6F)
            val endX = canvas.width.toFloat() - canvas.width/2F
            val startY = canvas.height - canvas.height/2F
            val endY = canvas.height - canvas.height/4F
        }

        var line2 = object {
            val startX = canvas.width.toFloat() - canvas.width/2F
            val endX = canvas.width.toFloat() - canvas.width/6F
            val startY = canvas.height - canvas.height/3F
            val endY = canvas.height - canvas.height/2F
        }

        path.setLastPoint(line1.startX, line1.startY)
        path.lineTo(line1.endX, line1.endY)
        path.lineTo(line2.endX, line2.endY)


        canvas.drawPath(path, paint)

        this.canvas = canvas
    }
}




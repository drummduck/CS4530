package com.example.natha.assignment1

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.widget.TextView
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.view.*

/**
 * Created by Nathan on 9/11/2017.
 */
class Brush : View {
    val paint : Paint = Paint()

    constructor(context: Context?) : super(context) {
        paint.color = Color.BLACK
        paint.strokeWidth = 20F
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        paint.color = Color.BLACK
        paint.strokeWidth = 20F
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        paint.color = Color.BLACK
        paint.strokeWidth = 20F
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        paint.color = Color.BLACK
        paint.strokeWidth = 20F
    }

     fun setColor(r : Int, g : Int, b : Int) {
        paint.setARGB(0, r, g, b)
    }

     fun setWidth(width : Float) {
        paint.strokeWidth = width
    }

    override fun onDraw(canvas : Canvas?)
    {
        super.onDraw(canvas)
        if(canvas !is Canvas) return

        var line1 = object {
            val startX = canvas.width - (canvas.width - canvas.width/5F)
            val endX = canvas.width.toFloat() - canvas.width/2F
            val startY = canvas.height - canvas.height/8F
            val endY = canvas.height - canvas.height/4F
        }

        var line2 = object {

        }

        canvas.drawLine(line1.startX, line1.startY, line1.endX, line1.endY, paint)
    }


}




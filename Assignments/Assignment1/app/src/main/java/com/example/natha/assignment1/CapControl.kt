package com.example.natha.assignment1

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by Nathan on 9/19/2017.
 */
class CapControl : View {

    val paint = Paint()
    val rect = Rect()
    val rectPaint = Paint()
    var firstDraw = true

    var canvas = Canvas()

    lateinit var brush : Brush

    constructor(context: Context?) : super(context)
    {
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 80F
        paint.strokeCap = Paint.Cap.BUTT

        rectPaint.color = Color.BLACK
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = 30F
        rectPaint.strokeCap = Paint.Cap.BUTT

        brush = findViewById(R.id.Brush)
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    {
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 80F
        paint.strokeCap = Paint.Cap.BUTT

        rectPaint.color = Color.BLACK
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = 30F
        rectPaint.strokeCap = Paint.Cap.BUTT

        brush = findViewById(R.id.Brush)

    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    {
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 80F
        paint.strokeCap = Paint.Cap.BUTT

        rectPaint.color = Color.BLACK
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = 30F
        rectPaint.strokeCap = Paint.Cap.BUTT

        brush = findViewById(R.id.Brush)

    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    {
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 20F
        paint.strokeCap = Paint.Cap.BUTT

        rectPaint.color = Color.BLACK
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = 30F
        rectPaint.strokeCap = Paint.Cap.BUTT

        brush = findViewById(R.id.Brush)
    }


    fun setColor(r : Int, g : Int, b : Int) {
        paint.setARGB(255, r, g, b)
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {

        if(canvas !is Canvas) return

        super.onDraw(canvas)

        Log.e("DIMENSIONS", "Height: " + canvas.height + " Width: " + canvas.width)

        canvas.drawLine((canvas.width/7).toFloat(), canvas.height - canvas.height/8F ,((canvas.width/7)*2).toFloat(), canvas.height - canvas.height/8F, paint)
        if(firstDraw)
        {
            rect.set(canvas.width/7 + rectPaint.strokeWidth.toInt(), (canvas.height - canvas.height/8) + rectPaint.strokeWidth.toInt(), ((canvas.width/7)*2) + rectPaint.strokeWidth.toInt(), (canvas.height - canvas.height/8) + rectPaint.strokeWidth.toInt())
            canvas.drawRect(rect, rectPaint)
            firstDraw = false
        }
        paint.strokeCap = Paint.Cap.SQUARE
        canvas.drawLine(((canvas.width/7)*3).toFloat(), canvas.height - canvas.height/8F, ((canvas.width/7)*4).toFloat(), canvas.height - canvas.height/8F, paint)
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawLine(((canvas.width/7)*5).toFloat(), canvas.height - canvas.height/8F, ((canvas.width/7)*6).toFloat(), canvas.height - canvas.height/8F, paint)

        this.canvas = canvas
    }


    val onTouch = object : OnTouchListener
    {
        override fun onTouch(view: View?, event: MotionEvent?): Boolean {

            if(event !is MotionEvent || view !is View) return false

            if(event.getX() > (canvas.width/7).toFloat() && event.getX() < (canvas.width/7)*2F && event.getY() > canvas.height - canvas.height/8F + paint.strokeWidth && event.getY() < canvas.height - canvas.height/8F + paint.strokeWidth - paint.strokeWidth)
            {
                brush.setCap(Paint.Cap.BUTT)
            }
            else if(event.getX() > (canvas.width/7)*3F && event.getX() < (canvas.width/7)*4F && event.getY() > canvas.height - canvas.height/8F + paint.strokeWidth && event.getY() < canvas.height - canvas.height/8F + paint.strokeWidth - paint.strokeWidth)
            {
                brush.setCap(Paint.Cap.SQUARE)
            }
            else if(event.getX() > (canvas.width/7)*5F && event.getX() < (canvas.width/7)*6F && event.getY() > canvas.height - canvas.height/8F + paint.strokeWidth && event.getY() < canvas.height - canvas.height/8F + paint.strokeWidth - paint.strokeWidth)
            {
                brush.setCap(Paint.Cap.ROUND)
            }

            return true
        }

    }
}
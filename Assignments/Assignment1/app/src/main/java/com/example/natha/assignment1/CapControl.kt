package com.example.natha.assignment1

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Created by Nathan on 9/19/2017.
 */
class CapControl : View {

    lateinit var paint : Paint

    constructor(context: Context?) : super(context)
    {
        paint.color = Color.BLACK
        paint.strokeWidth = 20F
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    {
        paint.color = Color.BLACK
        paint.strokeWidth = 20F
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    {
        paint.color = Color.BLACK
        paint.strokeWidth = 20F
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    {
        paint.color = Color.BLACK
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

    override fun onDraw(canvas: Canvas?) {

        if(canvas !is Canvas) return

        super.onDraw(canvas)

        canvas.drawLine((canvas.width/7).toFloat(), ((canvas.height/3)*2).toFloat(), ((canvas.width/7)*2).toFloat(), ((canvas.height/3)*2).toFloat(), paint)
        canvas.drawLine(((canvas.width/7)*3).toFloat(), ((canvas.height/3)*2).toFloat(), ((canvas.width/7)*4).toFloat(), ((canvas.height/3)*2).toFloat(), paint)
        canvas.drawLine(((canvas.width/7)*5).toFloat(), ((canvas.height/3)*2).toFloat(), ((canvas.width/7)*6).toFloat(), ((canvas.height/3)*2).toFloat(), paint)

    }
}
package com.example.natha.assignment2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.shapes.OvalShape
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.R.attr.y
import android.R.attr.x
import java.nio.file.Files.size



/**
 * Created by Natha on 10/4/2017.
 */
class DrawCanvas : View {

    var paint : Paint

    var xPos : Float = 0F
    var yPos : Float = 0F

    var currentDrawing = ArrayList<Pair<Float,Float>>()
    var fullDrawing = ArrayList<ArrayList<Pair<Float, Float>>>()

    var path = Path()

    constructor(context: Context?) : super(context)
    {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.setStyle(Paint.Style.STROKE)
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.setStyle(Paint.Style.STROKE)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.setStyle(Paint.Style.STROKE)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.setStyle(Paint.Style.STROKE)
    }

    override fun onDraw(canvas : Canvas?)
    {
        super.onDraw(canvas)

        if(canvas !is Canvas) return
        val currentPath = Path()
        val fullPath = Path()

        for(d in fullDrawing) {
        var first = true
        var i = 0
        while (i < d.size) {
            val point = d.get(i)
            if (first) {
                first = false
                fullPath.moveTo(point.first, point.second)
            } else if (i < d.size - 1) {
                val next = d.get(i + 1)
                fullPath.quadTo(point.first, point.second, next.first, next.second)
            } else {
                fullPath.lineTo(point.first, point.second)
            }
            i += 2
        }
    }

        var first = true
        var i = 0
        while (i < currentDrawing.size) {
            val point = currentDrawing.get(i)
            if (first) {
                first = false
                currentPath.moveTo(point.first, point.second)
            } else if (i < currentDrawing.size - 1) {
                val next = currentDrawing.get(i + 1)
                currentPath.quadTo(point.first, point.second, next.first, next.second)
            } else {
                currentPath.lineTo(point.first, point.second)
            }
            i += 2
        }
        canvas.drawPath(fullPath, paint)
        canvas.drawPath(currentPath, paint)
    }

    fun setColor(argb : IntArray) { paint.setARGB(255, argb[0],argb[1],argb[2]) }
    fun getColor() : IntArray {return intArrayOf(Color.red(paint.color), Color.green(paint.color), Color.blue(paint.color))}

    fun setCap(capValue : String) {paint.strokeCap = Paint.Cap.valueOf(capValue)}
    fun getCap() : String {return paint.strokeCap.name}

    fun setWidth(wValue : Float) {paint.strokeWidth = wValue}
    fun getPaintWidth() : Float {return paint.strokeWidth}

    fun setJoin(joinValue : String) {paint.strokeJoin = Paint.Join.valueOf(joinValue)}
    fun getJoin() : String {return paint.strokeJoin.name}

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if(event !is MotionEvent) return false
        xPos = event.x
        yPos = event.y

        if(event.action == android.view.MotionEvent.ACTION_UP)
        {
            currentDrawing.add(Pair(xPos, yPos))
            var tempCurrentDrawing = ArrayList<Pair<Float,Float>>()
            for(i in currentDrawing) tempCurrentDrawing.add(i)
            fullDrawing.add(tempCurrentDrawing)
            onTouchDrawListener?.onTouchDraw(this, tempCurrentDrawing, true)
            currentDrawing.clear()
            invalidate()
        }
        else
        {
            currentDrawing.add(Pair(xPos, yPos))
            onTouchDrawListener?.onTouchDraw(this, currentDrawing, false)
            invalidate()
        }

        return true
    }

    fun setCanvas(fullDrawing : ArrayList<ArrayList<Pair<Float,Float>>>)
    {
        this.fullDrawing.clear()
        for(i in fullDrawing)
        {
            this.fullDrawing.add(i)
        }
        invalidate()
    }

    interface OnTouchDrawListener
    {
        fun onTouchDraw(drawCanvas : DrawCanvas, currentDraw : ArrayList<Pair<Float, Float>>, release : Boolean)
    }

    private var onTouchDrawListener : OnTouchDrawListener?  = null

    fun setOnTouchDrawListener(onTouchDrawListener : OnTouchDrawListener)
    {
        this.onTouchDrawListener  = onTouchDrawListener
    }

    fun setOnTouchDrawListener(onTouchDrawListener: ((drawCanvas : DrawCanvas, currentDraw : ArrayList<Pair<Float, Float>>, release : Boolean) -> Unit))
    {
        this.onTouchDrawListener = object : OnTouchDrawListener
        {
            override fun onTouchDraw(drawCanvas : DrawCanvas, currentDraw : ArrayList<Pair<Float, Float>>, release : Boolean)
            {
                onTouchDrawListener(drawCanvas, currentDraw, release)
            }
        }
    }
}
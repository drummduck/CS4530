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
    var currentDrawing = Pair<Quadruple, ArrayList<Pair<Float,Float>>>(Quadruple(intArrayOf(0,0,0), 20F, Paint.Join.MITER.name, Paint.Cap.BUTT.name), ArrayList<Pair<Float, Float>>())
    var fullDrawing = ArrayList<Pair<Quadruple, ArrayList<Pair<Float,Float>>>>()

    var path = Path()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onDraw(canvas : Canvas?)
    {
        super.onDraw(canvas)

        if(canvas !is Canvas) return
        val currentPath = Path()
        val fullPaintArray = ArrayList<Paint>()

        for(d in fullDrawing) {
            val fullPath = Path()
            var first = true
            var i = 0
            var drawPaint = Paint()
            drawPaint.setARGB(255, d.first.color[0], d.first.color[1], d.first.color[2])
            drawPaint.strokeWidth = d.first.width
            drawPaint.strokeJoin = Paint.Join.valueOf(d.first.join)
            drawPaint.strokeCap = Paint.Cap.valueOf(d.first.cap)
            drawPaint.setStyle(Paint.Style.STROKE)
            fullPaintArray.add(drawPaint)
        while (i < d.second.size) {
            val point = d.second.get(i)
            if (first) {
                first = false
                fullPath.moveTo(point.first, point.second)
            } else if (i < d.second.size - 1) {
                    val next = d.second.get(i + 1)
                fullPath.quadTo(point.first, point.second, next.first, next.second)
            } else {
                fullPath.lineTo(point.first, point.second)
            }
            i += 2
        }
            canvas.drawPath(fullPath, drawPaint)
    }

        var first = true
        var i = 0
        val currentPaint = Paint()
        currentPaint.setStyle(Paint.Style.STROKE)
        currentPaint.setARGB(255, currentDrawing.first.color[0], currentDrawing.first.color[1], currentDrawing.first.color[2])
        currentPaint.strokeWidth = currentDrawing.first.width
        currentPaint.strokeJoin = Paint.Join.valueOf(currentDrawing.first.join)
        currentPaint.strokeCap = Paint.Cap.valueOf(currentDrawing.first.cap)
        while (i < currentDrawing.second.size) {
            val point = currentDrawing.second.get(i)
            if (first) {
                first = false
                currentPath.moveTo(point.first, point.second)
            } else if (i < currentDrawing.second.size - 1) {
                val next = currentDrawing.second.get(i + 1)
                currentPath.quadTo(point.first, point.second, next.first, next.second)
            } else {
                currentPath.lineTo(point.first, point.second)
            }
            i += 2
        }
        canvas.drawPath(currentPath, currentPaint)
    }

    fun setColor(rgb : IntArray) {currentDrawing.first.color = rgb}
    fun getColor() : IntArray {return currentDrawing.first.color}

    fun setCap(capValue : String) {currentDrawing.first.cap = capValue}
    fun getCap() : String {return currentDrawing.first.cap}

    fun setWidth(wValue : Float) {currentDrawing.first.width = wValue}
    fun getPaintWidth() : Float {return currentDrawing.first.width}

    fun setJoin(joinValue : String) {currentDrawing.first.join = joinValue}
    fun getJoin() : String {return currentDrawing.first.join}

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if(event !is MotionEvent) return false
        var xPos = event.x
        var yPos = event.y

        if(event.action == android.view.MotionEvent.ACTION_UP)
        {
            var quad = Quadruple(currentDrawing.first.color, currentDrawing.first.width, currentDrawing.first.join, currentDrawing.first.cap)
            var arrayList = ArrayList<Pair<Float, Float>>()
            currentDrawing.second.add(Pair(xPos, yPos))
            for(i in currentDrawing.second)arrayList.add(Pair(i.first, i.second))
            var tempCurrentDrawing = Pair(quad, arrayList)
            fullDrawing.add(tempCurrentDrawing)
            onTouchDrawListener?.onTouchDraw(this, tempCurrentDrawing, true)
            currentDrawing.second.clear()
            invalidate()
        }
        else
        {
            currentDrawing.second.add(Pair(xPos, yPos))
            onTouchDrawListener?.onTouchDraw(this, currentDrawing, false)
            invalidate()
        }

        return true
    }

    fun setCanvas(fullDrawing : ArrayList<Pair<Quadruple, ArrayList<Pair<Float,Float>>>>)
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
        fun onTouchDraw(drawCanvas : DrawCanvas, currentDraw : Pair<Quadruple, ArrayList<Pair<Float,Float>>>, release : Boolean)
    }

    private var onTouchDrawListener : OnTouchDrawListener?  = null

    fun setOnTouchDrawListener(onTouchDrawListener : OnTouchDrawListener)
    {
        this.onTouchDrawListener  = onTouchDrawListener
    }

    fun setOnTouchDrawListener(onTouchDrawListener: ((drawCanvas : DrawCanvas, currentDraw : Pair<Quadruple, ArrayList<Pair<Float,Float>>>, release : Boolean) -> Unit))
    {
        this.onTouchDrawListener = object : OnTouchDrawListener
        {
            override fun onTouchDraw(drawCanvas : DrawCanvas, currentDraw : Pair<Quadruple, ArrayList<Pair<Float,Float>>>, release : Boolean)
            {
                onTouchDrawListener(drawCanvas, currentDraw, release)
            }
        }
    }
}
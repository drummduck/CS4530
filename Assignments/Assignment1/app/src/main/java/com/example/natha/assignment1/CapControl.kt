package com.example.natha.assignment1

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.icu.util.Measure
import android.net.wifi.p2p.WifiP2pManager
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
    val linePaint = Paint()
    val rect = Rect()
    val rectPaint = Paint()
    var firstDraw = true
    var currentCap = Paint.Cap.BUTT
    var timer = 0L

    lateinit var canvas : Canvas

    constructor(context: Context?) : super(context)
    {
        paint.setARGB(100,0,0,0)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 80F

        linePaint.style = Paint.Style.STROKE
        linePaint.color = Color.BLACK
        linePaint.strokeWidth = 5F

        rectPaint.color = Color.BLACK
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = 10F

    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    {
        paint.setARGB(100,0,0,0)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 80F

        linePaint.style = Paint.Style.STROKE
        linePaint.color = Color.BLACK
        linePaint.strokeWidth = 5F

        rectPaint.color = Color.BLACK
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = 10F
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    {
        paint.setARGB(100,0,0,0)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 80F

        linePaint.style = Paint.Style.STROKE
        linePaint.color = Color.BLACK
        linePaint.strokeWidth = 5F

        rectPaint.color = Color.BLACK
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = 10F
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    {
        paint.setARGB(100,0,0,0)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 80F

        linePaint.style = Paint.Style.STROKE
        linePaint.color = Color.BLACK
        linePaint.strokeWidth = 5F

        rectPaint.color = Color.BLACK
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = 10F
    }

    interface OnCapChangedListener
    {
        fun onCapChanged(capControl : CapControl, cap : Paint.Cap)
    }

    private var onCapChangedListener : OnCapChangedListener?  = null

    fun setOnCapChangedListener(onCapChangedListener : OnCapChangedListener)
    {
        this.onCapChangedListener  = onCapChangedListener
    }

    fun setOnCapChangedListener(onCapChangedListener: ((capControl: CapControl, cap: Paint.Cap) -> Unit))
    {
        this.onCapChangedListener = object : OnCapChangedListener
        {
            override fun onCapChanged(capControl: CapControl, cap: Paint.Cap)
            {
                onCapChangedListener(capControl, cap)
            }
        }
    }



    fun setColor(r : Int, g : Int, b : Int) {
        paint.setARGB(100, r, g, b)
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {

        if(canvas !is Canvas) return
        super.onDraw(canvas)

        paint.strokeCap = Paint.Cap.BUTT
        canvas.drawLine((canvas.width/7).toFloat(), canvas.height - canvas.height/8F ,((canvas.width/7)*2).toFloat(), canvas.height - canvas.height/8F, paint)
        canvas.drawLine((canvas.width/7).toFloat(), canvas.height - canvas.height/8F ,((canvas.width/7)*2).toFloat(), canvas.height - canvas.height/8F, linePaint)
        paint.strokeCap = Paint.Cap.SQUARE
        canvas.drawLine(((canvas.width/7)*3).toFloat(), canvas.height - canvas.height/8F, ((canvas.width/7)*4).toFloat(), canvas.height - canvas.height/8F, paint)
        canvas.drawLine(((canvas.width/7)*3).toFloat(), canvas.height - canvas.height/8F, ((canvas.width/7)*4).toFloat(), canvas.height - canvas.height/8F, linePaint)
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawLine(((canvas.width/7)*5).toFloat(), canvas.height - canvas.height/8F, ((canvas.width/7)*6).toFloat(), canvas.height - canvas.height/8F, paint)
        canvas.drawLine(((canvas.width/7)*5).toFloat(), canvas.height - canvas.height/8F, ((canvas.width/7)*6).toFloat(), canvas.height - canvas.height/8F, linePaint)

        if(firstDraw) {
            rect.set(canvas.width / 7 - paint.strokeWidth.toInt(), (canvas.height - canvas.height / 8) - paint.strokeWidth.toInt(), ((canvas.width / 7) * 2) + paint.strokeWidth.toInt(), (canvas.height - canvas.height / 8) + paint.strokeWidth.toInt())
        }
        canvas.drawRect(rect, rectPaint)
        firstDraw = false
        this.canvas = canvas
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if(event !is MotionEvent) return false

        if(event.y.toInt() > context.resources.displayMetrics.heightPixels - context.resources.displayMetrics.heightPixels/3)
        {

            if (timer == 0L) {
                if (currentCap == Paint.Cap.BUTT) {
                    rect.set(((canvas!!.width / 7) * 3) - paint.strokeWidth.toInt() - 20, (canvas!!.height - canvas!!.height / 8) - paint.strokeWidth.toInt() - 40, ((canvas!!.width / 7) * 4) + paint.strokeWidth.toInt()/2, (canvas!!.height - canvas!!.height / 8) + paint.strokeWidth.toInt()/2)
                    currentCap = Paint.Cap.SQUARE
                    onCapChangedListener?.onCapChanged(this, currentCap)
                    timer = System.currentTimeMillis()
                } else if (currentCap == Paint.Cap.SQUARE) {
                    rect.set(((canvas!!.width / 7) * 5) - paint.strokeWidth.toInt() - 25, (canvas!!.height - canvas!!.height / 8) - paint.strokeWidth.toInt() - 40, ((canvas!!.width / 7) * 6) + paint.strokeWidth.toInt()/3, (canvas!!.height - canvas!!.height / 8) + paint.strokeWidth.toInt()/2)
                    currentCap = Paint.Cap.ROUND
                    onCapChangedListener?.onCapChanged(this, currentCap)
                    timer = System.currentTimeMillis()
                } else if (currentCap == Paint.Cap.ROUND) {
                    rect.set((canvas!!.width / 7) - paint.strokeWidth.toInt() + 20, (canvas!!.height - canvas!!.height / 8) - paint.strokeWidth.toInt() - 40, ((canvas!!.width / 7) * 2) + paint.strokeWidth.toInt()/2, (canvas!!.height - canvas!!.height / 8) + paint.strokeWidth.toInt()/2)
                    currentCap = Paint.Cap.BUTT
                    onCapChangedListener?.onCapChanged(this, currentCap)
                    timer = System.currentTimeMillis()
                }
            }

            else if(System.currentTimeMillis() - timer > 500) timer = 0L

        }


//        if(event.getX() > (canvas.width/7).toFloat() && event.getX() < (canvas.width/7)*2F && event.getY() > canvas.height - canvas.height/8F - 80F && event.getY() < canvas.height - canvas.height/8F + 80F)
//        {
//            rect.set(canvas.width/7 - 80, (canvas.height - canvas.height/8) - 80, ((canvas.width/7)*2) + 80, (canvas.height - canvas.height/8) + 80)
//            currentCap = Paint.Cap.BUTT
//        }
//        else if(event.getX() > (canvas.width/7)*3F && event.getX() < (canvas.width/7)*4F && event.getY() > canvas.height - canvas.height/8F - 80F && event.getY() < canvas.height - canvas.height/8F + 80F)
//        {
//            rect.set(((canvas.width/7)*3), (canvas.height - canvas.height/8) - 80, ((canvas.width/7)*4) + 80, (canvas.height - canvas.height/8) + 80)
//            currentCap = Paint.Cap.SQUARE
//        }
//        else if(event.getX() > (canvas.width/7)*5F && event.getX() < (canvas.width/7)*6F && event.getY() > canvas.height - canvas.height/8F - 80F && event.getY() < canvas.height - canvas.height/8F + 80F)
//        {
//            rect.set(((canvas.width/7)*5) - 80, (canvas.height - canvas.height/8) - 80, ((canvas.width/7)*6) + 80, (canvas.height - canvas.height/8) + 80)
//            currentCap = Paint.Cap.ROUND
//        }



        return true
    }

//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//
//        val widthMode : Int = MeasureSpec.getMode(widthMeasureSpec)
//        val heightMode : Int = MeasureSpec.getMode(heightMeasureSpec)
//        val widthSize : Int = MeasureSpec.getSize(widthMeasureSpec)
//        val heightSize : Int = MeasureSpec.getSize(heightMeasureSpec)
//
//        setMeasuredDimension(resolveSize(500, widthSize), resolveSize(500, heightSize))
//    }
}
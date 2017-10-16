package com.example.natha.assignment2

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ImageButton
import kotlinx.android.synthetic.main.activity_file_selection.*
import android.os.Environment.MEDIA_MOUNTED_READ_ONLY
import android.os.Environment.MEDIA_MOUNTED
import android.provider.ContactsContract
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import java.io.*
import android.app.Activity
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.util.DisplayMetrics
import com.example.natha.assignment2.R.drawable.*
import kotlinx.android.synthetic.main.activity_draw.*
import kotlinx.android.synthetic.main.activity_file_selection.view.*
import kotlinx.android.synthetic.main.view_titled_image.view.*
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Paths


class Draw : AppCompatActivity() {

    lateinit var brushButton: ImageButton
    lateinit var undoButton : ImageButton
    lateinit var redoButton : ImageButton
    lateinit var drawCanvas : DrawCanvas
    lateinit var playButton : ImageButton

    var redoEnabled = false
    var undoEnabled = false

    var undoArray = ArrayList<Pair<Quadruple, ArrayList<Pair<Float,Float>>>>()
    var redoArray = ArrayList<Pair<Quadruple, ArrayList<Pair<Float,Float>>>>()
    var fileName : String = ""
    var numOfFiles : Int = 0

    var handler = Handler()

    var fromColorPicker = false
    var fromFileSelection = false

    var fromOrientation = false

    val clickListener = View.OnClickListener { view ->
        when (view.getId()) {
            R.id.brushButton ->
            {
                intent = Intent(applicationContext, ColorPicker::class.java)
                intent.putExtra("rValue", drawCanvas.getColor()[0])
                intent.putExtra("gValue", drawCanvas.getColor()[1])
                intent.putExtra("bValue", drawCanvas.getColor()[2])
                intent.putExtra("wValue", drawCanvas.getPaintWidth())
                intent.putExtra("capValue", drawCanvas.getCap())
                intent.putExtra("joinValue", drawCanvas.getJoin())
                intent.putExtra("fileName", fileName)
                startActivityForResult(intent,1)
            }

            R.id.undoButton ->
            {
                if(!undoArray.isEmpty()) {
                    redoArray.add(undoArray[undoArray.size - 1])
                    undoArray.removeAt(undoArray.size - 1)
                    drawCanvas.setCanvas(undoArray)
                    if (undoArray.isEmpty()) undoButton.setColorFilter(Color.argb(180, 255, 255, 255))
                    redoButton.setColorFilter(Color.argb(0, 255, 255, 255))
                    writeToFile()
                    drawCanvas.saveThumbnail()
                }
            }

            R.id.redoButton ->
            {
                if(!redoArray.isEmpty())
                {
                    undoArray.add(redoArray[redoArray.size - 1])
                    redoArray.removeAt(redoArray.size - 1)
                    drawCanvas.setCanvas(undoArray)
                    if(redoArray.isEmpty()) redoButton.setColorFilter(Color.argb(180,225,225,225))
                    undoButton.setColorFilter(Color.argb(0,255,255,255))
                    writeToFile()
                    drawCanvas.saveThumbnail()
                }
            }

            R.id.playButton ->
            {
                redoButton.isClickable = false
                undoButton.isClickable = false
                playButton.isClickable = false
                brushButton.isClickable = false
                drawCanvas.touchable = false
                undoButton.setColorFilter(Color.argb(180,255,255,255))
                redoButton.setColorFilter(Color.argb(180,255,255,255))
                brushButton.setColorFilter(Color.argb(180, 255, 255, 255))
                var tempUndoArray = ArrayList<Pair<Quadruple, ArrayList<Pair<Float,Float>>>>()
                drawCanvas.setCanvas(tempUndoArray)
                var timer = 1
                for(i in undoArray)
                {
                    Handler().postDelayed(Runnable {
                        tempUndoArray.add(i)
                    drawCanvas.setCanvas(tempUndoArray)
                    if (tempUndoArray.size == undoArray.size)
                    {
                        redoButton.isClickable = true
                        undoButton.isClickable = true
                        playButton.isClickable = true
                        brushButton.isClickable = true
                        drawCanvas.touchable = true

                        if(undoArray.size > 0) undoButton.setColorFilter(Color.argb(0,255,255,255))
                        if(redoArray.size > 0) redoButton.setColorFilter(Color.argb(0,255,255,255))
                        brushButton.setColorFilter(Color.argb(0, 255, 255, 255))
                    }},500 * timer.toLong())
                    timer++
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw)

        if(!fromColorPicker) {
            drawCanvas = findViewById(R.id.drawingSpace)

            brushButton = findViewById(R.id.brushButton)
            brushButton.setOnClickListener(clickListener)

            undoButton = findViewById(R.id.undoButton)
            undoButton.setOnClickListener(clickListener)

            redoButton = findViewById(R.id.redoButton)
            redoButton.setOnClickListener(clickListener)

            playButton = findViewById(R.id.playButton)
            playButton.setOnClickListener(clickListener)

            var intent = getIntent()

            if (savedInstanceState != null) {
                var red: Int = 0
                var green: Int = 0
                var blue: Int = 0
                for (i in savedInstanceState.keySet()) {
                    when (i) {
                        "rValue" -> red = savedInstanceState.getInt(i, 0)
                        "gValue" -> green = savedInstanceState.getInt(i, 0)
                        "bValue" -> blue = savedInstanceState.getInt(i, 0)
                        "wValue" -> drawCanvas.setWidth(savedInstanceState.getFloat("wValue", 20F))
                        "capValue" -> drawCanvas.setCap(savedInstanceState.getString("capValue"))
                        "joinValue" -> drawCanvas.setJoin(savedInstanceState.getString("joinValue"))
                        "fileName" -> fileName = savedInstanceState.getString("fileName")
                    }
                }
                drawCanvas.setColor(intArrayOf(red, green, blue))
                readFromFile()
            }
            else if (intent != null && intent.extras != null) {
                for (i in intent.extras.keySet()) {
                    when (i) {
                        "fileName" -> fileName = intent.getStringExtra("fileName")
                        "numOfFiles" -> numOfFiles = intent.getIntExtra("numOfFiles", 0)
                    }
                }

                fromFileSelection = true

                if (fileName.equals("New Project"))
                {
                    fileName = "Project" + (numOfFiles + 1)
                    drawCanvas.setFilename(fileName)
                    writeToFile()
                }

                else
                {
                    drawCanvas.setFilename(fileName)
                    readFromFile()
                }
            }

            drawCanvas.setOnTouchDrawListener { _, currentDrawing, release ->
                if (release) {
                    undoArray.add(currentDrawing)
                    undoButton.setColorFilter(Color.argb(0, 255, 255, 255))
                    writeToFile()
                } else {
                    redoArray.clear()
                    redoButton.setColorFilter(Color.argb(180, 255, 255, 255))
                }
            }
        }
        fromColorPicker = false
        fromFileSelection = false
    }

    fun writeToFile()
    {
        var file : File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/Assignment2Draw/" + fileName)

        if(file.exists())
        {
            file.delete()
            file.createNewFile()
        }
        else file.createNewFile()
        val outputWriter = FileOutputStream(file)
        val outputStream = DataOutputStream(outputWriter)

        outputStream.writeInt(undoArray.size)
        for(i in undoArray)
        {
            outputStream.writeInt(i.first.color[0])
            outputStream.writeInt(i.first.color[1])
            outputStream.writeInt(i.first.color[2])
            outputStream.writeFloat(i.first.width)
            outputStream.writeChars(i.first.join)
            outputStream.writeChars("\t")
            outputStream.writeChars(i.first.cap)
            outputStream.writeChars("\t")
            outputStream.writeInt(i.second.size)
        }
        for(i in undoArray)
        {
            for(j in i.second)
            {
                var displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                outputStream.writeFloat(j.first / displayMetrics.widthPixels)
                outputStream.writeFloat(j.second / displayMetrics.heightPixels)
            }
        }

        outputStream.writeInt(redoArray.size)
        for(i in redoArray)
        {
            outputStream.writeInt(i.first.color[0])
            outputStream.writeInt(i.first.color[1])
            outputStream.writeInt(i.first.color[2])
            outputStream.writeFloat(i.first.width)
            outputStream.writeChars(i.first.join)
            outputStream.writeChars("\t")
            outputStream.writeChars(i.first.cap)
            outputStream.writeChars("\t")
            outputStream.writeInt(i.second.size)
        }
        for(i in redoArray)
        {
            for(j in i.second)
            {
                var displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                outputStream.writeFloat(j.first/displayMetrics.widthPixels)
                outputStream.writeFloat(j.second/displayMetrics.heightPixels)
            }
        }

        outputStream.writeInt(drawCanvas.getColor()[0])
        outputStream.writeInt(drawCanvas.getColor()[1])
        outputStream.writeInt(drawCanvas.getColor()[2])
        outputStream.writeFloat(drawCanvas.getPaintWidth())
        outputStream.writeChars(drawCanvas.getJoin())
        outputStream.writeChars("\t")
        outputStream.writeChars(drawCanvas.getCap())
        outputStream.writeChars("\t")

        outputStream.flush()
        outputStream.close()
        outputWriter.close()
    }

    fun readFromFile()
    {
        var file : File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/Assignment2Draw/" + fileName)
        val inputFile = FileInputStream(file)
        val inputReader = DataInputStream(inputFile)
        var undoArraySize = inputReader.readInt()
        var undoPaintArray = ArrayList<Quadruple>()
        var undoPairArraySize = ArrayList<Int>()
        for(i in 1..undoArraySize)
        {
            undoPaintArray.add(Quadruple(intArrayOf(inputReader.readInt(), inputReader.readInt(), inputReader.readInt()), inputReader.readFloat(), readString(inputReader), readString(inputReader)))
            undoPairArraySize.add(inputReader.readInt())
        }

        var num = 0
        var undoArrayScaled = ArrayList<Pair<Quadruple, ArrayList<Pair<Float,Float>>>>()
        for(i in undoPairArraySize)
        {
            var pairArrayScaled = ArrayList<Pair<Float,Float>>()
            for(j in 1..i)
            {
                var displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                var xVal = inputReader.readFloat()
                var yVal = inputReader.readFloat()
                var pairScale = Pair(displayMetrics.widthPixels*xVal, displayMetrics.heightPixels*yVal)
                pairArrayScaled.add(pairScale)
            }
            undoArrayScaled.add(Pair(undoPaintArray[num], pairArrayScaled))
            num++
        }

        if(undoArrayScaled.size > 0)
        {
            undoArray = undoArrayScaled
        }


        var redoArraySize = inputReader.readInt()
        var redoPaintArray = ArrayList<Quadruple>()
        var redoPairArraySize = ArrayList<Int>()
        for(i in 1..redoArraySize)
        {
            redoPaintArray.add(Quadruple(intArrayOf(inputReader.readInt(), inputReader.readInt(), inputReader.readInt()), inputReader.readFloat(), readString(inputReader), readString(inputReader)))
            redoPairArraySize.add(inputReader.readInt())
        }

        num = 0
        var redoArrayScaled = ArrayList<Pair<Quadruple, ArrayList<Pair<Float,Float>>>>()
        for(i in redoPairArraySize)
        {
            var pairArrayScaled = ArrayList<Pair<Float,Float>>()
            for(j in 1..i)
            {
                var displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                var xVal = inputReader.readFloat()
                var yVal = inputReader.readFloat()
                var pairScale = Pair(displayMetrics.widthPixels*xVal, displayMetrics.heightPixels*yVal)
                pairArrayScaled.add(pairScale)
            }
            redoArrayScaled.add(Pair(redoPaintArray[num], pairArrayScaled))
            num++
        }

        if(redoArrayScaled.size > 0)
        {
            redoArray = redoArrayScaled
        }

        if(redoArray.size > 0) redoButton.setColorFilter(Color.argb(0,255,255,255))
        else redoButton.setColorFilter(Color.argb(180,255,255,255))
        if(undoArray.size > 0) undoButton.setColorFilter(Color.argb(0,255,255,255))
        else undoButton.setColorFilter(Color.argb(180,255,255,255))

        drawCanvas.setColor(intArrayOf(inputReader.readInt(), inputReader.readInt(), inputReader.readInt()))
        drawCanvas.setWidth(inputReader.readFloat())
        drawCanvas.setJoin(readString(inputReader))
        drawCanvas.setCap(readString(inputReader))

        drawCanvas.setCanvas(undoArray)
        drawCanvas.invalidate()
        inputReader.close()
        inputFile.close()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
            savedInstanceState!!.putInt("redValue", drawCanvas.getColor()[0])
            savedInstanceState.putInt("greenValue", drawCanvas.getColor()[1])
            savedInstanceState.putInt("blueValue", drawCanvas.getColor()[2])
            savedInstanceState.putFloat("wValue", drawCanvas.getPaintWidth())
            savedInstanceState.putString("capValue", drawCanvas.getCap())
            savedInstanceState.putString("joinValue", drawCanvas.getJoin())
            savedInstanceState.putString("fileName", fileName)
            if(!fromOrientation)writeToFile()
            fromOrientation = false
            super.onSaveInstanceState(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {

        if (resultCode == Activity.RESULT_OK) {
                var red = 0
                var green = 0
                var blue = 0
                for(i in data.extras.keySet())
                {
                    when(i)
                    {
                        "rValue" -> red = data.getIntExtra(i, 0)
                        "gValue" -> green = data.getIntExtra(i, 0)
                        "bValue" -> blue = data.getIntExtra(i, 0)
                        "wValue" -> drawCanvas.setWidth(data.getFloatExtra(i, 20F))
                        "capValue" -> drawCanvas.setCap(data.getStringExtra("capValue"))
                        "joinValue" -> drawCanvas.setJoin(data.getStringExtra("joinValue"))
                    }
                }

                drawCanvas.setColor(intArrayOf(red, green, blue))
                fromColorPicker = true
                writeToFile()
            }
    }

    override fun onBackPressed() {
        var intent = Intent(this, FileSelection::class.java)
        startActivity(intent)
        finish()
    }

    fun readString(reader : DataInputStream) : String
    {
        var returnString = ""
        while(true)
        {
            var readInChar = reader.readChar()
            if(readInChar == '\t') return returnString
            else returnString = returnString + readInChar
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        fromOrientation = true
        super.onConfigurationChanged(newConfig)
    }

    fun viewToBitmap(view : View) : Bitmap
    {
        var bitMap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bitMap)
        view.draw(canvas)
        return bitMap
    }
}
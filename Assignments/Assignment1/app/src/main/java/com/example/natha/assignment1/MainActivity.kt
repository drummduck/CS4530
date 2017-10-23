package com.example.natha.assignment1

import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.TextKeyListener
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import kotlinx.android.synthetic.main.activity_main.*
import org.w3c.dom.Text


class MainActivity : AppCompatActivity() {

    lateinit var redSlider : SeekBar
    lateinit var redValue : EditText

    lateinit var greenSlider : SeekBar
    lateinit var greenValue : EditText

    lateinit var blueSlider : SeekBar
    lateinit var blueValue : EditText

    lateinit var widthSlider : SeekBar
    lateinit var widthValue : EditText

    lateinit var joinSelect : Spinner

    lateinit var brush : Brush

    lateinit var capControl : CapControl

    val sliderListener = object : SeekBar.OnSeekBarChangeListener
    {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean)
        {
            if(fromUser) {
                if (redValue.hasFocus()) redValue.clearFocus()
                else if (greenValue.hasFocus()) greenValue.clearFocus()
                else if (blueValue.hasFocus()) blueValue.clearFocus()
                else if (widthValue.hasFocus()) widthValue.clearFocus()
                if (seekBar.id == redSlider.id) {
                    redValue.setText(redSlider.progress.toString())
                    brush.setColor(redSlider.progress, greenSlider.progress, blueSlider.progress)
                    capControl.setColor(redSlider.progress, greenSlider.progress, blueSlider.progress)

                } else if (seekBar.id == greenSlider.id) {
                    greenValue.setText(greenSlider.progress.toString())
                    brush.setColor(redSlider.progress, greenSlider.progress, blueSlider.progress)
                    capControl.setColor(redSlider.progress, greenSlider.progress, blueSlider.progress)

                } else if (seekBar.id == blueSlider.id){
                    blueValue.setText(blueSlider.progress.toString())
                    brush.setColor(redSlider.progress, greenSlider.progress, blueSlider.progress)
                    capControl.setColor(redSlider.progress, greenSlider.progress, blueSlider.progress)
                }
                else
                {
                    widthValue.setText((widthSlider.progress.toDouble()/10).toString())
                    brush.setWidth((widthSlider.progress/10).toFloat())
                }
            }
        }
        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    }

    val focusTextListener = object : View.OnFocusChangeListener
    {
        override fun onFocusChange(view : View?, focused: Boolean) {
            if(view !is View) return
            else if(focused)
            {
                if(view.id == redValue.id)
                {
                    redValue.setInputType(InputType.TYPE_CLASS_TEXT)
                    redValue.setText("")
                }
                else if(view.id == greenValue.id)
                {
                    greenValue.setInputType(InputType.TYPE_CLASS_TEXT)
                    greenValue.setText("")
                }
                else if(view.id == blueValue.id)
                {
                    blueValue.setInputType(InputType.TYPE_CLASS_TEXT)
                    blueValue.setText("")
                }

                else if(view.id == widthValue.id)
                {
                    widthValue.setInputType(InputType.TYPE_CLASS_TEXT)
                    widthValue.setText("")
                }
            }
            else
            {
                if(view.id == redValue.id && redValue.text.toString().isEmpty())
                {
                    redValue.inputType = InputType.TYPE_CLASS_NUMBER
                    redValue.setText(redSlider.progress.toString())
                }
                else if(view.id == greenValue.id && greenValue.text.toString().isEmpty())
                {
                    greenValue.inputType = InputType.TYPE_CLASS_NUMBER
                    greenValue.setText(greenSlider.progress.toString())
                }
                else if(view.id == blueValue.id && blueValue.text.toString().isEmpty())
                {
                    blueValue.inputType = InputType.TYPE_CLASS_NUMBER
                    blueValue.setText(blueSlider.progress.toString())
                }
                else if(view.id == widthValue.id && widthValue.text.toString().isEmpty())
                {
                    widthValue.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
                    widthValue.setText((widthSlider.progress.toDouble()/10).toString())
                }
            }
        }
    }

    val joinSelection = object : OnItemSelectedListener
    {
        override fun onNothingSelected(p0: AdapterView<*>?) {}

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, p3: Long) {
            if(parent !is AdapterView<*>) return
            if(position == 0) {
                brush.setJoin(Paint.Join.MITER)
            }
            else if(position == 1) {
                brush.setJoin(Paint.Join.BEVEL)
            }
            else if(position == 2) {
                brush.setJoin(Paint.Join.ROUND)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

         brush = findViewById(R.id.Brush)

        capControl = findViewById(R.id.capControl)
        capControl.setOnCapChangedListener{_, cap ->
            brush.setCap(cap)
        capControl.invalidate()}

        joinSelect = findViewById(R.id.joinSelect)
        joinSelect.onItemSelectedListener = joinSelection

        widthSlider = findViewById(R.id.widthSlider)
        widthSlider.progress = 200
        widthValue = findViewById(R.id.widthValue)
        widthValue.setText("20.0")
        redSlider = findViewById(R.id.redSlider)
        redValue = findViewById(R.id.redValue)

        greenSlider = findViewById(R.id.greenSlider)
        greenValue = findViewById(R.id.greenValue)

        blueSlider = findViewById(R.id.blueSlider)
        blueValue = findViewById(R.id.blueValue)


        widthSlider.setOnSeekBarChangeListener(sliderListener)
        widthValue.setOnFocusChangeListener(focusTextListener)
        widthValue.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(character: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if(character !is CharSequence) return

                var sequence : String = character.toString()

                try
                {
                    if(sequence.contains('.') && sequence.split(".")[1].length > 1)
                    {
                        sequence = character.toString().substring(0,character.length-1 )
                        widthValue.setText(sequence)
                    }

                    var number : Double = sequence.toDouble()
                    if(number > 50)
                    {
                        number = 50.0
                        widthValue.setText(number.toString())
                    }

                    if(number < 0.5)
                    {
                        number = 0.5
                        widthValue.setText(number.toString())
                    }
                    if(widthValue.inputType == InputType.TYPE_CLASS_TEXT) widthValue.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
                    widthSlider.setProgress(number.toInt()*10)
                }
                catch(e : Throwable){return}
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        }
        )

        redSlider.setOnSeekBarChangeListener(sliderListener)
        redValue.setOnFocusChangeListener(focusTextListener)
        redValue.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(character: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try
                {
                    var number : Int = Integer.parseInt(character.toString())
                    if(number > 255)
                    {
                        number = 255
                        redValue.setText(number.toString())
                    }

                    if(redValue.inputType == InputType.TYPE_CLASS_TEXT) redValue.inputType = InputType.TYPE_CLASS_NUMBER

                    redSlider.setProgress(number)
                }
                catch(e : NumberFormatException){return}
            }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}
            }
        )

        greenSlider.setOnSeekBarChangeListener(sliderListener)
        greenValue.setOnFocusChangeListener(focusTextListener)
        greenValue.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(character: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try
                {
                    var number : Int = Integer.parseInt(character.toString())
                    if(number > 255)
                    {
                        number = 255
                        greenValue.setText(number.toString())
                    }

                    if(greenValue.inputType == InputType.TYPE_CLASS_TEXT) greenValue.inputType = InputType.TYPE_CLASS_NUMBER

                    greenSlider.setProgress(number)
                }
                catch(e : NumberFormatException){return}
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        }
        )

        blueSlider.setOnSeekBarChangeListener(sliderListener)
        blueValue.setOnFocusChangeListener(focusTextListener)
        blueValue.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(character: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try
                {
                    var number : Int = Integer.parseInt(character.toString())
                    if(number > 255)
                    {
                        number = 255
                        blueValue.setText(number.toString())
                    }

                    if(blueValue.inputType == InputType.TYPE_CLASS_TEXT) blueValue.inputType = InputType.TYPE_CLASS_NUMBER

                    blueSlider.setProgress(number)
                }
                catch(e : NumberFormatException){return}
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        }
        )
    }
}

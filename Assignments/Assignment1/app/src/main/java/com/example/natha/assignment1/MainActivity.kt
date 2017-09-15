package com.example.natha.assignment1

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.TextKeyListener
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import org.w3c.dom.Text


class MainActivity : AppCompatActivity() {

    lateinit var redSlider : SeekBar
    lateinit var redValue : EditText

    lateinit var greenSlider : SeekBar
    lateinit var greenValue : EditText

    lateinit var blueSlider : SeekBar
    lateinit var blueValue : EditText

    lateinit var brush : Brush

    val sliderListener = object : SeekBar.OnSeekBarChangeListener
    {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean)
        {
            if(fromUser) {
                if (redValue.hasFocus()) redValue.clearFocus()
                else if (greenValue.hasFocus()) greenValue.clearFocus()
                else if (blueValue.hasFocus()) blueValue.clearFocus()
                if (seekBar.id == redSlider.id) {
                    redValue.setText(progress.toString())
                    brush.setColor(Integer.parseInt(redValue.text.toString()), Integer.parseInt(greenValue.text.toString()), Integer.parseInt(blueValue.text.toString()))
                    brush.invalidate()
                } else if (seekBar.id == greenSlider.id) {
                    greenValue.setText(progress.toString())
                    brush.setColor(Integer.parseInt(redValue.text.toString()), Integer.parseInt(greenValue.text.toString()), Integer.parseInt(blueValue.text.toString()))
                    brush.invalidate()
                } else {
                    blueValue.setText(progress.toString())
                    brush.setColor(Integer.parseInt(redValue.text.toString()), Integer.parseInt(greenValue.text.toString()), Integer.parseInt(blueValue.text.toString()))
                    brush.invalidate()
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
        }
    }

    val focusTextListener = object : View.OnFocusChangeListener
    {
        override fun onFocusChange(view : View?, focused: Boolean) {
            if(view !is View) return
            else if(focused)
            {
                if(view.id == redValue.id && redValue.text.toString().equals("0"))
                {
                    redValue.setInputType(InputType.TYPE_CLASS_TEXT)
                    redValue.text.clear()
                }
                else if(view.id == greenValue.id && greenValue.text.toString().equals("0"))
                {
                    greenValue.setInputType(InputType.TYPE_CLASS_TEXT)
                    greenValue.setText("")
                }
                else if(view.id == blueValue.id && blueValue.text.toString().equals("0"))
                {
                    blueValue.setInputType(InputType.TYPE_CLASS_TEXT)
                    blueValue.setText("")
                }
            }
            else
            {
                redValue.setInputType(InputType.TYPE_CLASS_NUMBER)
                if(view.id == redValue.id && redValue.text.toString().isEmpty()) { redValue.setText("0") }
                else if(view.id == greenValue.id && greenValue.text.toString().isEmpty()) { greenValue.setText("0") }
                else if(view.id == blueValue.id && blueValue.text.toString().isEmpty()) { blueValue.setText("0") }
            }
        }
    }

//    val editTextListener = object : TextWatcher
//    {
//
//        override fun onTextChanged(character : CharSequence?, p1: Int, p2: Int, p3: Int) {
//
//
//
//            try {
//                if (textView!!.inputType == InputType.TYPE_CLASS_TEXT && Integer.parseInt(keyEvent!!.characters) is Int) InputType.TYPE_CLASS_NUMBER
//
//                if (textView.id == redValue.id) {
//                    redSlider.setProgress(Integer.parseInt(redValue.text.toString()))
//                } else if (textView.id == greenValue.id) greenSlider.setProgress(Integer.parseInt(redValue.text.toString()))
//                else blueSlider.setProgress(Integer.parseInt(redValue.text.toString()))
//                return true
//            } catch (e: NumberFormatException) {
//                return true
//            }
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        brush = findViewById(R.id.Brush)

        redSlider = findViewById(R.id.redSlider)
        redValue = findViewById(R.id.redValue)

        greenSlider = findViewById(R.id.greenSlider)
        greenValue = findViewById(R.id.greenValue)

        blueSlider = findViewById(R.id.blueSlider)
        blueValue = findViewById(R.id.blueValue)

        redSlider.setOnSeekBarChangeListener(sliderListener)
        redValue.setOnFocusChangeListener(focusTextListener)
        redValue.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(character: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try
                {
                    var number : Int = Integer.parseInt(character.toString())
                    if(redValue.inputType == InputType.TYPE_CLASS_TEXT) redValue.inputType = InputType.TYPE_CLASS_NUMBER

                    else(redSlider.setProgress(number))
                }
                catch(e : NumberFormatException){return}
            }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {
                }
            }
        )

        greenSlider.setOnSeekBarChangeListener(sliderListener)
        greenValue.setOnFocusChangeListener(focusTextListener)
        greenValue.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(character: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try
                {
                    var number : Int = Integer.parseInt(character.toString())
                    if(greenValue.inputType == InputType.TYPE_CLASS_TEXT) greenValue.inputType = InputType.TYPE_CLASS_NUMBER

                    else(greenSlider.setProgress(number))
                }
                catch(e : NumberFormatException){return}
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        }
        )

        blueSlider.setOnSeekBarChangeListener(sliderListener)
        blueValue.setOnFocusChangeListener(focusTextListener)
        blueValue.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(character: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try
                {
                    var number : Int = Integer.parseInt(character.toString())
                    if(blueValue.inputType == InputType.TYPE_CLASS_TEXT) blueValue.inputType = InputType.TYPE_CLASS_NUMBER

                    else(blueSlider.setProgress(number))
                }
                catch(e : NumberFormatException){return}
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        }
        )
    }
}

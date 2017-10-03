package com.example.natha.assignment2

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

import kotlinx.android.synthetic.main.view_titled_image.view.imageView as importedImageView
import kotlinx.android.synthetic.main.view_titled_image.view.titleTextView as importedTitledTextView

/**
 * Created by Natha on 10/2/2017.
 */
class TitledImageView : FrameLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    var image: Drawable
        get() = imageView.drawable
        set(newimage){imageView.setImageDrawable(newimage)}

    var title: String
        get() = titleTextView.text.toString()
        set(newTitle) { titleTextView.text = newTitle}

    private var imageView: ImageView = ImageView(context)
    private var titleTextView = TextView(context)

    override fun onFinishInflate() {
        super.onFinishInflate()
        imageView = importedImageView
        titleTextView = importedTitledTextView
    }
}
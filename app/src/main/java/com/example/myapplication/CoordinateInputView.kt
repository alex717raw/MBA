package com.example.myapplication

import android.content.Context
import android.text.InputType
import android.view.Gravity
import android.widget.FrameLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * UI компонент для ввода координат
 */
class CoordinateInputView(private val context: Context) {
    
    private val coordInputLayout: TextInputLayout
    private val coordEditText: TextInputEditText
    
    init {
        // TextInputEditText
        coordEditText = TextInputEditText(context).apply {
            hint = "lat, lng"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            setSingleLine(true)
        }
        
        // TextInputLayout
        coordInputLayout = TextInputLayout(
            context, 
            null, 
            com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox
        ).apply {
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            setPadding(0, 0, 0, 0)
            addView(coordEditText)
        }
    }
    
    /**
     * Добавление в контейнер
     */
    fun addToContainer(container: FrameLayout) {
        val editParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = (context.resources.displayMetrics.heightPixels * 0.05).toInt()
            leftMargin = (context.resources.displayMetrics.widthPixels * 0.05).toInt()
            rightMargin = (context.resources.displayMetrics.widthPixels * 0.05).toInt()
            gravity = Gravity.TOP
        }
        container.addView(coordInputLayout, editParams)
    }
    
    /**
     * Получение введенного текста
     */
    fun getText(): String = coordEditText.text.toString()
    
    /**
     * Очистка поля ввода
     */
    fun clearText() {
        coordEditText.setText("")
    }
    
    /**
     * Получение layout для внешнего использования
     */
    fun getLayout(): TextInputLayout = coordInputLayout
} 
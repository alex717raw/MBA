package com.example.myapplication

import android.content.Context
import android.content.res.ColorStateList
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton

/**
 * UI компонент для кнопок навигации
 */
class NavigationButtons(private val context: Context) {
    
    private val goButton: MaterialButton
    private val refreshButton: MaterialButton
    private val cancelButton: MaterialButton
    private val buttonLayout: LinearLayout
    
    init {
        // Создание кнопок
        goButton = createButton("Go")
        refreshButton = createButton("Refresh")
        cancelButton = createButton("Cancel")
        
        // Создание контейнера для кнопок
        buttonLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        
        // Добавление кнопок в контейнер
        setupButtons()
    }
    
    /**
     * Создание кнопки с общим стилем
     */
    private fun createButton(text: String): MaterialButton {
        return MaterialButton(
            context, 
            null, 
            com.google.android.material.R.attr.materialButtonOutlinedStyle
        ).apply {
            this.text = text
            backgroundTintList = ColorStateList.valueOf(0xFFF79E05.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            cornerRadius = 32
        }
    }
    
    /**
     * Настройка расположения кнопок
     */
    private fun setupButtons() {
        val btnMargin = (context.resources.displayMetrics.heightPixels * 0.01).toInt()
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { 
            bottomMargin = btnMargin 
        }
        
        buttonLayout.addView(goButton, lp)
        buttonLayout.addView(refreshButton, lp)
        buttonLayout.addView(cancelButton, lp)
    }
    
    /**
     * Добавление в контейнер
     */
    fun addToContainer(container: FrameLayout) {
        val buttonParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            val margin = (context.resources.displayMetrics.widthPixels * 0.05).toInt()
            bottomMargin = margin
            rightMargin = margin
        }
        container.addView(buttonLayout, buttonParams)
    }
    
    /**
     * Установка обработчиков нажатий
     */
    fun setOnClickListeners(
        onGoClick: () -> Unit,
        onRefreshClick: () -> Unit,
        onCancelClick: () -> Unit
    ) {
        goButton.setOnClickListener { onGoClick() }
        refreshButton.setOnClickListener { onRefreshClick() }
        cancelButton.setOnClickListener { onCancelClick() }
    }
    
    /**
     * Получение отдельных кнопок для внешнего использования
     */
    fun getGoButton(): MaterialButton = goButton
    fun getRefreshButton(): MaterialButton = refreshButton
    fun getCancelButton(): MaterialButton = cancelButton
} 
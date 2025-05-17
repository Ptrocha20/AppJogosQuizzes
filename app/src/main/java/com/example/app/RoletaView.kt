package com.example.app

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class RoletaView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        textSize = 24f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val categories = listOf("Phishing", "Senhas", "Malware", "Redes Seguras", "Backup")

    private val sectorColors = listOf(
        Color.parseColor("#E53935"), // Vermelho
        Color.parseColor("#3949AB"), // Azul
        Color.parseColor("#43A047"), // Verde
        Color.parseColor("#FDD835"), // Amarelo
        Color.parseColor("#00ACC1")  // Ciano
    )

    private var rotation = 0f

    override fun setRotation(angle: Float) {
        rotation = angle
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(centerX, centerY) - 16f

        val rect = RectF(
            centerX - radius, centerY - radius,
            centerX + radius, centerY + radius
        )

        canvas.save()
        canvas.rotate(rotation, centerX, centerY)

        val sectors = categories.size
        val sectorAngle = 360f / sectors

        for (i in 0 until sectors) {
            // Desenhar o setor
            paint.color = sectorColors[i % sectorColors.size]
            canvas.drawArc(
                rect,
                i * sectorAngle,
                sectorAngle,
                true,
                paint
            )

            // Desenhar linha separadora
            paint.color = Color.WHITE
            paint.strokeWidth = 3f
            val angle = Math.toRadians((i * sectorAngle).toDouble())
            val x = centerX + cos(angle) * radius
            val y = centerY + sin(angle) * radius
            canvas.drawLine(centerX, centerY, x.toFloat(), y.toFloat(), paint)

            // Desenhar texto da categoria
            drawCategoryText(canvas, categories[i], centerX, centerY, radius, i * sectorAngle, sectorAngle)
        }

        // Desenhar círculo no centro
        paint.color = Color.WHITE
        canvas.drawCircle(centerX, centerY, radius / 6, paint)

        // Desenhar borda do círculo central
        paint.color = Color.parseColor("#193180")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        canvas.drawCircle(centerX, centerY, radius / 6, paint)
        paint.style = Paint.Style.FILL

        // Adicionar borda externa à roleta
        paint.color = Color.parseColor("#193180")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f
        canvas.drawCircle(centerX, centerY, radius, paint)
        paint.style = Paint.Style.FILL

        canvas.restore()
    }

    private fun drawCategoryText(canvas: Canvas, text: String, centerX: Float, centerY: Float,
                                 radius: Float, startAngle: Float, sweepAngle: Float) {
        val path = Path()
        val angle = startAngle + (sweepAngle / 2)

        // Ajustar raio para posicionar o texto no meio do setor
        val textRadius = radius * 0.7f

        // Calcular posição do texto
        val angleInRadians = Math.toRadians(angle.toDouble())
        val x = centerX + cos(angleInRadians) * textRadius
        val y = centerY + sin(angleInRadians) * textRadius

        // Desenhar o texto
        canvas.save()
        canvas.rotate(angle + 90, x.toFloat(), y.toFloat())
        canvas.drawText(text, x.toFloat(), y.toFloat(), textPaint)
        canvas.restore()
    }
}

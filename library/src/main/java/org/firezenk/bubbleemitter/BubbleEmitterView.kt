package org.firezenk.bubbleemitter

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color.rgb
import android.graphics.Paint
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import java.util.UUID
import kotlin.math.abs
import kotlin.random.Random

class BubbleEmitterView @JvmOverloads constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    companion object {
        private const val BUBBLE_LIMIT = 25
        private const val BASE_ALPHA = 255
        private const val NO_VALUE = -1F
    }

    class Bubble(val uuid: UUID, var radius: Float, var x: Float = NO_VALUE, var y: Float = NO_VALUE,
                 var alpha: Int = BASE_ALPHA, var alive: Boolean = true, var animating: Boolean = false)

    private val pushHandler = Handler()
    private var bubbles: MutableList<Bubble> = mutableListOf()

    private var emissionDelayMillis:Long = 10L * bubbles.size
    private var canExplode: Boolean = true

    private val paintStroke = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.ghostWhite)
        strokeWidth = 2F
        style = Paint.Style.STROKE
    }

    private val paintFill = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.whiteSmoke)
        style = Paint.Style.FILL
    }

    private val paintGloss = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context, android.R.color.white)
        style = Paint.Style.FILL
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)

        bubbles = bubbles.filter { it.alive }.toMutableList()

        bubbles.forEach {
            val diameter = (it.radius * 2).toInt()
            if (it.x == -1F) {
                it.x = Random.nextInt(0 + diameter, width - diameter).toFloat()
            }

            paintStroke.alpha = it.alpha
            paintFill.alpha = it.alpha
            paintGloss.alpha = it.alpha

            c.drawCircle(it.x, it.y, it.radius, paintStroke)
            c.drawCircle(it.x, it.y, it.radius, paintFill)
            c.drawCircle(it.x + it.radius / 2.5F, it.y - it.radius / 2.5F, it.radius / 4, paintGloss)

            if (!it.animating) {
                it.animating = true
                moveAnimation(it.uuid, it.radius).start()
                if (canExplode) {
                    explodeAnimation(it.uuid, it.radius).start()
                }
                fadeOutAnimation(it.uuid, it.radius).start()
            }
        }
    }

    fun emitBubble(strength: Int) {
        if (bubbles.size >= BUBBLE_LIMIT) {
            return
        }

        val uuid: UUID = UUID.randomUUID()
        val radius: Float = abs(strength) / 4F
        val bubble = Bubble(uuid, radius)

        pushHandler.postDelayed({
            bubbles.add(bubble)
        }, emissionDelayMillis)

        invalidate()
    }

    fun setColors(@ColorInt stroke: Int = rgb(249,249,249),
                  @ColorInt fill: Int = rgb(236,236,236),
                  @ColorInt gloss: Int = rgb(255,255,255)) {
        paintStroke.color = stroke
        paintFill.color = fill
        paintGloss.color = gloss
    }

    fun setColorResources(@ColorRes stroke: Int = R.color.ghostWhite,
                          @ColorRes fill: Int = R.color.whiteSmoke,
                          @ColorRes gloss: Int = android.R.color.white) {
        paintStroke.color = ContextCompat.getColor(context, stroke)
        paintFill.color = ContextCompat.getColor(context, fill)
        paintGloss.color = ContextCompat.getColor(context, gloss)
    }

    fun setEmissionDelay(delayMillis: Long = 10L * bubbles.size) {
        emissionDelayMillis = delayMillis
    }

    fun canExplode(boolean: Boolean = true) {
        canExplode = boolean
    }

    private fun moveAnimation(uuid: UUID, radius: Float): ValueAnimator {
        val animator: ValueAnimator = ValueAnimator.ofFloat(height.toFloat(), height / 2F - radius * 10)
        with(animator) {
            addUpdateListener { animation ->
                bubbles.firstOrNull { it.uuid == uuid }?.y = animation.animatedValue as Float
                invalidate()
            }
            duration = 2000L + 100L * radius.toLong()
            interpolator = LinearInterpolator()
        }
        return animator
    }

    private fun fadeOutAnimation(uuid: UUID, radius: Float): ValueAnimator {
        val animator: ValueAnimator = ValueAnimator.ofInt(BASE_ALPHA, 0)
        with(animator) {
            addUpdateListener { animation ->
                bubbles.firstOrNull { it.uuid == uuid }?.alpha = animation.animatedValue as Int
            }
            doOnEnd {
                bubbles.firstOrNull { it.uuid == uuid }?.alive = false
                invalidate()
            }
            duration = 200L
            startDelay = 1000L + 100L * radius.toLong()
            interpolator = LinearInterpolator()
        }
        return animator
    }

    private fun explodeAnimation(uuid: UUID, radius: Float): ValueAnimator {
        val animator: ValueAnimator = ValueAnimator.ofFloat(radius, radius * 2)
        with(animator) {
            addUpdateListener { animation ->
                bubbles.firstOrNull { it.uuid == uuid }?.radius = animation.animatedValue as Float
            }
            duration = 300L
            startDelay = 1000L + 100L * radius.toLong()
            interpolator = LinearInterpolator()
        }
        return animator
    }
}
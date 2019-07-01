package org.firezenk.bubbleemitter

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.main.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        emitBubbles()
    }

    private fun emitBubbles() {
        Handler().postDelayed({
            val size = Random.nextInt(20, 80)
            bubbleEmitter.emitBubble(size)
            emitBubbles()
        }, Random.nextLong(100, 500))
    }
}
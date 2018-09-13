package kobayashi.taku.taptappun.net.datasender

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet

class DrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        invalidate()
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    }

    fun release() {
    }
}

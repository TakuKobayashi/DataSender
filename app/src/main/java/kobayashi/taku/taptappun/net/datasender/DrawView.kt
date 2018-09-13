package kobayashi.taku.taptappun.net.datasender

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log

class DrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var mImage: Bitmap? = null;
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if(mImage != null){
            canvas.drawBitmap(mImage!!, 0f, 0f, Paint());
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        invalidate()
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    }

    fun setImage(image: Bitmap?){
        Log.d(Config.TAG, "" + image);
        mImage = image;
        invalidate()
    }

    fun getImage(): Bitmap{
        return mImage!!;
    }

    fun release() {
        if(mImage != null){
            mImage!!.recycle();
            mImage = null;
        }
    }
}

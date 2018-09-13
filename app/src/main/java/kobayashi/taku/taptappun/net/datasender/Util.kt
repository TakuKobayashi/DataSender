package kobayashi.taku.taptappun.net.datasender

import android.view.Display
import android.os.Build
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import java.io.FileNotFoundException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object Util {

    //ImageViewを使用したときのメモリリーク対策
    fun releaseImageView(imageView: ImageView?) {
        if (imageView != null) {
            val bitmapDrawable = imageView!!.getDrawable() as BitmapDrawable
            if (bitmapDrawable != null) {
                bitmapDrawable.callback = null
            }
            imageView!!.setImageBitmap(null)
        }
    }

    //WebViewを使用したときのメモリリーク対策
    fun releaseWebView(webview: WebView?) {
        var webview = webview
        webview!!.stopLoading()
        webview!!.setWebChromeClient(null)
        webview!!.setWebViewClient(null)
        webview!!.destroy()
        webview = null
    }

    fun isAssetFileIsDirectory(context: Context, path: String): Boolean {
        val mngr = context.getAssets()
        var isDirectory = false
        try {
            if (mngr.list(path).size > 0) { //子が含まれる場合はディレクトリ
                isDirectory = true
            } else {
                // オープン可能かチェック
                mngr.open(path)
            }
        } catch (fnfe: FileNotFoundException) {
            isDirectory = true
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return isDirectory
    }

    fun loadImageFromAsset(context: Context, path: String): Bitmap? {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val mngr = context.getAssets()
        try {
            val `is` = mngr.open(path)
            return BitmapFactory.decodeStream(`is`, null, options)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    fun loadFilePathes(context: Context, path: String): Array<String> {
        val mngr = context.getAssets()
        try {
            return mngr.list(path)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return arrayOf()
    }

    fun getDisplaySize(activity: Activity): Rect {
        val display = activity.windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        return Rect(0, 0, point.x, point.y)
    }

    fun getRealDisplaySize(activity: Activity): Rect {

        val display = activity.windowManager.defaultDisplay
        val point = Point(0, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // Android 4.2~
            display.getRealSize(point)
            return Rect(0, 0, point.x, point.y)

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            // Android 3.2~
            try {
                val getRawWidth = Display::class.java.getMethod("getRawWidth")
                val getRawHeight = Display::class.java.getMethod("getRawHeight")
                val width = getRawWidth.invoke(display) as Int
                val height = getRawHeight.invoke(display) as Int
                point.set(width, height)
                return Rect(0, 0, point.x, point.y)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        return Rect(0, 0, point.x, point.y)
    }

    fun bitmapScaled(orgImage: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(orgImage, width, height, true)
    }

    fun horizontalMirror(orgImage: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.preScale(-1f, 1f)
        return Bitmap.createBitmap(orgImage, 0, 0, orgImage.width, orgImage.height, matrix, false)
    }

    fun getViewCapture(view: View): Bitmap {
        view.setDrawingCacheEnabled(true)

        // Viewのキャッシュを取得
        val cache = view.getDrawingCache()
        val screenShot = Bitmap.createBitmap(cache)
        view.setDrawingCacheEnabled(false)
        return screenShot
    }

    fun getBitmapFromURL(urlString: String): Bitmap? {
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.setDoInput(true)
            connection.connect()
            val input = connection.getInputStream()
            return BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

    }
}

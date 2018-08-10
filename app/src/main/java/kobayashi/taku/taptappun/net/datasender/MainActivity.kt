package kobayashi.taku.taptappun.net.datasender

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example of a call to a native method
        sample_text.text = stringFromJNI();

        val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Bluetooth非搭載の場合はアプリを終了させる
        if (adapter == null) {
            finish();
        }
        // Bluetoothが有効になっていなかったらBluetoothを有効にするかどうか聞くようにする
        if (!adapter.isEnabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Bluetoothが有効になった場合のみアプリを継続させる
        if(requestCode == REQUEST_ENABLE_BT){
            if(resultCode != RESULT_OK){
                finish();
            }
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        const val REQUEST_ENABLE_BT:Int = 1;
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}

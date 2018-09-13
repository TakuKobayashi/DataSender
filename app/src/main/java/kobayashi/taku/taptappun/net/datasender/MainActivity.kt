package kobayashi.taku.taptappun.net.datasender

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private lateinit var mDrawView: DrawView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_view);

        val connectionDeviceNameTextView = findViewById<TextView>(R.id.connection_device_name);
        val connectDeviceButton = findViewById<Button>(R.id.connect_device_button);
        connectDeviceButton.setOnClickListener({
           val intent = Intent(this, BluetoothConnectionActivity::class.java);
           startActivity(intent);
        });

        mDrawView = findViewById<DrawView>(R.id.main_draw_view);

        val gallaryButton = findViewById<Button>(R.id.gallary_button);
        val gallaryClearButton = findViewById<Button>(R.id.gallary_clear_button);
        val drawClearButton = findViewById<Button>(R.id.draw_clear_button);


        val sendButton = findViewById<Button>(R.id.send_message_button);
        sendButton.setOnClickListener({
            val editText = findViewById<EditText>(R.id.send_message_edittext);
            BluetoothConnectionThreadManager.
                    getSocketThreadPairs().
                    forEach{(socket, connectionThread) ->
                        if(connectionThread != null){
                            connectionThread.sendData(editText.editableText.toString().toByteArray(Charsets.UTF_8));
                        }
            };
            editText.setText("");
        });

    }

    override fun onDestroy() {
        super.onDestroy();
    }
}
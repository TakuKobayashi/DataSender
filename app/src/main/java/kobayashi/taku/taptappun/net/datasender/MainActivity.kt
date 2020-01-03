package kobayashi.taku.taptappun.net.datasender

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.*

class MainActivity : AppCompatActivity() {
    private lateinit var mDrawView: DrawView;
    private lateinit var mConnectedDeviceAdapter: BluetoothDeviceAdapter;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_view);

        /*
        val connectionDeviceNameTextView = findViewById<TextView>(R.id.connection_device_name);
        val connectDeviceButton = findViewById<Button>(R.id.connect_device_button);
        connectDeviceButton.setOnClickListener({
           val intent = Intent(this, BluetoothConnectionActivity::class.java);
           startActivity(intent);
        });
        */
        val headerToolbar = findViewById<Toolbar>(R.id.header_toolbar);

        val adapter = BluetoothDeviceAdapter(this);
        val spinner = headerToolbar.findViewById<Spinner>(R.id.header_connected_device_spinner);

        setSupportActionBar(headerToolbar)

        mDrawView = findViewById<DrawView>(R.id.main_draw_view);
        //mDrawView.setImage(Util.loadImageFromAsset(this, "twitter_sample.jpg"));

        val gallaryButton = findViewById<Button>(R.id.gallary_button);
        val gallaryClearButton = findViewById<Button>(R.id.gallary_clear_button);
        val drawClearButton = findViewById<Button>(R.id.draw_clear_button);


        val sendButton = findViewById<Button>(R.id.send_button);
        sendButton.setOnClickListener({
            var image = mDrawView.getImage();
            val bytes = Util.imageConvertToBytearray(image);
            val rgbbytes = Util.argbTorgbBytearray(bytes);
            BluetoothConnectionThreadManager.
                    getSocketThreadPairs().
                    forEach{(socket, connectionThread) ->
                        if(connectionThread != null){
                            connectionThread.sendData(rgbbytes);
                        }
                    };
        });

        val sendMessageButton = findViewById<Button>(R.id.send_message_button);
        sendMessageButton.setOnClickListener({
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

    private fun UpdateConnectedDeviceAdapter(){
        BluetoothConnectionThreadManager.getSocketThreadPairs().
                forEach{(device, connectionThread) ->
                    if(connectionThread != null){
                        connectionThread.close();
                    }
                };
        mConnectedDeviceAdapter
    }

    override fun onDestroy() {
        super.onDestroy();
        mDrawView.release();
        BluetoothConnectionThreadManager.
                getSocketThreadPairs().
                forEach{(device, connectionThread) ->
                    if(connectionThread != null){
                        connectionThread.close();
                    }
                };
        BluetoothConnectionThreadManager.clear();
    }
}
package kobayashi.taku.taptappun.net.datasender

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.util.Log
import android.bluetooth.BluetoothDevice
import android.content.IntentFilter
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.ProgressBar

class MainActivity : Activity() {
    private val mReceiver: BluetoothReceiver = BluetoothReceiver();
    private var mBluetoothAdapter: BluetoothAdapter? = null;
    private lateinit var mScanProgressBar: ProgressBar;
    private lateinit var mDeviceListAdapter: BluetoothScanDeviceAdapter;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sample_text.text = stringFromJNI();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Bluetooth非搭載の場合はアプリを終了させる
        if (mBluetoothAdapter == null) {
            finish();
        }
        // Bluetoothが有効になっていなかったらBluetoothを有効にするかどうか聞くようにする
        if (!(mBluetoothAdapter!!.isEnabled())) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        val filter = IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(mReceiver, filter)

        mDeviceListAdapter = BluetoothScanDeviceAdapter(this);

        mScanProgressBar = findViewById(R.id.device_list_progressbar);
        mScanProgressBar.visibility = View.INVISIBLE;

        val scanButton = findViewById<Button>(R.id.bluetooth_scan_button);
        scanButton.setOnClickListener({
            Log.d("datasender", "click");
            if (mBluetoothAdapter!!.isDiscovering()) {
                //検索中の場合は検出をキャンセルする
                mBluetoothAdapter!!.cancelDiscovery();
            }
            //デバイスを検索する
            //一定時間の間検出を行う
            mBluetoothAdapter!!.startDiscovery();
        });

        val scanableButton = findViewById<Button>(R.id.bluetooth_scanable_button);
        scanableButton.setOnClickListener({
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            startActivity(discoverableIntent)
        })
        if (mBluetoothAdapter!!.getScanMode() !== BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            scanableButton.visibility = View.VISIBLE
        } else {
            scanableButton.visibility = View.INVISIBLE
        }
        mReceiver.setOnReceiveCallback(object : BluetoothReceiver.ReceiveCallback {
            override fun onDiscoverFinished(foundDevices: HashSet<BluetoothDevice>) {
                mScanProgressBar.visibility = View.INVISIBLE;
            }

            override fun onDiscoveryStart() {
                mScanProgressBar.visibility = View.VISIBLE;
                mDeviceListAdapter.clearList();
            }

            override fun onDeviceFound(device: BluetoothDevice) {
                mDeviceListAdapter.addUniqDevice(device);
            }

            override fun onDeviceChanged(device: BluetoothDevice) {
                mDeviceListAdapter.addUniqDevice(device);
            }
        });

        val bluetoothScanListView = findViewById<ListView>(R.id.bluetooth_scanned_device_listview);
        bluetoothScanListView.adapter = mDeviceListAdapter;
    }

    override fun onDestroy() {
        super.onDestroy();
        if (mBluetoothAdapter != null && mBluetoothAdapter!!.isDiscovering()) {
            //検索中の場合は検出をキャンセルする
            mBluetoothAdapter!!.cancelDiscovery();
        }
        mDeviceListAdapter.clearList();
        unregisterReceiver(mReceiver);
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

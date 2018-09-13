package kobayashi.taku.taptappun.net.datasender

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.IntentFilter
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private val mReceiver: BluetoothReceiver = BluetoothReceiver();
    private var mBluetoothAdapter: BluetoothAdapter? = null;
    private lateinit var mScanProgressBar: ProgressBar;
    private lateinit var mDeviceListAdapter: BluetoothScanDeviceAdapter;
    private lateinit var mBluetoothServerThread: BluetoothServerThread;
    private lateinit var mReceiveTextView: TextView;
    private var mBluetoothClientThreadDeviceMap: HashMap<BluetoothDevice, BluetoothClientThread> = HashMap<BluetoothDevice, BluetoothClientThread>();
    private var mBluetoothSocketConnectionThread: HashMap<BluetoothSocket, BluetoothConnectionThread> = HashMap<BluetoothSocket, BluetoothConnectionThread>();
    private var mReceiveMessages = ArrayList<String>();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        mBluetoothServerThread = BluetoothServerThread(mBluetoothAdapter!!);
        mBluetoothServerThread.addOnConnectionCallback(object : BluetoothServerThread.ConnectionCallback{
            override fun onConnectionSuccess(connectionSocket: BluetoothSocket, connectionThread: BluetoothConnectionThread) {
                Log.d(Config.TAG, "ConnectionServerSuccess:" + connectionSocket);
                setupConnectionThread(connectionSocket, connectionThread);
            }

            override fun onClose() {
                Log.d(Config.TAG, "Close");
            }
        })
        mBluetoothServerThread.startWaitConnectionServer();

        mDeviceListAdapter = BluetoothScanDeviceAdapter(this);

        mScanProgressBar = findViewById<ProgressBar>(R.id.device_list_progressbar);
        mScanProgressBar.visibility = View.INVISIBLE;

        mReceiveTextView = findViewById<TextView>(R.id.receive_message_textview);

        val scanButton = findViewById<Button>(R.id.bluetooth_scan_button);
        scanButton.setOnClickListener({
            discoverBluetoothDevice();
        });

        val scanableButton = findViewById<Button>(R.id.bluetooth_scanable_button);
        scanableButton.setOnClickListener({
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
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

        val bluetoothBoundedListView = findViewById<ListView>(R.id.bounded_device_listview);
        val connectedListAdapter = BluetoothScanDeviceAdapter(this);
        bluetoothBoundedListView.adapter = connectedListAdapter;
        for(device in mBluetoothAdapter!!.bondedDevices){
            connectedListAdapter.addUniqDevice(device);
        }
        bluetoothBoundedListView.setOnItemClickListener({adapterView: AdapterView<*>, view1: View, position: Int, l: Long ->
            val device = mDeviceListAdapter.getDevice(position);
            connectAndSetDevice(device);
        });

        val bluetoothScanListView = findViewById<ListView>(R.id.bluetooth_scanned_device_listview);
        bluetoothScanListView.adapter = mDeviceListAdapter;
        bluetoothScanListView.setOnItemClickListener({adapterView: AdapterView<*>, view1: View, position: Int, l: Long ->
            val device = mDeviceListAdapter.getDevice(position);
            connectAndSetDevice(device);
        });

        val sendButton = findViewById<Button>(R.id.send_message_button);
        sendButton.setOnClickListener({
            val editText = findViewById<EditText>(R.id.send_message_edittext);
            mBluetoothSocketConnectionThread.forEach{(device, connectionThread) ->
                if(connectionThread != null){
                    connectionThread.sendData(editText.editableText.toString().toByteArray(Charsets.UTF_8));
                }
            };

            editText.editableText.toString();
        });

        discoverBluetoothDevice();
    }

    private fun connectAndSetDevice(device: BluetoothDevice){
        val clientThread = BluetoothClientThread(device);
        clientThread.addOnClientCallback(object : BluetoothClientThread.ConnectionCallback{
            override fun onConnectionSuccess(device: BluetoothDevice, connectionSocket: BluetoothSocket, connectionThread: BluetoothConnectionThread) {
                Log.d(Config.TAG, "ConnectionClientSuccess:" + connectionSocket);
                setupConnectionThread(connectionSocket, connectionThread);
            }

            override fun onClose(device: BluetoothDevice, connectionSocket: BluetoothSocket) {
                mBluetoothClientThreadDeviceMap.remove(device);
                mBluetoothSocketConnectionThread.remove(connectionSocket);
                Log.d(Config.TAG, "close");
            }
        });
        clientThread.startConnection();
        mBluetoothClientThreadDeviceMap.put(device, clientThread);
    }

    private fun setupConnectionThread(connectionSocket: BluetoothSocket, connectionThread: BluetoothConnectionThread){
        connectionThread.addOnSendReceivedCallback(object : BluetoothConnectionThread.SendReceivedCallback{
            override fun onReceive(bytes: Int, data: ByteArray) {
                mReceiveMessages.add(data.toString(Charsets.UTF_8));
                runOnUiThread({
                    mReceiveTextView.setText(mReceiveMessages.joinToString("\n"));
                });
                Log.d(Config.TAG, "socketReceive:" + data.toString(Charsets.UTF_8));
            }

            override fun onSend(data: ByteArray) {
                Log.d(Config.TAG, "socketSend:" + data.toString(Charsets.UTF_8));
            }

            override fun onClose(connectionSocket: BluetoothSocket) {
                mBluetoothSocketConnectionThread.remove(connectionSocket);
                Log.d(Config.TAG, "SocketClose");
            }
        });
        mBluetoothSocketConnectionThread.put(connectionSocket, connectionThread);
    }

    private fun discoverBluetoothDevice(){
        val filter = IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(mReceiver, filter)

        if (mBluetoothAdapter!!.isDiscovering()) {
            //検索中の場合は検出をキャンセルする
            mBluetoothAdapter!!.cancelDiscovery();
        }
        //デバイスを検索する
        //一定時間の間検出を行う
        mBluetoothAdapter!!.startDiscovery();
    }

    override fun onDestroy() {
        super.onDestroy();
        if (mBluetoothAdapter != null && mBluetoothAdapter!!.isDiscovering()) {
            //検索中の場合は検出をキャンセルする
            mBluetoothAdapter!!.cancelDiscovery();
        }
        mBluetoothSocketConnectionThread.forEach{(device, connectionThread) ->
            if(connectionThread != null){
                connectionThread.close();
            }
        };
        mBluetoothClientThreadDeviceMap.forEach{(device, clientThread) ->
            clientThread.close();
        };
        mBluetoothSocketConnectionThread.clear();
        mBluetoothClientThreadDeviceMap.clear();
        mDeviceListAdapter.clearList();
        mBluetoothServerThread.closeServerSocket();
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

    external fun stringFromJNI(): String

    companion object {
        const val REQUEST_ENABLE_BT:Int = 1;
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}

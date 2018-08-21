package kobayashi.taku.taptappun.net.datasender

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BluetoothReceiver : BroadcastReceiver(){
    private var mReceiveCallback: ReceiveCallback? = null;
    private var mDeviceList: HashSet<BluetoothDevice> = HashSet<BluetoothDevice>();

    override fun onReceive(context: Context?, intent: Intent) {
        val action = intent.getAction();
        Log.d(Config.TAG, action);
        if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
            if(mReceiveCallback != null) mReceiveCallback!!.onDiscoveryStart();
            Log.d(Config.TAG, "start");
        }
        if(BluetoothDevice.ACTION_FOUND.equals(action)){
            //デバイスが検出された
            val foundDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE);
            mDeviceList.add(foundDevice);
            if(mReceiveCallback != null) mReceiveCallback!!.onDeviceFound(foundDevice);
            Log.d(Config.TAG, "found device:" + foundDevice.getName() + " address:" + foundDevice.getAddress());
        }
        if(BluetoothDevice.ACTION_NAME_CHANGED.equals(action)){
            //名前が検出された
            val foundDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE);
            mDeviceList.add(foundDevice);
            if(mReceiveCallback != null) mReceiveCallback!!.onDeviceChanged(foundDevice);
            Log.d(Config.TAG, "changed device:" + foundDevice.getName() + " address:" + foundDevice.getAddress());
        }
        if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
            if(mReceiveCallback != null) mReceiveCallback!!.onDiscoverFinished(mDeviceList);
            Log.d(Config.TAG, "finish");
        }
    }

    fun setOnReceiveCallback(callback: ReceiveCallback) {
        mReceiveCallback = callback;
    }

    interface ReceiveCallback {
        fun onDiscoveryStart()
        fun onDeviceFound(device: BluetoothDevice)
        fun onDeviceChanged(device: BluetoothDevice)
        fun onDiscoverFinished(foundDevices: HashSet<BluetoothDevice>)
    }
}
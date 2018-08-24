package kobayashi.taku.taptappun.net.datasender

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


// https://woshidan.hatenablog.com/entry/2015/10/30/083000
public class BluetoothClientThread(device: BluetoothDevice){
    private val mBluetoothDevice = device;
    private var mSocket: BluetoothSocket? = null
    private var mConnectionThread: Thread;
    private val sppuuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val mConnectionCallbackList: ArrayList<ConnectionCallback> = ArrayList<ConnectionCallback>();

    init{
        mConnectionThread = Thread(Runnable {
            connectionRoutine();
        });
    }

    private fun connectionRoutine(){
        try {
            mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(sppuuid);
            mConnectionCallbackList.forEach {callback ->
                callback.onTryConnection(mBluetoothDevice);
            }
            if(mSocket == null){
                Log.e(Config.TAG, "connection null");
                return;
            }
            mSocket!!.connect();
            mConnectionCallbackList.forEach {callback ->
                callback.onConnectionSuccess(mBluetoothDevice);
            }
        } catch (e: IOException) {
            close();
            Log.e(Config.TAG, "failed", e);
            return;
        }
    }

    fun close(){
        if(mSocket != null){
            try {
                mSocket!!.close()
            } catch (e2: IOException) {
                Log.e(Config.TAG, "unable to close() socket during connection failure", e2)
            }
        }
        mConnectionCallbackList.forEach {callback ->
            callback.onClose(mBluetoothDevice);
        }
    }

    fun startConnection() {
        mConnectionThread.start()
    }

    fun addOnClientCallback(callback: ConnectionCallback) {
        mConnectionCallbackList.add(callback);
    }

    fun removeOnClientCallback(callback: ConnectionCallback) {
        mConnectionCallbackList.remove(callback);
    }

    interface ConnectionCallback {
        fun onTryConnection(device: BluetoothDevice);
        fun onConnectionSuccess(device: BluetoothDevice);
        fun onClose(device: BluetoothDevice);
    }
}
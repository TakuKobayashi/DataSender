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
    private val mConnectionCallbackList: ArrayList<ConnectionCallback> = ArrayList<ConnectionCallback>();

    init{
        mConnectionThread = Thread(Runnable {
            var clientSocket = connection();
            if(clientSocket != null){
                var connectionThread = BluetoothConnectionThread(clientSocket);
                connectionThread.startThread();
                mConnectionCallbackList.forEach {callback ->
                    callback.onConnectionSuccess(mBluetoothDevice, clientSocket, connectionThread);
                }
            }
        });
    }

    private fun connection(): BluetoothSocket?{
        try {
            mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(Config.BLUETOOTH_SPPUUID);
            if(mSocket == null){
                Log.e(Config.TAG, "connection null");
                return null;
            }
            mSocket!!.connect();
            Log.d(Config.TAG, "connect!!");
            return mSocket;
        } catch (e: IOException) {
            close();
            Log.e(Config.TAG, "failed", e);
            mSocket = null;
            return null;
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
        fun onConnectionSuccess(device: BluetoothDevice, connectionSocket: BluetoothSocket, connectionThread: BluetoothConnectionThread);
        fun onClose(device: BluetoothDevice);
    }
}
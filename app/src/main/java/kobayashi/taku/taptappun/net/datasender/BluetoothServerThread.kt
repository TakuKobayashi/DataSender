package kobayashi.taku.taptappun.net.datasender

import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.util.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket

// https://woshidan.hatenablog.com/entry/2015/10/30/083000
public class BluetoothServerThread(adapter: BluetoothAdapter){
    private var mServerSocket: BluetoothServerSocket? = null
    private var mIsConnectionServerActive = false;
    private var mConnectionWaitServerThread: Thread? = null;
    private val mConnectionCallbackList: ArrayList<ConnectionCallback> = ArrayList<ConnectionCallback>();

    init{
        try {
            mServerSocket = adapter.listenUsingRfcommWithServiceRecord(adapter.name, Config.BLUETOOTH_SPPUUID)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    public fun closeServerSocket(){
        if(mServerSocket != null){
            try {
                //処理が完了したソケットは閉じる。
                mServerSocket!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mServerSocket = null;
        }
        for(callback in mConnectionCallbackList){
            callback.onClose();
        }
        mIsConnectionServerActive = false;
    }

    public fun startWaitConnectionServer(){
        mConnectionWaitServerThread = Thread(Runnable {
            mIsConnectionServerActive = true;
            while (mIsConnectionServerActive) {
                var socket = acceptConnection();
                if (socket != null) {
                    Log.d(Config.TAG, "connect");
                    var connectionThread = BluetoothConnectionThread(socket);
                    connectionThread.startThread();
                    for(callback in mConnectionCallbackList){
                        callback.onConnectionSuccess(socket, connectionThread);
                    }
                    closeServerSocket();
                }
            }
        });
        mConnectionWaitServerThread!!.start();
    }

    private fun acceptConnection(): BluetoothSocket?{
        try {
            // This is a blocking call and will only return on a
            // successful connection or an exception
            var socket = mServerSocket!!.accept();
            return socket;
        } catch (e: IOException) {
            Log.e(Config.TAG, "accept() failed", e);
            return null;
        }
    }

    fun addOnConnectionCallback(callback: ConnectionCallback) {
        mConnectionCallbackList.add(callback);
    }

    fun removeOnConnectionCallback(callback: ConnectionCallback) {
        mConnectionCallbackList.remove(callback);
    }

    interface ConnectionCallback {
        fun onConnectionSuccess(connectionSocket: BluetoothSocket, connectionThread: BluetoothConnectionThread);
        fun onClose();
    }
}
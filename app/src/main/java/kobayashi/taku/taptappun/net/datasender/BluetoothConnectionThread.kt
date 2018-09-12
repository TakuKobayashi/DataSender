package kobayashi.taku.taptappun.net.datasender

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

// https://woshidan.hatenablog.com/entry/2015/10/30/083000
public class BluetoothConnectionThread(connectionSocket: BluetoothSocket){
    private var mConnectionSocket: BluetoothSocket = connectionSocket;
    private var mSocketInput: InputStream? = null
    private var mSocketOutput: OutputStream? = null
    private var mIsConnectionThreadActive = false;

    private var mConnectionThread: Thread? = null;
    private val mSendReceivedList: ArrayList<SendReceivedCallback> = ArrayList<SendReceivedCallback>();

    init{
        try {
            mSocketInput = connectionSocket.getInputStream()
            mSocketOutput = connectionSocket.getOutputStream()
        } catch (e: IOException) {
            Log.e(Config.TAG, "sockets created fail!!", e)
        }
    }

    public fun startThread(){
        mIsConnectionThreadActive = true;
        mConnectionThread = Thread(Runnable {
            while (mIsConnectionThreadActive) {
                receiveData();
            }
        })
        mConnectionThread!!.start();
    }

    private fun receiveData(){
        val buffer = ByteArray(1024)
        var bytes = 0
        try {
            // Read from the InputStream
            bytes = mSocketInput!!.read(buffer)
        } catch (e: IOException) {
            e.printStackTrace();
            Log.d(Config.TAG, "disconnected:" + e.message);
            close();
        }
        if(bytes > 0) {
            val receivedData = buffer.slice(IntRange(0, bytes - 1));
            for(callback in mSendReceivedList){
                callback.onReceive(bytes, receivedData.toByteArray());
            }
        }

        Log.d(Config.TAG, "bytes:" + bytes)
    }

    public fun sendData(buffer: ByteArray) {
        if (mSocketOutput == null) return
        try {
            mSocketOutput!!.write(buffer);
            for(callback in mSendReceivedList){
                callback.onSend(buffer);
            }
        } catch (e: IOException) {
            Log.d(Config.TAG, "Exception during write" + e.message)
        }
    }

    public fun close(){
        mIsConnectionThreadActive = false
        if (mConnectionSocket != null) {
            try {
                mConnectionSocket.close()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d(Config.TAG, "Exception socket close" + e.message)
            }
        }
        for(callback in mSendReceivedList){
            callback.onClose(mConnectionSocket);
        }
    }

    public fun addOnSendReceivedCallback(callback: SendReceivedCallback) {
        mSendReceivedList.add(callback);
    }

    public fun removeOnSendReceivedCallback(callback: SendReceivedCallback) {
        mSendReceivedList.remove(callback);
    }

    interface SendReceivedCallback {
        fun onReceive(bytes: Int, data: ByteArray)
        fun onSend(data: ByteArray)
        fun onClose(connectionSocket: BluetoothSocket);
    }
}
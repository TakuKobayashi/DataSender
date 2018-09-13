package kobayashi.taku.taptappun.net.datasender

import android.bluetooth.BluetoothSocket
import android.content.Context

object BluetoothConnectionThreadManager{
    private var mBluetoothSocketConnectionThread: HashMap<BluetoothSocket, BluetoothConnectionThread> = HashMap<BluetoothSocket, BluetoothConnectionThread>();

    public fun putSocketConnectionThread(connectionSocket: BluetoothSocket, connectionThread: BluetoothConnectionThread){
        mBluetoothSocketConnectionThread.put(connectionSocket, connectionThread);
    }

    public fun removeSocket(connectionSocket: BluetoothSocket){
        mBluetoothSocketConnectionThread.remove(connectionSocket);
    }

    public fun getSocketThreadPairs(): HashMap<BluetoothSocket, BluetoothConnectionThread>{
        return mBluetoothSocketConnectionThread;
    }

    public fun clear(){
        mBluetoothSocketConnectionThread.clear();
    }

}
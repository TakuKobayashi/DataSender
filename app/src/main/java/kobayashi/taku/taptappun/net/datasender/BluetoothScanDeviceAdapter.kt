package kobayashi.taku.taptappun.net.datasender

import android.widget.BaseAdapter
import android.view.View
import android.widget.TextView
import android.view.ViewGroup
import android.app.Activity
import android.bluetooth.BluetoothDevice

// https://woshidan.hatenablog.com/entry/2015/10/30/083000
public class BluetoothScanDeviceAdapter(activity: Activity) : BaseAdapter(){
    private val mActivity: Activity = activity;
    private var mDevices = ArrayList<BluetoothDevice>()

    fun setDeviceList(devices: List<BluetoothDevice>){
        mDevices = ArrayList(devices);
        this.notifyDataSetChanged();
    }

    fun addUniqDevice(device: BluetoothDevice){
        if(!mDevices.contains(device)){
            mDevices.add(device);
            this.notifyDataSetChanged();
        }
    }

    fun getDevice(position: Int){
        mDevices[position];
    }

    fun clearList(){
        mDevices.clear();
        this.notifyDataSetChanged();
    }

    override fun getCount(): Int {
        return mDevices.size;
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var mainConvertView = convertView
        if (mainConvertView == null) {
            mainConvertView = mActivity.getLayoutInflater().inflate(R.layout.device_list_cell, null)
        }
        val deviceNameTextView = mainConvertView!!.findViewById<TextView>(R.id.device_name_text);
        deviceNameTextView.setText(mDevices[position].name + " " + mDevices[position].address);

        return mainConvertView!!;
    }

}
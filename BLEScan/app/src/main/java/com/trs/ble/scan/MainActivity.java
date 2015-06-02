package com.trs.ble.scan;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;


public class MainActivity extends Activity {

    ListView deviceListView;
    ArrayList<Item> data;
    MyAdapter adapter;

    BluetoothAdapter bluetoothAdapter;
    BluetoothManager bluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initListView();

        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // low api level
            bluetoothAdapter.startLeScan(new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    dealBLE(device, rssi, scanRecord);
                }
            });
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // high api level
            BluetoothLeScanner leScanner = bluetoothAdapter.getBluetoothLeScanner();
            leScanner.startScan(new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    dealBLE(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                    super.onScanResult(callbackType, result);
                }
            });
        }

    }

    /**
     * deal ble info
     *
     * @param device bluetooth device
     * @param rssi   rssi
     * @param data   scan data.
     */
    void dealBLE(BluetoothDevice device, int rssi, byte[] data) {
        if (device == null) {
            return;
        }
        // deal ble info.
        String name = device.getName();
        String mac = device.getAddress();
        boolean exist = false;
        for (int i = 0; i < this.data.size(); i++) {
            if (this.data.get(i).mac.equals(mac)) {
                exist = true;
                break;
            }
        }
        if (!exist) {
            Item item = new Item(name, mac);
            this.data.add(item);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * init listview
     */
    void initListView() {
        deviceListView = (ListView) findViewById(R.id.lv_devices);
        data = new ArrayList<Item>();
        adapter = new MyAdapter();
        deviceListView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class MyAdapter extends BaseAdapter {

        LayoutInflater inflater;

        public MyAdapter() {
            inflater = LayoutInflater.from(MainActivity.this);
        }

        @Override
        public int getCount() {
            if (data == null) {
                return 0;
            }
            return data.size();
        }

        @Override
        public Object getItem(int position) {

            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.list_item, null);
                viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.list_item_name);
                viewHolder.macTextView = (TextView) convertView.findViewById(R.id.list_item_mac);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Item item = data.get(position);
            if (item.name == null || item.name.length() == 0) {
                viewHolder.nameTextView.setText("no name");
            } else {
                viewHolder.nameTextView.setText(item.name);
            }

            viewHolder.macTextView.setText(item.mac);

            return convertView;
        }
    }

    class Item {
        String name;
        String mac;

        public Item(String name, String mac) {
            this.name = name;
            this.mac = mac;
        }
    }

    class ViewHolder {
        TextView nameTextView;
        TextView macTextView;
    }


}

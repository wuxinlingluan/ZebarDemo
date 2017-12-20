package com.github.zebardemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;

import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity    {

    public static Timer t;
    public static ArrayList<ReaderDevice> readersList = new ArrayList<>();
    private DeviceConnectTask deviceConnectTask;
    //    private static final String RFD8500 = "RFD8500";
    private ReaderListAdapter readerListAdapter;
    private ListView pairedListView;
    private TextView tv_emptyView;
    private CustomProgressDialog progressDialog;
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> av, View v, int pos, long arg3) {
            Toast.makeText(MainActivity.this, "点击", Toast.LENGTH_SHORT).show();
            if (MainActivity.isBluetoothEnabled()) {
                // Get the device MAC address, which is the last 17 chars in the View
                ReaderDevice readerDevice = readerListAdapter.getItem(pos);
                if (MyApplication.mConnectedReader == null) {
                    if (deviceConnectTask == null || deviceConnectTask.isCancelled()) {
                        MyApplication.is_connection_requested = true;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            deviceConnectTask = new DeviceConnectTask(readerDevice, "连接" + readerDevice.getName(), getReaderPassword(readerDevice.getName()));
                            deviceConnectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            deviceConnectTask = new DeviceConnectTask(readerDevice, "连接" + readerDevice.getName(), getReaderPassword(readerDevice.getName()));
                            deviceConnectTask.execute();
                        }
                    }
                } else {
                    {
                        if (MyApplication.mConnectedReader.isConnected()) {
                            MyApplication.is_disconnection_requested = true;
                            readerListAdapter.checkedTextView.setClickable(true);
                            readerListAdapter.notifyDataSetChanged();
                            try {
                                MyApplication.mConnectedReader.disconnect();
                            } catch (InvalidUsageException e) {
                                e.printStackTrace();
                            } catch (OperationFailureException e) {
                                e.printStackTrace();
                            }
                            //
                            ReaderDevice readerDevice1=new ReaderDevice();
                            bluetoothDeviceDisConnected(readerDevice1);
                            if (MyApplication.NOTIFY_READER_CONNECTION)
                                //    sendNotification(Constants.ACTION_READER_DISCONNECTED, "Disconnected from " + Application.mConnectedReader.getHostName());
                                //
                                clearSettings();
                        }
                        if (!MyApplication.mConnectedReader.getHostName().equalsIgnoreCase(readerDevice.getName())) {
                            MyApplication.mConnectedReader = null;
                            if (deviceConnectTask == null || deviceConnectTask.isCancelled()) {
                                MyApplication.is_connection_requested = true;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                    deviceConnectTask = new DeviceConnectTask(readerDevice, "正在连接" + readerDevice.getName(), getReaderPassword(readerDevice.getName()));
                                    deviceConnectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    deviceConnectTask = new DeviceConnectTask(readerDevice, "正在连接" + readerDevice.getName(), getReaderPassword(readerDevice.getName()));
                                    deviceConnectTask.execute();
                                }
                            }
                        } else {
                            MyApplication.mConnectedReader = null;
                        }
                    }
                }
                // Create the result Intent and include the MAC address
            } else
                Toast.makeText(MainActivity.this, "请打开蓝牙", Toast.LENGTH_SHORT).show();
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button bt = (Button)findViewById(R.id.bt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setClass(MainActivity.this,StartActivity.class);
                startActivity(intent);
            }
        });
    //    MyApplication.eventHandler = new EventHandler();
        initializeViews();
        if (!isBluetoothEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);
        }
        readersList.clear();
        loadPairedDevices();
        if (MyApplication.mConnectedDevice != null) {
            int index = readersList.indexOf(MyApplication.mConnectedDevice);
            if (index != -1) {
                readersList.remove(index);
                readersList.add(index, MyApplication.mConnectedDevice);
            } else {
                MyApplication.mConnectedDevice = null;
                MyApplication.mConnectedReader = null;
            }
        }
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices = adapter.getBondedDevices();
//        for(int i=0; i<devices.size(); i++)
//        {
//            BluetoothDevice device = (BluetoothDevice) devices.iterator().next();
//            Log.i("1111111111111",device.getName());
//        }
//
//        {
//            BluetoothDevice device = (BluetoothDevice) devices.iterator().next();
//            System.out.println(device.getName());
//        }
        readerListAdapter = new ReaderListAdapter(this, R.layout.readers_list_item, readersList);

        if (readerListAdapter.getCount() == 0) {
            pairedListView.setEmptyView(tv_emptyView);
        } else
            pairedListView.setAdapter(readerListAdapter);

        pairedListView.setOnItemClickListener(mDeviceClickListener);
        pairedListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    private void initializeViews() {
        pairedListView = (ListView) findViewById(R.id.bondedReadersList);
        tv_emptyView = (TextView) findViewById(R.id.empty);
    }



    public static boolean isBluetoothEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }


    private String getReaderPassword(String address) {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.READER_PASSWORDS, 0);
        return sharedPreferences.getString(address, null);
    }
    public void bluetoothDeviceDisConnected(ReaderDevice device) {
        if (deviceConnectTask != null && !deviceConnectTask.isCancelled() && deviceConnectTask.getConnectingDevice().getName().equalsIgnoreCase(device.getName())) {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            if (deviceConnectTask != null)
                deviceConnectTask.cancel(true);
        }
        if (device != null) {
            changeTextStyle(device);
        } else
            Constants.logAsMessage(Constants.TYPE_ERROR, "ReadersListFragment", "deviceName is null or empty");
    //    MainActivity.clearSettings();
    }
    /**
     * method to clear reader's settings on disconnection
     */
    public static void clearSettings() {
        MyApplication.antennaPowerLevel = null;
        MyApplication.antennaRfConfig = null;
        MyApplication.singulationControl = null;
        MyApplication.rfModeTable = null;
        MyApplication.regulatory = null;
        MyApplication.batchMode = -1;
        MyApplication.tagStorageSettings = null;
        MyApplication.reportUniquetags = null;
        MyApplication.dynamicPowerSettings = null;
        MyApplication.settings_startTrigger = null;
        MyApplication.settings_stopTrigger = null;
        MyApplication.beeperVolume = null;
        MyApplication.preFilters = null;
        if (MyApplication.versionInfo != null)
            MyApplication.versionInfo.clear();
        MyApplication.regionNotSet = false;
        MyApplication.isBatchModeInventoryRunning = null;
      //  Application.BatteryData = null;
        MyApplication.is_disconnection_requested = false;
        MyApplication.mConnectedDevice = null;
//        Application.mConnectedReader = null;
    }


    /**
     * async task to go for BT connection with reader
     */
    private class DeviceConnectTask extends AsyncTask<Void, String, Boolean> {
        private final ReaderDevice connectingDevice;
        private String prgressMsg;
        private OperationFailureException ex;
        private String password;

        DeviceConnectTask(ReaderDevice connectingDevice, String prgressMsg, String Password) {
            this.connectingDevice = connectingDevice;
            this.prgressMsg = prgressMsg;
            password = Password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(MainActivity.this, prgressMsg);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... a) {
            try {
                if (password != null)
                    connectingDevice.getRFIDReader().setPassword(password);
                    connectingDevice.getRFIDReader().connect();
                if (password != null) {
                    SharedPreferences.Editor editor = getSharedPreferences(Constants.READER_PASSWORDS, 0).edit();
                    editor.putString(connectingDevice.getName(), password);
                    editor.commit();
                }
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
                ex = e;
            }
            if (connectingDevice.getRFIDReader().isConnected()) {
                MyApplication.mConnectedReader = connectingDevice.getRFIDReader();
                progressDialog.dismiss();
                try {
                    readerListAdapter.checkedTextView.setChecked(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                connectingDevice.getRFIDReader().Events.setBatchModeEvent(true);
                connectingDevice.getRFIDReader().Events.setReaderDisconnectEvent(true);
                connectingDevice.getRFIDReader().Events.setBatteryEvent(true);
                connectingDevice.getRFIDReader().Events.setInventoryStopEvent(true);
                connectingDevice.getRFIDReader().Events.setInventoryStartEvent(true);
                // if no exception in connect
                if (ex == null) {
                    try {
                        MainActivity.UpdateReaderConnection(false);
                    } catch (InvalidUsageException e) {
                        e.printStackTrace();
                    } catch (OperationFailureException e) {
                        e.printStackTrace();
                    }
                } else {
                    MainActivity.clearSettings();
                }
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            MyApplication.NOTIFY_READER_CONNECTION=result;
            progressDialog.cancel();
            if (ex != null) {

                if (ex.getResults() == RFIDResults.RFID_CONNECTION_PASSWORD_ERROR) {
                    //showPasswordDialog(connectingDevice);
                    bluetoothDeviceConnected(connectingDevice);
                } else if (ex.getResults() == RFIDResults.RFID_BATCHMODE_IN_PROGRESS) {
                    MyApplication.isBatchModeInventoryRunning = true;
                    MyApplication.mIsInventoryRunning = true;
                    bluetoothDeviceConnected(connectingDevice);
                    if (MyApplication.NOTIFY_READER_CONNECTION){}
               //         sendNotification(Constants.ACTION_READER_CONNECTED, "Connected to " + connectingDevice.getName());
                    //Events.StatusEventData data = Application.mConnectedReader.Events.GetStatusEventData(RFID_EVENT_TYPE.BATCH_MODE_EVENT);
//                    Intent detailsIntent = new Intent(getActivity(), MainActivity.class);
//                    detailsIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                    detailsIntent.putExtra(RFID_EVENT_TYPE.BATCH_MODE_EVENT.toString(), 0/*data.BatchModeEventData.get_RepeatTrigger()*/);
//                    startActivity(detailsIntent);
                } else if (ex.getResults() == RFIDResults.RFID_READER_REGION_NOT_CONFIGURED) {
                    bluetoothDeviceConnected(connectingDevice);
                    MyApplication.regionNotSet = true;
                //    sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, getString(R.string.set_region_msg));
//                    Intent detailsIntent = new Intent(getActivity(), SettingsDetailActivity.class);
//                    detailsIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                    detailsIntent.putExtra(Constants.SETTING_ITEM_ID, 7);
//                    startActivity(detailsIntent);
                } else
                    bluetoothDeviceConnFailed(connectingDevice);
            } else {
                if (result) {
                    if (MyApplication.NOTIFY_READER_CONNECTION)
                        //sendNotification(Constants.ACTION_READER_CONNECTED, "Connected to " + connectingDevice.getName());
                    bluetoothDeviceConnected(connectingDevice);
                } else {
                    bluetoothDeviceConnFailed(connectingDevice);
                }
            }
            deviceConnectTask = null;
        }

        @Override
        protected void onCancelled() {
            deviceConnectTask = null;
            super.onCancelled();
        }

        public ReaderDevice getConnectingDevice() {
            return connectingDevice;
        }
    }


    /**
     * method to update reader device in the readers list on device connection failed event
     *
     * @param device device to be updated
     */
    public void bluetoothDeviceConnFailed(ReaderDevice device) {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        if (deviceConnectTask != null)
            deviceConnectTask.cancel(true);
        if (device != null)
            changeTextStyle(device);
        else
            Constants.logAsMessage(Constants.TYPE_ERROR, "ReadersListFragment", "deviceName is null or empty");

      //  sendNotification(Constants.ACTION_READER_CONN_FAILED, "Connection Failed!! was received");

        MyApplication.mConnectedReader = null;
        MyApplication.mConnectedDevice = null;
    }


    public static void UpdateReaderConnection(Boolean fullUpdate) throws InvalidUsageException, OperationFailureException {
        MyApplication.mConnectedReader.Events.setBatchModeEvent(true);
        MyApplication.mConnectedReader.Events.setReaderDisconnectEvent(true);
        MyApplication.mConnectedReader.Events.setInventoryStartEvent(true);
        MyApplication.mConnectedReader.Events.setInventoryStopEvent(true);
        MyApplication.mConnectedReader.Events.setTagReadEvent(true);
        MyApplication.mConnectedReader.Events.setHandheldEvent(true);
        MyApplication.mConnectedReader.Events.setBatteryEvent(true);
        MyApplication.mConnectedReader.Events.setPowerEvent(true);
        MyApplication.mConnectedReader.Events.setOperationEndSummaryEvent(true);

        if (fullUpdate)
            MyApplication.mConnectedReader.PostConnectReaderUpdate();

        MyApplication.regulatory = MyApplication.mConnectedReader.Config.getRegulatoryConfig();
        MyApplication.regionNotSet = false;
        MyApplication.rfModeTable = MyApplication.mConnectedReader.ReaderCapabilities.RFModes.getRFModeTableInfo(0);
        MyApplication.antennaRfConfig = MyApplication.mConnectedReader.Config.Antennas.getAntennaRfConfig(1);
        MyApplication.singulationControl = MyApplication.mConnectedReader.Config.Antennas.getSingulationControl(1);
        MyApplication.settings_startTrigger = MyApplication.mConnectedReader.Config.getStartTrigger();
        MyApplication.settings_stopTrigger = MyApplication.mConnectedReader.Config.getStopTrigger();
        MyApplication.tagStorageSettings = MyApplication.mConnectedReader.Config.getTagStorageSettings();
        MyApplication.dynamicPowerSettings = MyApplication.mConnectedReader.Config.getDPOState();
        MyApplication.beeperVolume = MyApplication.mConnectedReader.Config.getBeeperVolume();
        MyApplication.batchMode = MyApplication.mConnectedReader.Config.getBatchModeConfig().getValue();
        MyApplication.reportUniquetags = MyApplication.mConnectedReader.Config.getUniqueTagReport();
        MyApplication.mConnectedReader.Config.getDeviceVersionInfo(MyApplication.versionInfo);
        startTimer();
    }
    public static void startTimer() {
        if (t == null) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (MyApplication.mConnectedReader != null)
                            MyApplication.mConnectedReader.Config.getDeviceStatus(true, false, false);
                        else
                            stopTimer();
                    } catch (InvalidUsageException e) {
                        e.printStackTrace();
                    } catch (OperationFailureException e) {
                        e.printStackTrace();
                    }
                }
            };
            t = new Timer();
            t.scheduleAtFixedRate(task, 0, 60000);
        }
    }

    public static void stopTimer() {
        if (t != null) {
            t.cancel();
            t.purge();
        }
        t = null;
    }
    public void bluetoothDeviceConnected(ReaderDevice device) {
        if (device != null) {
            MyApplication.mConnectedDevice = device;
            MyApplication.is_connection_requested = false;
            changeTextStyle(device);
        } else
            Constants.logAsMessage(Constants.TYPE_ERROR, "ReadersListFragment", "deviceName is null or empty");
    }

    private void changeTextStyle(ReaderDevice device) {
        int i = readerListAdapter.getPosition(device);
        if (i >= 0) {
            readerListAdapter.remove(device);
            readerListAdapter.insert(device,i);
            readerListAdapter.notifyDataSetChanged();
        }
    }
    private void loadPairedDevices() {
        Readers readers=new Readers();
        readersList.addAll(readers.GetAvailableRFIDReaderList());
    }

    /**
     * method to update serial and model of connected reader device
     */
    public void capabilitiesRecievedforDevice() {
       runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (readerListAdapter.getPosition(MyApplication.mConnectedDevice) >= 0) {
                    ReaderDevice readerDevice = readerListAdapter.getItem(readerListAdapter.getPosition(MyApplication.mConnectedDevice));
                    //readerDevice.setModel(Application.mConnectedDevice.getModel());
                    //readerDevice.setSerial(Application.mConnectedDevice.getSerial());
                    readerListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        capabilitiesRecievedforDevice();
    }
}




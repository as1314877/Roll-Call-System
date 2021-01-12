package com.example.user.whoarrived;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main3Activity extends AppCompatActivity {
        private List<Button> buttons;
        private static final int[] BUTTON_IDS = {
                R.id.s10359001,
                R.id.s10359002,
                R.id.s10359003,
        };
    private static final String TAG = MainActivity.class.getSimpleName(); //get class's name
    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS =10021;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    public static final int REQUEST_FINE_LOCATION_PERMISSION = 99;
    //
    private Handler mHandler = new Handler(Looper.getMainLooper()); //Returns the application's main looper, which lives in the main thread of the application.

    private ListView mBle_scannerResult;
    ArrayAdapter<String> adapter;
    List<String> values = new ArrayList<>();
    List<String> devicesName = new ArrayList<>();
    List<Integer> devicesRSSI = new ArrayList<>();
    String username="";
    String meName="";
    private String UserNameList[]={"s10359001","s10359002","s10359003"};
    private String UserPassList[]={"s001","s002","s003"};
    private String NameList[]={"王黎","黃平","蔡雪"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main2);


        buttons = new ArrayList<Button>();
        for(int id : BUTTON_IDS) {
            Button button = (Button)findViewById(id);
            buttons.add(button);
        }


        Intent intent = getIntent();
        String result[]= intent.getStringExtra("result").split("//");
        username=result[0];
        meName=result[1];
        Arrived(meName);
        mBle_scannerResult = (ListView)findViewById(R.id.ble_scannerResult);

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, android.R.id.text1, values);


        BluetoothManager manager =
                (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "No Bluetooth Support.", Toast.LENGTH_SHORT).show();
            return;
        }
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
    }
    protected void Arrived(String name){
        int iter=0;
        Log.e(TAG, "Arrived: "+ name);
        while(iter<=buttons.size()){
//            Log.e(TAG, "Arrived:123 "+ buttons.get(iter).getText());
            if(buttons.get(iter).getText().equals(name)){
                Drawable drawableLeft = getResources().getDrawable(
                        R.drawable.button_yes);
                buttons.get(iter).setCompoundDrawablesWithIntrinsicBounds(drawableLeft,
                        null, null, null);
                break;
            }
            iter++;
        }
    }
    protected int get_nameIndex(String id){
        int index=-1;
        for(int i=0;i<UserNameList.length;i++){
            if(UserNameList[i].equals(id)){
                index=i;
            }
        }
        return index;
    }
    protected void Arrived_ID(String sid){
        int iter=0;
        Log.e(TAG, "Arrived: "+ sid);
        int index=get_nameIndex(sid);
        String name="";
        if(index!=-1){
            name=NameList[index];
            while(iter<=buttons.size()){
                Log.e(TAG, "Arrived:123 "+ buttons.get(iter).getText());
                if(buttons.get(iter).getText().equals(name)){
                    Drawable drawableLeft = getResources().getDrawable(
                            R.drawable.button_yes);
                    buttons.get(iter).setCompoundDrawablesWithIntrinsicBounds(drawableLeft,
                            null, null, null);
                    break;
                }
                iter++;
            }
        }
    }

    private  void requestLocationPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int hasPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

            if(hasPermission != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_PERMISSION);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) { //Return true if Bluetooth is currently enabled and ready for use.

            Intent enableBtIntent =
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            return;
        }

        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {   //Return PackageManager instance to find global package information.
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        DeviceUtil.startScanning(mBluetoothLeScanner, mScanCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DeviceUtil.stopAdvertising(mBluetoothLeAdvertiser,
                mAdvertiseCallback);
        DeviceUtil.stopScanning(mBluetoothLeScanner, mScanCallback);
    }

    private ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            values.clear();
            devicesName.clear();
            for (ScanResult r : results) {
                //values.add(r.getScanRecord().getDeviceName() + "," + r.getDevice().getAddress());
                //values.add(r.getDevice().getAddress());
                String tempValue = DeviceUtil.unpackPayload_char(r.getScanRecord().getManufacturerSpecificData(DeviceUtil.GENERAL));
                r.getRssi();
                values.add(tempValue);
                devicesName.add(r.getScanRecord().getDeviceName());
                devicesRSSI.add(r.getRssi());
                //values.add(r.getScanRecord()+"\n");
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateScanResult();
                    updateDatabase();
                }
            });
        }


        @Override
        public void onScanFailed(int errorCode) {
            Log.w("MainActivity", "Error scanning devices: "+errorCode);
        }
    };

    private void updateScanResult() {
        mBle_scannerResult.setAdapter(adapter);
    }

    //要改
    public void broadcastBtnClicked(View view) {
        DeviceUtil.restartAdvertising(mBluetoothLeAdvertiser,
                mAdvertiseCallback,
                username,
                "Text_input");
    }


    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(TAG, "LE Advertise Started.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w(TAG, "LE Advertise Failed: " + errorCode);
        }
    };

    private void updateDatabase() {
        Log.e(TAG, "updateData: "+"666" );
        for (int i=0;i<values.size();i++) {
            String otherUser=values.get(i).substring(1,values.get(i).length());
            if(otherUser.indexOf("ull")==-1){
                Arrived_ID(otherUser);
            }

        }
    }

//    @Override
//    public void onBackPressed() {
//        Intent intent = new Intent();
//        intent.setClass(Main3Activity.this, MainActivity.class);
//        intent.putExtra("jsondata",FB_ID);
//        startActivity(intent);
//    }
}

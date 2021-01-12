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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main2Activity extends AppCompatActivity {
    private List<Button> buttons;
    private Handler mHandler2;
    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 10021;
    private static final int[] BUTTON_IDS = {
            R.id.s10359001,
            R.id.s10359002,
            R.id.s10359003,
    };
    private static final String TAG = MainActivity.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    public static final int REQUEST_FINE_LOCATION_PERMISSION = 99;
    ArrayAdapter<String> adapter;
    List<String> values = new ArrayList<>();
    List<String> devicesName = new ArrayList<>();
    String username = "";
    String meName = "";
    private String UserNameList[] = {"s10359001", "s10359002", "s10359003"};
    private String UserPassList[] = {"s001", "s002", "s003"};
    private String NameList[] = {"王黎", "黃平", "蔡雪"};
    private String PhoneList[] ={"0979","0981","0921"};
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
            requestLocationPermission();
            return;
        }
        buttons = new ArrayList<Button>();
        for (int id : BUTTON_IDS) {
            Button button = (Button) findViewById(id);
            buttons.add(button);
        }

        Intent intent = getIntent();
        String result[] = intent.getStringExtra("result").split("//");
        username = result[0];
        meName = result[1];
        Log.e(TAG, "onCreate: " + username + "//" + meName);
        Arrived(meName);


        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        BluetoothManager manager =
                (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "No Bluetooth Support.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
    }

    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_PERMISSION);
            }
        }
    }

    protected void Arrived(String name) {
        int iter = 0;
        Log.e(TAG, "Arrived: " + name);
        while (iter <= buttons.size()) {
            Log.e(TAG, "Arrived:123 " + buttons.get(iter).getText());
            if (buttons.get(iter).getText().equals(name)) {
                Drawable drawableLeft = getResources().getDrawable(
                        R.drawable.button_yes);
                buttons.get(iter).setCompoundDrawablesWithIntrinsicBounds(drawableLeft,
                        null, null, null);
                break;
            }
            iter++;
        }
    }

    protected int get_nameIndex(String id) {
        int index = -1;
        for (int i = 0; i < UserNameList.length; i++) {
            if (UserNameList[i].equals(id)) {
                index = i;
            }
        }
        return index;
    }

    protected void Arrived_ID(String sid) {
        int iter = 0;
        Log.e(TAG, "Arrived: " + sid);
        int index = get_nameIndex(sid);
        String name = "";
        if (index != -1) {
            name = NameList[index];
            while (iter <= buttons.size()) {
                Log.e(TAG, "Arrived:123 " + buttons.get(iter).getText());
                if (buttons.get(iter).getText().equals(name)) {
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

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {

            Intent enableBtIntent =
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            return;
        }

        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
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
                String tempValue = DeviceUtil.unpackPayload_char(r.getScanRecord().getManufacturerSpecificData(DeviceUtil.GENERAL));
                values.add(tempValue);
//                devicesName.add(r.getScanRecord().getDeviceName());
                //values.add(r.getScanRecord()+"\n");
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
//                    updateScanResult();
                    updateData();
                }
            });
        }

        Runnable mStatusChecker = new Runnable() {
            @Override
            public void run() {
                try {
                    Handler handler = new Handler();
                    Runnable runnable = new Runnable() {
                        public void run() {
                            for (int i = 0; i < buttons.size(); i++) {
                                Drawable drawableLeft = getResources().getDrawable(
                                        R.drawable.button_not);
                                buttons.get(i).setCompoundDrawablesWithIntrinsicBounds(drawableLeft,
                                        null, null, null);
                            }
                            Arrived(meName);
                        }
                    };
                    handler.postAtTime(runnable, System.currentTimeMillis() + 5000);
                    handler.postDelayed(runnable, 5000);

                } finally {
                    // 100% guarantee that this always happens, even if
                    // your update method throws an exception
                    mHandler.postDelayed(mStatusChecker, 10000);
                }
            }
        };

        void startRepeatingTask() {
            mStatusChecker.run();
        }

        void stopRepeatingTask() {
            mHandler.removeCallbacks(mStatusChecker);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.w("MainActivity", "Error scanning devices: " + errorCode);
        }
    };

    public void broadcastBtnClicked(View view) {
        DeviceUtil.restartAdvertising(mBluetoothLeAdvertiser,
                mAdvertiseCallback,
                username,
                "Text_input");//傳送data加藍牙mac

    }

    private void updateData() {
        for (int i = 0; i < values.size(); i++) {
            String otherUser = values.get(i).substring(1, values.get(i).length());
            Log.e(TAG, "777Data: " + otherUser + "//" + otherUser.indexOf("ull"));
            if (otherUser.indexOf("ull") == -1) {
                Arrived_ID(otherUser);
            }

        }
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


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {

            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        ) {
                    // All Permissions Granted
                    // Permission Denied
                    //Toast.makeText(Main2Activity.this, "All Permission GRANTED !! Thank You :)", Toast.LENGTH_SHORT)
                    //.show();
                } else {
                    // Permission Denied
                    Toast.makeText(Main2Activity.this, "One or More Permissions are DENIED Exiting App :(", Toast.LENGTH_SHORT)
                            .show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void fuckMarshMallow() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("Show Location");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "App need access to " + permissionsNeeded.get(0);

                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);

                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }

//        Toast.makeText(Main2Activity.this, "No new Permission Required- Launching App .You are Awesome!!", Toast.LENGTH_SHORT)
//                .show();
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(Main2Activity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {

        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    protected void call_1(View view){
        Uri uri = Uri.parse("tel:"+PhoneList[0]);
        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
        startActivity(intent);
    }
    protected void call_2(View view){
        Uri uri = Uri.parse("tel:"+PhoneList[1]);
        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
        startActivity(intent);
    }
    protected void call_3(View view){
        Uri uri = Uri.parse("tel:"+PhoneList[2]);
        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
        startActivity(intent);
    }
}

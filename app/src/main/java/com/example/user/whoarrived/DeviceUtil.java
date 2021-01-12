package com.example.user.whoarrived;

/**
 * Created by user on 2017/7/15.
 */

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanSettings;
import android.util.SparseArray;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DeviceUtil {

    /*
     * Adding manufacturer-specific custom data to the ad packet.
     * We will use Google's Bluetooth SIG identifier as the example.
     */
    public static final int MANUFACTURER_GOOGLE = 0x00E0;
    public static final int SONY_ERICSSON = 0x0056;
    public static final int GENERAL = 0xFFFF;



    public static boolean hasManufacturerData(ScanRecord record) {
        SparseArray<byte[]> data =
                record.getManufacturerSpecificData();


        return (data != null
                && data.get(GENERAL) != null);
    }

    //官方廣播https://developer.android.com/reference/android/bluetooth/le/BluetoothLeAdvertiser.html
    public static void startAdvertising(BluetoothLeAdvertiser advertiser,
                                        AdvertiseCallback callback,
                                        String tempValue,
                                        String click_name) {
        if (advertiser == null) return;

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(false)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()

//                .setIncludeDeviceName(true)//占用10bytes

                .setIncludeTxPowerLevel(true)//占用4bytes

                .addManufacturerData(GENERAL,
                        buildPayload(tempValue , click_name))
                .build();

        advertiser.startAdvertising(settings, data, callback);
    }


    public static void stopAdvertising(BluetoothLeAdvertiser advertiser,
                                       AdvertiseCallback callback) {
        if (advertiser == null) return;

        advertiser.stopAdvertising(callback);
    }


    public static void restartAdvertising(BluetoothLeAdvertiser advertiser,
                                          AdvertiseCallback callback,
                                          String newTempValue,
                                          String click_name) {
        stopAdvertising(advertiser, callback);
        startAdvertising(advertiser, callback, newTempValue ,click_name);
    }


    public static void startScanning(BluetoothLeScanner scanner,
                                     ScanCallback callback) {
        ScanSettings settings = new ScanSettings.Builder()

                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setReportDelay(1000)
                .build();
        scanner.startScan(null, settings, callback);
    }


    public static void stopScanning(BluetoothLeScanner scanner,
                                    ScanCallback callback) {
        scanner.stopScan(callback);
    }

    //創建一個buffer(payload)
    private static byte[] buildPayload(String value, String click_name) {
        //Set the MSB to indicate fahrenheit
        byte flags = (byte)0x8000000;

        byte[] b = {};
        try{
            b = value.getBytes("UTF-8");
        }catch(Exception e){

        }

        int max = 16;//如果你要加device name，最大是16個字，不加是26(不含flag)
        int value_dynamic = 0;
        if(b.length <= max)
            value_dynamic = b.length + 1;//flag 所以加1
        else {
            value_dynamic = max + 1;//flag 所以加1
            System.arraycopy(b, 0, b, 0, max);//保險不出錯
        }

        return ByteBuffer.allocate(value_dynamic)
                //GATT APIs expect LE order
                .order(ByteOrder.LITTLE_ENDIAN)
                //Add the flags byte
                .put(flags)
                //Add the temperature value
                .put(b)
                .array();
    }

    //解payload
    public static String unpackPayload_char(byte[] data) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data)
                    .order(ByteOrder.LITTLE_ENDIAN);

            buffer.get();
            String s = "";
            byte[] b = new byte[buffer.limit()];//給他一個大小為buffer.limit()的陣列,不給為0
            for(int i=0;i<buffer.limit();i++){
                b[i] = buffer.get(i);
            }
            s = new String(b, "UTF-8");
            return s;
        }catch(Exception e){

        }
        return "null";
    }
}



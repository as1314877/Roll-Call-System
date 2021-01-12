package com.example.user.whoarrived;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private String UserNameList[]={"s10359001","s10359002","s10359003","teacher"};
    private String NameList[]={"王黎","黃平","蔡雪","陳宗禧"};
    private String UserPassList[]={"s001","s002","s003","0000"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }



    public void Register(View view){
        Intent y = new Intent();
        startActivity(y);
    }
    public void Login(View view){
        EditText editText1 = (EditText) findViewById(R.id.username);
        String username = editText1.getText().toString();
        EditText editText2 = (EditText) findViewById(R.id.password);
        String password = editText2.getText().toString();
        int index=get_nameIndex(username);
        if(index!=-1 && UserPassList[index].equals(password)){
            Intent y = new Intent();
            y.setClass(MainActivity.this, Main2Activity.class);
            y.putExtra("result",username+"//"+NameList[index]);
            startActivity(y);
        }else{
            Toast toast = Toast.makeText(MainActivity.this,"帳號密碼不符合!", Toast.LENGTH_LONG);
            //顯示Toast
            toast.show();
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

}

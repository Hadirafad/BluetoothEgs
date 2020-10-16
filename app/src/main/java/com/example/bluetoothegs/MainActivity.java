package com.example.bluetoothegs;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

    public static final int REQUEST_ACCESS_COARSE_LOCATION=1;
    public static final int REQUEST_ENABLE_BLUETOOTH=11;
    private ListView deviceList;
    private ImageButton scanningBtn;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        deviceList=findViewById(R.id.list);
        scanningBtn=findViewById(R.id.btnSearch);

        listAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        deviceList.setAdapter(listAdapter);

        checkBluetoothState();


        scanningBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothAdapter!=null &&bluetoothAdapter.isEnabled()){
                    if(CheckCoarseLocationPermission()){
                        listAdapter.clear();
                        bluetoothAdapter.startDiscovery();
                    }else{
                        checkBluetoothState();
                    }
                }
            }
        });
        CheckCoarseLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(devicesFoundReceiver,new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(devicesFoundReceiver,new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(devicesFoundReceiver,new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(devicesFoundReceiver);
    }

    private boolean CheckCoarseLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ACCESS_COARSE_LOCATION);
            return false;
        }else {
            return true;
        }
    }

    private void checkBluetoothState(){
        if(bluetoothAdapter==null){
            Toast.makeText(this,"Bluetooth is not supported on your device!",Toast.LENGTH_LONG).show();
        }else {
            if (bluetoothAdapter.isEnabled()){
                if (bluetoothAdapter.startDiscovery()){
                    Toast.makeText(this,"Device discovering process...",Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(this,"Bluetooth is enabled",Toast.LENGTH_LONG).show();
                    scanningBtn.setEnabled(true);
                }
            }else {
                Toast.makeText(this,"you need to enable bluetooth",Toast.LENGTH_LONG).show();
                Intent enableIntent =new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent,REQUEST_ENABLE_BLUETOOTH);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUEST_ENABLE_BLUETOOTH){
            checkBluetoothState();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_ACCESS_COARSE_LOCATION:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"allowed",Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(this,"forbidden",Toast.LENGTH_LONG).show();

                }break;
        }
    }

    private final BroadcastReceiver devicesFoundReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                listAdapter.add(device.getName()+"\n"+device.getAddress());
                listAdapter.notifyDataSetChanged();
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                //scanningBtn.setText("Scanning...");
            }else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //scanningBtn.setText("Scanning in progress...");
            }
        }

    };
}

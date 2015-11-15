package com.example.jhw.check;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.wizturn.sdk.central.Central;
import com.wizturn.sdk.central.CentralManager;
import com.wizturn.sdk.peripheral.Peripheral;
import com.wizturn.sdk.peripheral.PeripheralScanListener;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_ENABLE_BT = 1000;
    private String uuid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CentralManager.getInstance().init(getApplicationContext());
        CentralManager.getInstance().setPeripheralScanListener(new PeripheralScanListener() {
            @Override
            public void onPeripheralScan(Central central, final Peripheral peripheral) {
                // TODO do something with the scanned peripheral(beacon)
                Log.i("ExampleActivity", "peripheral : " + peripheral);
                uuid = peripheral.getProximityUUID();
                if(uuid.equals("d5756247-57a2-4344-915d-9599497940a7")){
                    CentralManager.getInstance().stopScanning();
                }
            }
        });
        if(!CentralManager.getInstance().isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else{
            CentralManager.getInstance().startScanning();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // TODO Okay. Now bluetooth is on. Let's scan.
                CentralManager.getInstance().startScanning();
            }
        }
    }
}
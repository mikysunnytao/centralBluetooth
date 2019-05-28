package com.fengtao.client.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fengtao.client.APP;
import com.fengtao.client.R;
import com.fengtao.client.broadcast.HomeWatcherReceiver;
import com.fengtao.client.broadcast.OnePixelReceiver;
import com.fengtao.client.service.ble.BleBackgroundService;
import com.fengtao.client.service.ble.BleBindService;
import com.fengtao.client.util.DateUtil;

import java.util.Arrays;
import java.util.UUID;

/**
 * Ble服务端(从机/外围设备/peripheral)
 */
public class BleClientActivity extends Activity {

    public static final UUID UUID_SERVICE = UUID.fromString("10000000-0000-0000-0000-000000000000"); //自定义UUID
    public static final UUID UUID_CHAR_READ_NOTIFY = UUID.fromString("11000000-0000-0000-0000-000000000000");
    public static final UUID UUID_DESC_NOTITY = UUID.fromString("11100000-0000-0000-0000-000000000000");
    public static final UUID UUID_CHAR_WRITE = UUID.fromString("12000000-0000-0000-0000-000000000000");
    private static final String TAG = BleClientActivity.class.getSimpleName();
    private TextView mTips;
    private ScrollView scrollView;
    private boolean isBind = false;
    private BleBindService bindService;

    private HomeWatcherReceiver homeWatcherReceiver;
    private OnePixelReceiver onePixelReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bleserver);
        mTips = findViewById(R.id.tv_tips);
        scrollView = findViewById(R.id.content_scroller);
        Intent intent = new Intent(this,BleBindService.class);
        bindService(intent,connection,Context.BIND_AUTO_CREATE);
        onePixelReceiver = new OnePixelReceiver();
        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(onePixelReceiver,screenFilter);
        homeWatcherReceiver = new HomeWatcherReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(homeWatcherReceiver, filter);
    }

    public void clearConsole(View view){
        mTips.setText("");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isBind = true;
            sendBroadcast(new Intent(BleBackgroundService.CLOSE_TAG));
            Log.i(TAG, "service connected");
            BleBindService.ValueChangeBinder binder = (BleBindService.ValueChangeBinder) service;
            bindService = binder.getService();
            bindService.setMsgChangeListener(new BleBindService.OnMsgChangeListener() {
                @Override
                public void onChange(String msg) {
                    logTv(msg);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBind = false;
        }
    };

    private void logTv(final String msg) {
        if (isDestroyed())
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                APP.toast(msg, 0);
                mTips.append(DateUtil.getCurrDateStr() + msg + "\n\n");
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}

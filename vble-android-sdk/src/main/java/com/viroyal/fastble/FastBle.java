package com.viroyal.fastble;

import android.app.Application;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.text.TextUtils;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanAndConnectCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.scan.BleScanRuleConfig;

import java.util.UUID;

/**
 * Created by zy on 2018/6/27.
 * <p>
 * Ble 蓝牙协议封装
 */

public class FastBle implements Ble {
    private static FastBle sFastBle;
    private Application sContext;

    public static Ble get() {
        return sFastBle;
    }

    private FastBle(Application sContext) {
        this.sContext = sContext;
        BleManager.getInstance().init(sContext);
        BleManager.getInstance()
                .enableLog(false)
                .setReConnectCount(20, 1000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000)
                /*.setSplitWriteNum(100)*/;
    }

    public static void init(Application context) {
        sFastBle = new FastBle(context);
    }

    private String targetMac;
    private String[] targetUUID;
    private String[] targetNames;
    private boolean isAutoConnect = false;
    private final long DefaultScanTimeout = 30000L;

    @Override
    public Ble setTargetMac(String mac) {
        targetMac = mac;

        if (mac != null && !TextUtils.isEmpty(mac)) {
            BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                    .setDeviceMac(mac)
                    .build();
            BleManager.getInstance().initScanRule(scanRuleConfig);
        }

        return this;
    }

    @Override
    public Ble setTargetBroadcastNames(String broadcastNames) {
        if (TextUtils.isEmpty(broadcastNames)) {
            targetNames = null;
        } else {
            //targetNames = broadcastNames.split("_");

            //  直接根据broadcastNames寻找对应的ble设备
            targetNames = new String[]{broadcastNames};

        }
        return this;
    }

    @Override
    public Ble setTargetUUID(String uuid) {
        if (TextUtils.isEmpty(uuid)) {
            targetUUID = null;
        } else {
            targetUUID = uuid.split(",");
        }
        return this;
    }

    @Override
    public Ble setAutoConnect(boolean autoConnect) {
        isAutoConnect = autoConnect;
        return this;
    }

    public void setScanRule() {
        UUID[] serviceUuids = null;
        if (targetUUID != null && targetUUID.length > 0) {
            serviceUuids = new UUID[targetUUID.length];
            for (int i = 0; i < targetUUID.length; i++) {
                String name = targetUUID[i];
                String[] components = name.split("-");
                if (components.length != 5) {
                    serviceUuids[i] = null;
                } else {
                    serviceUuids[i] = UUID.fromString(targetUUID[i]);
                }
            }
        }
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setServiceUuids(serviceUuids)      // 只扫描指定的服务的设备，可选
                .setDeviceName(false, targetNames)   // 只扫描指定广播名的设备，可选
                .setDeviceMac(targetMac)                  // 只扫描指定mac的设备，可选
                .setAutoConnect(isAutoConnect)      // 连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(DefaultScanTimeout)              // 扫描超时时间，可选，默认10秒
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    @Override
    public Ble startScanBle(BleScanCallback bleScanCallback) {
        BleManager.getInstance().scan(bleScanCallback);
        return this;
    }

    @Override
    public void CancelScanBle(){
        BleManager.getInstance().cancelScan();
    }

    @Override
    public Ble scanAndConnect(BleScanAndConnectCallback callback) {
        BleManager.getInstance().scanAndConnect(callback);
        return this;
    }

    public boolean isConnected(BleDevice bleDevice) {
        return BleManager.getInstance().isConnected(bleDevice);
    }


    @Override
    public Ble connect(BleDevice bleDevice, BleGattCallback bleGattCallback) {
        if (!BleManager.getInstance().isConnected(bleDevice)) {
            BleManager.getInstance().cancelScan();
            BleManager.getInstance().connect(bleDevice, bleGattCallback);
        }

        return this;
    }

    @Override
    public Ble disConnect(BleDevice bleDevice) {
        if (BleManager.getInstance().isConnected(bleDevice)) {
            BleManager.getInstance().disconnect(bleDevice);
        }
        return this;
    }

    @Override
    public BluetoothGatt getBluetoothGatt(BleDevice bleDevice) {
        return BleManager.getInstance().getBluetoothGatt(bleDevice);
    }

    @Override
    public void write(BluetoothGattCharacteristic c, BleDevice b,
                      String content, BleWriteCallback bleWriteCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            byte[] bs = content.getBytes();
            BleManager.getInstance().write(
                    b,
                    c.getService().getUuid().toString(),
                    c.getUuid().toString(),
                    bs,
                    false,
                    bleWriteCallback);
        }
    }

    @Override
    public void noti(BluetoothGattCharacteristic c, BleDevice b, BleNotifyCallback bleReadCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BleManager.getInstance().notify(
                    b,
                    c.getService().getUuid().toString(),
                    c.getUuid().toString(),
                    bleReadCallback);
        }
    }

    public void setMtu(BleDevice bleDevice, int mtu, BleMtuChangedCallback callback) {
        BleManager.getInstance().setMtu(
                bleDevice,
                mtu,
                callback);
    }
}

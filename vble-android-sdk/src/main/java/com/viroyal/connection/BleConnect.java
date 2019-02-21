package com.viroyal.connection;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

import java.util.List;
import java.util.UUID;

import com.viroyal.fastble.Ble;
import com.viroyal.fastble.FastBle;

/**
 * Created by zy on 2018/2/7.
 */

public class BleConnect {

    private final String TAG = "[BTM]" + getClass().getSimpleName();
    public static String RX_TAG_STATUS_CHANGE = "RX_TAG_STATUS_CHANGE";

    private static BleConnect sBleConnect;
    private Ble mBle;
    private BleDevice mBleDevice;
    private boolean foundBle = false;
    private BluetoothGattService mGattService;
    private BluetoothGattCharacteristic mGattCharacteristic;

    /*
     * 提供给BTManager回调方法
     * */
    private BleGattCallback mBleGattCallback;
    private BleNotifyCallback mBleNotifyCallback;
    private BleMtuChangedCallback mBleMtuCB;

    /*指定服务UUID*/
    private UUID mUUIDService = UUID.fromString("0000ff10-0000-1000-8000-00805f9b34fb");
    /*指定特征UUID*/
    private UUID mUUIDCharacteristic = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");

    public static BleConnect get() {
        if (sBleConnect == null) {
            sBleConnect = new BleConnect();
        }
        return sBleConnect;
    }

    public BleConnect() {
        mBle = FastBle.get();
    }

    public void setBleGattCallback(BleGattCallback bleGattCallback) {
        mBleGattCallback = bleGattCallback;
    }

    public void setBleNotifyCallback(BleNotifyCallback callback) {
        mBleNotifyCallback = callback;
    }

    public void setBleMtuCB(BleMtuChangedCallback bleMtuCB) {
        mBleMtuCB = bleMtuCB;
    }

    BleScanCallback mBleScanCallback = new BleScanCallback() {
        @Override
        public void onScanFinished(List<BleDevice> list) {
            Log.d(TAG, "searchBLE onScanFinished  [list.size]:" + list.size());
            if (list.size() == 1) {
                foundBleDevice(list.get(0));
            }
        }

        @Override
        public void onScanStarted(boolean b) {
            Log.d(TAG, "searchBLE onScanStarted  [b]:" + b);
            if (mBleGattCallback != null) {
                mBleGattCallback.onStartConnect();
            }
        }

        @Override
        public void onScanning(BleDevice bleDevice) {
            Log.d(TAG, "searchBLE onScanning  [bleDevice]:" + bleDevice.getName()
                    + " , address: " + bleDevice.getMac());
            /*
             *  根据系统蓝牙设置手动连接的BT device地址或者名称
             *  应该只能搜索到唯一的一个
             *  找到后立即停止，停止后立即连接
             */
            mBle.CancelScanBle();
        }
    };

    /**
     * 获取蓝牙BLE 的连接状态
     *
     * @return
     * @throws
     * @parm a
     */
    public boolean isConnected() {
        if (mBleDevice == null) {
            return false;
        } else {
            return mBle.isConnected(mBleDevice);
        }
    }

    private void setStatus(int status) {
        statusChangeNotify();
    }

    //发送通知，蓝牙连接状态发送变化
    private void statusChangeNotify() {
        //TODO
        // RxBus.get().post(RX_TAG_STATUS_CHANGE, new Integer(1));
    }

    /**
     * 通过蓝牙audio设备名称去获取对应的BLE设备名称
     * 这里定义BLE名称为audio设备名称中间添加’BLE‘  字串
     *
     * @return 返回对应的BLE设备的名称
     */
    public String getConnectedBLEName(BluetoothDevice device) {
        if (device != null) {
            String[] splitName = device.getName().split("_");
            if (splitName.length == 2) {
                String tName = splitName[0] + "_BLE_" + splitName[1];
                Log.d(TAG, "Find the BLE name form BT audio name: " + tName);
                return tName;
            }
        }

        Log.e(TAG, "Error : Can NOT find the BLE name form BT audio name: " + device);
        return null;
    }

    public BluetoothDevice getConnectedBLEDevice() {
        if (isConnected()) {
            return mBleDevice.getDevice();
        }
        return null;
    }

    /*找到了这个蓝牙设备*/
    private void foundBleDevice(BleDevice bleDevice) {
        Log.d(TAG, "foundBleDevice  [bleDevice]:" + bleDevice.getName());
        if (foundBle) {
            Log.d(TAG,"BleDevice already find!");
            return;
        }
        foundBle = true;
        //TODO
        // RxBus.get().post(BT_STATUS_CHANGE, "搜索到蓝牙： " + bleDevice.getName());
        mBle.connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                Log.d(TAG, "foundBleDevice onStartConnect  []:");
                mBleDevice = null;
                foundBle = true;
                if (mBleGattCallback != null) {
                    mBleGattCallback.onStartConnect();
                }
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException e) {
                Log.d(TAG, "foundBleDevice onConnectFail  [bleDevice, e]:" + e);
                mBleDevice = null;
                foundBle = false;
                if (mBleGattCallback != null) {
                    mBleGattCallback.onConnectFail(bleDevice, e);
                }
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt bluetoothGatt, int i) {
                Log.d(TAG, "foundBleDevice onConnectSuccess  [bleDevice, bluetoothGatt, i]:");
                mBleDevice = bleDevice;
                foundBle = true;
                getGattServiceCharacteristic();

                if (mBleGattCallback != null) {
                    mBleGattCallback.onConnectSuccess(bleDevice, bluetoothGatt, i);
                }
            }

            @Override
            public void onDisConnected(boolean b, BleDevice bleDevice, BluetoothGatt bluetoothGatt, int i) {
                Log.d(TAG, "foundBleDevice onDisConnected  [b, bleDevice, bluetoothGatt, i]:");
                mBleDevice = null;
                foundBle = false;
                if (mBleGattCallback != null) {
                    mBleGattCallback.onDisConnected(b, bleDevice, bluetoothGatt, i);
                }
            }
        });
    }

    /**
     * 搜寻Ble 设备
     */
    public void searchBLE(String deviceName) {
        if (isConnected()) {
            Log.d(TAG, "SearchBLE  CANCEL , BLE is already connected!");
            return;
        }
        Log.d(TAG, "SearchBLE  [] deviceName :" + deviceName);

        // 先设置需要扫描的BLE地址
        mBle.setTargetBroadcastNames(deviceName);
        mBle.setScanRule();

        //TODO
        // RxBus.get().post(BT_STATUS_CHANGE, "开始搜索： ");
        // 开始搜索
        mBle.startScanBle(mBleScanCallback);
    }

    public void disconnectBleDevice() {
        if (mBle == null || mBleDevice == null) {
            Log.e(TAG, "disconnectBleDevice[] " + "Ble:" + mBle + " , mBleDevice=" + mBleDevice);
            return;
        }

        Log.d(TAG,"To disconnectBleDevice...");
        mBle.disConnect(mBleDevice);
    }

    public void setBleMtu() {
        if (mBle == null || mBleDevice == null || mBleMtuCB == null) {
            Log.e(TAG, "setBleMtu state is ERROR,  " + "Ble:" + mBle
                    + " , mBleDevice=" + mBleDevice
                    + " ,mBleMtuCB=" + mBleMtuCB);
            return;
        }

        mBle.setMtu(mBleDevice, 64, mBleMtuCB);
    }

    private boolean getGattServiceCharacteristic() {
        if (mBle == null || mBleDevice == null) {
            Log.e(TAG, "getGattServiceCharacteristic Fail ! > " + "Ble:" + mBle + " , mBleDevice=" + mBleDevice);
            return false;
        }
        mGattService = BleManager.getInstance().getBluetoothGatt(mBleDevice).getService(mUUIDService);
        if (mGattService != null) {
            mGattCharacteristic = mGattService.getCharacteristic(mUUIDCharacteristic);
            if (mGattCharacteristic != null) {
                mBle.noti(mGattCharacteristic, mBleDevice, mBleNotifyCallback);
                Log.d(TAG, "BLE 成功獲取服務特征！");
                return true;
            }
        }

        Log.d(TAG, "BLE 沒法獲取服務特征！");
        return false;
    }

    private boolean checkWriteGattCharacteristicAvailable() {
        if (mBle == null || mBleDevice == null) {
            Log.e(TAG, "checkWriteGattCharacteristicAvailable Fail! "
                    + "Ble:" + mBle + " , mBleDevice=" + mBleDevice);
            return false;
        }
        if (mGattService == null || mGattCharacteristic == null) {
            Log.e(TAG, "Try  getGattServiceCharacteristic again > " +
                    "mGattService:" + mGattService + " , mGattCharacteristic:" + mGattCharacteristic);
            getGattServiceCharacteristic();
        }

        return mGattService != null && mGattCharacteristic != null;
    }

    public void writeGattCharacteristic(String c, final IWriteGattCharacteristicCB cb) {
        if (checkWriteGattCharacteristicAvailable()) {
            Log.d(TAG, "writeGattCharacteristic : " + c);
            mBle.write(mGattCharacteristic, mBleDevice, c,
                    new BleWriteCallback() {
                        @Override
                        public void onWriteSuccess(int i, int i1, byte[] bytes) {
                            Log.d(TAG, "writeGattCharacteristic.onWriteSuccess :" + new String(bytes)
                                    + "[" + formatHexString(bytes, true) + "]");
                            if (cb != null) {
                                cb.onWriteGattCharacteristicSuccessCB();
                            }
                        }

                        @Override
                        public void onWriteFailure(BleException e) {
                            Log.d(TAG, "writeGattCharacteristic.onWriteFailure :" + e);
                            if (cb != null) {
                                cb.onWriteGattCharacteristicFailCB();
                            }
                        }
                    });
        }
    }

    private static String formatHexString(byte[] data, boolean addSpace) {
        if (data == null || data.length < 1)
            return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            String hex = Integer.toHexString(data[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex);
            if (addSpace)
                sb.append(" ");
        }
        return sb.toString().trim();
    }

    public interface IWriteGattCharacteristicCB {
        void onWriteGattCharacteristicSuccessCB();
        void onWriteGattCharacteristicFailCB();
    }
}

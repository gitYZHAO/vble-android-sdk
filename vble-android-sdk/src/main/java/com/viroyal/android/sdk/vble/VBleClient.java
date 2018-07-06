package com.viroyal.android.sdk.vble;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.UUID;

public class VBleClient {
    private static final String TAG = "VBleClient";

    public final static String VBLE_COMMAND_CALL_ANSWER = "CALL_ANSWER";
    public final static String VBLE_COMMAND_CALL_END = "CALL_END";
    public final static String VBLE_COMMAND_MAKE_CALL = "MAKE_CALL";
    public final static String VBLE_COMMAND_WAKE_UP = "WAKE_UP";
    public final static String VBLE_COMMAND_SET_FM = "SET_FM";

    // 实际设备中使用的UUID
    private static final UUID SPECIFIC_SERVICE_UUID = UUID.fromString("0000ff10-0000-1000-8000-00805f9b34fb");
    private static final UUID READWRITE_CHARACTERISTIC_UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private Context mContext;
    private VBleCallback mVBleCallback;
    private int mVBleStatus = VBleResult.VBLE_STATUS_DISCONNECTED;

    private String[][] command = {
            {VBLE_COMMAND_CALL_ANSWER, "AT+CALLANSW", "+CALLANSW"},
            {VBLE_COMMAND_CALL_END, "AT+CALLEND", "+CALLEND"},
            {VBLE_COMMAND_MAKE_CALL, "AT+DIAL=", "+DIAL"},
            {VBLE_COMMAND_WAKE_UP, "AT+WAKEUP", "+WAKEUP"},
            {VBLE_COMMAND_SET_FM, "AT+FMFREQ=", "+FMFREQ"}
    };

    public VBleClient(Context context, VBleCallback callback) {
        mContext = context;
        mVBleCallback = callback;
    }

    public boolean VBleClient_InitBLEClient(Context context) {
        if (context == null) return false;


        // 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }
        // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) mBluetoothAdapter = bluetoothManager.getAdapter();

        // 检查设备上是否支持蓝牙
        if (mBluetoothAdapter == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
            //判断是否具有权限
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        Log.d(TAG, "VBleClient_InitBLEClient: InitBLEClient successfully! Start scan BLE Service...");
        ConnectBLEService();
        return true;
    }

    public void VBleClient_DisconnectBLEClient() {
        if (mVBleStatus == VBleResult.VBLE_STATUS_ENABLE_GATT_CHARACTERISTIC) {
            if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                Log.d(TAG, "VBleClient_DisconnectBLEClient: BluetoothAdapter not initialized");
                return;
            }
            mBluetoothGatt.disconnect();
        } else {
            Log.d(TAG, "VBleClient_DisconnectBLEClient: Can not disconnect BLE Service, status=" + mVBleStatus);
        }
    }

    public boolean VBleClient_SendCommand(String str) {
        return VBleClient_SendCommand(str, null);
    }

    public boolean VBleClient_SendCommand(String str, String ext) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null || mNotifyCharacteristic == null) {
            Log.w(TAG, "BluetoothAdapter not initialized : mBluetoothAdapter=" + mBluetoothAdapter
                    + "  ,mBluetoothGatt=" + mBluetoothGatt
                    + "  ,mNotifyCharacteristic=" + mNotifyCharacteristic
            );
            return false;
        }

        for (String[] s : command) {
            if (s[0].equals(str)) {
                if (s[1] == null) return false;
                if (s[1].length() == 0) return false;

                String chara = s[1];

                byte[] arrayOfByte1 = new byte[20];
                byte[] arrayOfByte2 = new byte[20];
                arrayOfByte2[0] = 0;
                mNotifyCharacteristic.setValue(arrayOfByte2[0], 17, 0);

                // NOTE:
                // 1. 带参数命令包含"=" 符号
                // 2. 参数不做判断，只要不为空，变添加到AT命令末尾
                if (s[1].contains("=") && ext != null) {
                    chara += ext;
                }
                Log.d(TAG, "VBleClient_SendCommand: sendCharacteristic=" + chara);
                arrayOfByte1 = chara.getBytes();
                mNotifyCharacteristic.setValue(arrayOfByte1);


                return mBluetoothGatt.writeCharacteristic(mNotifyCharacteristic);
            }
        }

        return false;
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d(TAG, "BLEService.onLeScan: Scam successful! BLE Device:" + device.getAddress());
            // 扫描得到指定设备的蓝牙信息,并连接此设备
            // TODO : 需要处理多个设备同名的情况
            boolean isFindSpecDevice = false;

            if ("BLE 55B.02".equals(device.getName())) {// ‘BLE 55B.02’ for inner test devices ...
                isFindSpecDevice = true;
            } else if ("Q11".equals(device.getName()) || "Yujia_Ble".equals(device.getName())) {
                isFindSpecDevice = true;
            }

            if (isFindSpecDevice) {
                Log.d(TAG, "onLeScan: Find the Spec device：" + device.getName()
                        + ",add=" + device.getAddress()
                        + " ,uuid=" + device.getUuids());

                mVBleStatus = VBleResult.VBLE_STATUS_SCANNED;
                VBleResult result = new VBleResult(VBleResult.VBLE_OPERATOR_CONTROL,
                        VBleResult.VBLE_STATUS_SCANNED,
                        null);
                ProcessResultReport(mVBleCallback, result);
                ProcessUnsolicitedMsg(mVBleCallback, device.getName());

                mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
                if (mBluetoothGatt != null) {
                    if (!mBluetoothGatt.connect()) {
                        Log.d(TAG, " ERROR : mBluetoothGatt.connect is Fail!");
                    }
                } else {
                    Log.d(TAG, "ERROR : mBluetoothGatt is NULL!");
                }

                // 停止扫描
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    };

    private void ConnectBLEService() {
        final UUID[] mSpecUUID = {SPECIFIC_SERVICE_UUID};

        if (mBluetoothAdapter != null) {
            // 首先扫描指定设备
            // TODO : uuid? 此方法没法正常工作
            //boolean ret = mBluetoothAdapter.startLeScan(mSpecUUID, mLeScanCallback);

            //扫描所有ble设备
            boolean ret = mBluetoothAdapter.startLeScan(mLeScanCallback);
            if (ret) {
                // 更新状态 - 扫描中...
                mVBleStatus = VBleResult.VBLE_STATUS_SCANNING;

                VBleResult result = new VBleResult(VBleResult.VBLE_OPERATOR_CONTROL,
                        VBleResult.VBLE_STATUS_SCANNING,
                        null);
                ProcessResultReport(mVBleCallback, result);

            }
            Log.d(TAG, "ConnectBLEService: startLeScan ret=" + ret);
        }
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "GattCallback: onConnectionStateChange: newState" + newState);
            ProcessResultReport(mVBleCallback,
                    new VBleResult(VBleResult.VBLE_OPERATOR_CONTROL,
                            newState,
                            null));

            // 当GATT连接完成后，开始发现其服务
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mVBleStatus = VBleResult.VBLE_STATUS_CONNECTED;

                if (!mBluetoothGatt.discoverServices()) {
                    Log.d(TAG, "onConnectionStateChange:ERROR-BluetoothGatt.discoverServices is Fail!");
                }
            } else {
                mVBleStatus = VBleResult.VBLE_STATUS_DISCONNECTED;
            }
        }

        //发现服务
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "GattCallback: onServicesDiscovered: status=" + status);
            if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                Log.w(TAG, "GattCallback: onServicesDiscovered : BluetoothAdapter not initialized");
                return;
            }

            BluetoothGattService gattServices = mBluetoothGatt.getService(SPECIFIC_SERVICE_UUID);

            if (gattServices != null) {
                mNotifyCharacteristic = gattServices.getCharacteristic(READWRITE_CHARACTERISTIC_UUID);
            }

            EnableCharacteristicNotify();

            // For now , no descriptors form service ...
            for (BluetoothGattDescriptor dp : mNotifyCharacteristic.getDescriptors()) {
                dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(dp);
            }

            mVBleStatus = VBleResult.VBLE_STATUS_ENABLE_GATT_CHARACTERISTIC;
            ProcessResultReport(mVBleCallback, new VBleResult(VBleResult.VBLE_OPERATOR_CONTROL,
                    VBleResult.VBLE_STATUS_ENABLE_GATT_CHARACTERISTIC,
                    null));
        }

        //被读
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.d(TAG, "GattCallback: onCharacteristicRead: characteristic=" + new String(characteristic.getValue())
                    + " , status=" + status);
            EnableCharacteristicNotify();
        }

        //特性改变
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            EnableCharacteristicNotify();

            // 接收和发送消息，产生此回调
            byte[] msgByte = characteristic.getValue();
            String msgString = new String(msgByte);

            Log.d(TAG, "GattCallback: onCharacteristicChanged: msgString=" + msgString);

            if (!ProcessCommandCallback(mVBleCallback, msgString)) {

                // 如果上报的消息不在已知的命令内， 透传此消息；
                ProcessUnsolicitedMsg(mVBleCallback, msgString);
            }
        }

        //特性书写
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            Log.d(TAG, "GattCallback: onCharacteristicWrite: characteristic=" + new String(characteristic.getValue())
                    + " ,status=" + status);
            EnableCharacteristicNotify();
            /*
             * DO NOTHING... 在onCharacteristicChanged统一处理
             */
        }
    };

    private void EnableCharacteristicNotify() {
        if (mBluetoothGatt != null && mNotifyCharacteristic != null) {
            boolean ret = mBluetoothGatt.setCharacteristicNotification(mNotifyCharacteristic, true);

            if (!ret) {
                Log.d(TAG, "EnableCharacteristicNotify is fail!");
            }
        }
    }

    private boolean ProcessCommandCallback(VBleCallback callback, String repString) {
        if (repString == null) return false;

        for (String[] s : command) {
            if (repString.startsWith(s[2])) {
                if (callback != null) {
                    callback.onVBleCommandCallback(s[0], true);
                    return true;
                }
            }
        }

        return false;
    }

    private void ProcessResultReport(VBleCallback callback, VBleResult result) {
        if (callback != null) {
            callback.onVBleStatusCallback(result);
        }
    }

    private void ProcessUnsolicitedMsg(VBleCallback callback, String unsolicatedMsg) {
        if (callback != null) {
            callback.processUnsolicitedMsg(unsolicatedMsg);
        }
    }
}
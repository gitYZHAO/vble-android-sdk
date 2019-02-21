package com.viroyal.fastble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanAndConnectCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;

/**
 * Created by zy on 2018/6/27.
 */
public interface Ble {

    /**
     * 设置目标蓝牙 mac
     */
    Ble setTargetMac(String mac);

    /**
     * 设置目标蓝牙 广播名称
     */
    Ble setTargetBroadcastNames(String broadcastNames);


    /**
     * 设置目标蓝牙 UUID
     */
    Ble setTargetUUID(String uuid);

    /**
     * 设置默认扫描设置
     */
    void setScanRule();
    /**
     * 设置是否自动连接
     */
    Ble setAutoConnect(boolean autoconnect);

    /**
     * 开始扫描蓝牙BLE服务
     *
     * @param bleScanCallback 扫描结果回调
     */
    Ble startScanBle(BleScanCallback bleScanCallback);

    /**
     * 停止扫描蓝牙BLE服务
     * @return
     */
    void CancelScanBle();

    /**
     * 扫描，找到后自动连接
     */
    Ble scanAndConnect(BleScanAndConnectCallback callback);

    /**
     * 连接一个已知的蓝牙设备
     */
    Ble connect(BleDevice bleDevice, BleGattCallback bleGattCallback);

    /**
     * 断开连接指定的BLE设备
     */
    Ble disConnect(BleDevice bleDevice);

    /**
     * 获取蓝牙服务信息
     */
    BluetoothGatt getBluetoothGatt(BleDevice bleDevice);

    /**
     * 蓝牙是否已经连接
     */
    boolean isConnected(BleDevice bleDevice);

    /**
     * 发送消息
     */
    void write(BluetoothGattCharacteristic c, BleDevice b,
               String content, BleWriteCallback bleWriteCallback);

    /**
     * 读取消息
     */
    void noti(BluetoothGattCharacteristic c, BleDevice b, BleNotifyCallback bleReadCallback);

    /**
     * 设置MTU大小
     */
    void setMtu(BleDevice bleDevice, int mtu, BleMtuChangedCallback callback);
}

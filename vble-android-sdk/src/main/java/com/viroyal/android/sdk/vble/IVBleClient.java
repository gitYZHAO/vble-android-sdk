package com.viroyal.android.sdk.vble;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

public interface IVBleClient {

    /**
     * 获取当前蓝牙固件控制模块，并设置回调方法
     */
    void init(Application sContext , IVBleCallback callback);

    /**
     * 获取当前蓝牙Audio和BLE是否已经连接
     */
    boolean isBTAudioAndBleAvailable();

    /**
     * 获取当前蓝牙Audio是否已经连接
     */
    boolean isBTAudioAvailable();

    /**
     * 获取当前BLE是否已经连接
     */
    boolean isBleAvailable();

    /**
     * 获取当前BLE的BluetoothDevice
     */
    BluetoothDevice getConnectedBLEDevice();

    /**
     * 设置FM频段
     */
    void setFM(String fm);

    /**
     * 获取固件版本号
     */
    String getFirmwareVersion();

    /**
     * 获取固件客户号
     */
    String getFirmwareCustomer();

    /**
     * 通知蓝牙拨打电话
     */
    void sendCustString(String custString);

    /**
     * 通知蓝牙拨打电话
     */
    void makePhoneCall(String callNum);

    /**
     * 通知蓝牙接听电话
     */
    void answer();

    /**
     * 通知蓝牙挂断电话
     */
    void hangUp();

    /**
     * 通知蓝牙当前开始语音识别
     */
    void voiceStart();

    /**
     * 通知蓝牙当前结束语音识别
     */
    void voiceStop();

    /**
     * 使用蓝牙录音
     */
    void enableBTRecord(boolean on);

    /**
     * 使用喇叭播放
     */
    void setSpeakerphoneOn(boolean on);
}

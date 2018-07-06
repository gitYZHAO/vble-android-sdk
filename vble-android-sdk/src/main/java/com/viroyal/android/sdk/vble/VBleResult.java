package com.viroyal.android.sdk.vble;

import android.bluetooth.BluetoothDevice;

public class VBleResult {
    public static final int VBLE_OPERATOR_CONTROL = 1;
    public static final int VBLE_OPERATOR_SEND_COMMAND = 2;

    public static final int VBLE_STATUS_DISCONNECTED = 0;
    public static final int VBLE_STATUS_CONNECTING = 1;
    public static final int VBLE_STATUS_CONNECTED = 2;
    public static final int VBLE_STATUS_DISCONNECTING = 3;
    public static final int VBLE_STATUS_SCANNING = 4;
    public static final int VBLE_STATUS_SCANNED = 5;
    public static final int VBLE_STATUS_ENABLE_GATT_CHARACTERISTIC = 6;
    public static final String[] vBLE_Status = {
            "VBLE_STATUS_DISCONNECTED",
            "VBLE_STATUS_CONNECTING",
            "VBLE_STATUS_CONNECTED",
            "VBLE_STATUS_DISCONNECTING",
            "VBLE_STATUS_SCANNING",
            "VBLE_STATUS_SCANNED",
            "VBLE_STATUS_ENABLE_GATT_CHARACTERISTIC",
            "NULL", "NULL", "NULL", "NULL"};

    public static final int VBLE_ERROR_NULL = 0;


    private BluetoothDevice device;
    private int mVBle_operator;
    private int mVBle_status;

    private String mVBle_reportString;

    public VBleResult(int opt, int status, String reportString) {
        mVBle_operator = opt;
        mVBle_status = status;
        mVBle_reportString = reportString;
    }

    public int getmVBle_status() {
        return mVBle_status;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public boolean isVBleServiceConnected() {
        return (mVBle_status == VBLE_STATUS_CONNECTED);
    }

    public boolean isVBleCanSendCommand() {
        return (mVBle_status == VBLE_STATUS_ENABLE_GATT_CHARACTERISTIC);
    }
}

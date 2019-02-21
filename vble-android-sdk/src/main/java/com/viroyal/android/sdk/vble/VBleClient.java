package com.viroyal.android.sdk.vble;

import android.Manifest;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.viroyal.connection.BleConnect;
import com.viroyal.connection.BluetoothConnect;
import com.viroyal.fastble.FastBle;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class VBleClient implements IVBleClient {

    private final String TAG = "[BTM]" + getClass().getSimpleName();

    private Context sContext;
    private static IVBleCallback mVBleCallback;

    private BluetoothConnect mBluetoothConnect;
    private BleConnect mBleConnect;
    //private BTDataManager mDM;

    private int mtuSize;
    private boolean isSendingCommand = false;
    private String mFirmwareVersion;
    private String mFirmwareCustomer;

    private final static String VBLE_SEND_COMMAND_CALL_ANSWER = "AT+CALLANSW";
    private final static String VBLE_SEND_COMMAND_CALL_END = "AT+CALLEND";
    private final static String VBLE_SEND_COMMAND_MAKE_CALL = "AT+DIAL=";
    private final static String VBLE_SEND_COMMAND_SET_FM = "AT+FMFREQ=";
    private final static String VBLE_SEND_COMMAND_VOICE_START = "AT+VOICESTART";
    private final static String VBLE_SEND_COMMAND_VOICE_STOP = "AT+VOICESTOP";
    private final static String VBLE_SEND_COMMAND_RECORD_START = "AT+RECORDSTART";
    private final static String VBLE_SEND_COMMAND_RECORD_STOP = "AT+RECORDSTOP";
    private final static String VBLE_SEND_COMMAND_GET_VERSION = "AT+VERSION";
    private final static String VBLE_SEND_COMMAND_GET_CUSTOMER = "AT+CUSTOMER";

    private final static String VBLE_RECEIVE_COMMAND_WAKE_UP = "AT+WAKEUP";
    private final static String VBLE_RECEIVE_COMMAND_PSERSOR_FAR = "AT+PSERSORFAR";
    private final static String VBLE_RECEIVE_COMMAND_CALL_ANSWER = "+CALLANSW";
    private final static String VBLE_RECEIVE_COMMAND_CALL_END = "+CALLEND";
    private final static String VBLE_RECEIVE_COMMAND_MAKE_CALL = "+DIAL";
    private final static String VBLE_RECEIVE_COMMAND_SET_FM = "+FMFREQ";
    private final static String VBLE_RECEIVE_COMMAND_VOICE_START = "+VOICESTART";
    private final static String VBLE_RECEIVE_COMMAND_VOICE_STOP = "+VOICESTOP";
    private final static String VBLE_RECEIVE_COMMAND_RECORD_START = "+RECORDSTART";
    private final static String VBLE_RECEIVE_COMMAND_RECORD_STOP = "+RECORDSTOP";
    private final static String VBLE_RECEIVE_COMMAND_GET_VERSION = "+VER";
    private final static String VBLE_RECEIVE_COMMAND_GET_CUSTOMER = "+CUSTOMER";

    private final String[] mSendCommand = {
            VBLE_SEND_COMMAND_CALL_ANSWER,
            VBLE_SEND_COMMAND_CALL_END,
            VBLE_SEND_COMMAND_MAKE_CALL,
            VBLE_SEND_COMMAND_SET_FM,
            VBLE_SEND_COMMAND_VOICE_START,
            VBLE_SEND_COMMAND_VOICE_STOP,
            VBLE_SEND_COMMAND_RECORD_START,
            VBLE_SEND_COMMAND_RECORD_STOP,
            VBLE_SEND_COMMAND_GET_VERSION,
            VBLE_SEND_COMMAND_GET_CUSTOMER,
    };

    private final String[] mReceiveCommand = {
            VBLE_RECEIVE_COMMAND_WAKE_UP,
            VBLE_RECEIVE_COMMAND_PSERSOR_FAR,
            VBLE_RECEIVE_COMMAND_CALL_ANSWER,
            VBLE_RECEIVE_COMMAND_CALL_END,
            VBLE_RECEIVE_COMMAND_MAKE_CALL,
            VBLE_RECEIVE_COMMAND_SET_FM,
            VBLE_RECEIVE_COMMAND_VOICE_START,
            VBLE_RECEIVE_COMMAND_VOICE_STOP,
            VBLE_RECEIVE_COMMAND_RECORD_START,
            VBLE_RECEIVE_COMMAND_RECORD_STOP,
            VBLE_RECEIVE_COMMAND_GET_VERSION,
            VBLE_RECEIVE_COMMAND_GET_CUSTOMER,
    };

    private final static int MSG_WHAT_RECHECK_BT_STATUS = 101;

    private final static int MSG_WHAT_HAND_STATUS = 102;

    private final static int MSG_WHAT_RECHECK_COMMAND = 103;

    private final static int MSG_WHAT_SET_MTU = 104;

    private final static int MSG_WHAT_INIT_DONE = 105;

    private final String CHECK_COMMANDS = "CHECK_COMMANDS";

    private Handler mHandler;

    private int btConnectState = VBleState.DISCONNECTED;
    private int bleConnectState = VBleState.DISCONNECTED;

    private Queue<String> commands = new LinkedList<>();

    private static class BTManagerHold {
        public BTManagerHold() {
        }

        private final static IVBleClient sBTManager = new VBleClient();
    }

    public VBleClient() {
    }

    public static IVBleClient getInstance() {
        return BTManagerHold.sBTManager;
    }

    public void init(Application sContext, IVBleCallback callback) {
        this.sContext = sContext;
        mVBleCallback = callback;

        //初始化蓝牙Audio监听
        mBluetoothConnect = new BluetoothConnect(sContext);
        //mDM = BTDataManager.getInstance();

        //初始化BLE相关
        FastBle.init(sContext);
        mBleConnect = new BleConnect();

        // BLE设备GATT回调
        mBleConnect.setBleGattCallback(new BleGattCallback() {
            @Override
            public void onStartConnect() {
                bleConnectState = VBleState.CONNECTING;
                onBTStatusChanged();
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException e) {
                bleConnectState = VBleState.CONNECTED_FAILED;
                onBTStatusChanged();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt bluetoothGatt, int i) {
                bleConnectState = VBleState.CONNECTED;
                onBTStatusChanged();
            }

            @Override
            public void onDisConnected(boolean b, BleDevice bleDevice, BluetoothGatt bluetoothGatt, int i) {
                bleConnectState = VBleState.DISCONNECTED;
                onBTStatusChanged();
            }
        });

        // BLE设备Notify 回调
        mBleConnect.setBleNotifyCallback(new BleNotifyCallback() {
            @Override
            public void onNotifySuccess() {
                Log.d(TAG, "read onNotifySuccess  []:");
                Message.obtain(mHandler, MSG_WHAT_SET_MTU).sendToTarget();
            }

            @Override
            public void onNotifyFailure(BleException e) {
                Log.d(TAG, "read onNotifyFailure  [e]:" + e.getDescription());
            }

            @Override
            public void onCharacteristicChanged(byte[] bytes) {
                Log.d(TAG, "read onCharacteristicChanged [bytes]:" + new String(bytes));
                try {
                    boolean isCommand = false;
                    if (bytes[0] == 0x2B /*0x2B= "+"*/ ||
                            (bytes[0] == 0x41 && bytes[1] == 0x54) /*0x41= "A" , 0x54= "T"*/
                            ) {
                        // 解析接收到的特征通知消息
                        isCommand = parseCommand(new String(bytes, "UTF-8"));
                    }

                    //if (!isCommand) {
                    //如果不是命令，则视为音频数据
                    //if (mDM != null && mDM.isSocketConnected()) {
                    //    mDM.updateReceiveFileDate(bytes);
                    //} else {
                    //    Log.d(TAG, "onCharacteristicChanged: 丢弃语音数据");
                    //}
                    //}
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });

        // BLE设备设置MTU回调
        mBleConnect.setBleMtuCB(new BleMtuChangedCallback() {
            @Override
            public void onSetMTUFailure(BleException e) {
                Log.e(TAG, "onSetMTUFailure");
                processSetMTUComplete();
            }

            @Override
            public void onMtuChanged(int i) {
                mtuSize = i;
                Log.d(TAG, "onMtuChanged: mtu=" + mtuSize);
                processSetMTUComplete();
            }
        });

        //搜索，并自动连接BLE蓝牙
        //mBleConnect.searchBLE();

        // 设置监听蓝牙Audio等profile回调
        mBluetoothConnect.setBTCallBack(new BluetoothConnect.BTCallBack() {
            @Override
            public void statusChange(int status) {
                int oldBTStatus = btConnectState;
                //蓝牙状态变化回调
                if (status == VBleState.CONNECTED) {
                    //蓝牙Audio连接成功

                    // 根据蓝牙名称去搜索其BLE设备
                    String name = mBleConnect.getConnectedBLEName(
                            mBluetoothConnect.getConnectedBTDevice());
                    if (name != null) {
                        mBleConnect.searchBLE(name);
                    }
                } else if (status == VBleState.DISCONNECTED) {
                    //蓝牙断开连接
                    //同时断开BLE连接
                    mBleConnect.disconnectBleDevice();
                }

                btConnectState = status;
                Log.d(TAG, "statusChange  [btConnectState status]:" + btConnectState);
                onBTStatusChanged();

                // 蓝牙从连接到断开，需要弹出
                if (oldBTStatus == VBleState.CONNECTED
                        && btConnectState == VBleState.DISCONNECTED) {
                    checkBTDeviceAvailablePrompt();
                }
            }
        });

        mHandler = new Handler(sContext.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_WHAT_RECHECK_BT_STATUS:
                        checkBTDeviceAvailablePrompt();
                        break;
                    case MSG_WHAT_HAND_STATUS:
                        ProcessUnsolicitedMsg("wakeup-ble");
                        break;
                    case MSG_WHAT_RECHECK_COMMAND:
                        sendCommandToBLEDevice(CHECK_COMMANDS);
                        break;
                    case MSG_WHAT_SET_MTU:
                        setMTU();
                        break;
                    case MSG_WHAT_INIT_DONE:
                        sendCommandToBLEDevice(VBLE_SEND_COMMAND_GET_VERSION);
                        sendCommandToBLEDevice(VBLE_SEND_COMMAND_GET_CUSTOMER);
                        break;
                    default:
                        break;
                }
            }
        };
    }

//    public void close() {
//        if (mDM != null) {
//            mDM.closeBTDataResource();
//        }
//    }

    /**
     * 检查蓝牙状态，如果蓝牙不可用将弹出提示
     */
    public boolean checkBTDeviceAvailablePrompt() {
        if (!isBTAudioAndBleAvailable()) {
            if (mHandler.hasMessages(MSG_WHAT_RECHECK_BT_STATUS)) {
                Log.d(TAG, "Already check , please wait a sec ... ");
                return false;
            }

            if (btConnectState == VBleState.CONNECTED
                    && bleConnectState == VBleState.CONNECTING) {
                Log.d(TAG, "BLE status is CONNECTING , Recheck late...");
                mHandler.sendEmptyMessageDelayed(MSG_WHAT_RECHECK_BT_STATUS, 10000);
                return false;
            }

            Log.d(TAG, "需要重新连接蓝牙提示- btConnectState=" + btConnectState
                    + " ,bleConnectState=" + bleConnectState);

        }

        return true;
    }

    /**
     * BLE 初始化完成后处理
     * <p>
     * -- 获取固件的版本号和客户代码
     */
    private void processSetMTUComplete() {
        bleConnectState = VBleState.INIT_DONE;
        onBTStatusChanged();
        checkReservedCommand();
        mHandler.obtainMessage(MSG_WHAT_INIT_DONE).sendToTarget();
    }

    private void onBTStatusChanged() {
        int state;
        if (bleConnectState == VBleState.CONNECTING || btConnectState == VBleState.CONNECTING) {
            //正在连接,只要有一个是正在连接，认为是连接中
            state = VBleState.CONNECTING;
        } else if (bleConnectState == VBleState.CONNECTED && btConnectState == VBleState.CONNECTED) {
            //连个都连接上了，认为是连接成功
            state = VBleState.CONNECTED;
        } else if (bleConnectState == VBleState.INIT_DONE && btConnectState == VBleState.CONNECTED) {
            //BLE初始化完成
            state = VBleState.INIT_DONE;
        } else if (bleConnectState == VBleState.CONNECTED_FAILED || btConnectState == VBleState.CONNECTED_FAILED) {
            // Audio和BLE中任何一个连接失败， 都认为是 连接失败
            state = VBleState.CONNECTED_FAILED;
        } else {
            state = VBleState.DISCONNECTED;
        }
        Log.d(TAG, "onBTStatusChanged : btConnectState:" + btConnectState
                + ", bleConnectState:" + bleConnectState
                + ",  VBleState:" + state);

        // Notify BT status to register this event
        ProcessStatusCallback(state);
    }


    @Override
    public boolean isBTAudioAndBleAvailable() {
        return (btConnectState == VBleState.CONNECTED && bleConnectState == VBleState.INIT_DONE);
    }

    @Override
    public boolean isBTAudioAvailable() {
        return btConnectState == VBleState.CONNECTED;
    }

    /**
     * 获取当前BLE是否已经连接
     */
    @Override
    public boolean isBleAvailable() {
        return bleConnectState == VBleState.INIT_DONE;
    }

    /**
     * 获取当前BLE的BluetoothDevice
     */
    @Override
    public BluetoothDevice getConnectedBLEDevice() {
        return mBleConnect.getConnectedBLEDevice();
    }

    @Override
    public void setFM(String fm) {
        if (fm != null && fm.length() > 0) {
            String fmReq = fm.replace(".", "");
            sendCommandToBLEDevice(VBLE_SEND_COMMAND_SET_FM + fmReq);
        }
    }

    @Override
    public String getFirmwareVersion() {
        return mFirmwareVersion;
    }

    @Override
    public String getFirmwareCustomer() {
        return mFirmwareCustomer;
    }

    @Override
    public void sendCustString(String custString) {
        if (custString != null && !custString.isEmpty()) {
            sendCommandToBLEDevice(custString);
        }
    }

    @Override
    public void makePhoneCall(String callNum) {
        if (callNum != null && callNum.length() > 2) {
            sendCommandToBLEDevice(VBLE_SEND_COMMAND_MAKE_CALL + callNum);
        }
    }

    @Override
    public void answer() {
        sendCommandToBLEDevice(VBLE_SEND_COMMAND_CALL_ANSWER);
    }

    @Override
    public void hangUp() {
        sendCommandToBLEDevice(VBLE_SEND_COMMAND_CALL_END);
    }

    @Override
    public void voiceStart() {
        sendCommandToBLEDevice(VBLE_SEND_COMMAND_VOICE_START);
    }

    @Override
    public void voiceStop() {
        sendCommandToBLEDevice(VBLE_SEND_COMMAND_VOICE_STOP);
    }

    @Override
    public void enableBTRecord(boolean on) {
        if (mBluetoothConnect != null) {
            if (on && isBTAudioAvailable()) {
                mBluetoothConnect.enableAudioModeCOMMUNICATION();
            } else {
                mBluetoothConnect.enableAudioModeNORMAL();
            }
        }
    }

    @Override
    public void setSpeakerphoneOn(boolean on) {
        if (mBluetoothConnect != null) {
            mBluetoothConnect.setSpeakerphoneOn(on);
        }
    }

    /*
     *  发送AT命令到BLE设备 ：
     *
     *  1. 如果当前ble没有连接，那么将丢弃命令。
     *  2. 如果当前ble已经连接，那么将判断是否立即发送，否则将保存命令等待条件发送。
     *  3. 不支持错误重连。
     * */
    private void sendCommandToBLEDevice(@NonNull String command) {

        if (mBleConnect == null) {
            Log.e(TAG, "sendCommandToBLEDevice: mBleConnect is null ");
        }

        if (mBleConnect != null && !mBleConnect.isConnected()) {
            Log.e(TAG, "sendCommandToBLEDevice: BLE device is NOT connected! ");
            return;
        }

        if (!isBleAvailable() || isSendingCommand) {
            commands.add(command);
            Log.d(TAG, "sendCommandToBLEDevice: setMUT NOT Complete, PUSH IN, size=" + commands.size());
            return;
        }

        if (CHECK_COMMANDS.equals(command)) {
            pollReservedCommand();
            return;
        }

        if (!commands.isEmpty()) {
            commands.add(command);
            Log.d(TAG, "sendCommandToBLEDevice: commands NOT Empty, PUSH IN, size= " + commands.size());
            return;
        }

        sendCommandDirect(command);
    }

    private void sendCommandDirect(final String command) {
        isSendingCommand = true;
        mBleConnect.writeGattCharacteristic(command,
                new BleConnect.IWriteGattCharacteristicCB() {
                    @Override
                    public void onWriteGattCharacteristicSuccessCB() {
                        isSendingCommand = false;
                        checkReservedCommand();
                        ProcessCommandCallback(command, true);
                    }

                    @Override
                    public void onWriteGattCharacteristicFailCB() {
                        isSendingCommand = false;
                        checkReservedCommand();
                        ProcessCommandCallback(command, false);
                    }
                });
    }

    private void checkReservedCommand() {
        if (!commands.isEmpty()) {
            Log.d(TAG, "checkReservedCommand: Size= " + commands.size());
            Message message = mHandler.obtainMessage(MSG_WHAT_RECHECK_COMMAND);
            message.obj = null;
            mHandler.sendMessage(message);
        }
    }

    private void pollReservedCommand() {
        if (!commands.isEmpty()) {
            Log.d(TAG, "pollReservedCommand: Size=" + commands.size());
            sendCommandDirect(commands.poll());
            return;
        }

        Log.d(TAG, "pollReservedCommand: Command is NULL");
    }


    private void setMTU() {
        if (mBleConnect != null) {
            mBleConnect.setBleMtu();
        }
    }

    /**
     * @param ss 御驾上报和反馈的字串
     */
    private boolean parseCommand(String ss) {
        if (TextUtils.isEmpty(ss)) {
            return false;
        }

        boolean isFindCommand = false;
        for (String rc :
                mReceiveCommand) {
            if (ss.startsWith(rc)) {
                isFindCommand = true;
                break;
            }
        }

        if (!isFindCommand) {
            ProcessUnsolicitedMsg(ss);
            return false;
        }

        //+WAKEUP\r\nOK\r\n
        if (ss.startsWith(VBLE_RECEIVE_COMMAND_WAKE_UP)) {
            //处理唤醒操作
            mHandler.sendEmptyMessageDelayed(MSG_WHAT_HAND_STATUS, 2000);
        } else if (ss.startsWith(VBLE_RECEIVE_COMMAND_PSERSOR_FAR)) {
            mHandler.removeMessages(MSG_WHAT_HAND_STATUS);
        }

        //+VER:Q11_V21\r\nOK\r\n
        if (ss.startsWith(VBLE_RECEIVE_COMMAND_GET_VERSION)) {
            mFirmwareVersion = ss.substring(ss.indexOf(":") + 1, ss.indexOf("\r"));
            Log.d(TAG, "GET Firmware Version：" + mFirmwareVersion);
        }

        //+CUSTOMER:YUJIA\r\nOK\r\n
        if (ss.startsWith(VBLE_RECEIVE_COMMAND_GET_CUSTOMER)) {
            mFirmwareCustomer = ss.substring(ss.indexOf(":") + 1, ss.indexOf("\r"));
            Log.d(TAG, "GET Firmware Customer：" + mFirmwareCustomer);
        }

        return true;
    }

    private void ProcessStatusCallback(int state) {
        if (mVBleCallback != null) {
            mVBleCallback.onVBleStatusCallback(state);
        }
    }

    private void ProcessUnsolicitedMsg(String msg) {
        if (mVBleCallback != null) {
            mVBleCallback.processUnsolicitedMsg(msg);
        }
    }

    private void ProcessCommandCallback(String command, boolean done) {
        if (mVBleCallback != null) {
            mVBleCallback.onVBleCommandCallback(command, done);
        }
    }
}
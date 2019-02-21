package com.viroyal.wearable;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.mediatek.ctrl.fota.common.FotaOperator;
import com.mediatek.ctrl.fota.common.FotaVersion;
import com.mediatek.ctrl.fota.common.IFotaOperatorCallback;
import com.mediatek.wearable.WearableManager;
import com.viroyal.android.sdk.vble.R;

public class FirmwareUpdateAdapter {
    final private String TAG = "[BTM]FirmwareUpdate";

    // TODO  固件版本路径需要更新
    final private String FirmwareImagePath =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/image.bin";

    private static final int MGS_TEXT_VIEW_UPDATE = 400;
    private static final int MSG_SEND_TIME_OUT = 401;
    private static final int MSG_CHECK_DEVICE_REBOOT = 402;

    private static final int SEND_TIMEOUT = 1 * 60 * 1000;

    public static final int STATE_FOTA_IDLE = 0;
    public static final int STATE_FOTA_SENDING = 1;
    public static final int STATE_FOTA_ERROR = 2;
    public static final int STATE_FOTA_TIME_OUT = 3;
    public static final int STATE_FOTA_WAIT_DEVICE_REBOOT = 4;
    public static final int STATE_FOTA_DEVICE_REBOOTED = 5;

    private FirmwareUpdateCB mFUCallback = null;
    private WearableManager mWearableManager = null;
    private FotaOperator mFotaOperator;
    private Context mContext;
    private boolean mIsBtTransferFinished = false;
    private boolean mTransferViaBTErrorHappened = false;
    private int mState = STATE_FOTA_IDLE;

    public FirmwareUpdateAdapter(Context context) {
        mContext = context;

        mWearableManager = WearableManager.getInstance();
        boolean isSuccess = mWearableManager.init(true, context, null, R.xml.wearable_config);
        Log.d(TAG, "WearableManager init " + isSuccess);
    }

    public void setRemoteDevice(BluetoothDevice device) {
        if (device == null) {
            Log.e(TAG, "setRemoteDevice: BLE device is null");
            return;
        }

        if (mWearableManager.getWorkingMode() == WearableManager.MODE_SPP) {
            mWearableManager.switchMode();
        }

        mWearableManager.setRemoteDevice(device);
        mWearableManager.connect();
    }

    public boolean isAvailable() {
        if (mWearableManager.getRemoteDevice() == null) {
            Log.d(TAG, "isAvailable: BT RemoteDevice is null");
            return false;
        }
        return mWearableManager.isAvailable();
    }

    public void initWearableFota() {
        mFotaOperator = FotaOperator.getInstance(mContext);
        mFotaOperator.registerFotaCallback(mFotaCallback);
    }

    public void start() {
        Log.d(TAG, "start: mstate=" + mState);
        if (getState() == STATE_FOTA_IDLE || getState() == STATE_FOTA_ERROR) {
            mState = STATE_FOTA_SENDING;
            removeTimeOutMessage(true);

            Log.d(TAG, "[doInBackground][FOTA-Measure] sendFotaFirmwareData : begin  ");
            // Request Connection Parameter
            // boolean res = WearableManager.getInstance().requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
            // Log.d(TAG, "[updateTextView][FOTA-Measure] begin " + res);

            mFotaOperator.sendFotaFirmwareData(FotaOperator.TYPE_FIRMWARE_FULL_BIN,
                    FirmwareImagePath);

        }
    }

    public void close() {
        removeTimeOutMessage(false);
        mFotaOperator.unregisterFotaCallback(mFotaCallback);

        if (mWearableManager != null) {
            //TODO 如果当前状态为连接状态，disconnect导致连接异常
            //mWearableManager.disconnect();
            mWearableManager.destroy();
        }
    }

    private void connect() {
        if (mWearableManager != null) {
            if (mWearableManager.getRemoteDevice().getAddress() != null
                    && !mWearableManager.isConnecting()) {
                Log.d(TAG, "do connect...");
                mWearableManager.connect();
            } else {
                Log.e(TAG, "WearableManager state is NOT available :" +
                        " Address=" + mWearableManager.getRemoteDevice().getAddress() +
                        ", isConnecting=" + mWearableManager.isConnecting());
            }
        } else {
            Log.e(TAG, "connect: WearableManager is null");
        }
    }

    public void setFirmwareUpdateCB(FirmwareUpdateCB cb) {
        mFUCallback = cb;
    }

    private Handler mHandler = new Handler(Looper.myLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "[handleMessage] msg.what" + msg.what);
            switch (msg.what) {
                case MSG_SEND_TIME_OUT:
                    mState = STATE_FOTA_TIME_OUT;
                    break;
                case MSG_CHECK_DEVICE_REBOOT:
                    connect();
                    break;
                default:
                    break;
            }
        }
    };

    private IFotaOperatorCallback mFotaCallback = new IFotaOperatorCallback() {

        @Override
        public void onCustomerInfoReceived(String information) {
            Log.d(TAG, "[onCustomerInfoReceived] information : " + information);
        }

        @Override
        public void onFotaVersionReceived(FotaVersion version) {
            Log.d(TAG, "[onFotaVersionReceived] FotaVersion : " + version);
        }

        @Override
        public void onStatusReceived(int status) {
            Log.d(TAG, "[onStatusReceived] status : " + status);

            switch (status) {
                /*
                 * 发送IMG到设备侧后，提示成功
                 * */
                case FotaUtils.FOTA_SEND_VIA_BT_SUCCESS:
                    Log.d(TAG, "[onStatusReceived] send succeed. update text view");
                    mIsBtTransferFinished = true;
//                mIsUpdating = false;
                    mTransferViaBTErrorHappened = false;
                    if (mFUCallback != null) {
                        mFUCallback.onFWSendSuccess();
                    }
                    break;

                /*
                 * FOTA升级成功后，等待设备重启后提示成功
                 * */
                case FotaUtils.FOTA_UPDATE_VIA_BT_SUCCESS:
                    mTransferViaBTErrorHappened = false;
                    mState = STATE_FOTA_IDLE;
                    if (mFUCallback != null) {
                        mFUCallback.onFWUpdateSuccess();
                    }
                    break;

                case FotaUtils.FOTA_UPDATE_VIA_BT_DISK_FULL:
                    Log.d(TAG, "[onStatusReceived] transfer error happened, FOTA_UPDATE_VIA_BT_DISK_FULL");
                    // remove pending FOTA BTNotify package
                    mWearableManager.clearSendList("fota");
                    mTransferViaBTErrorHappened = true;
                case FotaUtils.FOTA_UPDATE_VIA_BT_COMMON_ERROR:
                case FotaUtils.FOTA_UPDATE_VIA_BT_WRITE_FILE_FAILED:
                case FotaUtils.FOTA_UPDATE_VIA_BT_DATA_TRANSFER_ERROR:
                    Log.d(TAG, "[onStatusReceived] transfer error happened, set mTransferViaBTErrorHappened to be TRUE");
                    mTransferViaBTErrorHappened = true;
                case FotaUtils.FOTA_UPDATE_VIA_BT_TRIGGER_FAILED:
                case FotaUtils.FOTA_UPDATE_VIA_BT_FAILED:
                case FotaUtils.FOTA_UPDATE_TRIGGER_FAILED_CAUSE_LOW_BATTERY:
                case FotaUtils.FILE_NOT_FOUND_ERROR:
                case FotaUtils.READ_FILE_FAILED:
                    Log.d(TAG, "[onStatusReceived] update failed!");
                    mState = STATE_FOTA_ERROR;
                    mIsBtTransferFinished = false;

                    Message msg2 = mHandler.obtainMessage();
                    msg2.what = MGS_TEXT_VIEW_UPDATE;
                    if (status == FotaUtils.FILE_NOT_FOUND_ERROR) {
                        msg2.arg1 = FotaUtils.FILE_NOT_FOUND_ERROR;
                    } else if (status == FotaUtils.READ_FILE_FAILED) {
                        msg2.arg1 = FotaUtils.READ_FILE_FAILED;
                    } else {
                        msg2.arg1 = FotaUtils.MSG_ARG1_DOWNLOAD_FAILED;
                    }
                    mHandler.sendMessage(msg2);

                    reportFWUpdateFail();
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onConnectionStateChange(int newConnectionState) {
            switch (newConnectionState) {
                case WearableManager.STATE_CONNECT_LOST:
                    Log.d(TAG, "[onConnectionStateChange] state : STATE_CONNECT_LOST" +
                            ", mIsBtTransferFinished=" + mIsBtTransferFinished);

                    if (!mIsBtTransferFinished) {
                        Message msg = mHandler.obtainMessage();
                        msg.what = MGS_TEXT_VIEW_UPDATE;
                        msg.arg1 = FotaUtils.MSG_ARG1_UPDATE_FAILED_CAUSE_DISCONNECTED;
                        mHandler.sendMessage(msg);
                    }

                    // 配置默认为自动重连，
                    // 但是如果等待固件重启时候出现无法连接的情况，尝试主动重连。
                    if (mIsBtTransferFinished
                            && getState() == STATE_FOTA_WAIT_DEVICE_REBOOT) {
                        mState = STATE_FOTA_DEVICE_REBOOTED;
                    } else if (mIsBtTransferFinished
                            && getState() == STATE_FOTA_DEVICE_REBOOTED) {
                        mHandler.obtainMessage(MSG_CHECK_DEVICE_REBOOT).sendToTarget();
                    } else if (getState() != STATE_FOTA_WAIT_DEVICE_REBOOT) {
                        mState = STATE_FOTA_ERROR;
                        reportFWUpdateFail();
                    }
                    break;

                case WearableManager.STATE_CONNECTED:
                    if (getState() == STATE_FOTA_WAIT_DEVICE_REBOOT) {
                        mState = STATE_FOTA_IDLE;
                    }
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onProgress(int progress) {
            if (!mTransferViaBTErrorHappened) {
                Log.d(TAG, "[onProgress] progress : " + progress);
                removeTimeOutMessage(true);

                if (progress == 100) {
                    mState = STATE_FOTA_WAIT_DEVICE_REBOOT;

                    //发送完成后移除超时消息，不在发送
                    removeTimeOutMessage(false);
                }

                if (mFUCallback != null) {
                    mFUCallback.onFWUpdateProgress(progress);
                }
            } else {
                Log.d(TAG, "[onProgress] ERROR happened, Update progress STOP!");
            }
        }

    };

    private void reportFWUpdateFail() {
        if (mFUCallback != null) {
            mFUCallback.onFWUpdateFail();
        }

        removeTimeOutMessage(false);
    }

    private void removeTimeOutMessage(boolean isSendAgain) {
        if (mHandler.hasMessages(MSG_SEND_TIME_OUT)) {
            mHandler.removeMessages(MSG_SEND_TIME_OUT);
        }

        if (isSendAgain) {
            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(MSG_SEND_TIME_OUT),
                    SEND_TIMEOUT);
        }
    }

    public int getState() {
        return mState;
    }
}

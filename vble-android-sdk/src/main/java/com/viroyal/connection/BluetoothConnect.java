
package com.viroyal.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import com.viroyal.android.sdk.vble.Settings;
import com.viroyal.android.sdk.vble.VBleState;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class BluetoothConnect {
    private final String TAG = "[BTM]" + getClass().getSimpleName();

    private final static String TARGET_BLUETOOTH_NAME_PRE = "X1_";
    private BluetoothAdapter mBluetoothAdapter;
    private Context mContext;
    //蓝牙状态变化时的回调
    private BTCallBack mBTCallBack;
    private BluetoothHeadset mBluetoothHeadset;
    private BluetoothDevice mConnectedBTDevice;

    public BluetoothConnect(Context context) {
        Log.d(TAG, "BlueToothConnect  [context]:");

        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        BluetoothAdapter.getDefaultAdapter().getProfileProxy(context,
                new BluetoothProfile.ServiceListener() {
                    @Override
                    public void onServiceConnected(int profile, BluetoothProfile proxy) {
                        Log.i(TAG, "onServiceConnected: profile=" + profile + " ,proxy=" + proxy);
                        if (profile == BluetoothProfile.HEADSET) {
                            mBluetoothHeadset = ((BluetoothHeadset) proxy);
                            List<BluetoothDevice> connectedDevices = proxy.getConnectedDevices();
                            Log.i(TAG, "onServiceConnected: size= " + connectedDevices.size());

                            //only supports one connected Bluetooth Headset at a time
                            if (connectedDevices.size() == 1) {
                                if (getConnectedBTDevice() == null) {
                                    setConnectedBTDevice(connectedDevices.get(0));

                                    if (Settings.getInstance().isAudioForceToSpeaker()) setSpeakerphoneOn(true);
                                }
                            }
                        }
                    }

                    @Override
                    public void onServiceDisconnected(int profile) {

                    }
                }, BluetoothProfile.HEADSET);

        // 监听普通蓝牙设备的状态
        register();
    }

    /**
     * 判断是否支持蓝牙，并打开蓝牙
     * 获取到BluetoothAdapter之后，还需要判断是否支持蓝牙，以及蓝牙是否打开。
     * 如果没打开，需要让用户打开蓝牙：
     */
    /*public void checkBleDevice(Context context) {
        Log.d(TAG, "checkBleDevice  [context]:");
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Log.d(TAG, "checkBleDevice  [context]:Bluetooth not open");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(enableBtIntent);
            } else {
                //打印已经连接的设备
                Set<BluetoothDevice> bluetoothDevices = mBluetoothAdapter.getBondedDevices();
                for (BluetoothDevice device : bluetoothDevices) {
                    Log.d(TAG, "checkBleDevice  [context] name" + device.getName() +
                            ", status:" + device.getBondState() + ""
                    );
                }
                Log.d(TAG, "checkBleDevice  [context]:Bluetooth opened");
            }
        } else {
            Log.i(TAG, "该手机不支持蓝牙");
        }
    }*/

    /**
     * 判断是否支持蓝牙，并打开蓝牙
     * 获取到BluetoothAdapter之后，还需要判断是否支持蓝牙，以及蓝牙是否打开。
     * 如果没打开，需要让用户打开蓝牙：
     */
    public boolean checkBTDevice() {
        Log.d(TAG, "checkBTDevice  [context]:");
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Log.d(TAG, "checkBTDevice  [context]:Bluetooth not open");
                return false;
            } else {
                Log.d(TAG, "checkBTDevice  [context]:bluetooth open ");
                return true;

            }
        } else {
            Log.d(TAG, "checkBTDevice  [context]: device not support bluetooth");
            return false;
        }
    }

    /**
     * 搜索蓝牙设备
     * 通过调用BluetoothAdapter的startLeScan()搜索BLE设备。
     * 调用此方法时需要传入 BluetoothAdapter.LeScanCallback参数。
     * 因此你需要实现 BluetoothAdapter.LeScanCallback接口，BLE设备的搜索结果将通过这个callback返回。
     * <p/>
     * 由于搜索需要尽量减少功耗，因此在实际使用时需要注意：
     * 1、当找到对应的设备后，立即停止扫描；
     * 2、不要循环搜索设备，为每次搜索设置适合的时间限制。避免设备不在可用范围的时候持续不停扫描，消耗电量。
     * <p/>
     * 如果你只需要搜索指定UUID的外设，你可以调用 startLeScan(UUID[], BluetoothAdapter.LeScanCallback)方法。
     * 其中UUID数组指定你的应用程序所支持的GATT Services的UUID。
     * <p/>
     * 注意：搜索时，你只能搜索传统蓝牙设备或者BLE设备，两者完全独立，不可同时被搜索。
     */
    private boolean startSearchBltDevice(Context context) {
        Log.d(TAG, "startSearchBltDevice  [context]:");
        //开始搜索设备，当搜索到一个设备的时候就应该将它添加到设备集合中，保存起来
        checkBTDevice();
        //如果当前发现了新的设备，则停止继续扫描，当前扫描到的新设备会通过广播推向新的逻辑
        if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
            stopSearchBltDevice();
        }
        //开始搜索
        mBluetoothAdapter.startDiscovery();
        //这里的true并不是代表搜索到了设备，而是表示搜索成功开始。
        return true;
    }

    private boolean stopSearchBltDevice() {
        Log.d(TAG, "stopSearchBltDevice  []:");
        //暂停搜索设备
        if (mBluetoothAdapter != null) {
            return mBluetoothAdapter.cancelDiscovery();
        } else {
            return false;
        }
    }

    public BluetoothDevice getConnectedBTDevice() {
        return mConnectedBTDevice;
    }

    public void setConnectedBTDevice(BluetoothDevice device) {
        if (device != null) {
            mConnectedBTDevice = device;
        }
    }

    public boolean isConnectedToTargetBTDevice() {
        if (getConnectedBTDevice() != null) {
            return getConnectedBTDevice().getName().startsWith(TARGET_BLUETOOTH_NAME_PRE);
        }
        return false;
    }

    public void register() {
        Log.d(TAG, "register  []:");
        //注册蓝牙监听
        mContext.registerReceiver(mReceiver, makeFilter());
    }

    public void unregister() {
        Log.d(TAG, "unregister  []:");
        //注销蓝牙监听
        mContext.unregisterReceiver(mReceiver);
    }

    private IntentFilter makeFilter() {
        IntentFilter filter = new IntentFilter();
        //搜索发现设备
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        //状态改变
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //行动扫描模式改变了
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        //动作状态发生了变化
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //蓝牙连接成功
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        //蓝牙断开连接
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        //filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);

        //监听蓝牙HS的连接状态
        //  #EXTRA_STATE - The current state of the profile.
        // #EXTRA_PREVIOUS_STATE - The previous state of the profile.
        // #BluetoothDevice#EXTRA_DEVICE} - The remote device.
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);

        //监听蓝牙HS Audio的连接状态
        filter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);

        filter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED);
        //filter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        return filter;
    }

    /**
     * 蓝牙接收广播， 监听蓝牙连接的状态变化
     * <p>
     * intBOND_BONDED , 值为12;
     * intBOND_BONDING, 值为11;
     * intBOND_NONE, 值为10
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        //接收
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive  [action]:" + action);
            //VBleState VBleState = new VBleState();

            if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                int mmState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED);
                int mmPreState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE,
                        BluetoothProfile.STATE_DISCONNECTED);
                Log.d(TAG, "BT ACTION_CONNECTION_STATE_CHANGED!! mmState=" + mmState
                        + " , mmPreState=" + mmPreState
                        + " , device=" + intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));

                if (mmState == BluetoothProfile.STATE_CONNECTED && mmPreState != mmState) {
                    // 蓝牙audio已连接
                    mConnectedBTDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    // 确保蓝牙名称为 御驾
                    if (isConnectedToTargetBTDevice()) {
                        enableAudioModeCOMMUNICATION();
                        notifyStatus(VBleState.CONNECTED);
                    }

                } else if (mmState == BluetoothProfile.STATE_DISCONNECTED && mmPreState != mmState) {
                    enableAudioModeNORMAL();
                    notifyStatus(VBleState.DISCONNECTED);
                    mConnectedBTDevice = null;
                }

            } else if (BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED.equals(action)) {
                int mmState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED);
                Log.d(TAG, "BT AUDIO_STATE_CHANGED !!  STATE_AUDIO_CONNECTED is " +
                        (mmState == BluetoothHeadset.STATE_AUDIO_CONNECTED));

                if (false /*Settings.isAudioForceToSpeaker(mContext)*/) {
                    setSpeakerphoneOn(true);
                }

            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                // 蓝牙设备已连接 ，获取已连接的蓝牙设备信息
                Log.d(TAG, "BT is ACL CONNECTED! Device:" + intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)
                /*|| BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)*/) {
                // 蓝牙设备已断开( audio or gatt )
                Log.d(TAG, "BT is ACL Disconnected!!");
                checkStatus();
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)
                    || BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                // 蓝牙设备其他状态改变广播通知
                //checkStatus();
            } else if (AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED.equals(action)) {
                int mmState = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_DISCONNECTED);
                Log.d(TAG, "AudioManager SCO_AUDIO_STATE_CHANGED !!  SCO_AUDIO_STATE_CONNECTED is " +
                        (mmState == AudioManager.SCO_AUDIO_STATE_CONNECTED));
            }
        }
    };

    public void setBTCallBack(BTCallBack bTCallBack) {
        mBTCallBack = bTCallBack;

        /*初始化需要检查一次蓝牙的状态*/
        checkStatus();
    }

    private void checkStatus() {

        // 蓝牙开启，默认至少为DISCONNECTED状态
        int state = VBleState.DISCONNECTED;
        boolean tag = false;
        if (checkBTDevice()) {
            if (BluetoothProfile.STATE_CONNECTED ==
                    mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET)) {
                // 如果首次进入APP前已经连接上了对应的设备
                if (getConnectedBTDevice() == null) {
                    Log.e(TAG, "BT HEADSET is CONNECTED, but BT Device is null ! To getConnectedFormBondedDevice.");
                    setConnectedBTDevice(getConnectedFormBondedDevice());
                }
                // 确保蓝牙名称为 御驾 , 才能确定已连接
                if (isConnectedToTargetBTDevice()) {
                    state = VBleState.CONNECTED;
                    tag = true;
                }
            }
        }

        if (tag) {
            enableAudioModeCOMMUNICATION();
        } else {
            enableAudioModeNORMAL();
        }

        notifyStatus(state);
    }

    private BluetoothDevice getConnectedFormBondedDevice() {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            try {
                Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                if (isConnected) {
                    return device;
                }
            } catch (Exception e) {
            }
        }
        Log.e(TAG, "Error : Can NOT get device form bonded list !");
        return null;
    }

    private void notifyStatus(int status) {
        if (mBTCallBack != null) {
            mBTCallBack.statusChange(status);
        }
    }

    public interface BTCallBack {
        void statusChange(int status);
    }

    /**
     * 设置通过蓝牙mic获取音源
     */
    private static boolean isEnableAudioModeCommunication = false;

    public void enableAudioModeCOMMUNICATION() {
        if (Settings.getInstance().isMicFromBT() /*&& isEnableAudioModeCommunication == false*/) {
            AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            if (mAudioManager == null) {
                Log.e(TAG, "enableAudioModeCOMMUNICATION: Can not get Audio Service");
                return;
            }

            Log.d(TAG, "Enable AudioMode to MODE_IN_COMMUNICATION!");
            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

            if (!mAudioManager.isBluetoothScoOn()) {
                Log.d(TAG, "setBluetoothScoOn: true");
                mAudioManager.setBluetoothScoOn(true);
                mAudioManager.startBluetoothSco();
            }
            isEnableAudioModeCommunication = true;
        }
    }

    public void enableAudioModeNORMAL() {
        if (/*Settings.isMicfromBT(mContext) &&*/ isEnableAudioModeCommunication == true) {
            AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            if (mAudioManager == null) {
                Log.e(TAG, "enableAudioModeNORMAL: Can not get Audio Service");
                return;
            }
            Log.d(TAG, "Enable AudioMode to MODE_NORMAL!");
            mAudioManager.setMode(AudioManager.MODE_NORMAL);

            if (mAudioManager.isBluetoothScoOn()) {
                Log.d(TAG, "setBluetoothScoOn: false");
                mAudioManager.setBluetoothScoOn(false);
                mAudioManager.stopBluetoothSco();
            }
            isEnableAudioModeCommunication = false;
        }
    }

    public void setSpeakerphoneOn(boolean on) {
        AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManager == null) {
            Log.e(TAG, "setSpeakerphoneOn: Can not get Audio Service");
            return;
        }

        if (Settings.getInstance().isAudioForceToSpeaker() && on) {
            /*
            // First stop bt sco before force speaker on.
            if (mAudioManager.isBluetoothScoOn()) {
                Log.d(TAG, "setSpeakerphoneOn : stop bt sco first.");
                mAudioManager.setBluetoothScoOn(false);
                mAudioManager.stopBluetoothSco();
            }*/

            Log.d(TAG, "setSpeakerphoneOn: true");
            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            mAudioManager.setSpeakerphoneOn(true);

        } else if (!on) {
            Log.d(TAG, "setSpeakerphoneOn: false");
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
            mAudioManager.setSpeakerphoneOn(false);
        }
    }
}

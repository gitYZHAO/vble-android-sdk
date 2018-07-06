# viroyal-ble-android-sdk 简介
一个控制BLE GATT Client 的SDK(vble-android-sdk) 及其 示例。

# 环境要求
Android系统版本：5.0 及以上。

手机需要支持BLE（Bluetooth4.0以及以上）。

# 安装
直接引入jar包

# 获取依赖
```
compile project(':vble-android-sdk')
```

# 权限设置
以下是viroyal-ble-android-sdk所需要的Android权限，请确保您的AndroidManifest.xml文件中已经配置了这些权限，否则，SDK将无法正常工作。
```
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    <uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
```
    
# 快速入门

初始化VBleClient，并使用VBleCallback回调接收状态和消息
```
    mVBleClient = new VBleClient(getApplicationContext(), new VBleCallback() {
        @Override
        public void onVBleStatusCallback(VBleResult result) {
            //获取BLE状态更新
        }

        @Override
        public void onVBleCommandCallback(String whatCommand, boolean isSuccessful) {
            //获取已知的命令的状态返回
        }

        @Override
        public void processUnsolicitedMsg(String unsolicitedMsg) {
            //处理为止的消息上报
        }
    });
```

初始化BLE Client的连接和获取指定的BLE characteristic
```
        mVBleClient.VBleClient_InitBLEClient(getApplicationContext());
```

使用VBleClient_SendCommand接口发送（指定的）消息
```

    public final static String VBLE_COMMAND_CALL_ANSWER = "CALL_ANSWER";
    public final static String VBLE_COMMAND_CALL_END = "CALL_END";
    public final static String VBLE_COMMAND_MAKE_CALL = "MAKE_CALL";
    public final static String VBLE_COMMAND_WAKE_UP = "WAKE_UP";
    public final static String VBLE_COMMAND_SET_FM = "SET_FM";

    // 发送接听电话消息
    if (isCanSendCommand) {
        mVBleClient.VBleClient_SendCommand(VBleClient.VBLE_COMMAND_CALL_ANSWER);
    }

    // 发送挂断电话消息
    if (isCanSendCommand) {
        mVBleClient.VBleClient_SendCommand(VBleClient.VBLE_COMMAND_CALL_END);
    }

    // 设置FM 频率，98.5 设置成 字符串 "985"
    if (isCanSendCommand) {
        mVBleClient.VBleClient_SendCommand(VBleClient.VBLE_COMMAND_SET_FM ,fmReq);
    }
                
```

# viroyal-ble-android-sdk 简介
一个控制BLE GATT Client 的SDK 及其示例。

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

使用以下几个接口发送（指定的）消息
```
                // 发送接听电话消息
                mVBleClient.VBleClient_CallAnswerCommand();
                
                // 发送挂断电话消息
                mVBleClient.VBleClient_CallEndCommand();
                
                // 设置FM 频率，98.5 设置成 字符串 "985"
                if (isCanSendCommand) {
                    mVBleClient.VBleClient_FMReqCommand(fmReq);
                } 
                
```

# vble-android-sdk 简介
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
        bleClient = VBleClient.getInstance();
        bleClient.init(this.getApplication(), new IVBleCallback() {
            @Override
            public void onVBleStatusCallback(int state) {
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
#使用：

一般使用请参考：
VbleSampleMainActivity.java
```
        // 获取版本号
        bleClient.getFirmwareVersion();


        // 设置FM 频率，98.5 设置成 985
        bleClient.setFM(mSetFMReq.getText().toString());

```

OTA升级参考：
FirmwareUpdateActivity.java
```
        // 实例化对象
        mFU = new FirmwareUpdateAdapter(getApplicationContext());
        // 初始化WearableFota
        mFU.initWearableFota();
         // 设置需要升级的BLE设备
        mFU.setRemoteDevice(VBleClient.getInstance().getConnectedBLEDevice());
         // 设置回调，接收OTA升级消息
        mFU.setFirmwareUpdateCB(cb);
```

#注意：
设计根据当前已经连接好的蓝牙Audio判断与哪个BLE设备连接。



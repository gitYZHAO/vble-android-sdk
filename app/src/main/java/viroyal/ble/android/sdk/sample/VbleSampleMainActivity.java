package viroyal.ble.android.sdk.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.viroyal.android.sdk.vble.VBleCallback;
import com.viroyal.android.sdk.vble.VBleClient;
import com.viroyal.android.sdk.vble.VBleResult;

import viroyal.ble.android.sdk.R;

public class VbleSampleMainActivity extends AppCompatActivity {
    final static String TAG = "VBLESample";

    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;

    private static final int MSG_VBLE_CAN_SEND_COMMAND = 1;
    private static final int MSG_VBLE_DISABLE_SEND_COMMAND = 2;
    private static final int MSG_VBLE_UNSOLICITED_MSG = 3;
    private static final int MSG_VBLE_STATUS_UPDATE_MSG = 4;

    private Button mSendMsg;
    private Button mInitVBleClient;
    private Button mSendCommand;
    private EditText mSetFMReq;
    private TextView mVBleTextView;

    private boolean isCanSendCommand;

    VBleClient mVBleClient;
    private final Handler mHanler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_VBLE_CAN_SEND_COMMAND:
                    if (mInitVBleClient != null) mInitVBleClient.setEnabled(false);
                    if (mSendCommand != null) mSendCommand.setEnabled(true);
                    if (mSendMsg != null) mSendMsg.setEnabled(true);
                    break;

                case MSG_VBLE_DISABLE_SEND_COMMAND:
                    if (mInitVBleClient != null) mInitVBleClient.setEnabled(true);
                    if (mSendCommand != null) mSendCommand.setEnabled(false);
                    if (mSendMsg != null) mSendMsg.setEnabled(false);
                    break;

                case MSG_VBLE_UNSOLICITED_MSG:
                    Log.d(TAG, "handleMessage: MSG_VBLE_UNSOLICITED_MSG: " + msg.obj);
                    if (mVBleTextView != null) {
                        mVBleTextView.append("接收到消息：" + (CharSequence) msg.obj + "\n");
                    }
                    break;
                case MSG_VBLE_STATUS_UPDATE_MSG:
                    if (mVBleTextView != null) {
                        mVBleTextView.append("当前状态：" + VBleResult.vBLE_Status[msg.arg1] + "\n");
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vble_sample_main);

        CheckBlePermission();

        mVBleClient = new VBleClient(getApplicationContext(), new VBleCallback() {
            @Override
            public void onVBleStatusCallback(VBleResult result) {

                if (result.isVBleCanSendCommand()) {
                    isCanSendCommand = true;
                    mHanler.sendEmptyMessage(MSG_VBLE_CAN_SEND_COMMAND);
                } else {
                    isCanSendCommand = false;
                    mHanler.sendEmptyMessage(MSG_VBLE_DISABLE_SEND_COMMAND);
                }
                Message msg = new Message();
                msg.what = MSG_VBLE_STATUS_UPDATE_MSG;
                msg.arg1 = result.getmVBle_status();
                mHanler.sendMessage(msg);
            }

            @Override
            public void onVBleCommandCallback(String whatCommand, boolean isSuccessful) {
                Log.d(TAG, "onVBleCommandCallback: Send command:" + whatCommand + " is " + isSuccessful);
                final String[][] commandShow = {
                        {VBleClient.VBLE_COMMAND_CALL_ANSWER, "接听电话消息发送成功！"},
                        {VBleClient.VBLE_COMMAND_CALL_END, "挂断电话消息发送成功！"},
                        {VBleClient.VBLE_COMMAND_WAKE_UP, "接收到唤醒消息！"},
                        {VBleClient.VBLE_COMMAND_SET_FM, "设置FM频段消息发送成功！"}
                };

                for (String[] str : commandShow) {
                    if (str[0].equals(whatCommand)) {
                        Message msg = new Message();
                        msg.what = MSG_VBLE_UNSOLICITED_MSG;
                        msg.obj = str[1];
                        mHanler.sendMessage(msg);
                    }
                }
            }

            @Override
            public void processUnsolicitedMsg(String unsolicitedMsg) {
                Log.d(TAG, "processUnsolicitedMsg: " + unsolicitedMsg);
                Message msg = new Message();
                msg.what = MSG_VBLE_UNSOLICITED_MSG;
                msg.obj = unsolicitedMsg;
                mHanler.sendMessage(msg);

            }
        });

        //显示界面初始化
        ViewIinit();

    }

    private void ViewIinit() {
        // 文本显示
        mVBleTextView = (TextView) findViewById(R.id.receiveTextView);
        if (mVBleTextView != null) {
            mVBleTextView.append("消息显示\r\n");
        }

        // 蓝牙BLE初始化按钮
        mInitVBleClient = (Button) findViewById(R.id.initButton);
        mInitVBleClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVBleClient.VBleClient_InitBLEClient(getApplicationContext());
            }
        });

        // 蓝牙BLE命令发送按钮
        mSendCommand = (Button) findViewById(R.id.CommandButton);
        mSendCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 发送接听电话消息
                mVBleClient.VBleClient_CallAnswerCommand();
            }
        });

        mSetFMReq = (EditText) findViewById(R.id.msgSendText);
        mSendMsg = (Button) findViewById(R.id.msgSendBtn);
        mSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fmReq = mSetFMReq.getText().toString();

                // 设置FM 频率，98.5 设置成 985
                if (isCanSendCommand) {
                    mVBleClient.VBleClient_FMReqCommand(fmReq);
                } else {
                    Log.d(TAG, "mSendMsg.onClick: BLE is NOT connected");
                }
            }
        });

        mSendCommand.setEnabled(false);
        mSendMsg.setEnabled(false);

    }

    private void CheckBlePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
            //判断是否具有权限
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //判断是否需要向用户解释为什么需要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(this, "自Android 6.0开始需要打开位置权限才可以搜索到Ble设备", Toast.LENGTH_SHORT).show();
                }
                //请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户允许改权限，0表示允许，-1表示拒绝 PERMISSION_GRANTED = 0， PERMISSION_DENIED = -1
                //permission was granted, yay! Do the contacts-related task you need to do.
                //这里进行授权被允许的处理
            } else {
                //permission denied, boo! Disable the functionality that depends on this permission.
                //这里进行权限被拒绝的处理
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}

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
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.viroyal.android.sdk.vble.IVBleCallback;
import com.viroyal.android.sdk.vble.IVBleClient;
import com.viroyal.android.sdk.vble.VBleClient;
import com.viroyal.android.sdk.vble.VBleState;

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
    private Button mDisconnectVBleClient;
    private Button mSendCommand;
    private Button mSendCommand2;
    private EditText mSetFMReq;
    private TextView mVBleTextView;

    private boolean isCanSendCommand;

    IVBleClient bleClient;

    private final Handler mHanler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_VBLE_CAN_SEND_COMMAND:
                case MSG_VBLE_DISABLE_SEND_COMMAND:
                    SetButtonView(isCanSendCommand);
                    break;

                case MSG_VBLE_UNSOLICITED_MSG:
                    Log.d(TAG, "handleMessage: MSG_VBLE_UNSOLICITED_MSG: " + msg.obj);
                    if (mVBleTextView != null) {
                        mVBleTextView.append("接收到消息：" + msg.obj + "\n");

                        int offset = mVBleTextView.getLineCount() * mVBleTextView.getLineHeight();
                        if (offset > mVBleTextView.getHeight()) {
                            mVBleTextView.scrollTo(0, offset - mVBleTextView.getHeight());
                        }
                    }
                    break;
                case MSG_VBLE_STATUS_UPDATE_MSG:
                    int state = msg.arg1;
                    if (mVBleTextView != null) {
                        mVBleTextView.append("当前状态：" + (new VBleState(state).toString()) + "\n");
                    }
                    if (state == VBleState.INIT_DONE) {
                        isCanSendCommand = true;
                        SetButtonView(isCanSendCommand);
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

        bleClient = VBleClient.getInstance();
        bleClient.init(this.getApplication(), new IVBleCallback() {
            @Override
            public void onVBleStatusCallback(int state) {
                Message msg = new Message();
                msg.what = MSG_VBLE_STATUS_UPDATE_MSG;
                msg.arg1 = state;
                mHanler.sendMessage(msg);
            }

            @Override
            public void onVBleCommandCallback(String whatCommand, boolean isSuccessful) {
                Message msg = new Message();
                msg.what = MSG_VBLE_UNSOLICITED_MSG;
                msg.obj = whatCommand + (isSuccessful ? "发送成功" : "发送失败");
                mHanler.sendMessage(msg);
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
        ViewInit();

    }

    private void ViewInit() {
        // 文本显示
        mVBleTextView = findViewById(R.id.receiveTextView);
        if (mVBleTextView != null) {
            mVBleTextView.append("消息显示\r\n");
            mVBleTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        }

        // 获取版本号
        mInitVBleClient = findViewById(R.id.initButton);
        mInitVBleClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVBleTextView.append(bleClient.getFirmwareVersion() + "\r\n");
            }
        });

        mDisconnectVBleClient = findViewById(R.id.disconnectBtn);
        mDisconnectVBleClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mVBleClient.VBleClient_DisconnectBLEClient();
            }
        });


        // 蓝牙BLE命令发送按钮
        mSendCommand = findViewById(R.id.CommandButton);
        mSendCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 发送接听电话消息
                bleClient.answer();
            }
        });

        // 蓝牙BLE命令发送按钮
        mSendCommand2 = findViewById(R.id.CommandButton2);
        mSendCommand2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 发送挂断电话消息
                bleClient.hangUp();
            }
        });

        mSetFMReq = findViewById(R.id.msgSendText);
        mSendMsg = findViewById(R.id.msgSendBtn);
        mSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 设置FM 频率，98.5 设置成 985
                bleClient.setFM(mSetFMReq.getText().toString());
            }
        });

        SetButtonView(isCanSendCommand);

    }

    private void SetButtonView(boolean isGetChara) {
        if (mInitVBleClient != null) mInitVBleClient.setEnabled(isGetChara);
        if (mDisconnectVBleClient != null) mDisconnectVBleClient.setEnabled(isGetChara);
        if (mSendCommand != null) mSendCommand.setEnabled(isGetChara);
        if (mSendCommand2 != null) mSendCommand2.setEnabled(isGetChara);
        if (mSendMsg != null) mSendMsg.setEnabled(isGetChara);
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

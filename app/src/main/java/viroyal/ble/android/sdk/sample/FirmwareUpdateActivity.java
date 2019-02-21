package viroyal.ble.android.sdk.sample;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.viroyal.android.sdk.vble.VBleClient;
import com.viroyal.wearable.FirmwareUpdateAdapter;
import com.viroyal.wearable.FirmwareUpdateCB;

import viroyal.ble.android.sdk.R;

public class FirmwareUpdateActivity extends Activity {
    final private String TAG = "FirmwareUpdateActivity";
    private TextView info;
    private Button foatBtn;
    private FirmwareUpdateAdapter mFU;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firmware_update);

        mFU = new FirmwareUpdateAdapter(getApplicationContext());
        mFU.initWearableFota();
        mFU.setRemoteDevice(VBleClient.getInstance().getConnectedBLEDevice());
        mFU.setFirmwareUpdateCB(cb);

        foatBtn = (Button) findViewById(R.id.FotaBtn);
        foatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFU.isAvailable()) {
                    mFU.start();

                    if (mFU.getState() == FirmwareUpdateAdapter.STATE_FOTA_SENDING){
                        info.setText("正在发送...");
                    }
                } else {
                    Log.e(TAG, "Error: Wearable is NOT available...");
                }
            }
        });
        info = (TextView) findViewById(R.id.infoTv);

    }

    @Override
    protected void onDestroy() {
        if (mFU!=null){
            mFU.close();
        }
        super.onDestroy();
    }

    FirmwareUpdateCB cb = new FirmwareUpdateCB() {
        @Override
        public void onFWSendSuccess() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    info.setText("FOTA image 发送成功！");
                }
            });
        }

        @Override
        public void onFWUpdateSuccess() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    info.setText("FOAT 更新成功！");
                }
            });
        }

        @Override
        public void onFWUpdateFail() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    info.setText("FOTA 更新失败！");
                }
            });
        }

        @Override
        public void onFWUpdateStatus() {

        }

        @Override
        public void onFWUpdateProgress(final int progress) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    info.setText("设备升级中...  [" + progress + "%]");
                }
            });
        }
    };


}

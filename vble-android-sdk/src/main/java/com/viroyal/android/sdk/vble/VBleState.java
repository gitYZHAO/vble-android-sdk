package com.viroyal.android.sdk.vble;

public class VBleState {
    public static final int DISCONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;
    public static final int DISCONNECTING = 3;
    public static final int SCANNING = 4;
    public static final int SCANNED = 5;
    public static final int INIT_DONE = 6;
    public static final int CONNECTED_FAILED = 7;

    public final String[] mStatusString = {
            "DISCONNECTED",
            "CONNECTING",
            "CONNECTED",
            "DISCONNECTING",
            "SCANNING",
            "SCANNED",
            "INIT_DONE"
    };

    @Override
    public String toString() {
        return "VBleState{" +
                "mStatus=" + mStatusString[mStatus] +
                '}';
    }

    private int mStatus;

    public VBleState(int status) {
        mStatus = status;
    }

    public int getStatus() {
        return mStatus;
    }
}

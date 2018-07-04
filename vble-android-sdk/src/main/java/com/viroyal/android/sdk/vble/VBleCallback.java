package com.viroyal.android.sdk.vble;

public interface VBleCallback {
    public void onVBleStatusCallback(VBleResult result);

    public void onVBleCommandCallback(String whatCommand, boolean isSuccessful);

    public void processUnsolicitedMsg(String unsolicitedMsg);
}

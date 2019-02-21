package com.viroyal.android.sdk.vble;

public interface IVBleCallback {
    public void onVBleStatusCallback(int state);
    public void onVBleCommandCallback(String whatCommand, boolean isSuccessful);
    public void processUnsolicitedMsg(String unsolicitedMsg);
}

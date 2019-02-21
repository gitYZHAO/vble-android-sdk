package com.viroyal.wearable;

public interface FirmwareUpdateCB {
    public void onFWSendSuccess();
    public void onFWUpdateSuccess();
    public void onFWUpdateFail();
    public void onFWUpdateStatus();
    public void onFWUpdateProgress(int progress);
}

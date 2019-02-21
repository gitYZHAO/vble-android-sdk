package com.viroyal.wearable;

public class FotaUtils {
    // // M : update UX message begin
    //public static final int MSG_UPDATE_TEXT_VIEW = 1;

    public static final int MSG_ARG1_DOWNLOAD_FINISHED = 1;
    public static final int MSG_ARG1_UPDATE_FINISHED = 2;
    public static final int MSG_ARG1_UPDATE_FAILED_CAUSE_DISCONNECTED = 3;
    public static final int MSG_ARG1_DOWNLOAD_FAILED = 4;
    // // M : update UX message end

    // // M : update via bt signals begin
    // / M : send bin via bt success
    public static final int FOTA_SEND_VIA_BT_SUCCESS = 2;
    // update via bin success
    public static final int FOTA_UPDATE_VIA_BT_SUCCESS = 3;

    // update via bt errors
    public static final int FOTA_UPDATE_VIA_BT_COMMON_ERROR = -1;
    // FP write file failed
    public static final int FOTA_UPDATE_VIA_BT_WRITE_FILE_FAILED = -2;
    // FP disk full error
    public static final int FOTA_UPDATE_VIA_BT_DISK_FULL = -3;
    // FP data transfer failed
    public static final int FOTA_UPDATE_VIA_BT_DATA_TRANSFER_ERROR = -4;
    // FP update Fota trigger failed
    public static final int FOTA_UPDATE_VIA_BT_TRIGGER_FAILED = -5;
    // FP update fot failed
    public static final int FOTA_UPDATE_VIA_BT_FAILED = -6;
    // FP trigger failed cause of low battery
    public static final int FOTA_UPDATE_TRIGGER_FAILED_CAUSE_LOW_BATTERY = -7;
    // get FP version failed
    public static final String FOTA_VERSION_GET_FAILED = "-8";
    // //// M : update via bt signals end

    /**
     *
     */
    public static final int FILE_NOT_FOUND_ERROR = -100;
    public static final int READ_FILE_FAILED = -101;

    public static final String INTENT_EXTRA_INFO = "firmware_way";
    public static final String SEL_FILE_PATH = "sel_file_path";

    // // M
}

package com.github.zebardemo.util;

import android.util.Log;

import com.github.zebardemo.MyApplication;
import com.zebra.rfid.api3.ACCESS_OPERATION_CODE;
import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.TagData;

/**
 * Created by ${sheldon} on 2017/12/19.
 */

public class EventHandler implements RfidEventsListener {

    @Override
    public void eventReadNotify(RfidReadEvents e) {
        final TagData[] myTags = MyApplication.mConnectedReader.Actions.getReadTags(100);
        if (myTags != null) {
            for (int index = 0; index < myTags.length; index++) {
                if (myTags[index].getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
                        myTags[index].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS) {
                }
                if (myTags[index].isContainsLocationInfo()) {
                    final int tag = index;
                    MyApplication.TagProximityPercent = myTags[tag].LocationInfo.getRelativeDistance();
                }
                if (myTags[index] != null && (myTags[index].getOpStatus() == null || myTags[index].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS)) {
                    final int tag = index;
                    ;Log.i("888888888888888",myTags[tag].getTagID());
                }
            }
        }
    }

    @Override
    public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
    }

}

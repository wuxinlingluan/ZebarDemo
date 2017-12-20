package com.github.zebardemo.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.github.zebardemo.MyApplication;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;

/**
 * Created by ${sheldon} on 2017/12/19.
 */

public class ZebarUtil {

    /*
    * 开始扫描
    * */
    public static void startScan(Context context){
        RFIDReader mConnectedReader = new RFIDReader();
        if (MyApplication.mConnectedReader != null && MyApplication.mConnectedReader.isConnected()) {
            try {
                mConnectedReader.Actions.Inventory.perform();
                MyApplication.mConnectedReader.Events.addEventsListener(MyApplication.eventHandler);
                Log.i("11111111111","扫描");
            } catch (InvalidUsageException e) {
                e.printStackTrace();
                Log.i("2222222222",e.getMessage().toString());
            } catch (final OperationFailureException e) {
                e.printStackTrace();
                Log.i("3333333333",e.getVendorMessage().toString());
            }
        } else {
            Toast.makeText(context, "连接失败", Toast.LENGTH_SHORT).show();
        }
    }


    /*
    * 结束扫描
    * */
    public static void endScan(){
        try {
            MyApplication.mConnectedReader.Actions.Inventory.stop();
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }
}

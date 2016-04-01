package com.skw.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import android.widget.TextView;
      
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

/**
 * AltBeacon 的MonitorNotifier(区域监视)的测试程序
 *
 * @author skywang
 * @e-mail kuiwu-wang@163.com
 */
public class Demo1 extends Activity implements BeaconConsumer {
    private static final String TAG = "##skywang-Demo1";

    private TextView mTvInfo;

    private boolean mConntected = false;
    private BeaconManager mBeaconManager;
    private Region mMonitorRegion = new Region("demo1_MonitorRegion", null, null, null);

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mTvInfo.append((String)msg.obj);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo1);

        // 获取BeaconManager对象
        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        // 设置监听的Beacon的类型。这里的"m:2-3=0215"是苹果设备(iPhone, iPad作为Beacon基站)的前缀。
        // (1) m:2-3=0215   第2-3个字节，表示Beacon基站的prefix。苹果设备作为基站时的prefix固定是0215
        // (2) i:4-19       第4-19个字节，表示UUID(即独立的设备ID)。
        // (3) i:20-21      第20-21个字节，表示major
        // (4) i:22-23      第22-23个字节，表示minor
        // (5) p:24-24      第24个字节，表示RSSI。信号强度or距离
        // (6) d:25-25      第25个字节，data field。
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        // 设置"进入/退出区域"的监视器
        mBeaconManager.setMonitorNotifier(new MyMonitorNotifier());
        // 绑定到BeaconService服务
        mBeaconManager.bind(this);

        mTvInfo = (TextView) findViewById(R.id.tv_info);
        ToggleButton tglButton = (ToggleButton) findViewById(R.id.tgl);
        tglButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 如果没有连上BeaconService，则无法监控；此时，直接退出
                if (!mConntected) {
                    return ;
                }

                if (isChecked) {
                    try {
                        Log.i(TAG, "start monitor region="+mMonitorRegion);
                        mBeaconManager.startMonitoringBeaconsInRegion(mMonitorRegion);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Log.i(TAG, "stop monitor region="+mMonitorRegion);
                        mBeaconManager.stopMonitoringBeaconsInRegion(mMonitorRegion);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }); 
    }

    @Override 
    protected void onDestroy() {
        super.onDestroy();
        // 与BeaconService服务解除绑定
        mBeaconManager.unbind(this);
    }

    /**
     * 当前Activity连上BeaconService服务时的回调函数
     */
    @Override
    public void onBeaconServiceConnect() {
        Log.i(TAG, "is able to start monitor now");
        mConntected = true;
    }

    private class MyMonitorNotifier implements MonitorNotifier {
        @Override
        public void didEnterRegion(Region region) {
            Log.i(TAG, "I see an beacon");
            mHandler.sendMessage(mHandler.obtainMessage(0, new String("\ndidEnterRegion region="+region)));
        }

        @Override
        public void didExitRegion(Region region) {
            Log.i(TAG, "I no longer see an beacon");
            mHandler.sendMessage(mHandler.obtainMessage(1, new String("\ndidExitRegion region="+region)));
        }

        @Override
        public void didDetermineStateForRegion(int state, Region region) {
            Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);        
            mHandler.sendMessage(mHandler.obtainMessage(1, new String("\ndidDetermineStateForRegion Region="+region+", state="+state)));
        }
    }
}

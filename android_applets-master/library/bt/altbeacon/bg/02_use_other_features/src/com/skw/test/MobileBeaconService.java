package com.skw.test;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Binder;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.skw.test.util.GattAttributeResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
      
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * 服务
 *
 * @author sky
 * @e-mail sky.wang@oceanwing.com
 * @date 2015-08-13
 */
public class MobileBeaconService extends Service implements BeaconConsumer, LocationListener {
    private static final String TAG = "##skywang-MobileBeaconService";

    public static final String ACTION_BACKGROUND_BY_BEACON = "action_background_by_beacon";
    private BeaconManager mBeaconManager;
    private Region mRangeRegion = new Region("service1_RangeRegion", null, null, null);
    private Region mMonitorRegion = new Region("service1_MonitorRegion", null, null, null);

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        initBeacon();
        initBt();
    }

    private void initBeacon() {
        // 获取BeaconManager对象
        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
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
        mBeaconManager.setRangeNotifier(new MyRangeNotifier());         // 设置"RangeNotifier"的监视器
        mBeaconManager.bind(this);
    }

    /**
     * 当前Service连上BeaconService服务时的回调函数
     */
    @Override
    public void onBeaconServiceConnect() {
        try {
            mBeaconManager.startMonitoringBeaconsInRegion(mMonitorRegion);
            mBeaconManager.startRangingBeaconsInRegion(mRangeRegion);
        } catch (RemoteException e) {   }   
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        stopScan();
        // 与BeaconService服务解除绑定
        mBeaconManager.unbind(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }


    // ==================================== bt(BLE) =============================
    private boolean mScan = false;
    private ArrayList<BluetoothDevice> mLeDevices = new ArrayList<BluetoothDevice>();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBtManager;
    private MyLeScanCallback mLeScanCallback = new MyLeScanCallback();
    /**
     * 获取BT服务
     */
    private void initBt() {
        mBtManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBtManager.getAdapter();
    }

    /**
     * 开始BT扫描
     */
    private void startScan() {
        Log.d(TAG, "startScan");
        mScan = true;
        // 清空上一次的BT设备列表
        mLeDevices.clear();
        // 开始BLE扫描
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        // 开始BLE扫描后8秒钟，停止扫描
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "mHandler stopScan and connect");
                // 通知扫描
                stopScan();
                connect();
            }   
        }, 8000);
    }

    /**
     * 停止扫描
     */
    private void stopScan() {
        Log.d(TAG, "stopScan mScan="+mScan);
        if (mScan) {
            mScan = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    /**
     * 存储"蓝牙设备"到mLeDevices列表中
     */
    private void addDevice(BluetoothDevice device) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }

    /**
     * BLE扫描回调函数
     */
    private final class MyLeScanCallback implements BluetoothAdapter.LeScanCallback {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d(TAG, "onLeScan: name:" + device.getName() + ", address:" + device.getAddress() + ", uuid:" + device.getUuids());
            addDevice(device);
        }
    }

    /**
     * 从BLE扫描得到的BT设备列表中，选取一个BT设备并进行连接
     */
    private void connect() {
        if (mLeDevices.size() == 0) {
            return ;
        }

        // 选择一个BT设备，选择策略如下：
        // (1) 优先选择MAC地址为C9:A6:FE:8C:62:76
        // (2) 选择第一个设备
        BluetoothDevice device = null;
        for (BluetoothDevice item:mLeDevices) {
            if ("C9:A6:FE:8C:62:76".equals(item.getAddress())) {
                device = item;
                break;
            }
        }
        if (device==null) {
            device = mLeDevices.get(0);
        }

        // 连接选中的设备
        if (device != null) {
            Log.d(TAG, "connect: device="+device);
            BluetoothGatt btGatt = device.connectGatt(this, false, new MyBluetoothGattCallback());
        }
    }

    /**
     * 显示BT设备支持的服务
     */
    private void showGattServiceInfo(BluetoothGatt gatt) {
        StringBuilder sb = new StringBuilder();
        BluetoothDevice device = gatt.getDevice();
        List<BluetoothGattService> gattServices = gatt.getServices();
        sb.append("BT mac:"+device.getAddress()+", protocols:");
        Log.d(TAG, "Connect to : name:" + device.getName() + ", address:" + device.getAddress());
        for (BluetoothGattService service:gattServices) {
            // 获取uuid
            String uuid = service.getUuid().toString();
            // 获取uuid对应的协议
            String protocol = GattAttributeResolver.getAttributeName(uuid, "Unknown");
            sb.append(protocol+" ");
            Log.d(TAG, "  Procotol: protocol:"+protocol+", uuid:"+uuid);
        }
        sb.append(", name:"+device.getName());
        mHandler.sendMessage(mHandler.obtainMessage(0x105, sb.toString()));
    }

    /**
     * BLE连接回调接口
     */
    private final class MyBluetoothGattCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "==BT==Connected to GATT server：" + newState);
                // 发送连接上的BT设备所支持的服务
                gatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "==BT==Disonnected to GATT server：" + newState);
            }
        }

        @Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "==BT==onServicesDiscovered: success");
            } else {
                Log.e(TAG, "==BT==onServicesDiscovered received: " + status);
                showGattServiceInfo(gatt);
            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.e(TAG, "==BT==onCharacteristicRead: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "==BT==onCharacteristicRead: "+characteristic.getUuid());
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e(TAG, "==BT==onCharacteristicWrite: " + status);
        }
    }


    // ========================== GPS =============================
    /**
     * 获取GPS信息
     */
    public Location getLocation() {
        Location location = null;
        double latitude = 0.0;
        double longitude = 0.0;
        try {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 
            // getting GPS status
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 
            // getting network status
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 
            Log.d(TAG, "getLocation: isGPSEnabled="+isGPSEnabled+", isNetworkEnabled="+isNetworkEnabled);
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 6000, 10, this);
                    Log.d(TAG, "Network");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Log.d(TAG, "networkEnbled latitude="+latitude+", longitude="+longitude);
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 6000, 10, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                Log.d(TAG, "GPS Enabled latitude="+latitude+", longitude="+longitude);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    @Override
    public void onLocationChanged(Location arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderDisabled(String arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        // TODO Auto-generated method stub
    }


    // ========================== 网络请求(wifi/3g) =============================
    //方法：发送网络请求，获取百度首页的数据。
    private void sendRequestWithHttpClient() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //用HttpClient发送请求，分为五步
                //第一步：创建HttpClient对象
                HttpClient httpClient = new DefaultHttpClient();
                //第二步：创建代表请求的对象,参数是访问的服务器地址
                HttpGet httpGet = new HttpGet("http://www.baidu.com");
                try {
                    //第三步：执行请求，获取服务器返还的相应对象
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    //第四步：检查相应的状态是否正常：检查状态码的值是200表示正常
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        //第五步：从相应对象当中取出数据，放到entity当中
                        HttpEntity entity = httpResponse.getEntity();
                        String response = EntityUtils.toString(entity,"utf-8");//将entity当中的数据转换为字符串

                        // 网络请求成功
                        mHandler.sendMessage(mHandler.obtainMessage(0x101, new String("HttpGet Success")));
                    } else {
                        // 网络请求失败
                        mHandler.sendMessage(mHandler.obtainMessage(0x101, new String("HttpGet Fail!")));
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    mHandler.sendMessage(mHandler.obtainMessage(0x101, new String("HttpGet Fail(Exception)!")));
                }
            }
        }).start();//这个start()方法不要忘记了        
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, (String)msg.obj);
            generateNotification(MobileBeaconService.this, (String)msg.obj);
            // 发送广播给MainActivity
            final Intent intent = new Intent(ACTION_BACKGROUND_BY_BEACON);
            intent.putExtra("message", (String)msg.obj);
            sendBroadcast(intent);
        }
    };

    // ========================== 通知 =============================
    private static int mNotifyIndex = 0;
    private static void generateNotification(Context context, String message) {
        Intent launchIntent = new Intent(context, MainActivity.class).
            setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).
            notify(mNotifyIndex++, new NotificationCompat.Builder(context)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setTicker(message)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(message)
                    .setContentIntent(PendingIntent.getActivity(context, 1, launchIntent, 0))
                    .setAutoCancel(true)
                    // 设置：加入震動效果(DEFAULT_VIBRATE), 加入音效效果(DEFAULT_SOUND), 加入閃燈效果(DEFAULT_LIGHTS)
                    .setDefaults(Notification.DEFAULT_LIGHTS)
                    .build());
    }

    /**
     * iBeacon扫描信号监听器(用于监听iBeacon的周期扫描信息，周期的频率一般是1s)
     */
    private boolean mMacNotified = false;
    private class MyRangeNotifier implements RangeNotifier {
        @Override
        public void didRangeBeaconsInRegion(final Collection<Beacon> rangedBeacons, Region region) {
            Log.i(TAG, "range: beacons="+rangedBeacons.size()+", region="+region.getUniqueId());
            if (rangedBeacons.size()>0) {
                Beacon bb = null;
                Iterator<Beacon> iter = rangedBeacons.iterator();
                while (iter.hasNext()) {
                    bb = iter.next();
                    Log.d(TAG, "RangeBeacon: beacon="+bb);
                }

                // 选出检测到的iBeacon中的最后一个，并发送通知
                if ((!mMacNotified) && (bb!=null)) {
                    mMacNotified = true;
                    mHandler.sendMessage(mHandler.obtainMessage(4, "iBeacon Last: mac:"+bb.getBluetoothAddress()+", uuid:"+bb.getId1()));
                }
            }
        }
    }

    private class MyMonitorNotifier implements MonitorNotifier {
        @Override
        public void didEnterRegion(Region region) {
            Log.i(TAG, "I see an beacon");
            mHandler.sendMessage(mHandler.obtainMessage(0, new String("iBeacon Enter region="+region)));
            // 发送网络请求
            sendRequestWithHttpClient();
            // 开始BLE扫描
            startScan();
            // 获取GPS信息
            Location loc = getLocation();
            Log.i(TAG, "get Location: "+loc);
            mHandler.sendMessage(mHandler.obtainMessage(0x102, "GPS: loc:"+loc));
        }

        @Override
        public void didExitRegion(Region region) {
            Log.i(TAG, "I no longer see an beacon");
            mHandler.sendMessage(mHandler.obtainMessage(1, new String("iBeacon Exit region="+region)));
        }

        @Override
        public void didDetermineStateForRegion(int state, Region region) {
            Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);        
        }
    }
}

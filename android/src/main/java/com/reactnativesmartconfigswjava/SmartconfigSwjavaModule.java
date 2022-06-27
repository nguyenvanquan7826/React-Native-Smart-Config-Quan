
package com.reactnativesmartconfigswjava;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import esptouch.EsptouchTask;
import esptouch.IEsptouchListener;
import esptouch.IEsptouchResult;
import esptouch.IEsptouchTask;


public class SmartconfigSwjavaModule extends ReactContextBaseJavaModule {

    private static final String TAG = "SmartconfigSwjavaModule";

    private final ReactApplicationContext _reactContext;

    private IEsptouchTask mEsptouchTask;

    public SmartconfigSwjavaModule(ReactApplicationContext reactContext) {
        super(reactContext);
        _reactContext = reactContext;

    }

    @Override
    public String getName() {
        return "SmartconfigSwjava";
    }

    // Required for rn built in EventEmitter Calls.
    @ReactMethod
    public void addListener(String eventName) {
    }
    
    @ReactMethod
    public void removeListeners(Integer count) {
    }

    @ReactMethod
    public void stop() {
        if (mEsptouchTask != null) {
            Log.d(TAG, "cancel task");
            mEsptouchTask.interrupt();
        }
    }

    @ReactMethod
    public void start(String ssid, String bssid, String pass, int timeout, int taskCount, final Promise promise) {
        Log.d(TAG, "ssid " + ssid + ":pass " + pass);
        stop();
        new EsptouchAsyncTask(result -> onFinishScan())
            .execute(ssid, bssid, pass, Integer.toString(taskCount), Integer.toString(timeout));
    }


    public interface TaskListener {
        public void onFinished(List<IEsptouchResult> result);
    }

    private void sendToRN(String eventName, String data) {
        String eventSendToRN = "SmartConfig";
        Log.d(TAG, "send to RN " + eventName + " " + data);
        // Create map for params
        WritableMap payload = Arguments.createMap();
        // Put data to map
        payload.putString("eventName", eventName);
        payload.putString("data", data);
        // Get EventEmitter from context and send event thanks to it
        _reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventSendToRN, payload);
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            String data = (String) message.obj;
            sendToRN("onFoundDevice", data);
        }
    };

    private void onFoundDevice(String data) {
        Message message = handler.obtainMessage(0, data);
        message.sendToTarget();
    }

    private void onFinishScan() {
        sendToRN("onFinishScan", "");
    }

    private class EsptouchAsyncTask extends AsyncTask<String, Void, List<IEsptouchResult>> {
        private final TaskListener taskListener;

        public EsptouchAsyncTask(TaskListener listener) {
            // The listener reference is passed in through the constructor
            this.taskListener = listener;
        }


        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "Begin task");
        }

        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            Log.d(TAG, "doing task");
            int taskCount = -1;
            int timeout = 60000;
            synchronized (mLock) {
                String apSsid = params[0];
                String apBssid = params[1];
                String apPassword = params[2];
                Log.d(TAG, apSsid + " | " + apBssid + " | " + apPassword);
                String taskCountStr = params[3];
                String timeoutStr = params[4];
                taskCount = Integer.parseInt(taskCountStr);
                timeout = Integer.parseInt(timeoutStr);
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, _reactContext);
                mEsptouchTask.setPackageBroadcast(false);
                mEsptouchTask.setEsptouchListener(result -> {
                    String ip = result.getInetAddress().getHostAddress();
                    String bssid = result.getBssid();
                    String data = "{\"ip\":\"" + ip + "\", \"bssid\":\"" + bssid + "\"}";
                    onFoundDevice(data);
                });
            }
            List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskCount);
            return resultList;
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {

            IEsptouchResult firstResult = result.get(0);
            // check whether the task is cancelled and no results received
            if (!firstResult.isCancelled()) {
                if (this.taskListener != null) {

                    // And if it is we call the callback function on it.
                    this.taskListener.onFinished(result);
                }
            }
        }
    }
}

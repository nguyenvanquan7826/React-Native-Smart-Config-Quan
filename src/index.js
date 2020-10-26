import { DeviceEventEmitter, NativeEventEmitter, NativeModules, Platform } from 'react-native';

let { SmartconfigSwjava } = NativeModules;

const eventEmitter = new NativeEventEmitter(SmartconfigSwjava);
var subscription;

const startSmartConfig = (ssid, bssid, password, timeScan, callback) => {
  let eventNameToListener = 'SmartConfig';

  if (Platform.OS == 'ios') {
    console.log('start ios config');

    if (typeof subscription !== 'undefined' && subscription) {
      subscription.remove();
    }

    subscription = eventEmitter.addListener(eventNameToListener, callback);
    SmartconfigSwjava.start(ssid, bssid, password, timeScan, callback);

  } else {
    console.log('start android config');

    let taskCount = 1; // only find 1 device
    if (typeof subscription !== 'undefined' && subscription) {
      console.log('remove listener');
      subscription.remove();
    }
    subscription = DeviceEventEmitter.addListener(eventNameToListener, callback);
    SmartconfigSwjava.start(ssid, bssid, password, timeScan, taskCount);
  }
};

const stopSmartConfig = () => {
  console.log('Stop smart config');
  SmartconfigSwjava.stop();
};


class SmartConfig {
  static start(ssid, bssid, password, timeScan, callback) {
    startSmartConfig(ssid, bssid, password, timeScan, callback);
  }

  static stop() {
    stopSmartConfig();
  }
}

export default SmartConfig;

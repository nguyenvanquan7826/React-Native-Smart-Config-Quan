# react-native-smartconfig-quan

This library works both android and ios

## Installation
- step 1:
```sh
yarn add react-native-smartconfig-quan
```
- step 2:
```sh
cd ios && pod install
```


## Usage
Import the library:
```js
import SmartConfig from 'react-native-smartconfig-quan';
import {Button, Text, View} from "react-native";
import React, {useState} from "react";

export default function App() {

    const [wifiName, setWifiName] = useState('Viettel123');
    const [wifiPass, setWifiPass] = useState('0965677826');
    const [wifiBssid, setWifiBssid] = useState('8a:29:9c:69:af:9b');
    const [log, setLog] = useState('log here');

    // timeout not work with android, on android default is 45s
    const TIME_OUT_SMART_CONFIG = 30 * 1000; // 30s

    function config() {
        setLog('configuring...');
        SmartConfig.start(wifiName, wifiBssid, wifiPass, TIME_OUT_SMART_CONFIG, (event) => {
            if (event.eventName == 'onFoundDevice') {
                setLog("Found device with ip " + event.data);
            } else {
                setLog("Not Found");
            }
        });
    }

    return (
        <View style={{flex: 1, justifyContent: "center", alignItems: "center"}}>
            <Text>{log}</Text>

            <View style={{flexDirection: "row", justifyContent: "center", marginTop: 100}}>
                <Button title={'Start Config'} onPress={() => config()}/>
                <Button title={'Stop Config'} onPress={() => SmartConfig.stop()}/>
            </View>

        </View>
    )
}
```

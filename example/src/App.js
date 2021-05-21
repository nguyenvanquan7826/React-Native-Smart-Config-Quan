import SmartConfig from 'react-native-smartconfig-quan';
import { Button, Text, View } from 'react-native';
import React, { useState } from 'react';

export default function App() {

    const [log, setLog] = useState('log here');
    let foundDevice = false;

    const wifiName = 'Viettel';
    const wifiPass = '0965677826';
    // you can random bssid of wifi, but it need correct format
    const wifiBssid = '8a:29:9c:69:af:9b';

    // timeout not work with android, on android default is 45s
    const TIME_OUT_SMART_CONFIG = 30 * 1000; // 30s

    function config() {
        setLog('configuring...');
        foundDevice = false;

        SmartConfig.start(wifiName, wifiBssid, wifiPass, TIME_OUT_SMART_CONFIG, (event) => {
            console.log(event);
            let { eventName, data } = event;
            if (eventName === 'onFoundDevice') {
                foundDevice = true;
                data = JSON.parse(data);

                // data in event is ip of ESP
                setLog('Found device\nip: ' + data.ip + '\nbssid: ' + data.bssid);
            } else {
                if (!foundDevice) {
                    setLog('Not found');
                }
            }
        });
    }

    function stopConfig() {
        SmartConfig.stop();
        setLog('Stopped config');
    }

    return (
        <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
            <Text>{log}</Text>

            <View style={{ flexDirection: 'row', justifyContent: 'center', marginTop: 100 }}>
                <Button title={'Start Config'} onPress={() => config()} />

                <View width={20} />

                <Button title={'Stop Config'} onPress={() => stopConfig()} />
            </View>

        </View>
    );
}

import UIKit

@objc(SmartconfigSwjava)
class SmartconfigSwjava: RCTEventEmitter {

    let eventNameToRN = "SmartConfig"

    var SSID:String = ""
    var PASS:String = ""
    var BSSID:String = ""

    var isConfirmState: Bool!
    var condition:NSCondition!
    var esptouchTask: ESPTouchTask!
    //
    @objc var wifiPass = ""
    @objc var wifiName = ""
    var ipResult = "";
    
    @objc override static func requiresMainQueueSetup() -> Bool {
        return false
    }

    init(fromString string: NSString) {
        super.init()
        condition = NSCondition()
    }

    convenience override init() {
        self.init(fromString:"John") // calls above mentioned controller with default name
    }


    /* Configuration result */
    func executeForResult(timeScan: Int) -> ESPTouchResult {
        print("executeForResult")
        // Sync lock
        condition.lock()
        // Get the parameters required for configuration
        esptouchTask = ESPTouchTask(apSsid: SSID, andApBssid: BSSID, andApPwd: PASS, andTimeoutMillisecond : Int32(timeScan))
        // Set up proxy
        condition.unlock()
        let esptouchResult: ESPTouchResult = self.esptouchTask.executeForResult()
        return esptouchResult
    }

    /* Cancel distribution network */
    func cancel() {
        print("cancel")
        condition.lock()
        if self.esptouchTask != nil {
            self.esptouchTask.interrupt()
        }
        condition.unlock()
    }


    override func supportedEvents() -> [String]! {
        return [ eventNameToRN ]
    }

    // Takes an errorCallback as a parameter so that you know when things go wrong.
    // This will make more sense once we get to the Javascript
    // ["status": "penđing smartconfig", "data" : nil]
    //

    @objc func stop(){
        self.cancel();
    }

    @objc(start:bssid:password:timeScan:errorCallback:) func start( ssid: String, bssid: String, password:String, timeScan: Int, errorCallback: @escaping RCTResponseSenderBlock) {
        print("wifiName", ssid, bssid, password)

        if !ssid.isEmpty {
            SSID = ssid
            PASS = password
            BSSID = bssid

            print("SSID" , SSID, "PASS", PASS)
            print("Configuration in progress...")

            let queue = DispatchQueue.global(qos: .default)
            queue.async {
                print("Thread is working...")

                let esptouchResult: ESPTouchResult = self.executeForResult(timeScan: timeScan)
                DispatchQueue.main.async(execute: {
                    if !esptouchResult.isCancelled {
                        print(" esptouchResult.description",  esptouchResult.description)
                        // IP拼接

                        if (esptouchResult.getAddressString() != nil) {
                            let ip = esptouchResult.getAddressString() ?? ""
                            let bssid = esptouchResult.bssid ?? ""
                            let data = "{\"ip\":\"" + ip + "\", \"bssid\":\"" + bssid + "\"}"
                            self.sendEventToRN(eventName: "onFoundDevice", data: data)
                        }else {
                            self.sendEventToRN(eventName: "onFinishScan", data: "")
                        }
                    }
                })
            }
        } else {
            print("wifi disconnected")
            self.sendEventToRN(eventName: "onFinishScan", data: "")
            return
        }
    }

    func sendEventToRN(eventName: String, data: String) {
        self.sendEvent(withName: eventNameToRN, body: ["eventName":eventName, "data":data])
    }
}

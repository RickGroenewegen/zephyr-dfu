import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(ZephyrDfuPlugin)
public class ZephyrDfuPlugin: CAPPlugin {
    private let implementation = ZephyrDfu()

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }

    @objc func updateFirmware(_ call: CAPPluginCall) {
        call.keepAlive = true
        
        implementation.updateFirmware(call)
        
       
//        call.resolve([
//            "state": implementation.updateFirmware(call)
//        ])
    }
}



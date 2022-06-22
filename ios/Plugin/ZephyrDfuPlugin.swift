import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(ZephyrDfuPlugin)
public class ZephyrDfuPlugin: CAPPlugin {
    private let implementation = ZephyrDfu()

    @objc func updateFirmware(_ call: CAPPluginCall) {
        call.keepAlive = true        
        implementation.updateFirmware(call)        
    }
}



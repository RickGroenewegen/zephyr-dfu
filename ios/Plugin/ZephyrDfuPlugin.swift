import Foundation
import Capacitor

@objc(ZephyrDfuPlugin)
public class ZephyrDfuPlugin: CAPPlugin, CAPBridgedPlugin {

    public let identifier = "ZephyrDfuPlugin"
    public let jsName = "ZephyrDfu"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "updateFirmware", returnType: CAPPluginReturnCallback),
        CAPPluginMethod(name: "getVersion", returnType: CAPPluginReturnPromise)
    ]

    private let implementation = ZephyrDfu()

    @objc func updateFirmware(_ call: CAPPluginCall) {
        call.keepAlive = true
        implementation.updateFirmware(call)
    }

    @objc func getVersion(_ call: CAPPluginCall) {
        implementation.getVersion(call)
    }
}



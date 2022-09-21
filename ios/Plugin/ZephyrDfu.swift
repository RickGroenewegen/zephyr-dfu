import Foundation
import Capacitor
import iOSMcuManagerLibrary
import CoreBluetooth

@objc public class ZephyrDfu: UIViewController, CBPeripheralDelegate, CBCentralManagerDelegate, FirmwareUpgradeDelegate {
    
    private var dfuManagerConfiguration = FirmwareUpgradeConfiguration(
        estimatedSwapTime: 20,
        eraseAppSettings: false, 
        pipelineDepth: 4, 
        byteAlignment: .fourByte)
    
    var remotePeripheral: [CBPeripheral] = []
    
    var manager: CBCentralManager!
    var deviceID = "";
    var fileURL = "";
    var URLData:Data!
    var myCallback: CAPPluginCall!;
    
    public func upgradeDidStart(controller: FirmwareUpgradeController) {
        print("ZEPHYR-DFU - upgradeStarted")
        myCallback.resolve(["status": "upgradeStarted"])
    }
    
    public func upgradeStateDidChange(from previousState: FirmwareUpgradeState, to newState: FirmwareUpgradeState) {
        // States: validate, upload, test, reset, confirm
        print("ZEPHYR-DFU - stateChanged")
        myCallback.resolve(["status": "stateChanged", "data" : ["prevState" : "\(previousState)", "newState" : "\(newState)" ]])
    }
    
    public func upgradeDidComplete() {
        print("ZEPHYR-DFU - upgradeDidComplete")
        myCallback.resolve(["status": "upgradeCompleted"])
    }
    
    public func upgradeDidFail(inState state: FirmwareUpgradeState, with error: Error) {
        print("ZEPHYR-DFU - upgradeDidFail")
        myCallback.resolve(["status": "upgradeFailed"])
    }
    
    public func upgradeDidCancel(state: FirmwareUpgradeState) {
        print("ZEPHYR-DFU - upgradeDidCancel")
        myCallback.resolve(["status": "upgradeCanceled"])
    }
    
    public func uploadProgressDidChange(bytesSent: Int, imageSize: Int, timestamp: Date) {
        myCallback.resolve(["status": "uploadProgressChanged", "data" : ["bytesSent" : bytesSent, "imageSize" : imageSize ]])
        print("ZEPHYR-DFU - uploadProgressDidChange: " + String(bytesSent) + " / " + String(imageSize))
    }
        
    // In CBCentralManagerDelegate class/extension
    public func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        // Successfully connected. Store reference to peripheral if not already done.
        print("ZEPHYR-DFU - Connected to device")
        let bleTransport = McuMgrBleTransport(peripheral)
        // Initialize the FirmwareUpgradeManager using the transport and a delegate
        let dfuManager = FirmwareUpgradeManager(transporter: bleTransport,delegate: self)
        dfuManager.mode = FirmwareUpgradeMode.testAndConfirm
        do {
            print("ZEPHYR-DFU - Using file URL: " + fileURL);
            let package = try McuMgrPackage(from: URL(string: ("file://" + fileURL))!)
            try dfuManager.start(images: package.images, using: dfuManagerConfiguration)
        } catch {
            print("ZEPHYR-DFU - Error creating package: \(error)")
        }
    }
    
    public func centralManagerDidUpdateState(_ central: CBCentralManager) {
    
     switch central.state {
      case .poweredOff:
         print("ZEPHYR-DFU - Is Powered Off.")
      case .poweredOn:
         print("ZEPHYR-DFU - Is Powered On.")
         startScanning()
      case .unsupported:
         print("ZEPHYR-DFU - Is Unsupported.")
      case .unauthorized:
         print("ZEPHYR-DFU - Is Unauthorized.")
      case .unknown:
         print("ZEPHYR-DFU - Unknown")
      case .resetting:
         print("ZEPHYR-DFU - Resetting")
      @unknown default:
         print("ZEPHYR-DFU - Error")
      }
    }
    
    func startScanning() -> Void {
        print("ZEPHYR-DFU - Starting to scan ...")
        manager.scanForPeripherals(withServices: [])
    }
    
    @objc public func updateFirmware(_ call: CAPPluginCall) -> String {
        fileURL = call.getString("fileURL") ?? ""
        deviceID = call.getString("deviceIdentifier") ?? ""
        manager = CBCentralManager(delegate: self, queue: nil)
        myCallback = call;
        myCallback.resolve(["status": "started"])
        return deviceID + " // " + fileURL;
    }
    
    func doUpdateOnPeripheral(_ peripheral: CBPeripheral) {
        print("ZEPHYR-DFU - Peripheral found: \(peripheral)")
        self.remotePeripheral.append(peripheral)
        manager?.connect(peripheral, options: nil)
    }
    
    public func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral,advertisementData: [String : Any], rssi RSSI: NSNumber) {
            if(peripheral.identifier.uuidString == deviceID) {
                manager.stopScan()
                doUpdateOnPeripheral(peripheral);
            }
    }
}

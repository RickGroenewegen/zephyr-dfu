import Foundation
import Capacitor
import iOSMcuManagerLibrary
import CoreBluetooth

@objc public class ZephyrDfu: UIViewController, CBPeripheralDelegate, CBCentralManagerDelegate, FirmwareUpgradeDelegate {
    
    let serviceID = "AFB2040C-9519-4453-9079-BED75069BA91"
    private var dfuManagerConfiguration = FirmwareUpgradeConfiguration(
        eraseAppSettings: false, pipelineDepth: 4, byteAlignment: .fourByte)
    
    var remotePeripheral: [CBPeripheral] = []
    
    var manager: CBCentralManager!
    var deviceID = "";
    var fileURL = "";
    var URLData:Data!
    var myCallback: CAPPluginCall!;
    
    public func upgradeDidStart(controller: FirmwareUpgradeController) {
        print("upgradeStarted")
        myCallback.resolve(["status": "upgradeStarted"])
    }
    
    public func upgradeStateDidChange(from previousState: FirmwareUpgradeState, to newState: FirmwareUpgradeState) {
        print("stateChanged")
        myCallback.resolve(["status": "stateChanged", "data" : ["prevState" : "\(previousState)", "newState" : "\(newState)" ]])
    }
    
    public func upgradeDidComplete() {
        print("upgradeDidComplete")
    }
    
    public func upgradeDidFail(inState state: FirmwareUpgradeState, with error: Error) {
        print("upgradeDidFail")
    }
    
    public func upgradeDidCancel(state: FirmwareUpgradeState) {
        print("upgradeDidCancel")
    }
    
    public func uploadProgressDidChange(bytesSent: Int, imageSize: Int, timestamp: Date) {
        
        myCallback.resolve(["status": "uploadProgressChanged", "data" : ["bytesSent" : bytesSent, "imageSize" : imageSize ]])
        
        print("uploadProgressDidChange: " + String(bytesSent) + " / " + String(imageSize))
    }
        
    // In CBCentralManagerDelegate class/extension
    public func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        // Successfully connected. Store reference to peripheral if not already done.
        //self.connectedPeripheral = peripheral
        print("CONNECTED TO DEVICE!!!")
        let bleTransport = McuMgrBleTransport(peripheral)
        
        // Initialize the FirmwareUpgradeManager using the transport and a delegate
        let dfuManager = FirmwareUpgradeManager(transporter: bleTransport,delegate: self)

        dfuManager.mode = FirmwareUpgradeMode.testAndConfirm

        do {

            print("Using file URL: " + fileURL);


            print("Try to unzip BIN. Source: " + fileURL);
            
            let url = NSURL(fileURLWithPath: fileURL);
            let destinationURL = url.deletingLastPathComponent!
            
            print("Try to unzip BIN. Destination: " + destinationURL.absoluteString);
            
            do {
                let fileManager = FileManager()
                let sourceURL = URL(fileURLWithPath: fileURL)
                try fileManager.unzipItem(at: sourceURL, to: destinationURL)
            } catch {
                print("Extraction of ZIP archive failed with error:\(error)")
            }
            
            let binPath = destinationURL.absoluteString + "app_update.bin";
            
            print("Starting update: " + binPath);
            
            let package = try McuMgrPackage(from: URL(string: (binPath))!)


            print("Peripheral BEFORE START: \(peripheral)")

            try dfuManager.start(images: package.images, using: dfuManagerConfiguration)
            
            print("DONE WITH UPDATE")
            
        } catch {
            print("Error creating package: \(error)")
        }
        
    }
    
   
    
    public func centralManagerDidUpdateState(_ central: CBCentralManager) {
    
     switch central.state {
      case .poweredOff:
          print("Is Powered Off.")
      case .poweredOn:
          print("Is Powered On.")
          startScanning()
      case .unsupported:
          print("Is Unsupported.")
      case .unauthorized:
      print("Is Unauthorized.")
      case .unknown:
          print("Unknown")
      case .resetting:
          print("Resetting")
      @unknown default:
        print("Error")
      }
    }
    
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
    
    func startScanning() -> Void {
        print("Starting to scan ...")
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
        print("Peripheral FOUND: \(peripheral)")
        self.remotePeripheral.append(peripheral)
        manager?.connect(peripheral, options: nil)
    }
    
    public func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral,advertisementData: [String : Any], rssi RSSI: NSNumber) {
        print(peripheral)
        if(peripheral.identifier.uuidString == deviceID) {
            manager.stopScan()
            doUpdateOnPeripheral(peripheral);
            
        }
        
       }
}

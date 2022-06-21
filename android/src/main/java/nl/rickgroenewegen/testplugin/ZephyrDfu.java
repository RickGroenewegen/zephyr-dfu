package nl.rickgroenewegen.testplugin;

import android.app.Application;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import io.runtime.mcumgr.*;
import io.runtime.mcumgr.ble.*;
import io.runtime.mcumgr.dfu.FirmwareUpgradeManager;
import io.runtime.mcumgr.sample.utils.ZipPackage;
import android.content.Context;
import android.bluetooth.*;

import androidx.annotation.RequiresApi;

import com.getcapacitor.Plugin;

public class ZephyrDfu extends Plugin {

	private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
	private BluetoothLeScanner mLEScanner = null;
	private Handler mHandler;
	private String myDeviceIdentifier = "";
	private BluetoothDevice device = null;
	private Context myContext = null;

	private static final long SCAN_PERIOD = 5000;

	public String echo(String value) {
		Log.i("Echo", value);
		return value;
	}

	/* Scan result for SDK >= 21 */
	private ScanCallback mScanCallback = new ScanCallback() {

		@Override
		public void onScanResult(int callbackType, ScanResult result) {

			super.onScanResult(callbackType, result);

			try {

				if(result.getDevice().getAddress().equals(myDeviceIdentifier)) {

					System.out.println("BLE// result" + result.toString());
					System.out.println("BLE// Device Name: " + result.getDevice().getName());
					System.out.println("BLE// Device address: " + result.getDevice().getAddress());
					device = result.getDevice();
					mLEScanner.stopScan(mScanCallback);
					doUpdateFirmware(device);
				}
			} catch(Exception e) {
				System.out.println("BLE// callbackType");
			}
		}

		@Override
		public void onScanFailed(int errorCode) {
			System.out.println("BLE// onScanFailed");
			Log.e("Scan Failed", "Error Code: " + errorCode);
		}

	};


	void doUpdateFirmware(BluetoothDevice device){
		System.out.println("BLE// Do update: " + device.getAddress());
		McuMgrTransport transport = new McuMgrBleTransport(myContext, device);

		// Initialize the Firmware Upgrade Manager.
		FirmwareUpgradeManager dfuManager = new FirmwareUpgradeManager(transport);

		dfuManager.setEstimatedSwapTime(10000);
		dfuManager.setWindowUploadCapacity(4);
		dfuManager.setMode(FirmwareUpgradeManager.Mode.TEST_ONLY);

		//final ZipPackage zip = new ZipPackage(data);
		//images = zip.getBinaries();

		System.out.println("BLE// Update done: " + device.getAddress());
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	void startScanning(){

		mLEScanner = adapter.getBluetoothLeScanner();
		mHandler = new Handler();

		//stops scanning after a pre-defined scan period
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mLEScanner.stopScan(mScanCallback);
				System.out.println("BLE// mLEScanner.stopScan(mScanCallback)");
			}
		}, SCAN_PERIOD);

		mLEScanner.startScan(mScanCallback);
		System.out.println("BLE// mLEScanner.startScan(mScanCallback) ");

	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	public String updateFirmware(String fileURL, String deviceIdentifier, Context context) {
		String value = deviceIdentifier + " / " + fileURL;
		myContext = context;
		myDeviceIdentifier = deviceIdentifier;
		startScanning();
		return value;
	}
}

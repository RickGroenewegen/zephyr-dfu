package nl.rickgroenewegen.testplugin;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import io.runtime.mcumgr.*;
import io.runtime.mcumgr.ble.*;
import io.runtime.mcumgr.dfu.FirmwareUpgradeCallback;
import io.runtime.mcumgr.dfu.FirmwareUpgradeController;
import io.runtime.mcumgr.dfu.FirmwareUpgradeManager;
import io.runtime.mcumgr.exception.McuMgrException;
import io.runtime.mcumgr.sample.utils.ZipPackage;
import android.content.Context;
import android.bluetooth.*;
import android.util.Pair;

import androidx.annotation.RequiresApi;
import com.getcapacitor.Plugin;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class ZephyrDfu extends Plugin implements FirmwareUpgradeCallback {

	private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
	private BluetoothLeScanner mLEScanner = null;
	private Handler mHandler;
	private String myDeviceIdentifier = "";
	private String myFileURL = "";
	private BluetoothDevice device = null;
	private Context myContext = null;

	private static final long SCAN_PERIOD = 5000;

	public String echo(String value) {
		Log.i("Echo", value);
		return value;
	}

	@Override
	public void onUpgradeStarted(final FirmwareUpgradeController controller) {
		System.out.println("BLE// onUpgradeStarted");
	}

	@Override
	public void onStateChanged(
		final FirmwareUpgradeManager.State prevState,
		final FirmwareUpgradeManager.State newState)
	{
		System.out.println("BLE// onStateChanged. Old = " + prevState + " / New = " + newState);
	}

	@Override
	public void onUpgradeCompleted() {
		System.out.println("BLE// onUpgradeCompleted");
	}

	@Override
	public void onUpgradeCanceled(final FirmwareUpgradeManager.State state) {
		System.out.println("BLE// onUpgradeCanceled");
	}

	@Override
	public void onUpgradeFailed(final FirmwareUpgradeManager.State state, final McuMgrException error) {
		System.out.println("BLE// onUpgradeFailed");
	}

	@Override
	public void onUploadProgressChanged(final int bytesSent, final int imageSize, final long timestamp) {
		System.out.println("BLE// onUploadProgressChanged: " + bytesSent + " / " + imageSize);
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
				System.out.println("BLE// callbackTypeError");
			}
		}

		@Override
		public void onScanFailed(int errorCode) {
			System.out.println("BLE// onScanFailed");
			Log.e("Scan Failed", "Error Code: " + errorCode);
		}
	};

	byte[] readFile()  {
		String fixedFilePath = myFileURL.replace("file:/","");
		File file = new File(fixedFilePath);
		byte[] fileData = new byte[(int) file.length()];
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(file));
			dis.readFully(fileData);
			dis.close();
		} catch(IOException e) {
			System.out.println("BLE// File read ERROR");
			Log.e("MYAPP", "BLE// Exception", e);
		}
		return fileData;
	}

	void doUpdateFirmware(BluetoothDevice device) throws IOException {
		System.out.println("BLE// Do update: " + device.getAddress());
		McuMgrTransport transport = new McuMgrBleTransport(myContext, device);

		// Initialize the Firmware Upgrade Manager.
		FirmwareUpgradeManager dfuManager = new FirmwareUpgradeManager(transport,this);

		dfuManager.setEstimatedSwapTime(20000);
		dfuManager.setWindowUploadCapacity(4);
		dfuManager.setMode(FirmwareUpgradeManager.Mode.TEST_AND_CONFIRM);

		final ZipPackage zip = new ZipPackage(readFile());
		List<Pair<Integer, byte[]>> images = zip.getBinaries();

		try {
			dfuManager.start(images, false);
		} catch (McuMgrException e) {
			e.printStackTrace();
		}

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
		myFileURL = fileURL;
		startScanning();
		return value;
	}
}

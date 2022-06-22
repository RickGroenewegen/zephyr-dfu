package nl.rickgroenewegen.zephyrdfu;

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

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;

import org.json.JSONException;

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
	private FirmwareUpdateCallback myCallback = null;

	private static final long SCAN_PERIOD = 5000;

	public String echo(String value) {
		Log.i("Echo", value);
		return value;
	}

	@Override
	public void onUpgradeStarted(final FirmwareUpgradeController controller) {
		Log.i("ZEPHYR-DFU","onUpgradeStarted");
		myCallback.success("upgradeStarted",null);
	}

	@Override
	public void onStateChanged(
		final FirmwareUpgradeManager.State prevState,
		final FirmwareUpgradeManager.State newState)
	{
		// States: validate, upload, test, reset, confirm
		Log.i("ZEPHYR-DFU","onStateChanged. Old = " + prevState + " / New = " + newState);
		String convertedNewState = newState.toString().toLowerCase();
		myCallback.success("stateChanged",new JSObject().put("prevState", prevState).put("newState", convertedNewState));
	}

	@Override
	public void onUpgradeCompleted() {
		Log.i("ZEPHYR-DFU","onUpgradeCompleted");
		myCallback.success("upgradeCompleted",null);
	}

	@Override
	public void onUpgradeCanceled(final FirmwareUpgradeManager.State state) {
		Log.i("ZEPHYR-DFU","onUpgradeCanceled");
		myCallback.success("upgradeCanceled",null);
	}

	@Override
	public void onUpgradeFailed(final FirmwareUpgradeManager.State state, final McuMgrException error) {
		Log.i("ZEPHYR-DFU","onUpgradeFailed");
		myCallback.success("upgradeFailed",null);
	}

	@Override
	public void onUploadProgressChanged(final int bytesSent, final int imageSize, final long timestamp) {
		Log.i("ZEPHYR-DFU","onUploadProgressChanged: " + bytesSent + " / " + imageSize);
		myCallback.success("uploadProgressChanged", new JSObject().put("bytesSent", bytesSent).put("imageSize", imageSize));
	}

	/* Scan result for SDK >= 21 */
	private ScanCallback mScanCallback = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			super.onScanResult(callbackType, result);
			try {
				if(result.getDevice().getAddress().equals(myDeviceIdentifier)) {
					myCallback.success("deviceFound",null);
					Log.i("ZEPHYR-DFU","Device Name: " + result.getDevice().getName() + "(" + result.getDevice().getAddress() + ")");
					device = result.getDevice();
					mLEScanner.stopScan(mScanCallback);
					doUpdateFirmware(device);
				}
			} catch(Exception e) {
				Log.i("ZEPHYR-DFU","callbackTypeError");
			}
		}

		@Override
		public void onScanFailed(int errorCode) {
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
			Log.e("MYAPP", "ZEPHYR-DFU File read Exception", e);
		}
		return fileData;
	}

	void doUpdateFirmware(BluetoothDevice device) throws IOException {
		Log.i("ZEPHYR-DFU","Do update: " + device.getAddress());
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

		Log.i("ZEPHYR-DFU","Update done: " + device.getAddress());
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
				Log.i("ZEPHYR-DFU","mLEScanner.stopScan(mScanCallback)");
			}
		}, SCAN_PERIOD);

		mLEScanner.startScan(mScanCallback);
		Log.i("ZEPHYR-DFU","mLEScanner.startScan(mScanCallback)");

	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	public String updateFirmware(String fileURL, String deviceIdentifier, Context context, FirmwareUpdateCallback callback) {
		myCallback = callback;
		myCallback.success("starting",null);
		String value = deviceIdentifier + " / " + fileURL;
		myContext = context;
		myDeviceIdentifier = deviceIdentifier;
		myFileURL = fileURL;
		startScanning();
		return value;
	}
}

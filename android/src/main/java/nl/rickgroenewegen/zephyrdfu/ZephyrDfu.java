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
import io.runtime.mcumgr.managers.ImageManager;
import io.runtime.mcumgr.exception.McuMgrException;
import io.runtime.mcumgr.response.img.McuMgrImageStateResponse;
import io.runtime.mcumgr.sample.utils.ZipPackage;
import android.content.Context;
import android.bluetooth.*;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;

import org.jetbrains.annotations.NotNull;

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
	private String mode = "update";
	private McuMgrBleTransport myTransport = null;

	private static final long SCAN_PERIOD = 5000;

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
		myTransport.release();
		myCallback.success("upgradeCompleted",null);
	}

	@Override
	public void onUpgradeCanceled(final FirmwareUpgradeManager.State state) {
		Log.i("ZEPHYR-DFU","onUpgradeCanceled");
		myTransport.release();
		myCallback.success("upgradeCanceled",null);
	}

	@Override
	public void onUpgradeFailed(final FirmwareUpgradeManager.State state, final McuMgrException error) {
		Log.i("ZEPHYR-DFU","onUpgradeFailed");
		myTransport.release();
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
					Log.i("ZEPHYR-DFU","Device Name: " + result.getDevice().getName() + "(" + result.getDevice().getAddress() + ")");
					device = result.getDevice();
					mLEScanner.stopScan(mScanCallback);
					Log.i("ZEPHYR-DFU","scanning mode: " + mode);
					if(mode == "update") {
						doUpdateFirmware(device);
					} else if (mode == "version") {
						doVersion(device);
					}
				}
			} catch(Exception e) {
				Log.i("ZEPHYR-DFU","callbackTypeError: " + e.getLocalizedMessage());
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
		myTransport = new McuMgrBleTransport(myContext, device);

		// Initialize the Firmware Upgrade Manager.
		FirmwareUpgradeManager dfuManager = new FirmwareUpgradeManager(myTransport,this);

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

	void doVersion(BluetoothDevice device) throws IOException {
		Log.i("ZEPHYR-DFU","Do version 1: " + device.getAddress());
		McuMgrTransport transport = new McuMgrBleTransport(myContext, device);

		ImageManager manager = new ImageManager(transport);

		manager.list(new McuMgrCallback<>() {
			@Override
			public void onError(@NotNull McuMgrException error) {

			}

			@Override
			public void onResponse(@NonNull final McuMgrImageStateResponse response) {
				if (response.images != null) {
					Log.i("ZEPHYR-DFU","Do version RESPONSE 1: " + response.images[0].version);
					transport.release();
					myCallback.success(response.images[0].version,null);
				}
			}
		});

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
		mode = "update";
		myCallback = callback;
		myCallback.success("started",null);
		String value = deviceIdentifier + " / " + fileURL;
		myContext = context;
		myDeviceIdentifier = deviceIdentifier;
		myFileURL = fileURL;
		startScanning();
		return value;
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	public String getVersion(String deviceIdentifier, Context context, FirmwareUpdateCallback callback) {
		mode = "version";
		Log.i("ZEPHYR-DFU","getVersion 1");
		myCallback = callback;

		myContext = context;
		myDeviceIdentifier = deviceIdentifier;
		startScanning();

		//myCallback.success("0.0.21",null);
		return "";
	}
}

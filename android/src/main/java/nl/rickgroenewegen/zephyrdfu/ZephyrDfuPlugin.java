package nl.rickgroenewegen.zephyrdfu;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "ZephyrDfu")
public class ZephyrDfuPlugin extends Plugin {

    private ZephyrDfu implementation = new ZephyrDfu();

	@RequiresApi(api = Build.VERSION_CODES.M)
	@PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
    public void updateFirmware(PluginCall call) {
        String fileURL = call.getString("fileURL");
        String deviceIdentifier = call.getString("deviceIdentifier");
        call.setKeepAlive(true);
       	Log.i("BLE//","Starting firmware update");
       	implementation.updateFirmware(fileURL,deviceIdentifier,this.getActivity().getApplicationContext(),new FirmwareUpdateCallback() {
			@Override
			public void success(String msg, JSObject data) {
				JSObject ret = new JSObject();
				ret.put("status", msg);
				if(data != null) {
					ret.put("data", data);
				}
				//ret.put("data2", data2);
				call.resolve(ret);
			}
			@Override
			public void error(String msg) {
				JSObject ret = new JSObject();
				ret.put("status", msg);
				call.resolve(ret);
			}
		});
    }

}

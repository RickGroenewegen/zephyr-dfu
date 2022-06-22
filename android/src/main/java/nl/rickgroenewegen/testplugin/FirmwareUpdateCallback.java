package nl.rickgroenewegen.testplugin;

import com.getcapacitor.JSObject;

public interface FirmwareUpdateCallback {
	void success(String msg, JSObject data);
	void error(String msg);
}

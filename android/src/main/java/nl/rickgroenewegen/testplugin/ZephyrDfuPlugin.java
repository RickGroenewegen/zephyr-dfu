package nl.rickgroenewegen.testplugin;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "ZephyrDfu")
public class ZephyrDfuPlugin extends Plugin {

    private ZephyrDfu implementation = new ZephyrDfu();

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    public void updateFirmware(PluginCall call) {
        String fileURL = call.getString("fileURL");
        String deviceIdentifier = call.getString("deviceIdentifier");
        // More logic
        call.resolve();
    }

}

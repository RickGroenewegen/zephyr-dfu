#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(ZephyrDfuPlugin, "ZephyrDfu",
           CAP_PLUGIN_METHOD(updateFirmware, CAPPluginReturnCallback);
           CAP_PLUGIN_METHOD(getVersion, CAPPluginReturnPromise);
)

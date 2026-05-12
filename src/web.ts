import { WebPlugin } from '@capacitor/core';

import type { 
	ZephyrDfuPlugin, 
	firmwareUpdateCallback, 
	CallbackID 
} from './definitions';

export class ZephyrDfuWeb extends WebPlugin implements ZephyrDfuPlugin {
	async updateFirmware(options: {fileURL:string, deviceIdentifier: string}, callback: firmwareUpdateCallback): Promise<CallbackID> {
		/// logic here
		console.log(options.fileURL,options.deviceIdentifier);
		callback('Web callback');
		return ``;
	}
	async getVersion(): Promise<string> {
		/// logic here
		return '';
	}
}

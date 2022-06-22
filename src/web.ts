import {WebPlugin } from '@capacitor/core';

import type { 
	ZephyrDfuPlugin, 
	firmwareUpdateCallback, 
	CallbackID 
} from './definitions';

export class ZephyrDfuWeb extends WebPlugin implements ZephyrDfuPlugin {
	async echo(options: { value: string }): Promise<{ value: string }> {
		console.log('ECHO', options);
		return options;
	}
	async updateFirmware(options: {fileURL:string, deviceIdentifier: string}, callback: firmwareUpdateCallback): Promise<CallbackID> {
		/// logic here
		console.log(options.fileURL,options.deviceIdentifier);
		callback('Hoi ik ben web callback');
		return `blabladitisreturn`;
	}
}

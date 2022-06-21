import { WebPlugin } from '@capacitor/core';

import type { ZephyrDfuPlugin } from './definitions';

export class ZephyrDfuWeb extends WebPlugin implements ZephyrDfuPlugin {
	async echo(options: { value: string }): Promise<{ value: string }> {
		console.log('ECHO', options);
		return options;
	}
	async updateFirmware(options: {fileURL:string, deviceIdentifier: string}): Promise<{ value: string }> {
		/// logic here
		console.log(options.fileURL,options.deviceIdentifier);
		return { value : 'Ik ben web' };
	}
}

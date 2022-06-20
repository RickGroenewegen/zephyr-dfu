import { WebPlugin } from '@capacitor/core';

import type { RickTestPlugin } from './definitions';

export class RickTestWeb extends WebPlugin implements RickTestPlugin {
	async echo(options: { value: string }): Promise<{ value: string }> {
		console.log('ECHO', options);
		return options;
	}
	async updateFirmware(fileURL:string, deviceIdentifier: string): Promise<{ value: string }> {
		/// logic here
		console.log(fileURL,deviceIdentifier);
		return { value : 'Ik ben web' };
	}
}

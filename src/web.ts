import { WebPlugin } from '@capacitor/core';

import type { RickTestPlugin } from './definitions';

export class RickTestWeb extends WebPlugin implements RickTestPlugin {
	async echo(options: { value: string }): Promise<{ value: string }> {
		console.log('ECHO', options);
		return options;
	}
}

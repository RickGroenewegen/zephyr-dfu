import { registerPlugin } from '@capacitor/core';

import type { RickTestPlugin } from './definitions';

const RickTest = registerPlugin<RickTestPlugin>('RickTest', {
	web: () => import('./web').then(m => new m.RickTestWeb()),
});

export * from './definitions';
export { RickTest };

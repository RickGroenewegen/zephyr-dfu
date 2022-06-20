import { registerPlugin } from '@capacitor/core';

import type { ZephyrDfuPlugin } from './definitions';

const ZephyrDfu = registerPlugin<ZephyrDfuPlugin>('ZephyrDfu', {
	web: () => import('./web').then(m => new m.ZephyrDfuWeb()),
});

export * from './definitions';
export { ZephyrDfu };

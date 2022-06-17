export default {
	input: 'dist/esm/index.js',
	output: [
		{
			file: 'dist/plugin.js',
			format: 'iife',
			name: 'capacitorRickTest',
			globals: {
				'@capacitor/core': 'capacitorExports',
			},
			sourcemap: true,
			inlineDynamicImports: true,
		},
		{
			file: 'dist/plugin.cjs.js',
			format: 'cjs',
			sourcemap: true,
			inlineDynamicImports: true,
		},
	],
	external: ['@capacitor/core'],
};

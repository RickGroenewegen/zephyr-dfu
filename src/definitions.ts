export interface RickTestPlugin {
	echo(options: { value: string }): Promise<{ value: string }>;
}

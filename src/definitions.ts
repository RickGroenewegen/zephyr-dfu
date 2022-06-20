export interface ZephyrDfuPlugin {
	echo(options: { value: string }): Promise<{ value: string }>;
	updateFirmware(fileURL: string, deviceIdentifier: string): Promise<{ value: string }>;
}
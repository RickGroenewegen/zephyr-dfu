export interface ZephyrDfuPlugin {
	echo(options: { value: string }): Promise<{ value: string }>;
	updateFirmware(options: { fileURL: string, deviceIdentifier: string }): Promise<{ value: string }>;
}
export type CallbackID = string;

export interface ZephyrDfuPlugin {
	echo(options: { value: string }): Promise<{ value: string }>;
	updateFirmware(
		options: { fileURL: string, deviceIdentifier: string },
		callback: firmwareUpdateCallback
	): Promise<CallbackID>;
}

export type firmwareUpdateCallback = (
	msg: String | null,
	err?: any,
) => void;
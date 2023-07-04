export type CallbackID = string;

export interface ZephyrDfuPlugin {
	updateFirmware(
		options: { fileURL: string, deviceIdentifier: string },
		callback: firmwareUpdateCallback
	): Promise<CallbackID>;
	getVersion(): Promise<string>;
}

export type firmwareUpdateCallback = (
	msg: String | null,
	err?: any,
) => void;
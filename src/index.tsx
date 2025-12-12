import Rdservice, { type RdServiceResponse } from './NativeRdservice';

export type { RdServiceResponse };

export function getFingerPrint(
  deviceName: string,
  pidOption: string
): Promise<RdServiceResponse> {
  return Rdservice.getFingerPrint(deviceName, pidOption);
}

export function getFaceCapture(
  deviceName: string,
  pidOption: string
): Promise<RdServiceResponse> {
  return Rdservice.getFaceCapture(deviceName, pidOption);
}

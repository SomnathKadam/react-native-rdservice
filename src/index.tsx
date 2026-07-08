import Rdservice, { type RdServiceResponse } from './NativeRdservice';

export type { RdServiceResponse };

/** Possible values of {@link RdServiceResponse.status}. */
export type RdServiceStatus = 'SUCCESS' | 'FAILURE';

/**
 * Queries the RD service app for device/driver status (RD INFO call).
 *
 * @param deviceName Package name of the RD service app, e.g. `com.mantra.rdservice`.
 * @returns On success, `message` contains the `RDService` info XML.
 */
export function getDeviceInfo(deviceName: string): Promise<RdServiceResponse> {
  return Rdservice.getDeviceInfo(deviceName);
}

/**
 * Captures fingerprint biometric data via the given RD service app.
 *
 * @param deviceName Package name of the RD service app, e.g. `com.mantra.rdservice`.
 * @param pidOption Optional `PidOptions` XML. Falls back to a sensible
 *   single-finger capture configuration when omitted or blank.
 * @returns On success, `message` contains the signed `PidData` XML.
 */
export function getFingerPrint(
  deviceName: string,
  pidOption: string = ''
): Promise<RdServiceResponse> {
  return Rdservice.getFingerPrint(deviceName, pidOption);
}

/**
 * Captures iris biometric data via the given iris RD service app.
 *
 * @param deviceName Package name of the iris RD service app, e.g. `com.mantra.mis100v2.rdservice`.
 * @param pidOption Optional `PidOptions` XML. Falls back to a single-iris
 *   capture configuration when omitted or blank.
 * @returns On success, `message` contains the signed `PidData` XML.
 */
export function getIrisCapture(
  deviceName: string,
  pidOption: string = ''
): Promise<RdServiceResponse> {
  return Rdservice.getIrisCapture(deviceName, pidOption);
}

/**
 * Captures face biometric data via the UIDAI Face RD app.
 *
 * @param deviceName Package name of the face RD service app, e.g. `in.gov.uidai.facerd`.
 * @param pidOption Optional `PidOptions` XML. The default targets face
 *   authentication; pass your own options (with the correct `wadh`) for eKYC.
 * @returns On success, `message` contains the signed `PidData` XML.
 */
export function getFaceCapture(
  deviceName: string,
  pidOption: string = ''
): Promise<RdServiceResponse> {
  return Rdservice.getFaceCapture(deviceName, pidOption);
}

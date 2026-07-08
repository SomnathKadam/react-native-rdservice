import { TurboModuleRegistry, type TurboModule } from 'react-native';

export interface RdServiceResponse {
  /** "SUCCESS" or "FAILURE" */
  status: string;
  /** Signed XML from the RD service on success, human-readable error on failure */
  message: string;
}

export interface Spec extends TurboModule {
  getDeviceInfo(deviceName: string): Promise<RdServiceResponse>;
  getFingerPrint(
    deviceName: string,
    pidOption: string
  ): Promise<RdServiceResponse>;
  getIrisCapture(
    deviceName: string,
    pidOption: string
  ): Promise<RdServiceResponse>;
  getFaceCapture(
    deviceName: string,
    pidOption: string
  ): Promise<RdServiceResponse>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('Rdservice');

import { TurboModuleRegistry, type TurboModule } from 'react-native';

export interface RdServiceResponse {
  status: string;
  message: string;
}

export interface Spec extends TurboModule {
  getFingerPrint(
    deviceName: string,
    pidOption: string
  ): Promise<RdServiceResponse>;
  getFaceCapture(
    deviceName: string,
    pidOption: string
  ): Promise<RdServiceResponse>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('Rdservice');

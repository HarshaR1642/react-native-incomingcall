import { TurboModuleRegistry, type TurboModule } from 'react-native';

export interface Spec extends TurboModule {
  registerReceiver(): void;
  unregisterReceiver(): void;
  showIncomingCall(): void;
  endCall(): void;
  areNotificationsEnabled(): Promise<boolean>;
}

const IncomingCallTurboModule =
  TurboModuleRegistry.getEnforcing<Spec>('IncomingCall');

export default IncomingCallTurboModule;

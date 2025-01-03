import { type TurboModule } from 'react-native';
export interface Spec extends TurboModule {
    registerReceiver(): void;
    unregisterReceiver(): void;
    showIncomingCall(): void;
    endCall(): void;
    areNotificationsEnabled(): Promise<boolean>;
}
declare const IncomingCallTurboModule: Spec;
export default IncomingCallTurboModule;
//# sourceMappingURL=NativeIncomingCall.d.ts.map
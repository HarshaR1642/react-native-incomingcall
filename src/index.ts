import { NativeModules } from 'react-native';

const IncomingCall = NativeModules.IncomingCall;

const showIncomingCall = (options: { accessToken: string }): void => {
  IncomingCall.showIncomingCall(options);
};

const endCall = (): void => {
  IncomingCall.endCall();
};

const areNotificationsEnabled = async () => {
  const granted = await IncomingCall.areNotificationsEnabled();
  return granted;
};

export { showIncomingCall, endCall, areNotificationsEnabled };

import { NativeModules, Platform } from 'react-native';

const IncomingCall = NativeModules.IncomingCall;

const registerReceiver = (): void => {
  if (Platform.OS === 'android') {
    IncomingCall.registerReceiver();
  }
};

const unregisterReceiver = (): void => {
  if (Platform.OS === 'android') {
    IncomingCall.unregisterReceiver();
  }
};

const showIncomingCall = (options = {}): void => {
  if (Platform.OS === 'android') {
    IncomingCall.showIncomingCall(options);
  }
};

const endCall = (): void => {
  if (Platform.OS === 'android') {
    IncomingCall.endCall();
  }
};

const areNotificationsEnabled = async () => {
  const granted = await IncomingCall.areNotificationsEnabled();
  return granted;
};

export {
  showIncomingCall,
  endCall,
  areNotificationsEnabled,
  registerReceiver,
  unregisterReceiver,
};

import { Platform } from 'react-native';
import IncomingCall from './NativeIncomingCall';

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

const showIncomingCall = (): void => {
  if (Platform.OS === 'android') {
    IncomingCall.showIncomingCall();
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

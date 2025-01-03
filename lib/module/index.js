"use strict";

import { Platform } from 'react-native';
import IncomingCall from "./NativeIncomingCall.js";
const registerReceiver = () => {
  if (Platform.OS === 'android') {
    IncomingCall.registerReceiver();
  }
};
const unregisterReceiver = () => {
  if (Platform.OS === 'android') {
    IncomingCall.unregisterReceiver();
  }
};
const showIncomingCall = () => {
  if (Platform.OS === 'android') {
    IncomingCall.showIncomingCall();
  }
};
const endCall = () => {
  if (Platform.OS === 'android') {
    IncomingCall.endCall();
  }
};
const areNotificationsEnabled = async () => {
  const granted = await IncomingCall.areNotificationsEnabled();
  return granted;
};
export { showIncomingCall, endCall, areNotificationsEnabled, registerReceiver, unregisterReceiver };
//# sourceMappingURL=index.js.map
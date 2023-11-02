import { NativeModules } from 'react-native';

const IncomingCall = NativeModules.IncomingCall;

const showIncomingCall = (options: {
  channelName: string;
  channelId: string;
  timeout: number;
  component: string;
  callerName: string;
  accessToken: string;
}): any => {
  IncomingCall.showIncomingCall(options);
};

const endCall = (): any => {
  IncomingCall.endCall();
};

export { showIncomingCall, endCall };

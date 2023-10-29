import { NativeModules, DeviceEventEmitter, Platform } from 'react-native';
import { listeners } from './listener';
import RNVoipCall, { RNVoipPushKit } from 'react-native-voip-call';

const IsIOS = Platform.OS === 'ios';

const incomingCallEventHandlers = new Map();
const IncomingCall = IsIOS ? RNVoipCall : NativeModules.IncomingCall;
const IncomingCallPushKit = IsIOS ? RNVoipPushKit : NativeModules.IncomingCallPushKit;

const addEventListener = (type: string, handler: any) => {
  const listener = listeners[type](handler);

  incomingCallEventHandlers.set(type, listener);
};

const removeEventListener = (type: string) => {
  const listener = incomingCallEventHandlers.get(type);
  if (!listener) {
    return;
  }

  listener.remove();
  incomingCallEventHandlers.delete(type);
};

const initializeCall = async (options: any) =>
  new Promise((resolve, reject) => {
    if (!options.appName) {
      reject({ error: true, message: 'appName is required' });
    }
    if (typeof options.appName !== 'string') {
      reject({ error: true, message: 'appName should be of type "string"' });
    }
    if (!IsIOS) {
      reject({ error: true, message: 'Android not required' });
    }
    resolve(IncomingCall.initialize(options));
  });

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

const displayIncomingCall = (
  uuid: string,
  handle: string,
  handleType: string,
  hasVideo: boolean,
  localizedCallerName: string
): any => {
  IncomingCall.displayIncomingCall(
    uuid,
    handle,
    handleType,
    hasVideo,
    localizedCallerName
  );
};

const endCall = (): any => {
  IncomingCall.endCall();
};

var invariant = require('fbjs/lib/invariant');
var _notifHandlers = new Map();

const getPushKitDeviceToken = (handler: any) => {
  if (IsIOS) {
    IncomingCallPushKit.registerVoipToken();
    let type = 'register';
    invariant(
      type === 'register',
      'IncomingCall only supports `register` events'
    );
    let listener: any;
    if (type === 'register') {
      listener = DeviceEventEmitter.addListener(
        'voipRemoteNotificationsRegistered',
        (registrationInfo: any) => {
          handler({ ...registrationInfo, platform: Platform.OS });
          // listener?.remove();
        }
      );
    }
    _notifHandlers.set(handler, listener);
  } else {
    handler({ platform: Platform.OS });
  }
};

const remotePushKitNotificationReceived = (handler: any) => {
  if (IsIOS) {
    let type = 'notification';
    invariant(
      type === 'notification',
      'IncomingCall only supports `register` events'
    );
    let listener: any;
    if (type === 'notification') {
      listener = DeviceEventEmitter.addListener(
        'voipRemoteNotificationReceived',
        (registrationInfo) => {
          console.log('voipRemoteNotificationReceived', registrationInfo);
          handler({ ...registrationInfo, platform: Platform.OS });
          // listener?.remove();
        }
      );
    }
    _notifHandlers.set(handler, listener);
  } else {
    handler({ platform: Platform.OS });
  }
};

const removeListener = (type: string, handler: any) => {
  invariant(
    type === 'notification' || type === 'register',
    'RNVoipPushNotification only supports `notification`, `register` and `localNotification` events'
  );
  var listener = _notifHandlers.get(handler);
  if (!listener) {
    return;
  }
  listener.remove();
  _notifHandlers.delete(handler);
};

const requestPermissions = (permissions: any) => {
  var requestedPermissions = {};
  if (permissions) {
    requestedPermissions = {
      alert: !!permissions.alert,
      badge: !!permissions.badge,
      sound: !!permissions.sound,
    };
  } else {
    requestedPermissions = {
      alert: true,
      badge: true,
      sound: true,
    };
  }

  if (IsIOS) {
    IncomingCallPushKit.requestPermissions(requestedPermissions);
  }
};

const stickyImmersive = async () => {
  if (!IsIOS) {
    return await IncomingCall.stickyImmersive();
  }
};

export {
  addEventListener,
  removeEventListener,
  showIncomingCall,
  getPushKitDeviceToken,
  remotePushKitNotificationReceived,
  displayIncomingCall,
  initializeCall,
  endCall,
  removeListener,
  requestPermissions,
  stickyImmersive,
  IncomingCall,
  IncomingCallPushKit
};

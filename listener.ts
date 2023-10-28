import { NativeModules, NativeEventEmitter, Platform } from 'react-native';

const IncomingCallModule = NativeModules.IncomingCall;
const eventEmitter = new NativeEventEmitter(IncomingCallModule);

const IncomingCallPerformAnswerCallAction =
  'IncomingCallPerformAnswerCallAction';
const IncomingCallPerformEndCallAction = 'IncomingCallPerformEndCallAction';
const IncomingCallMissedCallTap = 'IncomingCallMissedCallTap';

//Ios
const IncomingCallDidReceiveStartCallAction =
  'IncomingCallDidReceiveStartCallAction';
const IncomingCallDidActivateAudioSession =
  'IncomingCallDidActivateAudioSession';
const IncomingCallDidDeactivateAudioSession =
  'IncomingCallDidDeactivateAudioSession';
const IncomingCallDidDisplayIncomingCall = 'IncomingCallDidDisplayIncomingCall';
const IncomingCallDidPerformSetMutedCallAction =
  'IncomingCallDidPerformSetMutedCallAction';
const IncomingCallDidToggleHoldAction = 'IncomingCallDidToggleHoldAction';
const IncomingCallDidPerformDTMFAction = 'IncomingCallDidPerformDTMFAction';
const IncomingCallProviderReset = 'IncomingCallProviderReset';
const IncomingCallCheckReachability = 'IncomingCallCheckReachability';

//Android
const IncomingCallFullScreenIntent = 'IncomingCallFullScreenIntent';
const IncomingCallNotificationTap = 'IncomingCallNotificationTap';

const isIOS = Platform.OS === 'ios';

const didReceiveStartCallAction = (handler: any) => {
  if (isIOS) {
    // Tell CallKeep that we are ready to receive `IncomingCallDidReceiveStartCallAction` event and prevent delay
    IncomingCallModule._startCallActionEventListenerAdded();
  }

  return eventEmitter.addListener(
    IncomingCallDidReceiveStartCallAction,
    (data) => handler(data)
  );
};

const answerCall = (handler: any) =>
  eventEmitter.addListener(IncomingCallPerformAnswerCallAction, (data) => {
    let uuids = isIOS ? data.callUUID : data.callerId;
    handler({ callerId: uuids });
  });

const endCall = (handler: any) =>
  eventEmitter.addListener(IncomingCallPerformEndCallAction, (data) => {
    let uuids = isIOS ? data.callUUID : data.callerId;
    handler({ callerId: uuids });
  });

const didActivateAudioSession = (handler: any) =>
  eventEmitter.addListener(IncomingCallDidActivateAudioSession, handler);

const didDeactivateAudioSession = (handler: any) =>
  eventEmitter.addListener(IncomingCallDidDeactivateAudioSession, handler);

const didDisplayIncomingCall = (handler: any) =>
  eventEmitter.addListener(IncomingCallDidDisplayIncomingCall, (data) =>
    handler(data)
  );

const didPerformSetMutedCallAction = (handler: any) =>
  eventEmitter.addListener(IncomingCallDidPerformSetMutedCallAction, (data) =>
    handler(data)
  );

const didToggleHoldCallAction = (handler: any) =>
  eventEmitter.addListener(IncomingCallDidToggleHoldAction, handler);

const didPerformDTMFAction = (handler: any) =>
  eventEmitter.addListener(IncomingCallDidPerformDTMFAction, (data) =>
    handler(data)
  );

const didResetProvider = (handler: any) =>
  eventEmitter.addListener(IncomingCallProviderReset, handler);

const checkReachability = (handler: any) =>
  eventEmitter.addListener(IncomingCallCheckReachability, handler);

const onMissedCallOpen = (handler: any) =>
  eventEmitter.addListener(IncomingCallMissedCallTap, handler);

//Android Only
const onCallOpenAppEvent = (handler: any) =>
  eventEmitter.addListener(IncomingCallFullScreenIntent, handler);

const onCallNotificationOpen = (handler: any) =>
  eventEmitter.addListener(IncomingCallNotificationTap, handler);

export const listeners: any = {
  didReceiveStartCallAction,
  answerCall,
  endCall,
  didActivateAudioSession,
  didDeactivateAudioSession,
  didDisplayIncomingCall,
  didPerformSetMutedCallAction,
  didToggleHoldCallAction,
  didPerformDTMFAction,
  didResetProvider,
  checkReachability,
  onMissedCallOpen,
  onCallNotificationOpen,
  onCallOpenAppEvent,
};

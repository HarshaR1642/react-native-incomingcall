declare module 'react-native-incomingcall' {
  export function registerReceiver(): void;
  export function unregisterReceiver(): void;
  export function showIncomingCall(): void;
  export function endCall(): void;
  export function areNotificationsEnabled(): Promise<boolean>;
}

[![npm](https://img.shields.io/npm/v/@rentlydev/react-native-incomingcall.svg)](https://npmjs.com/@rentlydev/react-native-incomingcall) [![React Native](https://img.shields.io/badge/React_Native-21232a?style=flat&logo=react&logoColor=0a7ea4&logoSize=small.svg)]() [![Android](https://img.shields.io/badge/Android-green?style=flat&logo=android&logoColor=white)]()

# @rentlydev/react-native-incomingcall

A react native module to display an incoming call UI triggered by push notifications. Instead of relying on TelephonyManager, this approach uses push notifications sent from a server to notify the app of an incoming call. Upon receiving the notification, the app launches a custom incoming call screen, mimicking the native call UI. 

Fully compatible with TypeScript.

## Supported platforms

| Platform  |  Support |
|---|---|
| iOS  |  ❌ |
| Android  |  ✅ |
| Web  |  ❌ |
| Windows  |  ❌ |
| macOS  |  ❌ |

## Installation

```sh
npm install @rentlydev/react-native-incomingcall
```
or

```sh
yarn add @rentlydev/react-native-incomingcall
```

## Configuration and Permissions

No permissions needed

## Summary

### Methods

* [`showIncomingCall`](#showIncomingCall)
* [`endCall`](#endCall)
* [`areNotificationsEnabled`](#areNotificationsEnabled)
* [`registerReceiver`](#registerReceiver)
* [`unregisterReceiver`](#unregisterReceiver)

---

## Usage

### showIncomingCall
```javascript
import IncomingCall from '@rentlydev/react-native-incomingcall';

IncomingCall.showIncomingCall();
```

### endCall
```javascript
import IncomingCall from '@rentlydev/react-native-incomingcall';

IncomingCall.endCall();
```

### areNotificationsEnabled
```javascript
import IncomingCall from '@rentlydev/react-native-incomingcall';

const response = IncomingCall.areNotificationsEnabled();

Reponse: true or false
```

### registerReceiver
```javascript
import IncomingCall from '@rentlydev/react-native-incomingcall';

IncomingCall.registerReceiver();
```

### unregisterReceiver
```javascript
import IncomingCall from '@rentlydev/react-native-incomingcall';

IncomingCall.unregisterReceiver();
```

### Events
```javascript
  React.useEffect(() => {
    const listener = DeviceEventEmitter.addListener("intercom_broadcast", (data) => {
      console.log("intercom_broadcast", data);
    });

    return () => listener.remove();
  });
```

## How To Run Example App ?

To run example app, follow the below steps

1. Clone the repository
2. Do `yarn install`
3. Next navigate to example folder i.e `cd example`
4. Do `yarn install`
5. Next navigate to ios folder i.e `cd ios` and do `pod install`, then `cd ..`
6. For android run `yarn android`
7. For ios run `yarn ios`

## Demo Video

[Android](https://drive.google.com/file/d/1X-4Mpmq86gD46IAy_QH6YbJMC_DzG1cb/preview) 


"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.unregisterReceiver = exports.showIncomingCall = exports.registerReceiver = exports.endCall = exports.areNotificationsEnabled = void 0;
var _reactNative = require("react-native");
var _NativeIncomingCall = _interopRequireDefault(require("./NativeIncomingCall.js"));
function _interopRequireDefault(e) { return e && e.__esModule ? e : { default: e }; }
const registerReceiver = () => {
  if (_reactNative.Platform.OS === 'android') {
    _NativeIncomingCall.default.registerReceiver();
  }
};
exports.registerReceiver = registerReceiver;
const unregisterReceiver = () => {
  if (_reactNative.Platform.OS === 'android') {
    _NativeIncomingCall.default.unregisterReceiver();
  }
};
exports.unregisterReceiver = unregisterReceiver;
const showIncomingCall = () => {
  if (_reactNative.Platform.OS === 'android') {
    _NativeIncomingCall.default.showIncomingCall();
  }
};
exports.showIncomingCall = showIncomingCall;
const endCall = () => {
  if (_reactNative.Platform.OS === 'android') {
    _NativeIncomingCall.default.endCall();
  }
};
exports.endCall = endCall;
const areNotificationsEnabled = async () => {
  const granted = await _NativeIncomingCall.default.areNotificationsEnabled();
  return granted;
};
exports.areNotificationsEnabled = areNotificationsEnabled;
//# sourceMappingURL=index.js.map
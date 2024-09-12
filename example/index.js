import { AppRegistry } from 'react-native';
import { App } from './src/App';
import { CallAccepted } from './src/CallAccepted';
import { name as appName, intercom } from './app.json';

AppRegistry.registerComponent(appName, () => App);
AppRegistry.registerComponent(intercom, () => CallAccepted);

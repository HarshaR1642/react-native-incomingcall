import { AppRegistry } from 'react-native';
import { App } from './src/App';
import { CallAccepted } from './src/CallAccepted';
import { name as appName } from './app.json';

AppRegistry.registerComponent(appName, () => App);
AppRegistry.registerComponent('Intercom', () => CallAccepted);

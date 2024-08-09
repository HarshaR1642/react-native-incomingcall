import { StyleSheet, View, TouchableOpacity, Text } from 'react-native';
import { endCall } from 'react-native-incomingcall';

const Button = ({
  title,
  style = {},
  textStyle = {},
  onPress,
}: {
  title: string;
  style: any;
  textStyle: any;
  onPress: () => void;
}) => {
  return (
    <TouchableOpacity style={style} onPress={onPress}>
      <Text style={textStyle}>{title}</Text>
    </TouchableOpacity>
  );
};

export const CallAccepted = () => {
  return (
    <View style={styles.container}>
      <Text style={styles.acceptedTextStyle}>Call Accepted!!</Text>
      <Button
        textStyle={styles.textStyle}
        style={styles.button}
        title="End Call"
        onPress={() => {
          endCall();
        }}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  button: {
    backgroundColor: 'red',
    width: 250,
    height: 50,
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
    marginVertical: 10,
  },
  acceptedTextStyle: {
    color: 'green',
    fontSize: 24,
    fontWeight: '500',
  },
  textStyle: {
    color: 'white',
    fontSize: 18,
    fontWeight: '500',
  },
});

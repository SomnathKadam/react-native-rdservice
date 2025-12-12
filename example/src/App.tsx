import { Text, View, StyleSheet, Button, Alert } from 'react-native';
import { getFingerPrint } from 'react-native-rdservice';

export default function App() {
  const handleCapture = async () => {
    try {
      const result = await getFingerPrint('com.mantra.rdservice', '');
      Alert.alert(result.status, result.message);
    } catch {
      Alert.alert('Error', 'Failed to capture');
    }
  };

  return (
    <View style={styles.container}>
      <Text>RD Service Biometric Capture</Text>
      <Button title="Capture Fingerprint" onPress={handleCapture} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});

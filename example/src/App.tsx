import { useState } from 'react';
import {
  Text,
  View,
  StyleSheet,
  Button,
  ScrollView,
  Platform,
} from 'react-native';
import {
  getDeviceInfo,
  getFingerPrint,
  getIrisCapture,
  getFaceCapture,
  type RdServiceResponse,
} from 'react-native-rdservice';

const FINGERPRINT_RD_PACKAGE = 'com.mantra.rdservice';
const IRIS_RD_PACKAGE = 'com.mantra.mis100v2.rdservice';
const FACE_RD_PACKAGE = 'in.gov.uidai.facerd';

export default function App() {
  const [result, setResult] = useState<RdServiceResponse | null>(null);
  const [busy, setBusy] = useState(false);

  const run = async (action: () => Promise<RdServiceResponse>) => {
    setBusy(true);
    setResult(null);
    try {
      setResult(await action());
    } catch (e) {
      setResult({ status: 'FAILURE', message: String(e) });
    } finally {
      setBusy(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>RD Service Biometric Capture</Text>
      {Platform.OS !== 'android' && (
        <Text style={styles.notice}>
          RD services are Android-only; captures will report FAILURE here.
        </Text>
      )}
      <View style={styles.buttons}>
        <Button
          title="Device Info"
          disabled={busy}
          onPress={() => run(() => getDeviceInfo(FINGERPRINT_RD_PACKAGE))}
        />
        <Button
          title="Capture Fingerprint"
          disabled={busy}
          onPress={() => run(() => getFingerPrint(FINGERPRINT_RD_PACKAGE))}
        />
        <Button
          title="Capture Iris"
          disabled={busy}
          onPress={() => run(() => getIrisCapture(IRIS_RD_PACKAGE))}
        />
        <Button
          title="Capture Face"
          disabled={busy}
          onPress={() => run(() => getFaceCapture(FACE_RD_PACKAGE))}
        />
      </View>
      {result && (
        <ScrollView style={styles.result}>
          <Text style={styles.status}>{result.status}</Text>
          <Text selectable>{result.message}</Text>
        </ScrollView>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 16,
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 12,
  },
  notice: {
    color: '#a00',
    marginBottom: 12,
    textAlign: 'center',
  },
  buttons: {
    gap: 8,
    alignSelf: 'stretch',
  },
  result: {
    marginTop: 16,
    maxHeight: 240,
    alignSelf: 'stretch',
  },
  status: {
    fontWeight: 'bold',
    marginBottom: 4,
  },
});

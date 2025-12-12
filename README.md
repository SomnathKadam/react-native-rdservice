# react-native-rdservice

A React Native library for capturing biometric data (fingerprint and face) using RD (Registered Device) services on Android. This library integrates with UIDAI-compliant biometric devices for Aadhaar-based authentication and eKYC services.

## Installation

```sh
npm install react-native-rdservice
```

or

```sh
yarn add react-native-rdservice
```

## Features

- Fingerprint biometric capture using RD services
- Face biometric capture using RD services
- Support for custom PID options
- TypeScript support with full type definitions
- Promise-based API for easy async/await usage

## API Reference

### Types

#### `RdServiceResponse`

Response object returned by both fingerprint and face capture methods.

```typescript
interface RdServiceResponse {
  status: string;    // "SUCCESS" or "FAILURE"
  message: string;   // XML data on success, error message on failure
}
```

### Methods

---

#### `getFingerPrint(deviceName, pidOption)`

Captures fingerprint biometric data using the specified RD service device.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `deviceName` | `string` | Yes | Package name of the RD service app (e.g., `"com.mantra.rdservice"`, `"com.precision.pb510.rdservice"`) |
| `pidOption` | `string` | No | XML string containing PID options. If empty or less than 10 characters, uses default configuration |

**Default PID Options:**
```xml
<?xml version="1.0"?>
<PidOptions ver="1.0">
  <Opts fCount="1" fType="2" iCount="0" pCount="0" format="0"
        pidVer="2.0" timeout="10000" posh="UNKNOWN" env="P" />
  <CustOpts></CustOpts>
</PidOptions>
```

**Returns:** `Promise<RdServiceResponse>`

**Success Response:**
```typescript
{
  status: "SUCCESS",
  message: "<?xml version='1.0'?><PidData>...</PidData>" // Biometric XML data
}
```

**Error Response:**
```typescript
{
  status: "FAILURE",
  message: "Device not ready" | "Selected device not found" | "RD services not available"
}
```

**Example:**

```typescript
import { getFingerPrint } from 'react-native-rdservice';

// Using default PID options
const captureFingerprint = async () => {
  try {
    const result = await getFingerPrint('com.mantra.rdservice', '');

    if (result.status === 'SUCCESS') {
      console.log('Fingerprint captured:', result.message);
      // Process the biometric XML data
      // result.message contains the PID XML with biometric information
    } else {
      console.error('Capture failed:', result.message);
    }
  } catch (error) {
    console.error('Error:', error);
  }
};

// Using custom PID options
const captureFingerprintCustom = async () => {
  const customPidOptions = `<?xml version="1.0"?>
    <PidOptions ver="1.0">
      <Opts fCount="2" fType="2" iCount="0" pCount="0" format="0"
            pidVer="2.0" timeout="15000" posh="UNKNOWN" env="P" />
      <CustOpts>
        <Param name="txnId" value="12345"/>
      </CustOpts>
    </PidOptions>`;

  const result = await getFingerPrint('com.precision.pb510.rdservice', customPidOptions);

  if (result.status === 'SUCCESS') {
    // Handle success
    console.log('Biometric data:', result.message);
  }
};
```

---

#### `getFaceCapture(deviceName, pidOption)`

Captures face biometric data using the specified RD service device.

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `deviceName` | `string` | Yes | Package name of the face RD service app (e.g., `"in.gov.uidai.facerd"`) |
| `pidOption` | `string` | No | XML string containing PID options for face capture. If empty or less than 10 characters, uses default configuration |

**Default PID Options:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<PidOptions ver="1.0" env="P">
  <Opts fCount="" fType="" iCount="" iType="" pCount="" pType=""
        format="" pidVer="2.0" timeout="" otp=""
        wadh="sgydIC09zzy6f8Lb3xaAqzKquKe9lFcNR9uTvYxFp+A=" posh="" />
  <CustOpts>
    <Param name="txnId" value="76435891"/>
    <Param name="purpose" value="auth"/>
    <Param name="language" value="en"/>
  </CustOpts>
</PidOptions>
```

**Returns:** `Promise<RdServiceResponse>`

**Success Response:**
```typescript
{
  status: "SUCCESS",
  message: "<?xml version='1.0'?><PidData>...</PidData>" // Face biometric XML data
}
```

**Error Response:**
```typescript
{
  status: "FAILURE",
  message: "Device not ready" | "Selected device not found" | "Face RD services not available"
}
```

**Example:**

```typescript
import { getFaceCapture } from 'react-native-rdservice';

// Using default PID options
const captureFace = async () => {
  try {
    const result = await getFaceCapture('in.gov.uidai.facerd', '');

    if (result.status === 'SUCCESS') {
      console.log('Face captured:', result.message);
      // Process the face biometric XML data
      // result.message contains the PID XML with face biometric information
    } else {
      console.error('Capture failed:', result.message);
    }
  } catch (error) {
    console.error('Error:', error);
  }
};

// Using custom PID options
const captureFaceCustom = async () => {
  const customPidOptions = `<?xml version="1.0" encoding="UTF-8"?>
    <PidOptions ver="1.0" env="P">
      <Opts fCount="" fType="" iCount="" iType="" pCount="" pType=""
            format="" pidVer="2.0" timeout="20000" otp=""
            wadh="your_wadh_value_here" posh="" />
      <CustOpts>
        <Param name="txnId" value="customTxnId123"/>
        <Param name="purpose" value="ekyc"/>
        <Param name="language" value="en"/>
      </CustOpts>
    </PidOptions>`;

  const result = await getFaceCapture('in.gov.uidai.facerd', customPidOptions);

  if (result.status === 'SUCCESS') {
    // Handle success
    console.log('Face biometric data:', result.message);
  }
};
```

---

## Complete Usage Example

```typescript
import React, { useState } from 'react';
import { View, Button, Text, Alert } from 'react-native';
import { getFingerPrint, getFaceCapture, type RdServiceResponse } from 'react-native-rdservice';

const BiometricCapture = () => {
  const [fpData, setFpData] = useState<string>('');
  const [faceData, setFaceData] = useState<string>('');

  const handleFingerprintCapture = async () => {
    try {
      const result: RdServiceResponse = await getFingerPrint(
        'com.mantra.rdservice',
        ''
      );

      if (result.status === 'SUCCESS') {
        setFpData(result.message);
        Alert.alert('Success', 'Fingerprint captured successfully');
      } else {
        Alert.alert('Error', result.message);
      }
    } catch (error) {
      Alert.alert('Error', 'Failed to capture fingerprint');
      console.error(error);
    }
  };

  const handleFaceCapture = async () => {
    try {
      const result: RdServiceResponse = await getFaceCapture(
        'in.gov.uidai.facerd',
        ''
      );

      if (result.status === 'SUCCESS') {
        setFaceData(result.message);
        Alert.alert('Success', 'Face captured successfully');
      } else {
        Alert.alert('Error', result.message);
      }
    } catch (error) {
      Alert.alert('Error', 'Failed to capture face');
      console.error(error);
    }
  };

  return (
    <View>
      <Button title="Capture Fingerprint" onPress={handleFingerprintCapture} />
      <Button title="Capture Face" onPress={handleFaceCapture} />

      {fpData ? <Text>Fingerprint Data: {fpData.substring(0, 100)}...</Text> : null}
      {faceData ? <Text>Face Data: {faceData.substring(0, 100)}...</Text> : null}
    </View>
  );
};

export default BiometricCapture;
```

## Common RD Service Package Names

**Fingerprint Devices:**
- Mantra: `com.mantra.rdservice`
- Morpho: `com.scl.rdservice`
- Precision: `com.precision.pb510.rdservice`
- Startek: `com.acpl.registersdk`
- Secugen: `com.secugen.rdservice`

**Face Devices:**
- UIDAI Face RD: `in.gov.uidai.facerd`

## Requirements

- React Native 0.60 or higher
- Android SDK 24 or higher
- RD service app installed on the device

## Platform Support

- Android: ✅ Supported
- iOS: ❌ Not supported (RD services are Android-only)

## Error Handling

The library returns descriptive error messages in the `message` field when `status` is `"FAILURE"`:

- **"Device not ready"**: The biometric device is not connected or not ready
- **"Selected device not found"**: The specified RD service package is not installed
- **"RD services not available"**: The RD service app is not available on the device
- **"No action taken"**: User cancelled the operation

## License

MIT

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository.

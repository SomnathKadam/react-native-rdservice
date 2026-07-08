# react-native-rdservice

A React Native library for capturing **fingerprint**, **iris**, and **face** biometric data using UIDAI-compliant RD (Registered Device) services on Android. Built for Aadhaar-based authentication and eKYC workflows.

[![npm version](https://img.shields.io/npm/v/react-native-rdservice.svg)](https://www.npmjs.com/package/react-native-rdservice)
[![license](https://img.shields.io/npm/l/react-native-rdservice.svg)](LICENSE)

## Features

- 🫆 Fingerprint capture via RD services (Mantra, Morpho, Startek, Precision, SecuGen, …)
- 👁️ Iris capture via iris RD services
- 🙂 Face capture via UIDAI Face RD
- ℹ️ Device readiness check (`getDeviceInfo`)
- Custom `PidOptions` XML support with sensible defaults
- Promise-based API with full TypeScript definitions
- New Architecture (TurboModule) — works with React Native 0.80+
- Android 11+ package visibility (`<queries>`) handled automatically — no manifest changes needed in your app

## Platform support

| Platform | Supported | Notes |
|----------|-----------|-------|
| Android  | ✅ | Requires an RD service app installed on the device |
| iOS      | ⚠️ Graceful stub | UIDAI certifies RD services for **Android and Windows only**. There are no iOS RD service apps, so real capture is impossible on iOS. The module still loads on iOS and every method resolves with `{ status: 'FAILURE', message: '…not available on iOS…' }`, so cross-platform apps can share one code path without crashing. |

## Requirements

- React Native **0.80 or higher** with the New Architecture enabled (default since RN 0.76)
- Android SDK 24+
- The relevant RD service app installed on the device (see [package names](#common-rd-service-package-names))

## Installation

```sh
npm install react-native-rdservice
# or
yarn add react-native-rdservice
```

No extra Android setup is required. The library ships the required `<queries>` entries for Android 11+ package visibility; they are merged into your app's manifest automatically.

## Quick start

```typescript
import { getFingerPrint } from 'react-native-rdservice';

const result = await getFingerPrint('com.mantra.rdservice');

if (result.status === 'SUCCESS') {
  // result.message is the signed PidData XML — send it to your backend as-is
} else {
  console.warn(result.message); // e.g. "Device not ready"
}
```

## API Reference

All methods return `Promise<RdServiceResponse>`:

```typescript
interface RdServiceResponse {
  status: string;  // 'SUCCESS' | 'FAILURE'
  message: string; // signed XML on success, human-readable error on failure
}
```

`deviceName` is always the **package name of the RD service app** installed on the device. `pidOption` is an optional `PidOptions` XML string — when omitted (or blank), a sensible default is used.

---

### `getDeviceInfo(deviceName)`

Checks whether the RD service and its device are ready (RD `INFO` call).

```typescript
import { getDeviceInfo } from 'react-native-rdservice';

const info = await getDeviceInfo('com.mantra.rdservice');
if (info.status === 'SUCCESS') {
  // info.message contains the RDService info XML (device status, dpId, rdsId, …)
}
```

---

### `getFingerPrint(deviceName, pidOption?)`

Captures fingerprint biometric data.

```typescript
import { getFingerPrint } from 'react-native-rdservice';

// Default options: single finger (fCount="1"), FMR format, 10s timeout, production env
const result = await getFingerPrint('com.mantra.rdservice');

// Custom PidOptions
const custom = await getFingerPrint(
  'com.precision.pb510.rdservice',
  `<?xml version="1.0"?>
   <PidOptions ver="1.0">
     <Opts fCount="2" fType="2" iCount="0" pCount="0" format="0"
           pidVer="2.0" timeout="15000" posh="UNKNOWN" env="P" />
     <CustOpts><Param name="txnId" value="12345"/></CustOpts>
   </PidOptions>`
);
```

Default `PidOptions`:

```xml
<?xml version="1.0"?>
<PidOptions ver="1.0">
  <Opts fCount="1" fType="2" iCount="0" pCount="0" format="0"
        pidVer="2.0" timeout="10000" posh="UNKNOWN" env="P" />
  <CustOpts></CustOpts>
</PidOptions>
```

---

### `getIrisCapture(deviceName, pidOption?)`

Captures iris biometric data.

```typescript
import { getIrisCapture } from 'react-native-rdservice';

// Default options: single iris (iCount="1"), 10s timeout, production env
const result = await getIrisCapture('com.mantra.mis100v2.rdservice');

if (result.status === 'SUCCESS') {
  // result.message contains the signed PidData XML with iris data
}
```

Default `PidOptions`:

```xml
<?xml version="1.0"?>
<PidOptions ver="1.0">
  <Opts fCount="0" fType="0" iCount="1" iType="0" pCount="0" pType="0"
        format="0" pidVer="2.0" timeout="10000" posh="UNKNOWN" env="P" />
  <CustOpts></CustOpts>
</PidOptions>
```

---

### `getFaceCapture(deviceName, pidOption?)`

Captures face biometric data via the UIDAI Face RD app.

```typescript
import { getFaceCapture } from 'react-native-rdservice';

const result = await getFaceCapture('in.gov.uidai.facerd');
```

The default `PidOptions` targets **face authentication** and uses the UIDAI-published `wadh` constant for that flow. For eKYC or other purposes, pass your own `PidOptions` with the appropriate `wadh` and `purpose`:

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

---

## Complete usage example

```typescript
import React, { useState } from 'react';
import { View, Button, Alert } from 'react-native';
import {
  getDeviceInfo,
  getFingerPrint,
  getIrisCapture,
  getFaceCapture,
  type RdServiceResponse,
} from 'react-native-rdservice';

export default function BiometricCapture() {
  const [busy, setBusy] = useState(false);

  const capture = async (fn: () => Promise<RdServiceResponse>) => {
    setBusy(true);
    try {
      const result = await fn();
      if (result.status === 'SUCCESS') {
        // Forward result.message (signed PID XML) to your backend over TLS.
        Alert.alert('Success', 'Biometric captured');
      } else {
        Alert.alert('Failed', result.message);
      }
    } finally {
      setBusy(false);
    }
  };

  return (
    <View>
      <Button
        title="Check Device"
        disabled={busy}
        onPress={() => capture(() => getDeviceInfo('com.mantra.rdservice'))}
      />
      <Button
        title="Fingerprint"
        disabled={busy}
        onPress={() => capture(() => getFingerPrint('com.mantra.rdservice'))}
      />
      <Button
        title="Iris"
        disabled={busy}
        onPress={() => capture(() => getIrisCapture('com.mantra.mis100v2.rdservice'))}
      />
      <Button
        title="Face"
        disabled={busy}
        onPress={() => capture(() => getFaceCapture('in.gov.uidai.facerd'))}
      />
    </View>
  );
}
```

## Common RD service package names

**Fingerprint:**

| Vendor | Package |
|--------|---------|
| Mantra MFS100 | `com.mantra.rdservice` |
| Morpho | `com.scl.rdservice` |
| Precision PB510 | `com.precision.pb510.rdservice` |
| Startek | `com.acpl.registersdk` |
| SecuGen | `com.secugen.rdservice` |
| Tatvik | `com.tatvik.bio.tmf20` |

**Iris:**

| Vendor | Package |
|--------|---------|
| Mantra MIS100V2 | `com.mantra.mis100v2.rdservice` |
| IriTech IriShield | `com.iritech.rdservice` |

**Face:**

| Vendor | Package |
|--------|---------|
| UIDAI Face RD | `in.gov.uidai.facerd` |

> Package names can change between vendor releases — verify against the RD service app actually installed on your target devices.

## Error handling

Failures resolve (never reject) with `status: 'FAILURE'` and one of:

| Message | Meaning |
|---------|---------|
| `Device not ready` | The biometric device is not connected, not initialised, or returned an invalid response |
| `Selected device not found` | No app with the given package name handles the RD capture intent |
| `No action taken` | The user cancelled the capture |
| `Invalid RD service package name` | `deviceName` is not a valid Android package name |
| `Another RD service request is already in progress` | A previous capture has not finished yet |
| `No foreground activity available to start RD service` | The app is not in the foreground |
| `UIDAI RD services are not available on iOS…` | Called on iOS, where RD services do not exist |

## Security notes

Biometric data is highly sensitive. When integrating this library:

- **Never modify the PID XML.** The `message` returned on success is digitally signed by the RD service; altering a single character breaks UIDAI signature verification. This library returns it byte-for-byte as produced.
- **Never log or persist raw PID data** on the device. Forward it to your backend over TLS and discard it.
- **The PID block is already encrypted** by the RD service (per UIDAI spec) — your app cannot and should not decrypt it; only the authorised backend/AUA can process it.
- **Validate the RD service app.** The library only launches explicit intents against a syntactically valid package name, so the capture request cannot be hijacked by an arbitrary app — but you should still pin the exact vendor package(s) you support.
- **Use `env="PP"` (pre-production) PidOptions during development** and `env="P"` only in production.
- Comply with the UIDAI Aadhaar Act and data-protection regulations applicable to your deployment.

## Troubleshooting

- **`Selected device not found` on Android 11+**: fixed by this library's built-in `<queries>` declarations — rebuild the app after installing/updating the library.
- **Capture never returns**: ensure the RD service app is installed, registered, and the device is plugged in; call `getDeviceInfo` first to check readiness.
- **Works in debug but not release**: confirm your ProGuard/R8 rules don't strip the RD service vendor SDK (this library itself needs no extra rules).

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

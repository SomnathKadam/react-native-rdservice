package com.rdservice

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = RdserviceModule.NAME)
class RdserviceModule(reactContext: ReactApplicationContext) :
  NativeRdserviceSpec(reactContext) {

  companion object {
    const val NAME = "Rdservice"

    private const val RDINFO_CODE = 1
    private const val RDCAPTURE_CODE = 2
    private const val IRIS_CAPTURE_CODE = 3
    private const val FACE_CAPTURE_CODE = 9

    private const val ACTION_FP_INFO = "in.gov.uidai.rdservice.fp.INFO"
    private const val ACTION_FP_CAPTURE = "in.gov.uidai.rdservice.fp.CAPTURE"
    private const val ACTION_IRIS_CAPTURE = "in.gov.uidai.rdservice.iris.CAPTURE"
    private const val ACTION_FACE_CAPTURE = "in.gov.uidai.rdservice.face.CAPTURE"

    private const val SUCCESS = "SUCCESS"
    private const val FAILURE = "FAILURE"

    // RD service responses shorter than this cannot contain valid XML.
    private const val MIN_VALID_RESPONSE_LENGTH = 10

    // Valid Android application id, e.g. com.mantra.rdservice. Rejecting anything
    // else prevents the capture intent from being sent as an implicit broadcast
    // that an arbitrary app could answer.
    private val PACKAGE_NAME_PATTERN =
      Regex("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)+$")

    private const val DEFAULT_FINGER_PID_OPTIONS =
      """<?xml version="1.0"?><PidOptions ver="1.0"><Opts fCount="1" fType="2" iCount="0" pCount="0" format="0" pidVer="2.0" timeout="10000" posh="UNKNOWN" env="P" /><CustOpts></CustOpts></PidOptions>"""

    private const val DEFAULT_IRIS_PID_OPTIONS =
      """<?xml version="1.0"?><PidOptions ver="1.0"><Opts fCount="0" fType="0" iCount="1" iType="0" pCount="0" pType="0" format="0" pidVer="2.0" timeout="10000" posh="UNKNOWN" env="P" /><CustOpts></CustOpts></PidOptions>"""

    // The wadh value below is the UIDAI-published constant for face authentication.
    // Pass custom PidOptions for eKYC or other flows.
    private const val DEFAULT_FACE_PID_OPTIONS =
      """<?xml version="1.0" encoding="UTF-8"?> <PidOptions ver="1.0" env="P"><Opts fCount="" fType="" iCount="" iType="" pCount="" pType="" format="" pidVer="2.0" timeout="" otp="" wadh="sgydIC09zzy6f8Lb3xaAqzKquKe9lFcNR9uTvYxFp+A=" posh="" /> <CustOpts><Param name="txnId" value="76435891"/><Param name="purpose" value="auth"/><Param name="language" value="en"/></CustOpts></PidOptions>"""
  }

  private var promise: Promise? = null

  private val activityEventListener: ActivityEventListener = object : BaseActivityEventListener() {
    override fun onActivityResult(
      activity: Activity,
      requestCode: Int,
      resultCode: Int,
      data: Intent?,
    ) {
      when (requestCode) {
        RDINFO_CODE,
        RDCAPTURE_CODE,
        IRIS_CAPTURE_CODE,
        FACE_CAPTURE_CODE,
        -> Unit
        // Results from unrelated activities must not touch a pending capture.
        else -> return
      }

      if (data == null) {
        resolve(FAILURE, "No action taken")
        return
      }

      when (requestCode) {
        RDINFO_CODE -> handleInfoResult(data.getStringExtra("RD_SERVICE_INFO"))
        RDCAPTURE_CODE, IRIS_CAPTURE_CODE -> handleCaptureResult(data.getStringExtra("PID_DATA"))
        FACE_CAPTURE_CODE -> handleCaptureResult(data.getStringExtra("response"))
      }
    }
  }

  init {
    reactContext.addActivityEventListener(activityEventListener)
  }

  override fun getName(): String = NAME

  override fun invalidate() {
    reactApplicationContext.removeActivityEventListener(activityEventListener)
    promise = null
    super.invalidate()
  }

  override fun getDeviceInfo(deviceName: String, promise: Promise) {
    startRdActivity(promise, deviceName, RDINFO_CODE) {
      Intent(ACTION_FP_INFO)
    }
  }

  override fun getFingerPrint(deviceName: String, pidOption: String, promise: Promise) {
    startRdActivity(promise, deviceName, RDCAPTURE_CODE) {
      Intent(ACTION_FP_CAPTURE)
        .putExtra("PID_OPTIONS", pidOptionOrDefault(pidOption, DEFAULT_FINGER_PID_OPTIONS))
    }
  }

  override fun getIrisCapture(deviceName: String, pidOption: String, promise: Promise) {
    startRdActivity(promise, deviceName, IRIS_CAPTURE_CODE) {
      Intent(ACTION_IRIS_CAPTURE)
        .putExtra("PID_OPTIONS", pidOptionOrDefault(pidOption, DEFAULT_IRIS_PID_OPTIONS))
    }
  }

  override fun getFaceCapture(deviceName: String, pidOption: String, promise: Promise) {
    startRdActivity(promise, deviceName, FACE_CAPTURE_CODE) {
      Intent(ACTION_FACE_CAPTURE)
        .putExtra("request", pidOptionOrDefault(pidOption, DEFAULT_FACE_PID_OPTIONS))
    }
  }

  private fun pidOptionOrDefault(pidOption: String, default: String): String =
    if (pidOption.trim().length >= MIN_VALID_RESPONSE_LENGTH) pidOption else default

  private fun startRdActivity(
    promise: Promise,
    packageName: String,
    requestCode: Int,
    intentBuilder: () -> Intent,
  ) {
    if (this.promise != null) {
      resolvePromise(promise, FAILURE, "Another RD service request is already in progress")
      return
    }
    if (!PACKAGE_NAME_PATTERN.matches(packageName)) {
      resolvePromise(promise, FAILURE, "Invalid RD service package name")
      return
    }
    val activity = reactApplicationContext.getCurrentActivity()
    if (activity == null) {
      resolvePromise(promise, FAILURE, "No foreground activity available to start RD service")
      return
    }

    this.promise = promise
    try {
      activity.startActivityForResult(intentBuilder().setPackage(packageName), requestCode)
    } catch (e: Exception) {
      // Log only the exception type; never log intent contents or biometric data.
      Log.w(NAME, "Unable to start RD service activity: ${e.javaClass.simpleName}")
      resolve(FAILURE, "Selected device not found")
    }
  }

  private fun handleInfoResult(info: String?) {
    if (info == null ||
      info.length <= MIN_VALID_RESPONSE_LENGTH ||
      info.lowercase().contains("notready")
    ) {
      resolve(FAILURE, "Device not ready")
      return
    }
    resolve(SUCCESS, info)
  }

  private fun handleCaptureResult(captureXml: String?) {
    if (captureXml == null || captureXml.length <= MIN_VALID_RESPONSE_LENGTH) {
      resolve(FAILURE, "Device not ready")
      return
    }
    if (captureXml.lowercase().contains("device not ready")) {
      resolve(FAILURE, "Device not ready")
      return
    }
    // Return the PID XML exactly as the RD service produced it. It is digitally
    // signed; altering even a single character breaks server-side verification.
    resolve(SUCCESS, captureXml)
  }

  private fun resolve(status: String, message: String) {
    val pending = promise ?: return
    promise = null
    resolvePromise(pending, status, message)
  }

  private fun resolvePromise(promise: Promise, status: String, message: String) {
    val map: WritableMap = Arguments.createMap()
    map.putString("status", status)
    map.putString("message", message)
    promise.resolve(map)
  }
}

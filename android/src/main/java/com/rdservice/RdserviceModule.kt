package com.rdservice

import android.app.Activity
import android.content.Intent
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
    const val RDINFO_CODE = 1
    const val RDCAPTURE_CODE = 2
    const val FACE_CAPTURE_CODE = 9
    const val IRIS_CAPTURE_CODE = 3
  }

  private val SUCCESS = "SUCCESS"
  private val FAILURE = "FAILURE"
  private var pckName = ""
  private var pidOption = ""
  private var promise: Promise? = null

  private val activityEventListener: ActivityEventListener = object : BaseActivityEventListener() {
    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
      if (data == null) {
        resolve(FAILURE, "No action taken")
        return
      }

      when (requestCode) {
        RDINFO_CODE -> {
          val requiredValue = data.getStringExtra("RD_SERVICE_INFO")

          if (requiredValue == null) {
            resolve(FAILURE, "Device not ready")
            return
          }
          if (requiredValue.length <= 10) {
            resolve(FAILURE, "Device not ready")
            return
          }
          if (requiredValue.lowercase().contains("notready")) {
            resolve(FAILURE, "Device not ready")
            return
          }

          captureData()
          return
        }

        RDCAPTURE_CODE -> {
          val captureXML = data.getStringExtra("PID_DATA")

          if (captureXML == null || captureXML.length <= 10) {
            resolve(FAILURE, "Device not ready")
            return
          }
          if (captureXML.lowercase().contains("device not ready")) {
            resolve(FAILURE, "Device not ready")
            return
          }
          resolve(SUCCESS, captureXML)
        }

        FACE_CAPTURE_CODE -> {
          val captureXML = data.getStringExtra("response")

          if (captureXML == null || captureXML.length <= 10) {
            resolve(FAILURE, "Device not ready")
            return
          }
          if (captureXML.lowercase().contains("device not ready")) {
            resolve(FAILURE, "Device not ready")
            return
          }
          resolve(SUCCESS, captureXML)
        }
        IRIS_CAPTURE_CODE -> {
          val captureXML = data.getStringExtra("PID_DATA")

          if (captureXML == null || captureXML.length <= 10) {
            resolve(FAILURE, "Device not ready")
            return
          }
          if (captureXML.lowercase().contains("device not ready")) {
            resolve(FAILURE, "Device not ready")
            return
          }
          resolve(SUCCESS, captureXML)
        }
      }
    }
  }

  init {
    reactContext.addActivityEventListener(activityEventListener)
  }

  override fun getName(): String {
    return NAME
  }

  private fun captureData() {
    var finalPidOption = """<?xml version="1.0"?><PidOptions ver="1.0"><Opts fCount="1" fType="2" iCount="0" pCount="0" format="0" pidVer="2.0" timeout="10000" posh="UNKNOWN" env="P" /><CustOpts></CustOpts></PidOptions>"""

    if (pidOption.length >= 10) {
      finalPidOption = pidOption
    }

    val intent = Intent().apply {
      action = "in.gov.uidai.rdservice.fp.CAPTURE"
      putExtra("PID_OPTIONS", finalPidOption)
      setPackage(pckName)
    }

    val currentActivity = currentActivity
    try {
      currentActivity?.startActivityForResult(intent, RDCAPTURE_CODE)
    } catch (e: Exception) {
      e.printStackTrace()
      resolve(FAILURE, "Selected device not found")
    }
  }

  private fun captureFaceData() {
    var finalPidOption = """<?xml version="1.0" encoding="UTF-8"?> <PidOptions ver="1.0" env="P"><Opts fCount="" fType="" iCount="" iType="" pCount="" pType="" format="" pidVer="2.0" timeout="" otp="" wadh="sgydIC09zzy6f8Lb3xaAqzKquKe9lFcNR9uTvYxFp+A=" posh="" /> <CustOpts><Param name="txnId" value="76435891"/><Param name="purpose" value="auth"/><Param name="language" value="en"/></CustOpts></PidOptions>"""

    if (pidOption.length >= 10) {
      finalPidOption = pidOption
    }

    val intent = Intent().apply {
      action = "in.gov.uidai.rdservice.face.CAPTURE"
      putExtra("request", finalPidOption)
      setPackage(pckName)
    }

    val currentActivity = currentActivity
    try {
      currentActivity?.startActivityForResult(intent, FACE_CAPTURE_CODE)
    } catch (e: Exception) {
      e.printStackTrace()
      resolve(FAILURE, "Selected device not found")
    }
  }

  private fun captureIrisData() {
    var finalPidOption = """<?xml version="1.0"?><PidOptions ver="1.0"><Opts fCount="0" fType="0" iCount="1" iType="0" pCount="0" pType="0" format="0" pidVer="2.0" timeout="10000" posh="UNKNOWN" env="P" /><CustOpts></CustOpts></PidOptions>"""

    if (pidOption.length >= 10) {
      finalPidOption = pidOption
    }

    val intent = Intent().apply {
      action = "in.gov.uidai.rdservice.iris.CAPTURE"
      putExtra("PID_OPTIONS", finalPidOption)
      setPackage(pckName)
    }

    val currentActivity = currentActivity
    try {
      currentActivity?.startActivityForResult(intent, IRIS_CAPTURE_CODE)
    } catch (e: Exception) {
      e.printStackTrace()
      resolve(FAILURE, "Selected device not found")
    }
  }

  override fun getFingerPrint(deviceName: String, pidOption: String, promise: Promise) {
    try {
      this.promise = promise
      this.pckName = deviceName
      this.pidOption = pidOption
      captureData()
    } catch (e: Exception) {
      e.printStackTrace()
      resolve(FAILURE, "RD services not available")
    }
  }

  override fun getFaceCapture(deviceName: String, pidOption: String, promise: Promise) {
    try {
      this.promise = promise
      this.pckName = deviceName
      this.pidOption = pidOption
      captureFaceData()
    } catch (e: Exception) {
      e.printStackTrace()
      resolve(FAILURE, "Face RD services not available")
    }
  }

  override fun getIrisCapture(deviceName: String, pidOption: String, promise: Promise) {
    try {
      this.promise = promise
      this.pckName = deviceName
      this.pidOption = pidOption
      captureIrisData()
    } catch (e: Exception) {
      e.printStackTrace()
      resolve(FAILURE, "Iris RD services not available")
    }
  }
  private fun parseBioMetricData(bioxml: String): String {
    return bioxml
      .replace("\"", "'")
      .replace("\n   ", " ")
      .replace("\n ", " ")
  }

  private fun resolve(status: String, message: String) {
    if (promise == null) {
      return
    }

    val map: WritableMap = Arguments.createMap()
    map.putString("status", status.uppercase())
    map.putString("message", parseBioMetricData(message))

    promise?.resolve(map)
    promise = null
  }
}
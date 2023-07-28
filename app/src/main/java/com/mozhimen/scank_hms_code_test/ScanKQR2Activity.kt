package com.mozhimen.scank_hms_code_test

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.os.Bundle
import android.text.TextUtils
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.mozhimen.basick.basek.BaseKActivityVB
import com.mozhimen.basick.extsk.cropBitmap
import com.mozhimen.basick.extsk.toJson
import com.mozhimen.basick.utilk.UtilKBitmap
import com.mozhimen.basick.utilk.UtilKScreen
import com.mozhimen.componentk.cameraxk.annors.CameraXKFacing
import com.mozhimen.componentk.cameraxk.helpers.ImageConverter
import com.mozhimen.componentk.permissionk.PermissionK
import com.mozhimen.componentk.permissionk.annors.PermissionKAnnor
import com.mozhimen.scank_hms_code.databinding.ScankQr2ActivityBinding
import java.util.concurrent.locks.ReentrantLock

@PermissionKAnnor(permissions = [Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE])
class ScanKQR2Activity : BaseKActivityVB<ScankQr2ActivityBinding>() {
    data class ScanK2Result(
        val hmsScan: HmsScan,
        val bitmap: Bitmap,
        val rectSize: Int
    )

    companion object {
        const val SCANK2_ACTIVITY_RESULT_PARAM = "SCANK2_ACTIVITY_RESULT_PARAM"
    }

    override fun initData(savedInstanceState: Bundle?) {
        PermissionK.initPermissions(this) {
            if (it) {
                initView(savedInstanceState)
            } else {
                PermissionK.applySetting(this)
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        initCamera()
    }

    private fun initCamera() {
        vb.scankQr2Preview.initCamera(this, CameraXKFacing.BACK)
        vb.scankQr2Preview.setImageAnalyzer(_frameAnalyzer)
        vb.scankQr2Preview.startCamera()
    }

    private fun onScanResult(scanK2Result: ScanK2Result) {
        val intent = Intent()
        intent.putExtra(SCANK2_ACTIVITY_RESULT_PARAM, scanK2Result.toJson())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private val _frameAnalyzer: ImageAnalysis.Analyzer by lazy {
        object : ImageAnalysis.Analyzer {
            private val _reentrantLock = ReentrantLock()

            private val _options: HmsScanAnalyzerOptions = HmsScanAnalyzerOptions.Creator()
                .setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE)
                .setPhotoMode(true)
                .create()

            @SuppressLint("UnsafeOptInUsageError")
            override fun analyze(image: ImageProxy) {
                image.use { tmpImage ->
                    val bitmap: Bitmap = if (tmpImage.format == ImageFormat.YUV_420_888) {
                        ImageConverter.yuv2Bitmap(tmpImage)!!
                    } else {
                        ImageConverter.jpeg2Bitmap(tmpImage)
                    }
                    val rotateBitmap = UtilKBitmap.rotateBitmap(bitmap, 90)
                    val ratio: Double =
                        vb.scankQr2Qrscan.getRectSize().toDouble() / UtilKScreen.getScreenWidth().toDouble()
                    val cropBitmap = rotateBitmap.cropBitmap(
                        (ratio * rotateBitmap.width).toInt(),
                        (ratio * rotateBitmap.width).toInt(),
                        ((1 - ratio) * rotateBitmap.width / 2).toInt(),
                        ((rotateBitmap.height - ratio * rotateBitmap.width) / 2).toInt()
                    )

                    //detect
                    val results = ScanUtil.decodeWithBitmap(this@ScanKQR2Activity, cropBitmap, _options)
                    if (results != null && results.isNotEmpty() && results[0] != null && !TextUtils.isEmpty(results[0].originalValue)) {
                        onScanResult(ScanK2Result(results[0], cropBitmap, vb.scankQr2Qrscan.getRectSize()))
                    }
                }
            }
        }
    }
}
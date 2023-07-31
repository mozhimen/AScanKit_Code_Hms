package com.mozhimen.scank_hms_code_test

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.camera.core.ImageProxy
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.mozhimen.basick.elemk.androidx.appcompat.bases.BaseActivityVB
import com.mozhimen.basick.lintk.optin.OptInFieldCall_Close
import com.mozhimen.basick.manifestk.cons.CPermission
import com.mozhimen.basick.manifestk.permission.ManifestKPermission
import com.mozhimen.basick.manifestk.permission.annors.APermissionCheck
import com.mozhimen.basick.utilk.android.app.UtilKLaunchActivity
import com.mozhimen.basick.utilk.android.graphics.compressScaled
import com.mozhimen.basick.utilk.android.graphics.crop
import com.mozhimen.basick.utilk.android.graphics.rotate
import com.mozhimen.basick.utilk.android.view.UtilKScreen
import com.mozhimen.basick.utilk.squareup.moshi.asJsonMoshi
import com.mozhimen.componentk.camerak.camerax.commons.ICameraXKFrameListener
import com.mozhimen.componentk.camerak.camerax.helpers.ImageProxyUtil
import com.mozhimen.scank_hms_code_test.databinding.ScankQr2ActivityBinding
import java.io.Serializable

@APermissionCheck(CPermission.CAMERA, CPermission.READ_EXTERNAL_STORAGE)
class ScanKQR2Activity : BaseActivityVB<ScankQr2ActivityBinding>() {
    data class ScanK2Result(
        val hmsScan: HmsScan,
        val bitmap: ByteArray,
        val rectSize: Int
    ) : Serializable {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ScanK2Result

            if (hmsScan != other.hmsScan) return false
            if (!bitmap.contentEquals(other.bitmap)) return false
            if (rectSize != other.rectSize) return false

            return true
        }

        override fun hashCode(): Int {
            var result = hmsScan.hashCode()
            result = 31 * result + bitmap.contentHashCode()
            result = 31 * result + rectSize
            return result
        }
    }

    companion object {
        const val SCANK2_ACTIVITY_RESULT_PARAM = "SCANK2_ACTIVITY_RESULT_PARAM"
    }

    override fun initData(savedInstanceState: Bundle?) {
        ManifestKPermission.requestPermissions(this) {
            if (it) {
                super.initData(savedInstanceState)
            } else {
                UtilKLaunchActivity.startSettingAppDetails(this)
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        initCamera()
    }

    private fun initCamera() {
        vb.scankQr2Preview.apply {
            initCameraX(this@ScanKQR2Activity)
            setCameraXFrameListener(_frameAnalyzer)
            startCameraX()
        }
    }

    private fun onScanResult(scanK2Result: ScanK2Result) {
        Log.d(TAG, "onScanResult: scanK2Result $scanK2Result")
        val intent = Intent()
        intent.putExtra(SCANK2_ACTIVITY_RESULT_PARAM, scanK2Result.asJsonMoshi())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private val _ratio by lazy {
        vb.scankQr2Qrscan.getRectSize().toDouble() / UtilKScreen.getCurrentWidth().toDouble()
    }
    private var _bitmap: Bitmap? = null
    private var _lastTime = System.currentTimeMillis()

    @OptIn(OptInFieldCall_Close::class)
    private val _frameAnalyzer = object : ICameraXKFrameListener {
        private val _options: HmsScanAnalyzerOptions = HmsScanAnalyzerOptions.Creator()
            .setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE)
            .setPhotoMode(true)
            .create()

        @SuppressLint("UnsafeOptInUsageError")
        override fun invoke(a: ImageProxy) {
            a.use { imageProxy ->
                if (System.currentTimeMillis() - _lastTime >= 2000) {
                    _bitmap = if (imageProxy.format == ImageFormat.YUV_420_888) {
                        ImageProxyUtil.yuv420888ImageProxy2JpegBitmap(imageProxy)!!
                    } else {
                        ImageProxyUtil.jpegImageProxy2JpegBitmap(imageProxy)
                    }.rotate(90).apply {
                        crop(
                            (_ratio * this.width).toInt(),
                            (_ratio * this.width).toInt(),
                            ((1 - _ratio) * this.width / 2).toInt(),
                            ((this.height - _ratio * this.width) / 2).toInt()
                        )
                    }

                    //detect
                    _bitmap?.let {
                        val results = ScanUtil.decodeWithBitmap(this@ScanKQR2Activity, it, _options)
                        if (results != null && results.isNotEmpty() && results[0] != null && !TextUtils.isEmpty(results[0].originalValue)) {
                            onScanResult(ScanK2Result(results[0], it.compressScaled(50), vb.scankQr2Qrscan.getRectSize()))
                        }
                    }

                    //////////////////////////////////////////////////
                    _lastTime = System.currentTimeMillis()
                }
            }
        }
    }
}
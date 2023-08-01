package com.mozhimen.scank_hms_code_test

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.huawei.hms.ml.scan.HmsScan
import com.mozhimen.basick.elemk.androidx.appcompat.bases.BaseActivityVB
import com.mozhimen.basick.utilk.squareup.moshi.asTMoshi
import com.mozhimen.scank_hms_code_test.databinding.ActivityMainBinding

class MainActivity : BaseActivityVB<ActivityMainBinding>() {
    override fun initView(savedInstanceState: Bundle?) {
        val activityResultLauncher1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val hmsScan: HmsScan? = result.data?.getStringExtra(ScanKQRActivity.SCANK_ACTIVITY_RESULT_PARAM)?.asTMoshi<HmsScan>() ?: kotlin.run {
                    Log.e(TAG, "initView: loss scanActivity params")
                    null
                }
                hmsScan?.let {
                    vb.scankDemoTxt.text = it.originalValue
                    vb.scankDemoImg.setImageBitmap(it.originalBitmap)
                }
            }
        }
        val activityResultLauncher2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val scanKQR2Result: ScanKQR2Activity.ScanK2Result? =
                    result.data?.getStringExtra(ScanKQR2Activity.SCANK2_ACTIVITY_RESULT_PARAM)?.asTMoshi<ScanKQR2Activity.ScanK2Result>() ?: kotlin.run {
                        Log.e(TAG, "initView: loss scanActivity2 params")
                        null
                    }
                scanKQR2Result?.let {
                    vb.scankDemoTxt.text = it.hmsScan.originalValue
                    vb.scankDemoImg.setImageBitmap(it.bitmap)
                }
            }
        }

        vb.scankDemoBtnScan.setOnClickListener {
            activityResultLauncher1.launch(Intent(this, ScanKQRActivity::class.java))
        }

        vb.scankDemoBtnScan2.setOnClickListener {
            activityResultLauncher2.launch(Intent(this, ScanKQR2Activity::class.java))
        }
    }
}
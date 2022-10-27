package com.mozhimen.scankit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.huawei.hms.ml.scan.HmsScan
import com.mozhimen.basick.basek.BaseKActivityVB
import com.mozhimen.basick.extsk.fromJson
import com.mozhimen.sankit.ScanKQR2Activity
import com.mozhimen.sankit.ScanKQRActivity
import com.mozhimen.scankit.databinding.ActivityMainBinding

class MainActivity : BaseKActivityVB<ActivityMainBinding>() {
    @SuppressLint("SetTextI18n")
    override fun initData(savedInstanceState: Bundle?) {
        val activityResultLauncher1 =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val hmsScan: HmsScan? =
                        result.data?.getStringExtra(ScanKQRActivity.SCANK_ACTIVITY_RESULT_PARAM)
                            ?.fromJson<HmsScan>() ?: kotlin.run {
                            Log.e(TAG, "initView: loss scanActivity params")
                            null
                        }
                    hmsScan?.let {
                        vb.scankDemoTxt.text = it.originalValue
                        vb.scankDemoImg.setImageBitmap(it.originalBitmap)
                    }
                }
            }
        val activityResultLauncher2 =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val scanKQR2Result: ScanKQR2Activity.ScanK2Result? =
                        result.data?.getStringExtra(ScanKQR2Activity.SCANK2_ACTIVITY_RESULT_PARAM)
                            ?.fromJson<ScanKQR2Activity.ScanK2Result>() ?: kotlin.run {
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
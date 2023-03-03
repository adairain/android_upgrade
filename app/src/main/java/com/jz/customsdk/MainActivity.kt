package com.jz.customsdk

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.jz.upgrade.UpgradeManager
import com.jz.upgrade.UpgradeManagerOption

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val option = UpgradeManagerOption()
        option.host = "192.168.31.67:8080"
        option.printLog = false
        UpgradeManager.init(this, option)

        findViewById<Button>(R.id.button).setOnClickListener {
            UpgradeManager
                .optionBuilder()
                .showHistoryVersion(true)
                .allowDownloadHistoryVersion(true)
                .doOnCancelUpgrade {
                    finish()
                }
                .build()
                .checkUpgrade({
                    Log.e("MainActivity", "onCreate: ${it.msg}")
                }, {

                })
        }

    }

}
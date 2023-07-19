package com.jz.upgrade

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.jz.upgrade.databinding.DialogVersionUpgradeBinding

/**
 * @author zhouyu
 * @date   2023/2/28 16:37
 */
class UpgradeDialog(
    var mContext: Context,
    private var versionList: List<AppVersion>,
    private var option: UpgradeManager.UpgradeOptionBuilder
) : Dialog(mContext, androidx.appcompat.R.style.Theme_AppCompat_Dialog) {
    private val TAG = "UpgradeDialog"
    private lateinit var mBinding: DialogVersionUpgradeBinding
    private var apkPath: String? = null

    private var mainHandler:Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DialogVersionUpgradeBinding.inflate(layoutInflater)
        mainHandler = Handler(context.mainLooper)
        setContentView(mBinding.root)
        initView()
    }

    /*private fun checkPermission() {
      if(context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }*/

    private fun download(newestVersion: AppVersion) {
        ApkUtils.clearCacheApks(context)
        val url = "http://${option.downloadHost}/distribute/app/down?fileId=${newestVersion.fileId}"
        LogUtil.e(TAG, "initView:$url")
        mBinding.tvUpdate.isEnabled = false
        OkHttpUtil.downloadFile(
            url,
            ApkUtils.getDownloadPath(context),
            ApkUtils.createApkFileName(newestVersion), {
                mainHandler?.post {
                    apkPath = it
                    LogUtil.d(TAG, "initView: 下载完成$it")
                    mBinding.tvUpdate.isEnabled = true
                    mBinding.tvUpdate.text = "下载完成"
                    option.doOnDownloadFinish?.invoke(it)
                    (mBinding.dnvRvVersions.adapter as VersionAdapter).allowDownload = true
                    ApkUtils.installApk(context, it)
                }
            }, {
                mainHandler?.post {
                    mBinding.tvUpdate.text = "正在下载".plus("（$it%）")
                    option.doOnDownloadProgressUpdate?.invoke(it)
                    LogUtil.d(TAG, "initView: 下载进度$it")
                }
            }, {
                mainHandler?.post {
                    mBinding.tvUpdate.isEnabled = true
                    mBinding.tvUpdate.text = "重新下载"
                    option.doOnError?.invoke(it)
                    (mBinding.dnvRvVersions.adapter as VersionAdapter).allowDownload = true
                    LogUtil.e(TAG, "initView: 下载失败${it.msg}")
                }
            })
    }

    private fun initView() {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mBinding.dnvRvVersions.post {
            val height = context.resources.displayMetrics.heightPixels


            val layoutParams = mBinding.dnvRvVersions.layoutParams
            layoutParams.height = height / 10 * 4
            mBinding.dnvRvVersions.layoutParams = layoutParams
        }

        val newestVersion = versionList.first()
        mBinding.tvCancel.setOnClickListener {
            if (option.doOnCancelUpgrade != null) {
                option.doOnCancelUpgrade?.invoke(newestVersion.forceUpgrade, this)
            } else {
                Log.e(TAG, "initView: $context", )
                if (mContext is Activity) {
                    val activity = mContext as Activity
                    val active = !activity.isDestroyed
                    if (active) {
                        dismiss()
                    }
                    if (active && newestVersion.forceUpgrade) {
                        activity.finish()
                    }
                }
            }
        }
        if (newestVersion.forceUpgrade) {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            mBinding.tvUpdate.append("（必要更新）")
        }
        mBinding.tvUpdate.setOnClickListener {
            when (mBinding.tvUpdate.text.toString()) {
                "下载完成" -> {
                    apkPath.let {
                        ApkUtils.installApk(context, apkPath!!)
                    }
                }
                else -> {
                    download(newestVersion)
                }
            }
        }

        val showList = if (option.showHistoryVersion) {
            versionList
        } else {
            listOf(newestVersion)
        }
        val adapter = VersionAdapter(context, showList)
        adapter.showDownloadHistory = option.allowDownloadHistoryVersion
        adapter.showHistoryVersion = option.showHistoryVersion
        mBinding.dnvRvVersions.adapter = adapter

        adapter.doOnUpgradeClick = fun(_: Int, version: AppVersion) {
            adapter.allowDownload = false
            download(version)
        }
    }
}
package com.jz.upgrade

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import com.jz.upgrade.databinding.DialogNewVersionBinding

/**
 * @author zhouyu
 * @date   2023/2/28 16:37
 */
class UpgradeDialog(
    context: Context,
    private var versionList: List<AppVersion>,
    private var option: UpgradeManager.UpgradeOptionBuilder
) : Dialog(context) {
    private val TAG = "UpgradeDialog"
    private lateinit var mBinding: DialogNewVersionBinding
    private var apkPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DialogNewVersionBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initView()
    }

    /*private fun checkPermission() {
      if(context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }*/

    private fun download(newestVersion: AppVersion) {
        ApkUtils.clearCacheApks(context)
        val url = "http://${option.downloadHost}/app/down?fileId=${newestVersion.fileId}"
        LogUtil.e(TAG, "initView:$url")
        mBinding.tvUpdate.isEnabled = false
        OkHttpUtil.downloadFile(
            url,
            ApkUtils.getDownloadPath(context),
            ApkUtils.createApkFileName(newestVersion), {
                Handler(context.mainLooper).post {
                    apkPath = it
                    LogUtil.d(TAG, "initView: 下载完成$it")
                    mBinding.tvUpdate.isEnabled = true
                    mBinding.tvUpdate.text = "下载完成"
                    option.doOnDownloadFinish?.invoke(it)
                    (mBinding.dnvRvVersions.adapter as VersionAdapter).allowDownload = true
                    ApkUtils.installApk(context, it)
                }
            }, {
                Handler(context.mainLooper).post {
                    mBinding.tvUpdate.text = "正在下载".plus("（$it%）")
                    option.doOnDownloadProgressUpdate?.invoke(it)
                    LogUtil.d(TAG, "initView: 下载进度$it")
                }
            }, {
                Handler(context.mainLooper).post {
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
        val newestVersion = versionList.first()
        mBinding.tvCancel.setOnClickListener {
            option.doOnCancelUpgrade?.invoke(newestVersion.forceUpgrade)
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
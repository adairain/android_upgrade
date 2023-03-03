package com.jz.upgrade

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import com.google.gson.reflect.TypeToken
import java.lang.ref.WeakReference
import java.lang.reflect.Type


class UpgradeManager {
    companion object {
        const val TAG: String = "OkHttpUtil"
        private var option: UpgradeManagerOption = UpgradeManagerOption()
        private var versionCode = 0L
        private val type: Type = object : TypeToken<BaseBean<List<AppVersion>>>() {}.type
        private var contextReference: WeakReference<Context>? = null

        private const val DEFAULT_HOST = "192.168.31.67:8080"
        private const val DEFAULT_DOWNLOAD_HOST = "192.168.31.67:8080"

        /**
         * 初始化模块
         * */
        fun init(context: Context, managerOption: UpgradeManagerOption? = null): Companion {

            this.option.host = managerOption?.host ?: DEFAULT_HOST
            LogUtil.printLog = managerOption?.printLog ?: false

            val manager = context.packageManager
            val appInfo =
                manager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val metaData = appInfo.metaData
            val packageInfo = manager.getPackageInfo(context.packageName, 0)

            this.option.appKey = managerOption?.appKey ?: metaData.get("upgrade_key").toString()


            versionCode = if (Build.VERSION.SDK_INT >= 28) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
            return this
        }

        /**
         * 设置接口主机地址
         * @param host 主机
         * */
        fun setHost(host: String) {
            this.option.host = host
        }

        fun setAppKey(appKey: String) {
            this.option.appKey = appKey
        }

        fun setOption(option: UpgradeManagerOption) {
            this.option = option
        }

        fun setPrintLog(printLog: Boolean) {
            this.option.printLog = printLog
        }


        /**
         * 手动检查更新
         * @param doOnSuccess 请求成功
         * @param doOnError 请求失败
         * */
        fun checkUpgrade(
            doOnSuccess: ((data: BaseBean<List<AppVersion>>) -> Unit),
            doOnError: ((e: CustomException) -> Unit)?
        ) {
            if (option.appKey == null) {
                doOnError?.invoke(CustomException.notInitException)
                return
            }


            val url = "http://${option.host}/app/checkUpgrade"

            val params = mutableMapOf<String, Any>()
            params["appKey"] = option.appKey!!
            params["versionCode"] = versionCode
            LogUtil.i(TAG, url)
            OkHttpUtil.postJson(url, OkHttpUtil.toJson(params), {
                val bean = OkHttpUtil.fromJson<BaseBean<List<AppVersion>>>(it, type)
                if (bean.isSuccess()) {
                    doOnSuccess.invoke(bean)
                } else {
                    doOnError?.invoke(CustomException.interfaceException(bean.code, bean.msg))
                }
            }, {
                doOnError?.invoke(it)
            })
        }

        /**
         *
         * */
        fun optionBuilder(context: Context): UpgradeOptionBuilder {
            contextReference = WeakReference(context)
            return UpgradeOptionBuilder(contextReference?.get(), option)
        }
    }

    class UpgradeInterface internal constructor(
        var context: Context?,
        var optionBuilder: UpgradeOptionBuilder
    ) {

        /**
         * 检查更新，自动处理异常情况
         *
        fun checkUpgrade(doOnNoNewVersion: (() -> Unit)?) {
        checkUpgrade({

        }, doOnNoNewVersion)
        }
         */


        /**
         * 检查更新，手动处理异常情况
         * */
        fun checkUpgrade(
            doOnError: ((e: CustomException) -> Unit)?,
            doOnNoNewVersion: (() -> Unit)?
        ) {
            if (context == null) {
                doOnError?.invoke(CustomException.nullContextException)
                return
            }
            checkUpgrade({
                Handler(context!!.mainLooper).post {
                    val versionList = it.data
                    if (versionList.isEmpty()) {
                        doOnNoNewVersion?.invoke()
                        return@post
                    }
                    var errorType = 2004
                    val contextAvailable = if (context != null) {
                        if (context is Activity) {
                            val activity = context as Activity
                            val active = !activity.isDestroyed
                            active
                        } else {
                            errorType = 2003
                            false
                        }
                    } else {
                        errorType = 2004
                        false
                    }
                    if (contextAvailable) {
                        optionBuilder.doOnError = doOnError
                        val upgradeDialog =
                            UpgradeDialog(context!!, versionList, optionBuilder)
                        upgradeDialog.show()
                    } else {
                        when (errorType) {
                            2002 -> {
                                doOnError?.invoke(CustomException.nullContextException)
                            }
                            2003 -> {
                                doOnError?.invoke(CustomException.appContextException)
                            }
                            2004 -> {
                                doOnError?.invoke(CustomException.activityDestroyException)
                            }
                        }
                    }
                }
            }, doOnError)
        }
    }

    class UpgradeOptionBuilder internal constructor(
        var context: Context?,
        option: UpgradeManagerOption
    ) {

        internal var showHistoryVersion = false
        internal var allowDownloadHistoryVersion = false
        internal var downloadHost = DEFAULT_DOWNLOAD_HOST
        internal var doOnCancelUpgrade: ((isForce: Boolean) -> Unit)? = null
        internal var doOnDownloadProgressUpdate: ((progress: Int) -> Unit)? = null
        internal var doOnDownloadFinish: ((path: String) -> Unit)? = null
        internal var doOnError: ((e: CustomException) -> Unit)? = null

        init {
            downloadHost = option.downloadHost ?: DEFAULT_DOWNLOAD_HOST
        }


        /**
         * 是否显示历史版本
         * */
        fun showHistoryVersion(showHistoryVersion: Boolean): UpgradeOptionBuilder {
            this.showHistoryVersion = showHistoryVersion
            return this
        }

        /**
         * 下载完成
         * */
        fun doOnDownloadFinish(doOnDownloadFinish: ((path: String) -> Unit)?): UpgradeOptionBuilder {
            this.doOnDownloadFinish = doOnDownloadFinish
            return this
        }

        /**
         * 下载进度
         * */
        fun doOnDownloadProgressUpdate(doOnDownloadProgressUpdate: ((progress: Int) -> Unit)?): UpgradeOptionBuilder {
            this.doOnDownloadProgressUpdate = doOnDownloadProgressUpdate
            return this
        }

        /**
         * 下载地址
         * */
        fun downloadHost(downloadHost: String): UpgradeOptionBuilder {
            this.downloadHost = downloadHost
            return this
        }

        /**
         * 取消更新后的操作
         * */
        fun doOnCancelUpgrade(doOnCancelUpgrade: ((isForce: Boolean) -> Unit)): UpgradeOptionBuilder {
            this.doOnCancelUpgrade = doOnCancelUpgrade
            return this
        }


        /**
         * 是否可以下载历史版本
         * */
        fun allowDownloadHistoryVersion(allowDownloadHistoryVersion: Boolean): UpgradeOptionBuilder {
            this.allowDownloadHistoryVersion = allowDownloadHistoryVersion
            return this
        }

        fun build(): UpgradeInterface {
            return UpgradeInterface(context, this)
        }
    }
}
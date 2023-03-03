package com.jz.upgrade

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider

import java.io.File


/**
 * @ClassName  :ApkUtils
 * @Package    :com.lau.ldk.version
 * @Author     :Lau
 * @CreateTime :2022/4/15 17:47
 * @Description:
 */
class ApkUtils {
    companion object {

        fun getDownloadPath(context: Context): String {
            return context.getExternalFilesDir("DownloadApks").toString() + File.separator
        }

        fun createApkFileName(newVersionBean: AppVersion): String {
            return "${newVersionBean.packageName}_${newVersionBean.versionCode}_v${newVersionBean.versionName}.apk"
        }

        fun clearCacheApks(context: Context) {
            val file = context.getExternalFilesDir("DownloadApks")
            if (file != null && file.exists() && file.isDirectory && file.listFiles() != null) {
                file.listFiles()?.forEach {
                    val b = it.delete()
                }
            }
        }

        fun installApk(context: Context, apkDownloadPath: String) {
            val intent = Intent(Intent.ACTION_VIEW)
            val file = File(apkDownloadPath)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val apkUri =
                    FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            } else {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                val uri = Uri.fromFile(file)
                intent.setDataAndType(uri, "application/vnd.android.package-archive")
            }
            context.startActivity(intent)
        }
    }
}
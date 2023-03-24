package com.jz.upgrade

import android.content.Context
import android.net.InetAddresses
import android.os.Build
import android.os.Handler
import android.util.Log
import android.util.Patterns
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Type


/**
 * @author zhouyu
 * @date   2022/11/21 16:37
 */
class OkHttpUtil {

    //TODO 回调内切换主线超
    companion object {
        const val TAG: String = "OkHttpUtil"
        fun verifyHost(host: String): Boolean {
            return if (Build.VERSION.SDK_INT >= 29) {
                InetAddresses.isNumericAddress(host)
            } else {
                Patterns.IP_ADDRESS.matcher(host).matches()
            }
        }

        fun <T> fromJson(json: String, type: Type): T {
            return gson.fromJson(json, type)
        }

        fun toJson(data: Any): String {
            return gson.toJson(data)
        }

        private val JSON: MediaType = "application/json".toMediaType()
        private val gson = Gson()
        private var client = OkHttpClient()
        fun postJson(
            context: Context?,
            api: String,
            json: String,
            doOnSuccess: ((data: String) -> Unit)?,
            doOnError: ((t: CustomException) -> Unit)?
        ) {
            LogUtil.i(TAG, "postJson: $json")
            val requestBody: RequestBody = json.toRequestBody(JSON)
            val request: Request = Request.Builder()
                .url(api)
                .post(requestBody)
                .build()
            val call = client.newCall(request)
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (context != null) {
                        Handler(context.mainLooper).post {
                            doOnError?.invoke(CustomException.netException(e))
                        }
                    } else {
                        doOnError?.invoke(CustomException.netException(e))
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    val code = response.code
                    if (code == 200) {
                        if (body?.isNotBlank() == true) {
                            LogUtil.d(TAG, "onResponse: $body")
                            if (context != null) {
                                Handler(context.mainLooper).post {
                                    doOnSuccess?.invoke(body)
                                }
                            } else {
                                doOnSuccess?.invoke(body)
                            }
                        }
                    } else {
                        doOnError?.invoke(CustomException.httpCodeErrorException(code))
                    }
                }
            })
        }

        fun downloadFile(
            url: String,
            savePath: String,
            fileName: String,
            doOnFinished: ((data: String) -> Unit)?,
            doOnProgress: ((progress: Int) -> Unit)?,
            doOnError: ((t: CustomException) -> Unit)?
        ) {
            val startTime = System.currentTimeMillis()
            val request: Request = Request.Builder()
                .url(url)
                .addHeader("Connection", "close")
                .build()
            val call = client.newCall(request)

            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    doOnError?.invoke(CustomException.netException(e))
                }

                override fun onResponse(call: Call, response: Response) {
                    var inputStream: InputStream? = null
                    val buf = ByteArray(2048)
                    var len: Int
                    var fos: FileOutputStream? = null
                    // 储存下载文件的目录
                    try {
                        val responseBody = response.body
                        inputStream = responseBody!!.byteStream()
                        val total = responseBody.contentLength()
                        Log.i(TAG, "onResponse: $total")
                        val file = File(savePath, fileName)
                        fos = FileOutputStream(file)
                        var sum: Long = 0
                        while (true) {
                            len = inputStream.read(buf)
                            if (len == -1)
                                break
                            fos.write(buf, 0, len)
                            sum += len.toLong()
                            val progress = (sum * 1.0f / total * 100).toInt()
                            doOnProgress?.invoke(progress)
                        }
                        fos.flush()
                        doOnFinished?.invoke(file.absolutePath)
                        LogUtil.e(TAG, "download success")
                        LogUtil.e(TAG, "totalTime=" + (System.currentTimeMillis() - startTime))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        LogUtil.e(TAG, "download failed : " + e.message)
                        doOnError?.invoke(CustomException.downloadException(e))
                    } finally {
                        try {
                            inputStream?.close()
                            fos?.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            })

        }

    }
}
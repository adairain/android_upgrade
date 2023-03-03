package com.jz.upgrade

/**
 * @author zhouyu
 * @date   2023/2/27 19:08
 */
class CustomException(var code: Int, var msg: String, var throwable: Throwable?) {
    var interfaceCode: Int? = null

    constructor(code: Int, msg: String, throwable: Throwable?, interfaceCode: Int) : this(
        code,
        msg,
        throwable
    ) {
        this.interfaceCode = interfaceCode
    }

    companion object {

        val notInitException = CustomException(2001, "模块未初始化！", null)
        val nullContextException = CustomException(2002, "context为空！", null)

        fun interfaceException(interfaceCode: Int, msg: String): CustomException {
            return CustomException(3000, msg, null, interfaceCode)
        }

        fun interfaceException(interfaceCode: Int): CustomException {
            return interfaceException(interfaceCode, "接口异常")
        }

        fun netException(throwable: Throwable): CustomException {
            return CustomException(1001, "网络错误！", throwable)
        }

        fun httpCodeErrorException(interfaceCode: Int): CustomException {
            return CustomException(1002, "Http状态码错误！", null, interfaceCode)
        }
        fun downloadException(throwable: Throwable?): CustomException {
            return CustomException(1003, "下载错误！", throwable)
        }
    }
}
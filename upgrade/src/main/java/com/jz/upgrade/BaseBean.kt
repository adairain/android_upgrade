package com.jz.upgrade


/**
 * @ClassName  :BaseBean
 * @Package    :com.jingzhi.scansystem.net
 * @Author     :Lau
 * @CreateTime :2022/3/17 9:46
 * @Description:
 */
class BaseBean<T>(var code: Int, var msg: String, var data: T) {
    companion object {
        const val CODE_SUCCESS = 20000
        const val CODE_ALLOCATE_OUT_CODE_OUT_OF_LIST = 20002
        const val CODE_ADD_NEW_CODE_OVER_LIMIT = 20002
        const val CODE_ADD_NEW_CODE_OVER_LIMIT_TOO_MUCH = 20003

        const val CODE_LOGIN_EXPIRED = 40000
    }

    fun isSuccess(): Boolean {
        return code == CODE_SUCCESS
    }

    fun isAddingNewCodeOverLimit(): Boolean {
        return code == CODE_ADD_NEW_CODE_OVER_LIMIT
    }

    fun isAddingNewCodeOverLimitTooMuch(): Boolean {
        return code == CODE_ADD_NEW_CODE_OVER_LIMIT_TOO_MUCH
    }

}
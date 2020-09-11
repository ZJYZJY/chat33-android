package com.fzm.login.source.impl

import com.fuzamei.common.net.Result
import com.fuzamei.common.net.rxjava.ApiException
import com.fuzamei.componentservice.ext.apiCall
import com.fzm.login.model.bean.ChatLogin
import com.fzm.login.net.LoginService
import com.fzm.login.source.LoginDataSource

/**
 * @author zhengjy
 * @since 2020/02/11
 * Description:
 */
class NetLoginDataSource(
        private val service: LoginService
) : LoginDataSource {

    override suspend fun login(phone: String, code: String): Result<ChatLogin> {
        val map = mapOf("phone" to phone, "code" to code)
        return apiCall { service.phoneLogin(map) }
    }

    override suspend fun loginV2(account: String, code: String, type: Int): Result<ChatLogin> {
        return when (type) {
            0 -> apiCall { service.phoneLogin(mapOf("phone" to account, "code" to code)) }
            1 -> apiCall { service.emailLogin(mapOf("email" to account, "code" to code)) }
            else -> Result.Error(ApiException("不支持的登录类型"))
        }
    }

    override suspend fun sendCode(phone: String): Result<Any> {
        val map = mapOf("phone" to phone)
        return apiCall { service.sendCode(map) }
    }

    override suspend fun sendEmail(email: String): Result<Any> {
        val map = mapOf("email" to email)
        return apiCall { service.sendEmail(map) }
    }
}
package com.fzm.login.source

import com.fuzamei.common.net.Result
import com.fzm.login.model.bean.ChatLogin

/**
 * @author zhengjy
 * @since 2020/02/11
 * Description:
 */
interface LoginDataSource {

    /**
     * 登录方法
     *
     * @param phone 手机号
     * @param code  验证码
     */
    suspend fun login(phone: String, code: String): Result<ChatLogin>

    /**
     * 登录方法
     *
     * @param account   手机号/邮箱
     * @param code      验证码
     * @param type      账户类型 0：手机，1：邮箱
     */
    suspend fun loginV2(account: String, code: String, type: Int): Result<ChatLogin>

    /**
     * 发送验证码
     *
     * @param phone 手机号
     */
    suspend fun sendCode(phone: String): Result<Any>

    /**
     * 发送邮件验证码
     *
     * @param email 邮箱
     */
    suspend fun sendEmail(email: String): Result<Any>
}
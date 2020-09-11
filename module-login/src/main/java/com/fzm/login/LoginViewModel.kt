package com.fzm.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.login.model.LoginRepository
import com.fzm.login.model.bean.ChatLogin

/**
 * @author zhengjy
 * @since 2019/08/09
 * Description:
 */
class LoginViewModel(
        private val repository: LoginRepository
) : LoadingViewModel() {

    private val _codeResult: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val codeResult: LiveData<Boolean>
        get() = _codeResult

    private val _loginResult: MutableLiveData<ChatLogin> by lazy { MutableLiveData<ChatLogin>() }
    val loginResult: LiveData<ChatLogin>
        get() = _loginResult

    fun sendCode(account: String, type: Int) {
        start {
            loading()
        }.request {
            if (type == 0) {
                repository.sendCode(account)
            } else {
                repository.sendEmail(account)
            }
        }.result({
            _codeResult.value = true
        }, {
            _codeResult.value = false
        }, {
            dismiss()
        })
    }

    fun login(account: String, code: String, type: Int) {
        start {
            loading()
        }.request {
            repository.loginV2(account, code, type)
        }.result({
            _loginResult.value = it
        }, {
            _loginResult.value = null
        }, {
            dismiss()
        })
    }
}
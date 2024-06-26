package com.tools.script.two.base.vm

import com.tools.script.two.common.ToastInfo
import com.tools.script.two.function.now

sealed class CommonAction {
    data class Error(val error: Throwable) : CommonAction()

    data class Toast(val toastInfo: ToastInfo) : CommonAction()

    data class Back(val timestamp: Long = now()) : CommonAction()
}

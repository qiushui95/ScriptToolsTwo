package com.tools.script.two.function

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.ToastUtils
import com.tools.script.two.base.vm.CommonAction


internal fun CommonAction.handle(owner: LifecycleOwner?) {
    when (this) {
        is CommonAction.Back -> handleBack(owner = owner)
        is CommonAction.Error -> handleError(error)
        is CommonAction.Toast -> toastInfo.showToast()
    }
}

private fun handleError(error: Throwable) {
    ToastUtils.showShort(error.message)
}

private fun handleBack(owner: LifecycleOwner?) {
    owner ?: return

    when (owner) {
        is DialogFragment -> owner.dismiss()
        is ComponentActivity -> owner.finishAfterTransition()

        else -> Log.d("CommonAction", "Unsupported owner type: $owner,skip back")
    }
}

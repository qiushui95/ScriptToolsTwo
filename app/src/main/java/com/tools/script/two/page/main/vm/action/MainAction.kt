package com.tools.script.two.page.main.vm.action

import androidx.lifecycle.LifecycleOwner
import com.lxj.xpopup.XPopup
import com.tools.script.two.base.vm.CommonAction
import com.tools.script.two.base.vm.VMAction
import com.tools.script.two.function.handle
import com.tools.script.two.function.now
import com.tools.script.two.page.main.MainActivity
import com.tools.script.two.page.main.vm.intent.MainIntent

internal sealed class MainAction : VMAction {
    data class Common(val info: CommonAction) : MainAction() {
        override fun invoke(owner: LifecycleOwner?) {
            info.handle(owner)
        }
    }

    data class ShowSchemaInputDialog(val timestamp: Long = now()) : MainAction() {
        override fun invoke(owner: LifecycleOwner?) {
            if (owner !is MainActivity) return

            XPopup.Builder(owner)
                .asInputConfirm(
                    "请输入自动精灵启动schema",
                    "zdjl://clientcall/**********"
                ) { input ->
                    owner.viewModel.intent(MainIntent.OnSchemaChanged(input))
                }.show()
        }
    }
}

package com.tools.script.two.page.main.vm

import com.blankj.utilcode.util.AppUtils
import com.tools.script.two.base.vm.BaseVM
import com.tools.script.two.base.vm.CommonAction
import com.tools.script.two.base.vm.CommonIntent
import com.tools.script.two.page.main.MainArgs
import com.tools.script.two.page.main.vm.action.MainAction
import com.tools.script.two.page.main.vm.intent.MainIntent
import org.koin.core.Koin

private typealias UI = MainUI
private typealias Intent = MainIntent
private typealias Action = MainAction
private typealias Args = MainArgs

internal class MainVM(
    koin: Koin,
) : BaseVM<UI, Intent, Action, Args>(koin, MainArgs(), UI()) {

    override fun provideCommonIntent(info: CommonIntent): Intent {
        return MainIntent.OnCommon(info)
    }

    override fun provideCommonAction(info: CommonAction): Action {
        return MainAction.Common(info)
    }

    override suspend fun reduceInitIntent(intent: CommonIntent.OnInit) {
        val isCurSystemApp = AppUtils.isAppSystem()
        val isAutoSystemApp = AppUtils.isAppSystem("com.zdanjian.zdanjian")

        updateState { copy(isCurSystemApp = isCurSystemApp, isAutoSystemApp = isAutoSystemApp) }
    }
}
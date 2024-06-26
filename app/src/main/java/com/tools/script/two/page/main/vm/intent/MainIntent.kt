package com.tools.script.two.page.main.vm.intent

import android.app.Application
import com.blankj.utilcode.util.ShellUtils
import com.tools.script.two.R
import com.tools.script.two.alias.Pipeline
import com.tools.script.two.base.vm.CommonIntent
import com.tools.script.two.base.vm.VMHandler
import com.tools.script.two.base.vm.VMIntent
import com.tools.script.two.function.now
import com.tools.script.two.page.main.MainArgs
import com.tools.script.two.page.main.vm.MainUI
import com.tools.script.two.page.main.vm.action.MainAction
import com.tools.script.two.utils.PathUtils
import kotlinx.coroutines.launch
import java.io.File

private typealias UI = MainUI
private typealias Intent = MainIntent
private typealias Action = MainAction
private typealias Args = MainArgs
private typealias Pipeline2 = Pipeline<UI, Intent, Action>
private typealias VMHandler2 = VMHandler<UI, Intent, Action, Args>

internal sealed class MainIntent : VMIntent<UI, Intent, Action, Args> {

    protected fun VMHandler2.runAssertCmd(pipeline: Pipeline2, fileName: String) {
        val app = getKoin().get<Application>()

        val input = app.assets.open(fileName)

        val cmd = input.reader().readText()

        input.close()

        ShellUtils.execCmd(cmd, true, false)

        getIoScope().launch { actionToast(pipeline, R.string.done_must_reboot_toast) }
    }

    data class OnCommon(val info: CommonIntent) : Intent() {
        override suspend fun VMHandler2.reduce(pipeline: Pipeline2) {
            handleCommonIntent(info)
        }
    }

    data class OnSchemaChanged(val input: String) : Intent() {
        override suspend fun VMHandler2.reduce(pipeline: Pipeline2) {
            if (input.startsWith("zdjl://").not()) {
                actionToast(pipeline, R.string.input_error_toast)
            } else {
                val path = PathUtils.getAutoSchemaPath()

                File(path).writeText(input)
                actionToast(pipeline, R.string.save_success_toast)
            }
        }
    }

    data class OnSetCurSystemClick(val timestamp: Long = now()) : Intent() {
        override suspend fun VMHandler2.reduce(pipeline: Pipeline2) {
            runAssertCmd(pipeline, "SetCurSystem.txt")
        }
    }

    data class OnUnsetCurSystemClick(val timestamp: Long = now()) : Intent() {
        override suspend fun VMHandler2.reduce(pipeline: Pipeline2) {
            runAssertCmd(pipeline, "UnsetCurSystem.txt")
        }
    }

    data class OnSetAutoSystemClick(val timestamp: Long = now()) : Intent() {
        override suspend fun VMHandler2.reduce(pipeline: Pipeline2) {
            runAssertCmd(pipeline, "SetAutoSystem.txt")
        }
    }

    data class OnUnsetAutoSystemClick(val timestamp: Long = now()) : Intent() {
        override suspend fun VMHandler2.reduce(pipeline: Pipeline2) {
            runAssertCmd(pipeline, "UnsetAutoSystem.txt")
        }
    }

    data class OnChangeSchemaClick(val timestamp: Long = now()) : Intent() {
        override suspend fun VMHandler2.reduce(pipeline: Pipeline2) {
            action(pipeline, MainAction.ShowSchemaInputDialog())
        }
    }

    data class OnRebootClick(val timestamp: Long = now()) : Intent() {
        override suspend fun VMHandler2.reduce(pipeline: Pipeline2) {
            ShellUtils.execCmd("reboot", true, false)
        }
    }

    data class OnStartAutoClick(val timestamp: Long = now()) : Intent() {
        override suspend fun VMHandler2.reduce(pipeline: Pipeline2) {
            val cmdList = listOf(
                "am stopservice com.tools.script.two/.services.AutoMonitorService",
                "am startservice -n com.tools.script.two/.services.AutoMonitorService"
            )

            ShellUtils.execCmd(cmdList, true, false)
        }
    }

    data class OnStopAutoClick(val timestamp: Long = now()) : Intent() {
        override suspend fun VMHandler2.reduce(pipeline: Pipeline2) {
            val cmd = "am stopservice com.tools.script.two/.services.AutoMonitorService"

            ShellUtils.execCmd(cmd, true, false)
        }
    }
}
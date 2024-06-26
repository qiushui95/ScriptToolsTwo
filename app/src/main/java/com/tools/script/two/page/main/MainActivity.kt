package com.tools.script.two.page.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.blankj.utilcode.util.ShellUtils
import com.tools.script.two.base.AppTheme
import com.tools.script.two.page.main.page.MainPage
import com.tools.script.two.page.main.vm.MainVM
import org.koin.androidx.viewmodel.ext.android.viewModel

internal class MainActivity : ComponentActivity() {
    val viewModel: MainVM by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                MainPage()
            }
        }

        viewModel.subscribe(this) { action ->
            action(this)
        }

        echoLaunchOnBoot()
    }

    private fun echoLaunchOnBoot() {
        val startService = "am startservice -n com.tools.script.two/.services.AutoMonitorService"

        val cmdList = listOf(
            "mkdir -p /data/misc/scripts/boot_completed",
            "echo '$startService' > /data/misc/scripts/boot_completed/auto_launch.sh",
            "chmod 766 /data/misc/scripts/boot_completed/auto_launch.sh"
        )

        ShellUtils.execCmd(cmdList, true, false)
    }
}
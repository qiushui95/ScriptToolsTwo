package com.tools.script.two.page.main.vm

import com.tools.script.two.base.vm.VMUI

internal data class MainUI(
    val isCurSystemApp: Boolean = false,
    val isAutoSystemApp: Boolean = false,
) : VMUI

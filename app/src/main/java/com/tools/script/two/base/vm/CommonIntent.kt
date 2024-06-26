package com.tools.script.two.base.vm

import androidx.lifecycle.Lifecycle
import com.tools.script.two.function.now

sealed class CommonIntent {
    data class OnError(val error: Throwable) : CommonIntent()

    data class OnLifecycleEvent(
        val event: Lifecycle.Event,
        val timestamp: Long = now(),
    ) : CommonIntent()

    data class OnInit(val timestamp: Long = now()) : CommonIntent()

    data class OnBackPressed(val timestamp: Long = now()) : CommonIntent()
}

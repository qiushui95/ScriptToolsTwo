package com.tools.script.two.base.vm

import com.tools.script.two.alias.Pipeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.koin.core.Koin

interface VMHandler<U : VMUI, I, A : VMAction, Args> where I : VMIntent<U, I, A, Args> {
    fun getKoin(): Koin

    fun getIoScope(): CoroutineScope

    fun getArgs(): Args

    fun provideCommonIntent(info: CommonIntent): I

    fun provideCommonAction(info: CommonAction): A

    suspend fun handleCommonIntent(intent: CommonIntent)

    suspend fun <T> startSuspendWork(
        key: String,
        interval: Long = 0L,
        startMapper: (U.() -> U)? = null,
        dataMapper: (U.(T) -> U)? = null,
        failMapper: (U.() -> U)? = null,
        onStart: (suspend () -> Unit)? = null,
        onStart2: (suspend () -> Unit)? = null,
        onFail: (suspend (Throwable) -> Unit)? = null,
        onFail2: (suspend (Throwable) -> Unit)? = null,
        onSuccess: (suspend (T) -> Unit)? = null,
        onSuccess2: (suspend (T) -> Unit)? = null,
        onEnd: (suspend () -> Unit)? = null,
        block: suspend (U) -> T,
    ): Job?

    fun intent(pipeline: Pipeline<U, I, A>, intent: I)

    fun intentCommon(pipeline: Pipeline<U, I, A>, info: CommonIntent)

    fun intentInit(pipeline: Pipeline<U, I, A>)

    fun intentError(pipeline: Pipeline<U, I, A>, error: Throwable)

    suspend fun action(pipeline: Pipeline<U, I, A>, action: A)

    suspend fun actionCommon(pipeline: Pipeline<U, I, A>, info: CommonAction)

    suspend fun actionToast(pipeline: Pipeline<U, I, A>, res: Int?)

    suspend fun actionBack(pipeline: Pipeline<U, I, A>, res: Int? = null)
}

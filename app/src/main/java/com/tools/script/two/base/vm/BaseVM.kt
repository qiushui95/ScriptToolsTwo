package com.tools.script.two.base.vm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.CacheMemoryStaticUtils
import com.tools.script.two.alias.Pipeline
import com.tools.script.two.common.ToastInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.Koin
import pro.respawn.flowmvi.api.ActionReceiver
import pro.respawn.flowmvi.api.ActionShareBehavior
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.api.StateProvider
import pro.respawn.flowmvi.api.StateReceiver
import pro.respawn.flowmvi.api.Store
import pro.respawn.flowmvi.api.SubscriberLifecycle
import pro.respawn.flowmvi.api.SubscriptionMode
import pro.respawn.flowmvi.compose.dsl.DefaultLifecycle
import pro.respawn.flowmvi.compose.dsl.subscribe
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.dsl.StoreConfigurationBuilder
import pro.respawn.flowmvi.dsl.lazyStore
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.plugins.whileCanInit

abstract class BaseVM<U : VMUI, I, A : VMAction, Args>(
    private val koin: Koin,
    private val args: Args,
    initialUI: U,
) : ViewModel(), VMHandler<U, I, A, Args> where I : VMIntent<U, I, A, Args> {
    private val _ioScope: CoroutineScope by lazy {
        object : CoroutineScope {
            override val coroutineContext = viewModelScope.coroutineContext + Dispatchers.IO
        }
    }

    protected val store: Store<U, I, A> by lazyStore(initialUI, viewModelScope) {

        configure { configure() }

        reduce { intent ->
            reduceByCatch(intent, this)
        }

        recover { ex ->
            onErrorIntent(ex)
            null
        }

        whileCanInit {
            onInit()
        }

        config()
    }

    protected open fun StoreConfigurationBuilder.configure() {
        parallelIntents = false
        coroutineContext = Dispatchers.Default
        actionShareBehavior = ActionShareBehavior.Share()
        intentCapacity = ActionShareBehavior.DefaultBufferSize
    }

    @Suppress("UNCHECKED_CAST")
    protected suspend fun updateState(transform: suspend U.() -> U) {
        (store as StateReceiver<U>).updateState(transform)
    }

    @Suppress("UNCHECKED_CAST")
    protected suspend fun withState(block: suspend U.() -> Unit) {
        (store as StateReceiver<U>).withState(block)
    }

    @Suppress("UNCHECKED_CAST")
    val states: StateFlow<U> = (store as StateProvider<U>).states

    @Suppress("UNCHECKED_CAST")
    suspend fun action(action: A?) {
        action ?: return

        (store as ActionReceiver<A>).action(action)
    }

    open fun intent(intent: I?) {
        intent ?: return

        store.intent(intent)
    }

    protected open fun StoreBuilder<U, I, A>.config() {
    }

    fun subscribe(
        lifecycleOwner: LifecycleOwner,
        lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
        consume: suspend (action: A) -> Unit,
    ): Job = lifecycleOwner.subscribe(store, consume, {}, lifecycleState)

    @Composable
    fun subscribe(
        lifecycle: SubscriberLifecycle = DefaultLifecycle,
        mode: SubscriptionMode = SubscriptionMode.Started,
        consume: suspend CoroutineScope.(action: A) -> Unit = {},
    ): State<U> = store.subscribe(lifecycle = lifecycle, mode = mode, consume = consume)

    protected open suspend fun onInit() {
        val info = CommonIntent.OnInit()

        intent(provideCommonIntent(info))
    }

    private suspend fun reduceByCatch(intent: I, pipeline: PipelineContext<U, I, A>) {
        try {
            intent.apply { reduce(pipeline) }
        } catch (ex: Throwable) {
            onErrorIntent(ex)
        }
    }

    protected fun onErrorIntent(ex: Throwable) {
        if (ex.javaClass.name == "kotlinx.coroutines.JobCancellationException") return

        ex.printStackTrace()

        intent(provideCommonIntent(CommonIntent.OnError(ex)))
    }

    private suspend fun <R> withStateResult(block: suspend U.() -> R): R {
        var result: R? = null
        withState {
            result = block()
        }

        return result!!
    }

    protected suspend fun doWork(
        key: String,
        interval: Long = 1000L,
        workBlock: suspend () -> Unit,
    ): Job? {
        if (interval > 0) {
            val intervalKey = "${javaClass.name}-$key"

            val lastTime = CacheMemoryStaticUtils.get<Long>(intervalKey) ?: 0L

            val now = System.currentTimeMillis()

            if (now - lastTime < interval) return null

            CacheMemoryStaticUtils.put(intervalKey, now)
        }

        return viewModelScope.launch(Dispatchers.IO) {
            workBlock()
        }
    }

    private fun getFailBlock(
        onFail: (suspend (Throwable) -> Unit)?,
    ): (suspend (Throwable) -> Unit) {
        return onFail ?: { this.onErrorIntent(it) }
    }

    override suspend fun <T> startSuspendWork(
        key: String,
        interval: Long,
        startMapper: (U.() -> U)?,
        dataMapper: (U.(T) -> U)?,
        failMapper: (U.() -> U)?,
        onStart: (suspend () -> Unit)?,
        onStart2: (suspend () -> Unit)?,
        onFail: (suspend (Throwable) -> Unit)?,
        onFail2: (suspend (Throwable) -> Unit)?,
        onSuccess: (suspend (T) -> Unit)?,
        onSuccess2: (suspend (T) -> Unit)?,
        onEnd: (suspend () -> Unit)?,
        block: suspend (U) -> T,
    ): Job? {
        return doWork(key = key, interval = interval) {
            doSuspendWork(
                startMapper = startMapper,
                dataMapper = dataMapper,
                failMapper = failMapper,
                onStartList = listOfNotNull(onStart, onStart2),
                onFailList = listOfNotNull(getFailBlock(onFail), onFail2),
                onSuccessList = listOfNotNull(onSuccess, onSuccess2),
                onEnd = onEnd,
                block = block,
            )
        }
    }


    private suspend fun <T> doSuspendWork(
        startMapper: (U.() -> U)?,
        dataMapper: (U.(T) -> U)?,
        failMapper: (U.() -> U)?,
        onStartList: List<suspend () -> Unit>,
        onFailList: List<suspend (Throwable) -> Unit>,
        onSuccessList: List<(suspend (T) -> Unit)>,
        onEnd: (suspend () -> Unit)?,
        block: suspend (U) -> T,
    ) {
        try {
            onStartList.forEach { it.invoke() }

            if (startMapper != null) {
                updateState { startMapper() }
            }

            val uiInfo = withStateResult { this }

            val result = block(uiInfo)


            if (dataMapper != null) {
                updateState { dataMapper(result) }
            }

            onSuccessList.forEach { it.invoke(result) }
        } catch (ex: Throwable) {

            if (failMapper != null) {
                updateState { failMapper() }
            }

            if (onFailList.isEmpty()) {
                onErrorIntent(ex)
            } else {
                onFailList.forEach { it(ex) }
            }
        } finally {
            onEnd?.invoke()
        }
    }

    override fun getKoin(): Koin {
        return koin
    }

    override fun getIoScope(): CoroutineScope {
        return _ioScope
    }

    override fun getArgs(): Args {
        return args
    }

    final override suspend fun handleCommonIntent(intent: CommonIntent) {
        when (intent) {
            is CommonIntent.OnError -> reduceErrorIntent(intent)
            is CommonIntent.OnInit -> reduceInitIntent(intent)
            is CommonIntent.OnLifecycleEvent -> reduceLifecycleEventIntent(intent)
            is CommonIntent.OnBackPressed -> reduceBackPressedIntent(intent)
        }
    }


    override fun intent(pipeline: Pipeline<U, I, A>, intent: I) {
        pipeline.intent(intent)
    }

    override fun intentCommon(pipeline: Pipeline<U, I, A>, info: CommonIntent) {
        intent(pipeline, provideCommonIntent(info))
    }

    override fun intentInit(pipeline: Pipeline<U, I, A>) {
        intentCommon(pipeline, CommonIntent.OnInit())
    }

    override fun intentError(pipeline: Pipeline<U, I, A>, error: Throwable) {
        intentCommon(pipeline, CommonIntent.OnError(error))
    }

    override suspend fun action(pipeline: Pipeline<U, I, A>, action: A) {
        pipeline.action(action)
    }

    override suspend fun actionCommon(pipeline: Pipeline<U, I, A>, info: CommonAction) {
        action(pipeline, provideCommonAction(info))
    }

    override suspend fun actionToast(pipeline: Pipeline<U, I, A>, res: Int?) {
        res ?: return

        val toastInfo = ToastInfo.Res(res)

        actionCommon(pipeline, CommonAction.Toast(toastInfo))
    }

    override suspend fun actionBack(pipeline: Pipeline<U, I, A>, res: Int?) {
        actionToast(pipeline, res)

        actionCommon(pipeline, CommonAction.Back())
    }

    fun onLifecycleEvent(event: Lifecycle.Event) {
        val intent = CommonIntent.OnLifecycleEvent(event)

        intent(provideCommonIntent(intent))
    }

    protected suspend fun actionCommon(info: CommonAction) {
        action(provideCommonAction(info))
    }

    protected suspend fun actionToast(res: Int?) {
        res ?: return

        val toastInfo = ToastInfo.Res(res)

        actionCommon(CommonAction.Toast(toastInfo))
    }

    protected suspend fun actionBack(res: Int? = null) {
        actionToast(res)

        actionCommon(CommonAction.Back())
    }

    protected open suspend fun reduceErrorIntent(intent: CommonIntent.OnError) {
        actionCommon(CommonAction.Error(intent.error))
    }

    protected abstract suspend fun reduceInitIntent(intent: CommonIntent.OnInit)

    protected open suspend fun reduceLifecycleEventIntent(info: CommonIntent.OnLifecycleEvent) {
    }

    protected open suspend fun reduceBackPressedIntent(intent: CommonIntent.OnBackPressed) {
        action(provideCommonAction(CommonAction.Back()))
    }
}

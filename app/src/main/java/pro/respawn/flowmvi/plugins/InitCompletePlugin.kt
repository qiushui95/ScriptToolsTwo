package pro.respawn.flowmvi.plugins

import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.PipelineContext
import pro.respawn.flowmvi.dsl.StoreBuilder
import java.util.concurrent.atomic.AtomicBoolean

@FlowMVIDSL
inline fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.whileCanInit(
    crossinline startInitBlock: suspend PipelineContext<S, I, A>.() -> Unit,
) {
    val hasInitAtomic = AtomicBoolean(false)

    whileSubscribed {
        if (hasInitAtomic.compareAndSet(false, true)) {
            startInitBlock()
        }
    }
}

package com.tools.script.two.base.vm

import com.tools.script.two.alias.Pipeline
import pro.respawn.flowmvi.api.MVIIntent

interface VMIntent<U : VMUI, I, A : VMAction, Args> : MVIIntent where I : VMIntent<U, I, A, Args> {
    fun throwOuterInvoke() {
        throw RuntimeException("需要在外部处理该Intent")
    }

    suspend fun VMHandler<U, I, A, Args>.reduce(pipeline: Pipeline<U, I, A>)
}

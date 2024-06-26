package com.tools.script.two.function

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy

fun <T> compositionLocal(
    policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy(),
): ProvidableCompositionLocal<T> = compositionLocalOf(policy) { error("no provide value") }

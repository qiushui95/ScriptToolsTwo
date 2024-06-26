package com.tools.script.two.function

import androidx.compose.runtime.Composable

public typealias ClickBlock = () -> Unit
public typealias ClickBlock1<T> = (T) -> Unit

public typealias OnIntent<T> = (T) -> Unit

public typealias ComposeBlock = @Composable () -> Unit
public typealias ComposeBlock1<T> = @Composable (T) -> Unit

public typealias ComposeBlockR<R> = @Composable () -> R

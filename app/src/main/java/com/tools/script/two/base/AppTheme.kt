package com.tools.script.two.base

import android.content.res.Configuration
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Density
import com.tools.script.two.R
import org.koin.compose.KoinContext
import org.koin.core.Koin
import org.koin.mp.KoinPlatformTools

@Composable
private fun CommonProvider(content: @Composable () -> Unit) {
    val colors = lightColors(primary = colorResource(id = R.color.teal_700))

    MaterialTheme(colors = colors) {
        CompositionLocalProvider(content = content)
    }
}

@Composable
internal fun AppTheme(
    context: Koin = KoinPlatformTools.defaultContext().get(),
    content: @Composable () -> Unit,
) {
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val orientation = LocalConfiguration.current.orientation
    val widthPixels = displayMetrics.widthPixels
    val heightPixels = displayMetrics.heightPixels

    val updateDensity = remember(widthPixels, heightPixels, orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Density(density = heightPixels / 375f, 1f)
        } else {
            Density(density = widthPixels / 375f, 1f)
        }
    }

    KoinContext(context = context) {
        LocalDensityProvider(
            density = updateDensity,
        ) {
            CommonProvider(content)
        }
    }
}

@Composable
internal fun AppThemePreview(
    vararg providedValues: ProvidedValue<*>,
    content: @Composable () -> Unit,
) {

    LocalDensityProvider(
        density = Density(density = 2.88f, 1f),
        *providedValues,
    ) {
        CommonProvider(content)
    }
}

@Composable
internal fun LocalDensityProvider(
    density: Density,
    vararg providedValues: ProvidedValue<*>,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalDensity provides density, *providedValues) {
        content()
    }
}
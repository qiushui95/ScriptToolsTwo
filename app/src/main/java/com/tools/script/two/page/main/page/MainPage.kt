package com.tools.script.two.page.main.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tools.script.two.base.AppThemePreview
import com.tools.script.two.base.vm.CommonIntent
import com.tools.script.two.function.OnIntent
import com.tools.script.two.function.compositionLocal
import com.tools.script.two.page.main.vm.MainUI
import com.tools.script.two.page.main.vm.MainVM
import com.tools.script.two.page.main.vm.intent.MainIntent
import org.koin.androidx.compose.koinViewModel

private typealias OnCommonIntent = MainIntent.OnCommon

@Composable
private fun PreviewPage(uiInfo: MainUI) {
    val uiInfoState = rememberUpdatedState(newValue = uiInfo)

    val pageState = getPageState(
        onIntent = {},
        uiInfoState = uiInfoState,
    )

    AppThemePreview(LocalPageState provides pageState) {
        CPPageContent()
    }
}

private val LocalPageState: ProvidableCompositionLocal<PageState> = compositionLocal()


@Composable
private fun getPageState(
    onIntent: OnIntent<MainIntent>,
    uiInfoState: State<MainUI>,
) = remember { PageState(onIntent = onIntent, uiInfoState = uiInfoState) }


private data class PageState(
    val onIntent: OnIntent<MainIntent>,
    val uiInfoState: State<MainUI>,
) {
    operator fun invoke(intent: MainIntent) {
        onIntent(intent)
    }

    fun onCommonIntent(info: CommonIntent) {
        onIntent(OnCommonIntent(info))
    }
}

@Composable
internal fun MainPage() {
    val viewModel: MainVM = koinViewModel()

    val uiInfoState = viewModel.subscribe()

    val pageState = getPageState(
        onIntent = viewModel::intent,
        uiInfoState = uiInfoState,
    )

    CompositionLocalProvider(LocalPageState provides pageState) {
        CPPageContent()
    }
}

@Composable
private fun CPPageContent() {

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Spacer(modifier = Modifier.weight(1f))
        CPSetCurSystemBtn()
        CPUnsetCurSystemBtn()
        CPSetAutoSystemBtn()
        CPUnsetAutoSystemBtn()
        CPChangeAutoSchemaBtn()
        CPStartAutoBtn()
        CPStopAutoBtn()
        CPRootBtn()
    }
}

@Composable
private fun CPBtn(text: String, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(text = text)
    }
}

@Composable
private fun CPSetCurSystemBtn() {
    val pageState = LocalPageState.current

    val isCurSystemState = remember(pageState) {
        derivedStateOf { pageState.uiInfoState.value.isCurSystemApp }
    }

    if (isCurSystemState.value) return

    CPBtn(text = "设置当前APP为系统APP", onClick = { pageState(MainIntent.OnSetCurSystemClick()) })
}

@Composable
private fun CPUnsetCurSystemBtn() {
    val pageState = LocalPageState.current

    val isCurSystemState = remember(pageState) {
        derivedStateOf { pageState.uiInfoState.value.isCurSystemApp }
    }

    if (isCurSystemState.value.not()) return

    CPBtn(
        text = "取消当前APP为系统APP",
        onClick = { pageState(MainIntent.OnUnsetCurSystemClick()) })
}

@Composable
private fun CPSetAutoSystemBtn() {
    val pageState = LocalPageState.current

    val isAutoSystemAppState = remember(pageState) {
        derivedStateOf { pageState.uiInfoState.value.isAutoSystemApp }
    }

    if (isAutoSystemAppState.value) return

    CPBtn(
        text = "设置自动精灵为系统APP",
        onClick = { pageState(MainIntent.OnSetAutoSystemClick()) })
}

@Composable
private fun CPUnsetAutoSystemBtn() {
    val pageState = LocalPageState.current

    val isAutoSystemAppState = remember(pageState) {
        derivedStateOf { pageState.uiInfoState.value.isAutoSystemApp }
    }

    if (isAutoSystemAppState.value.not()) return

    CPBtn(
        text = "取消自动精灵为系统APP",
        onClick = { pageState(MainIntent.OnUnsetAutoSystemClick()) })
}

@Composable
private fun CPChangeAutoSchemaBtn() {
    val pageState = LocalPageState.current

    val isCurSystemState = remember(pageState) {
        derivedStateOf { pageState.uiInfoState.value.isCurSystemApp }
    }

    if (isCurSystemState.value.not()) return

    val isAutoSystemAppState = remember(pageState) {
        derivedStateOf { pageState.uiInfoState.value.isAutoSystemApp }
    }

    if (isAutoSystemAppState.value.not()) return

    CPBtn(
        text = "更新自动精灵启动Schema",
        onClick = { pageState(MainIntent.OnChangeSchemaClick()) })
}

@Composable
private fun CPRootBtn() {
    val pageState = LocalPageState.current

    CPBtn(
        text = "重启",
        onClick = { pageState(MainIntent.OnRebootClick()) })
}

@Composable
private fun CPStartAutoBtn() {
    val pageState = LocalPageState.current

    val isCurSystemState = remember(pageState) {
        derivedStateOf { pageState.uiInfoState.value.isCurSystemApp }
    }

    if (isCurSystemState.value.not()) return

    val isAutoSystemAppState = remember(pageState) {
        derivedStateOf { pageState.uiInfoState.value.isAutoSystemApp }
    }

    if (isAutoSystemAppState.value.not()) return


    CPBtn(
        text = "启动自动精灵&开启监控",
        onClick = { pageState(MainIntent.OnStartAutoClick()) })
}

@Composable
private fun CPStopAutoBtn() {
    val pageState = LocalPageState.current

    val isCurSystemState = remember(pageState) {
        derivedStateOf { pageState.uiInfoState.value.isCurSystemApp }
    }

    if (isCurSystemState.value.not()) return

    val isAutoSystemAppState = remember(pageState) {
        derivedStateOf { pageState.uiInfoState.value.isAutoSystemApp }
    }

    if (isAutoSystemAppState.value.not()) return


    CPBtn(
        text = "关闭监控",
        onClick = { pageState(MainIntent.OnStopAutoClick()) })
}

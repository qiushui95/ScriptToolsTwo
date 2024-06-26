package com.tools.script.two.services

import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ShellUtils
import com.blankj.utilcode.util.ToastUtils
import com.tools.script.two.utils.PathUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.io.File

class AutoMonitorService : LifecycleService() {

    private val job = SupervisorJob()

    private data class Point(
        val x: String,
        val y: String
    )

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startMonitor()

        super.onStartCommand(intent, flags, startId)

        return START_STICKY
    }

    private fun startMonitor() {
        for (child in job.children) {
            if (child.isActive) return
        }

        lifecycleScope.launch(Dispatchers.IO + job) {
            startSingleMonitor(this)
        }
    }

    private suspend fun startSingleMonitor(scope: CoroutineScope) {

        if (startAutoApp().not()) return

        delay(10 * 1000L)

        while (scope.isActive) {
            val eventPath = PathUtils.getEventPath()

            val eventCmd = "getevent -t -l > $eventPath"

            val startTime = DateTime.now().toString("yyyy-MM-dd HH:mm:ss")

            ShellUtils.execCmd("echo '$startTime' >>/sdcard/auto_monitor.log", true, false)

            val process = Runtime.getRuntime().exec("su")

            process.outputStream.use { output ->
                output.write(eventCmd.toByteArray())
                output.flush()
            }

            delay(30 * 1000L)
            process.destroy()
            process.waitFor()
            handleEvent(eventPath)
        }
    }

    private suspend fun startAutoApp(): Boolean {
        val path = PathUtils.getAutoSchemaPath()

        val file = File(path)

        if (file.exists().not()) {
            ToastUtils.showLong("未设置自动精灵启动schema,监控任务无法启动")
            return false
        }

        val schema = file.readText()
        val startCmd = "am start -d '$schema' -a android.intent.action.VIEW"
        ShellUtils.execCmd(startCmd, false, false)

        delay(10 * 1000L)

        val permissionCmd = "input tap 570 850"
        ShellUtils.execCmd(permissionCmd, false, false)

        return true
    }

    private suspend fun handleEvent(eventPath: String) {
        val file = File(eventPath)

        val lines = file.readLines()

        lines.forEach { println(it) }

        val eventList = mutableSetOf<List<Point>>()

        var lineIndex = 0

        while (lineIndex < lines.size) {

            val (event, data) = getLineData(lines[lineIndex++]) ?: continue

            if (event == "BTN_TOUCH" && data == "DOWN") {
                val list = mutableListOf<Point>()

                var x = ""
                var y = ""

                while (lineIndex < lines.size) {
                    val (event2, data2) = getLineData(lines[lineIndex++]) ?: continue

                    when {
                        event2 == "ABS_MT_POSITION_X" -> x = data2
                        event2 == "ABS_MT_POSITION_Y" -> {
                            y = data2
                            if (x != "" && y != "") {
                                list.add(Point(x, y))
                            }
                            x = ""
                            y = ""
                        }

                        event2 == "BTN_TOUCH" && data2 == "DOWN" -> break

                        event2 == "BTN_TOUCH" && data2 == "UP" -> {
                            if (list.isNotEmpty()) {
                                eventList.add(list)
                            }

                            break
                        }
                    }
                }
            }
        }


        if (eventList.isEmpty()) {
            ToastUtils.showLong("过去30s内未检测到操作,即将重启自动精灵")
            echoErrorLog("未检测到操作")
            delay(5 * 1000L)
            startAutoApp()
        } else if (eventList.size == 1 && lines.size > 4) {
            ToastUtils.showLong("过去30s内只点击了同一个地方,即将重启自动精灵")
            echoErrorLog("只点击了同一个地方")
            delay(5 * 1000L)
            startAutoApp()
        }

        file.delete()

        println(eventList)
    }

    private fun getLineData(line: String): Pair<String, String>? {
        val regex = Regex("""\[.*?] /dev/input/event\d: \w+\s+(\w+)\s+(\w+)""")

        val matchResult = regex.find(line) ?: return null

        val (event, data) = matchResult.destructured

        return event to data
    }

    private fun echoErrorLog(msg: String) {

        val time = DateTime.now().toString("yyyy-MM-dd HH:mm:ss")

        ShellUtils.execCmd("echo '$time:$msg' >>/sdcard/auto_monitor_error.log", true, false)
    }
}
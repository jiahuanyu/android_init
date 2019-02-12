package me.jiahuan.android.init.test

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import me.jiahuan.android.init.Flow
import me.jiahuan.android.init.Init
import me.jiahuan.android.init.Task


class App : Application() {

    companion object {
        private const val TAG = "App"
    }

    override fun onCreate() {
        super.onCreate()
        val flow = Flow()
            .addTask(Task.TaskBuilder().name("task1").beginStage(1).endStage(1).async(true).job({
                Thread.sleep(100)
            }, { true }).build())
            .addTask(Task.TaskBuilder().name("task2").beginStage(1).endStage(1).async(true).job({
                Thread.sleep(100)
            }, { true }).build())
            .addTask(Task.TaskBuilder().name("task3").beginStage(1).endStage(1).async(false).job({
                Thread.sleep(300)
            }, { true }).build())
            .addTask(Task.TaskBuilder().name("task4").beginStage(2).endStage(2).async(false).job({
                Thread.sleep(10)
            }, { true }).build())
            .addTask(Task.TaskBuilder().name("task5").beginStage(2).endStage(2).async(false).job({
                Thread.sleep(10)
            }, { true }).build())
            .addTask(Task.TaskBuilder().name("task6").beginStage(2).endStage(3).async(false).job({
                Thread.sleep(20)
            }, { true }).build())
            .addTask(Task.TaskBuilder().name("task7").beginStage(3).endStage(3).async(true).job({
                Thread.sleep(20)
            }, { true }).build())
        Init.getInstance().initialize(flow)
    }

    fun getCurrentProcessName(context: Context): String {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val infos = manager.runningAppProcesses
        for (processInfo in infos) {
            if (processInfo.pid == android.os.Process.myPid()) {
                return processInfo.processName
            }
        }
        return ""
    }
}
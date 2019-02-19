package me.jiahuan.android.init.test

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import me.jiahuan.android.init.AppInit
import me.jiahuan.android.init.Flow
import me.jiahuan.android.init.Process
import me.jiahuan.android.init.Schedulers
import me.jiahuan.android.init.Task


class App : Application() {

    companion object {
        private const val TAG = "App"
    }

    override fun onCreate() {
        super.onCreate()
        val flow = Flow()
            .addTask(Task.TaskBuilder().name("task1").schedule(Schedulers.MAIN).dependsOn("task4", "task5").job {
                Thread.sleep(100)
            }.build())
            .addTask(Task.TaskBuilder().name("task2").schedule(Schedulers.NEW_THREAD).dependsOn("task5").job {
                Thread.sleep(200)
            }.build())
            .addTask(Task.TaskBuilder().name("task3").schedule(Schedulers.MAIN).dependsOn().job {
                Thread.sleep(300)
            }.build())
            .addTask(Task.TaskBuilder().name("task4").schedule(Schedulers.NEW_THREAD).dependsOn().job {
                Thread.sleep(400)
            }.build())
            .addTask(Task.TaskBuilder().name("task5").schedule(Schedulers.NEW_THREAD).dependsOn().job {
                Thread.sleep(500)
            }.build())
            .addTask(
                Task.TaskBuilder().name("task0").schedule(Schedulers.MAIN).dependsOn(
                    "task1",
                    "task2",
                    "task3"
                ).process(Process.MAIN).job {
                    Thread.sleep(1000)
                }.build()
            )


        AppInit.getInstance().initialize(flow, getCurrentProcessName(this) == "me.jiahuan.android.init")
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
package me.jiahuan.android.init

import android.os.Looper
import android.util.Log
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class Flow : TaskCallback {
    companion object {
        private const val TAG = "Flow"
        // 统一线程池
        private val sThreadPool: ExecutorService = Executors.newCachedThreadPool()
    }

    // 任务map
    val taskMap = HashMap<String, Task>()

    // 起始任务列表
    private val startTaskMap = ArrayList<Task>()

    // 依赖任务map
    private val dependTaskNameMap = HashMap<String, List<String>>()

    // 主线程运行任务
    private val mainThreadRunnable = LinkedList<() -> Unit>()

    // 任务完成
    var doneTaskCountDown = AtomicInteger()

    // 当前是否为主进程
    var isMainProcess = false


    fun addTask(task: Task): Flow {
        // 设置task所属
        task.setOwner(this)
        task.addTaskCallback(this)
        taskMap[task.name] = task
        if (task.dependTasks.isEmpty()) {
            startTaskMap.add(task)
        } else {
            dependTaskNameMap[task.name] = task.dependTasks
        }
        return this
    }

    // 开始流程
    internal fun start(isMainProcess: Boolean) {
        this.isMainProcess = isMainProcess
        val startTaskNameSB = StringBuffer()
        for (startTask in startTaskMap) {
            startTaskNameSB.append("${startTask.name} ")
        }
        Log.d(TAG, "起始任务为 $startTaskNameSB")

        doneTaskCountDown.set(taskMap.filter { it.value.innerOnCreate }.size)
        for ((key, value) in dependTaskNameMap) {
            val sb = StringBuffer()
            val task = taskMap[key] ?: continue
            for (dependTaskName in value) {
                taskMap[dependTaskName]?.addTaskCallback(task)
                sb.append("$dependTaskName ")
            }
            Log.d(TAG, "$key 依赖 $sb")
        }
        for (task in startTaskMap) {
            schedule(task)
        }
    }

    internal fun schedule(task: Task) {
        if ((task.process == Process.MAIN && !isMainProcess) || (task.process == Process.OTHER && isMainProcess)) {
            task.tagTaskEnd()
            return
        }
        if (task.thread == Schedulers.MAIN) {
            Log.d(TAG, "schedule ${task.name} 主线程运行")
            if (Looper.getMainLooper().thread == Thread.currentThread()) {
                task.run()
            } else {
                mainThreadRunnable.add { task.run() }
            }
        } else {
            Log.d(TAG, "schedule ${task.name} 线程池运行")
            if (Looper.getMainLooper().thread != Thread.currentThread()) {
                task.run()
            } else {
                sThreadPool.execute {
                    task.run()
                }
            }
        }
    }

    internal fun scheduleOnMainThread() {
        if (mainThreadRunnable.size > 0) {
            Log.d(TAG, "scheduleOnMainThread")
            mainThreadRunnable.first.invoke()
            mainThreadRunnable.removeFirst()
        }
    }

    override fun onTaskEnd(taskName: String) {
        doneTaskCountDown.decrementAndGet()
    }

}
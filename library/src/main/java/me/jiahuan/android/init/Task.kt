package me.jiahuan.android.init

import android.util.Log
import java.util.concurrent.atomic.AtomicInteger

// 任务
class Task private constructor(
    val name: String,
    val process: Int,
    val thread: Int,
    val dependTasks: ArrayList<String>,
    val job: () -> Unit
) : TaskCallback {

    companion object {
        private const val TAG = "Task"
        // 任务就绪，默认状态
        const val STATUS_READY = 0
        // 任务正在运行
        const val STATUS_RUNNING = 1
        // 任务结束
        const val STATUS_END = 2
    }


    // 属于哪个flow
    private var owner: Flow? = null

    // 当前task状态
    private var currentStatus = AtomicInteger(STATUS_READY)

    // task call back
    private val taskCallbackList = ArrayList<TaskCallback>()


    // 设置owner
    internal fun setOwner(flow: Flow) {
        owner = flow
    }


    // 执行任务
    internal fun run() {
        Log.d(TAG, "$name task start in ${Thread.currentThread().name}")
        currentStatus.set(STATUS_RUNNING)
        job.invoke()
        tagTaskEnd()
    }

    /**
     * 标记任务结束
     */
    internal fun tagTaskEnd() {
        currentStatus.set(STATUS_END)
        Log.d(TAG, "$name task end")
        notifyTaskCallback()
    }

    private fun notifyTaskCallback() {
        for (taskCallback in taskCallbackList) {
            taskCallback.onTaskEnd(name)
        }
    }


    internal fun addTaskCallback(taskCallback: TaskCallback) {
        taskCallbackList.add(taskCallback)
    }

    override fun onTaskEnd(taskName: String) {
        this.owner?.let {
            Log.d(TAG, "$name 检查前序任务是否执行完成")
            var ready = true
            for (taskName in dependTasks) {
                val task = it.taskMap[taskName]
                if (task == null || task.currentStatus.get() != STATUS_END) {
                    ready = false
                }
            }
            if (ready) {
                Log.d(TAG, "$name 前序任务执行完成")
                it.schedule(this)
            }
        }
    }

    class TaskBuilder {
        private var taskName: String = ""
        private var job: () -> Unit = {}
        private var thread = Schedulers.MAIN
        private var process = Process.ALL
        private var dependTaskList = ArrayList<String>()

        // task 名字，保证唯一性
        fun name(name: String): TaskBuilder {
            this.taskName = name
            return this
        }

        // 设置工作内容
        fun job(job: () -> Unit): TaskBuilder {
            this.job = job
            return this
        }

        // 设置工作线程
        fun schedule(thread: Int): TaskBuilder {
            this.thread = thread
            return this
        }

        // 设置允许工作的进程
        fun process(process: Int): TaskBuilder {
            this.process = process
            return this
        }

        // 设置依赖
        fun dependsOn(vararg tasks: String): TaskBuilder {
            this.dependTaskList.addAll(tasks.asList())
            return this
        }

        // 构建
        fun build(): Task {
            return Task(this.taskName, this.process, this.thread, this.dependTaskList, this.job)
        }
    }
}


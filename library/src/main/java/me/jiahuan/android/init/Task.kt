package me.jiahuan.android.init

import android.util.Log
import java.util.concurrent.atomic.AtomicInteger

// 任务
class Task private constructor(
    val name: String,
    val async: Boolean,
    val beginStageId: Int,
    val endStageId: Int,
    val job: () -> Unit,
    val condition: () -> Boolean
) : Runnable {

    companion object {
        private const val TAG = "Task"
        // 任务就绪，默认状态
        const val STATUS_READY = 0
        // 任务正在运行
        const val STATUS_RUNNING = 1
        // 任务结束
        const val STATUS_END = 2
    }

    // 当前task状态
    private var mCurrentStatus = AtomicInteger(STATUS_READY)

    // task call back
    private var mTaskCallback: TaskCallback? = null

    // 执行任务
    override fun run() {
        Log.d(TAG, "$name task start")
        mCurrentStatus.set(STATUS_RUNNING)
        if (condition.invoke()) {
            Log.d(TAG, "$name task doing")
            job.invoke()
        } else {
            Log.d(TAG, "$name task not meet the condition")
        }
        mCurrentStatus.set(STATUS_END)
        Log.d(TAG, "$name task end")
        mTaskCallback?.onEnd()
    }

    fun addTaskCallback(taskCallback: TaskCallback) {
        mTaskCallback = taskCallback
        if (mCurrentStatus.get() == STATUS_END) {
            mTaskCallback?.onEnd()
        }
    }

    interface TaskCallback {
        fun onEnd()
    }

    class TaskBuilder {
        private var mBeginStageId = Stage.STAGE_FIRST_ID
        private var mEndStageId = Stage.STAGE_LAST_ID
        private var mAsync = true // 默认同步
        private var mTaskName: String = ""
        private var mJob: () -> Unit = {}
        private var mCondition: () -> Boolean = { false }
        private var mNeedOnMainThread = false

        fun name(name: String): TaskBuilder {
            mTaskName = name
            return this
        }

        //  起始阶段
        fun beginStage(stageId: Int): TaskBuilder {
            mBeginStageId = stageId
            return this
        }

        // 结束阶段
        fun endStage(stageId: Int): TaskBuilder {
            mEndStageId = stageId
            return this
        }

        // 设置同步异步，默认同步true
        fun async(async: Boolean): TaskBuilder {
            mAsync = async
            return this
        }

        // 设置工作内容
        fun job(job: () -> Unit, condition: () -> Boolean): TaskBuilder {
            mJob = job
            mCondition = condition
            return this
        }

        // 是否需要在主线程运，默认为false
        fun needMainThread(needMainThread: Boolean): TaskBuilder {
            mNeedOnMainThread = needMainThread
            return this
        }

        fun build(): Task {
            return Task(mTaskName, mAsync, mBeginStageId, mEndStageId, mJob, mCondition)
        }
    }
}


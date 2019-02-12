package me.jiahuan.android.init

import android.util.Log
import java.util.concurrent.CountDownLatch

// 一个阶段拥有多个任务
class Stage internal constructor(val id: Int) : Task.TaskCallback {

    companion object {
        private const val TAG = "Stage"
        const val STAGE_FIRST_ID = Int.MIN_VALUE
        const val STAGE_LAST_ID = Int.MAX_VALUE
        // 就绪，默认状态
        const val STATUS_READY = 0
        // 正在运行
        const val STATUS_RUNNING = 1
        // 结束
        const val STATUS_END = 2
    }

    private val mBeginTask by lazy { ArrayList<Task>() }
    private val mEndTask by lazy { ArrayList<Task>() }
    private var mCurrentStatus = STATUS_READY
    private var mDoneSignal: CountDownLatch? = null


    fun addBeginTask(task: Task): Stage {
        mBeginTask.add(task)
        return this
    }

    fun addEndTask(task: Task): Stage {
        mEndTask.add(task)
        return this
    }

    fun addInnerTask(task: Task): Stage {
        mBeginTask.add(task)
        mEndTask.add(task)
        return this
    }

    internal fun start() {
        Log.d(TAG, "$id stage start")
        mCurrentStatus = STATUS_RUNNING

        mDoneSignal = CountDownLatch(mEndTask.size)

        for (endTask in mEndTask) {
            endTask.addTaskCallback(this)
        }

        val asyncTaskList = ArrayList<Task>()

        val threadPool = Init.getInstance().getThreadPool()
        for (beginTask in mBeginTask) {
            if (beginTask.async) {
                asyncTaskList.add(beginTask)
            } else {
                threadPool.execute(beginTask)
            }
        }

        for (asyncTask in asyncTaskList) {
            asyncTask.run()
        }

        mDoneSignal?.await()

        mCurrentStatus = STATUS_END
        Log.d(TAG, "$id stage end")
    }

    override fun onEnd() {
        mDoneSignal?.countDown()
    }
}
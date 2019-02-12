package me.jiahuan.android.init

import android.os.SystemClock
import android.util.Log
import java.util.*
import java.util.concurrent.Callable

// 流程，一般一个进程对应一个流程
// 一个flow拥有多个stage
class Flow {
    companion object {
        private const val TAG = "Flow"
    }

    // stages
    private val mStageMap = TreeMap<Int, Stage>()

    fun addTask(task: Task): Flow {
        val beginStageId = task.beginStageId
        var beginStage = mStageMap[beginStageId]
        if (beginStage == null) {
            beginStage = Stage(beginStageId)
            mStageMap[beginStageId] = beginStage
        }
        beginStage.addBeginTask(task)
        Log.d(TAG, "add task begin stage id = $beginStageId")

        val endStageId = task.endStageId
        var endStage = mStageMap[endStageId]
        if (endStage == null) {
            endStage = Stage(endStageId)
            mStageMap[endStageId] = endStage
        }
        endStage.addEndTask(task)
        Log.d(TAG, "add task end stage id = $endStageId")
        return this
    }

    internal fun start() {
        Log.d(TAG, "flow start")
        val startTime = SystemClock.elapsedRealtime()
        val flowTask = object : Callable<Boolean> {
            override fun call(): Boolean {
                for ((_, stage) in mStageMap) {
                    stage.start()
                }
                return true
            }
        }

        val initTask = Init.getInstance().getThreadPool().submit(flowTask)
        initTask.get()
        Log.d(TAG, "flow end cost time = ${SystemClock.elapsedRealtime() - startTime} ms")
    }
}
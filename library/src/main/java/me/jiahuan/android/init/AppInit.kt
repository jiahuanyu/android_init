package me.jiahuan.android.init

import android.os.SystemClock
import android.util.Log

class AppInit private constructor() {
    companion object {

        @Volatile
        private var sInstance: AppInit? = null

        private const val TAG = "AppInit"

        fun getInstance(): AppInit {
            if (sInstance == null) {
                synchronized(AppInit::class.java) {
                    if (sInstance == null) {
                        sInstance = AppInit()
                    }
                }
            }
            return sInstance!!
        }
    }

    // 初始化
    fun initialize(flow: Flow) {
        val startTime = SystemClock.elapsedRealtime() // 记录起始时间
        flow.start()
        while (flow.doneTaskCountDown.get() != 0) {
            flow.scheduleOnMainThread()
        }
        Log.d(TAG, "初始化任务花费 ${SystemClock.elapsedRealtime() - startTime} ms")
    }
}
package me.jiahuan.android.init

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Init private constructor() {
    companion object {
        private var sInstance: Init? = null

        fun getInstance(): Init {
            if (sInstance == null) {
                synchronized(Init::class.java) {
                    if (sInstance == null) {
                        sInstance = Init()
                    }
                }
            }
            return sInstance!!
        }
    }

    // 统一线程池
    private val mThreadPool: ExecutorService = Executors.newCachedThreadPool()

    internal fun getThreadPool(): ExecutorService {
        return mThreadPool
    }

    // 初始化
    fun initialize(flow: Flow) {
        flow.start()
    }
}
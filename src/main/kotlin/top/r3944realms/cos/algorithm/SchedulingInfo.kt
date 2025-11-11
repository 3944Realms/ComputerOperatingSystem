package top.r3944realms.cos.algorithm

/**
 * 调度相关信息
 */
data class SchedulingInfo(
    // 基本优先级信息
    var staticPriority: PriorityLevel = PriorityLevel.NORMAL,
    var dynamicPriority: Int = 0, // 动态调整的优先级

    // 总共需要时间
    var totalNeedTime: Int = 10,
    var remainingNeedTime: Int = totalNeedTime,

    // 时间片管理
    var timeSlice: Int = 100, // 时间片长度（毫秒）
    var timeSliceRemaining: Int = 0, // 剩余时间片
    var timeSliceExpired: Boolean = false, // 时间片是否耗尽

    // 调度时间统计
    var lastScheduledTime: Long = 0, // 上次调度时间
    var totalCpuTime: Long = 0, // 总CPU使用时间
    var lastCpuBurst: Long = 0, // 上次CPU突发时间
    var averageCpuBurst: Double = 0.0, // 平均CPU突发时间

    // 抢占控制
    var preemptable: Boolean = true, // 是否可抢占
    var needsReschedule: Boolean = false, // 是否需要重新调度

    // 多级反馈队列相关
    var queueLevel: Int = 0, // 在多级反馈队列中的级别
    var timeInQueue: Long = 0, // 在当前队列中的时间

    // 交互性指标
    var sleepTime: Long = 0, // 睡眠时间（用于计算交互性）
    var interactiveScore: Double = 0.0 // 交互性评分
) {
    /**
     * 计算动态优先级（考虑进程行为）
     */
    fun calculateDynamicPriority(): Int {
        val base = staticPriority.value * 10

        // 交互性进程提升优先级
        val interactiveBonus = (interactiveScore * 5).toInt()

        // CPU密集型进程降低优先级
        val cpuPenalty = if (averageCpuBurst > 100) -2 else 0

        // 在低级别队列中等待时间长的进程提升优先级
        val queueBonus = if (queueLevel > 0 && timeInQueue > 1000) 1 else 0

        return (base + interactiveBonus + cpuPenalty + queueBonus).coerceIn(0, 99)
    }
    /**
     * 更新平均CPU突发时间
     */
    fun updateAverageBurst(currentBurst: Long) {
        lastCpuBurst = currentBurst
        averageCpuBurst = if (averageCpuBurst == 0.0) {
            currentBurst.toDouble()
        } else {
            (averageCpuBurst * 0.7) + (currentBurst * 0.3)
        }

        // 根据CPU使用模式调整交互性评分
        interactiveScore = when {
            currentBurst < 10 -> 0.9 // 短突发，可能是交互式进程
            currentBurst > 100 -> 0.1 // 长突发，可能是CPU密集型
            else -> 0.5
        }
    }

    /**
     * 检查是否应该被抢占
     */
    fun shouldPreempt(currentTime: Long, higherPriority: Boolean, isRP: Boolean): Boolean {
        if (!preemptable) return false

        return higherPriority  ||
                (isRP && timeSliceExpired) ||
                (isRP && (currentTime - lastScheduledTime) > timeSlice)
    }
    /**
     * 重置时间片
     */
    fun resetTimeSlice() {
        timeSliceRemaining = timeSlice
        timeSliceExpired = false
        needsReschedule = false
    }

    /**
     * 消耗时间片
     * @param amount 请求使用的时间
     * @return 实际使用的 CPU 时间
     */
    fun consumeTime(amount: Int): Int {
        // 可用执行时间 = 给出时间 与 剩余时间片 的较小者
        val validTime = minOf(amount, timeSliceRemaining)
        return consumeTime0(validTime)
    }

    private fun consumeTime0(validTime: Int): Int {
        // 先扣除所需执行时间
        remainingNeedTime -= validTime
        val usedTime: Int
        if (remainingNeedTime < 0) {
            // 进程已执行完成，有多余的时间
            val extraTime = -remainingNeedTime
            timeSliceRemaining += extraTime     // 把富余的时间片加回去
            val actual = validTime - extraTime
            totalCpuTime += actual
            usedTime = actual                // 用掉的有效 CPU 时间
        } else {
            // 正常执行，未完成
            timeSliceRemaining -= validTime
            totalCpuTime += validTime
            usedTime = validTime
        }

        // 检查时间片是否用完
        if (timeSliceRemaining <= 0) {
            timeSliceRemaining = 0
            timeSliceExpired = true
        }

        return usedTime
    }
    override fun toString(): String {
        return """
        Scheduling Info:
          Priority: $staticPriority (dynamic: $dynamicPriority)
          Time Slice: $timeSliceRemaining/$timeSlice ms (expired: $timeSliceExpired)
          Preemptable: $preemptable, Needs Reschedule: $needsReschedule
          CPU Time: total=${totalCpuTime}ms, last_burst=${lastCpuBurst}ms, avg_burst=${"%.2f".format(averageCpuBurst)}ms
          Queue: level=$queueLevel, time_in_queue=$timeInQueue ms
          Interactive: score=${"%.2f".format(interactiveScore)}
        """.trimIndent()
    }
}

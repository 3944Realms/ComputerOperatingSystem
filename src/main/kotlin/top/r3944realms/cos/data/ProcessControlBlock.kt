package top.r3944realms.cos.data

import top.r3944realms.cos.algorithm.PriorityLevel
import top.r3944realms.cos.algorithm.SchedulingInfo
import top.r3944realms.cos.algorithm.SchedulingPolicy
import top.r3944realms.cos.hardware.CPURegisters
import top.r3944realms.cos.software.Logger

class ProcessControlBlock(
    val id: ProcessIdentification,
    var name: String = "Process-${id.pid}",
    var state: ProcessState,
    var registers: CPURegisters,
    var memoryInfo: MemoryManagementInfo = MemoryManagementInfo(),

    var timeUsed: Long = 0,

    // 内存管理指针
    var pageTablePointer: Long = 0,

    // 内存管理结构
    var pageTable: PageTable? = null,

    var schedulingInfo: SchedulingInfo = SchedulingInfo(),
    var policy: SchedulingPolicy = SchedulingPolicy.RP,
    // ... I/O 会计 省略
    ) {
    class ProcessIdentification(
        val pid: Int,
        val ppid: Int = 0,
        val uid: Int = -1,
        val gid: Int = -1
    )
    fun initializeMemoryLayout(
        textBase: Long = 0x400000,
        textSize: Long = 0x10000,
        dataBase: Long = 0x500000,
        dataSize: Long = 0x8000,
        bssBase: Long = 0x510000,
        bssSize: Long = 0x4000,
        heapBase: Long = 0x600000,
        heapSize: Long = 0x20000,
        stackBase: Long = 0x7FFFFFF0,
        stackSize: Long = 0x10000
    ) {
        memoryInfo = MemoryManagementInfo(
            textSegment = textBase,
            textSegmentSize = textSize,
            dataSegment = dataBase,
            dataSegmentSize = dataSize,
            bssSegment = bssBase,
            bssSegmentSize = bssSize,
            heapStart = heapBase,
            heapEnd = heapBase + heapSize,
            stackStart = stackBase - stackSize,
            stackEnd = stackBase
        )

        registers.stackPointer = stackBase
        registers.basePointer = stackBase
        registers.programCounter = textBase
    }

    fun setupPaging() {
        pageTable = PageTable(id.pid, memoryInfo)
        pageTablePointer = 0x100000
        pageTable!!.setupMemorySegments()
    }

    /**
     * 设置调度参数
     */
    fun setSchedulingParameters(
        priority: PriorityLevel = PriorityLevel.NORMAL,
        totalNeedTime: Int = 100,
        timeSlice: Int = 100,
        preemptable: Boolean = true,
        policy: SchedulingPolicy = SchedulingPolicy.RP
    ) {
        this.policy = policy
        schedulingInfo.totalNeedTime = totalNeedTime
        schedulingInfo.remainingNeedTime = totalNeedTime
        schedulingInfo.staticPriority = priority
        schedulingInfo.timeSlice = timeSlice
        schedulingInfo.preemptable = preemptable
        schedulingInfo.resetTimeSlice()
        schedulingInfo.dynamicPriority = schedulingInfo.calculateDynamicPriority()

        Logger.info("Process $name scheduling: priority=$priority, timeSlice=${timeSlice}ms, preemptable=$preemptable, policy=$policy")
    }

    /**
     * 更新调度统计信息
     * @param executionTime 执行时间（预期）
     * @return
     */
    fun updateSchedulingStats(currentTime: Long, executionTime: Long): Int {
        schedulingInfo.lastScheduledTime = currentTime
        val consumeTime = schedulingInfo.consumeTime(executionTime.toInt())
        schedulingInfo.updateAverageBurst(executionTime)
        schedulingInfo.dynamicPriority = schedulingInfo.calculateDynamicPriority()

        timeUsed += executionTime
        return consumeTime
    }

    /**
     * 标记为需要重新调度
     */
    fun markForReschedule() {
        schedulingInfo.needsReschedule = true
        Logger.info("Process $name marked for reschedule")
    }

    /**
     * 提升优先级（用于避免饥饿）
     */
    fun boostPriority() {
        if (schedulingInfo.queueLevel > 0) {
            schedulingInfo.queueLevel--
            schedulingInfo.timeInQueue = 0
            schedulingInfo.dynamicPriority = schedulingInfo.calculateDynamicPriority()
            Logger.info("Process $name priority boosted to queue level ${schedulingInfo.queueLevel}")
        }
    }

    /**
     * 降低优先级（用于多级反馈队列）
     */
    fun demotePriority() {
        schedulingInfo.queueLevel++
        schedulingInfo.timeInQueue = 0
        schedulingInfo.timeSlice = (schedulingInfo.timeSlice * 1.5).toInt() // 低优先级进程获得更长的时间片
        schedulingInfo.dynamicPriority = schedulingInfo.calculateDynamicPriority()
        Logger.info("Process $name demoted to queue level ${schedulingInfo.queueLevel}")
    }

    fun saveRegisters(currentRegisters: CPURegisters) {
        registers = currentRegisters.copy()
    }

    fun loadRegisters(): CPURegisters {
        return registers.copy()
    }

    override fun toString(): String {
        return """
        PCB [PID=${id.pid}, Name=$name, State=$state, Policy=$policy]
        $registers
        $schedulingInfo
        Memory Usage: ${memoryInfo.getTotalMemoryUsage()} bytes
        """.trimIndent()
    }

}
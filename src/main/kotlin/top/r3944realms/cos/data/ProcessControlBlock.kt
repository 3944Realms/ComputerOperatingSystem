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

    // 银行家算法资源管理
    var resourceInfo: ResourceManagementInfo = ResourceManagementInfo(),

    // ... I/O 会计 省略
    ) {
    class ProcessIdentification(
        val pid: Int,
        val ppid: Int = 0,
        val uid: Int = -1,
        val gid: Int = -1
    )
    // 银行家算法资源管理信息
    data class ResourceManagementInfo(
        // 最大需求矩阵 - 进程对每种资源类型的最大需求
        val maxDemand: MutableMap<String, Int> = mutableMapOf(),

        // 已分配资源 - 当前已分配给进程的资源
        val allocation: MutableMap<String, Int> = mutableMapOf(),

        // 需求矩阵 - 还需要多少资源 (need = maxDemand - allocation)
        val need: MutableMap<String, Int> = mutableMapOf(),

        // 资源请求历史
        val requestHistory: MutableList<ResourceRequest> = mutableListOf(),

        // 资源持有时间统计
        val resourceHoldTime: MutableMap<String, Long> = mutableMapOf(),

        // 死锁检测标记
        var deadlockDetected: Boolean = false,

        // 资源等待信息
        var waitingForResources: MutableSet<String> = mutableSetOf(),

        // 安全状态标记
        var inSafeState: Boolean = true
    ) {
        data class ResourceRequest(
            val resourceType: String,
            val amount: Int,
            val timestamp: Long,
            val granted: Boolean = false,
            val completed: Boolean = false
        )

        /**
         * 初始化资源需求
         */
        fun initializeResources(
            resourceTypes: List<String>,
            maxDemands: Map<String, Int>,
            initialAllocations: Map<String, Int> = emptyMap()
        ) {
            maxDemand.clear()
            allocation.clear()
            need.clear()
            resourceHoldTime.clear()

            resourceTypes.forEach { resourceType ->
                val max = maxDemands[resourceType] ?: 0
                val alloc = initialAllocations[resourceType] ?: 0

                maxDemand[resourceType] = max
                allocation[resourceType] = alloc
                need[resourceType] = max - alloc
                resourceHoldTime[resourceType] = 0
            }
        }

        /**
         * 请求资源
         */
        fun requestResource(resourceType: String, amount: Int, timestamp: Long): ResourceRequest {
            val request = ResourceRequest(resourceType, amount, timestamp)
            requestHistory.add(request)
            waitingForResources.add(resourceType)
            return request
        }

        /**
         * 授予资源请求
         */
        fun grantResource(resourceType: String, amount: Int) {
            allocation[resourceType] = (allocation[resourceType] ?: 0) + amount
            need[resourceType] = (need[resourceType] ?: 0) - amount
            waitingForResources.remove(resourceType)

            // 标记最近的请求为已授予
            requestHistory.lastOrNull { it.resourceType == resourceType && !it.granted }?.let { request ->
                requestHistory.remove(request)
                requestHistory.add(request.copy(granted = true))
            }
        }

        /**
         * 释放资源
         */
        fun releaseResource(resourceType: String, amount: Int): Int {
            val currentAllocation = allocation[resourceType] ?: 0
            val releaseAmount = minOf(amount, currentAllocation)

            allocation[resourceType] = currentAllocation - releaseAmount
            // 注意：释放资源后，maxDemand不变，但need会增加
            need[resourceType] = (maxDemand[resourceType] ?: 0) - (allocation[resourceType] ?: 0)

            // 标记相关的请求为已完成
            requestHistory.filter { it.resourceType == resourceType && it.granted && !it.completed }
                .forEach { request ->
                    requestHistory.remove(request)
                    requestHistory.add(request.copy(completed = true))
                }

            return releaseAmount
        }

        /**
         * 检查资源需求是否满足（need <= work）
         */
        fun canBeSatisfied(availableResources: Map<String, Int>): Boolean {
            return need.all { (resourceType, needAmount) ->
                val available = availableResources[resourceType] ?: 0
                needAmount <= available
            }
        }

        /**
         * 检查是否所有资源需求都已满足
         */
        fun isFinished(): Boolean {
            return need.all { (_, needAmount) -> needAmount == 0 }
        }

        /**
         * 获取总分配资源数量
         */
        fun getTotalAllocated(): Int {
            return allocation.values.sum()
        }

        /**
         * 获取资源使用统计
         */
        fun getResourceStats(): Map<String, Any> {
            return mapOf(
                "max_demand" to maxDemand,
                "allocation" to allocation,
                "need" to need,
                "waiting_for" to waitingForResources.toList(),
                "total_allocated" to getTotalAllocated(),
                "deadlock_detected" to deadlockDetected,
                "in_safe_state" to inSafeState
            )
        }

        override fun toString(): String {
            return """
            Resource Management Info:
              Max Demand: $maxDemand
              Allocation: $allocation
              Need: $need
              Waiting For: $waitingForResources
              Deadlock Detected: $deadlockDetected
              In Safe State: $inSafeState
              Total Requests: ${requestHistory.size}
            """.trimIndent()
        }
    }
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
     * 设置银行家算法资源参数
     */
    fun setResourceParameters(
        resourceTypes: List<String>,
        maxDemands: Map<String, Int>,
        initialAllocations: Map<String, Int> = emptyMap()
    ) {
        resourceInfo.initializeResources(resourceTypes, maxDemands, initialAllocations)
        Logger.info("Process $name resources: maxDemand=$maxDemands, allocation=$initialAllocations")
    }

    /**
     * 请求资源（银行家算法）
     */
    fun requestResource(resourceType: String, amount: Int, timestamp: Long): ResourceManagementInfo.ResourceRequest {
        Logger.info("Process $name requesting $amount of $resourceType")
        return resourceInfo.requestResource(resourceType, amount, timestamp)
    }

    /**
     * 授予资源
     */
    fun grantResource(resourceType: String, amount: Int) {
        resourceInfo.grantResource(resourceType, amount)
        Logger.info("Process $name granted $amount of $resourceType")
    }

    /**
     * 释放资源
     */
    fun releaseResource(resourceType: String, amount: Int): Int {
        val released = resourceInfo.releaseResource(resourceType, amount)
        Logger.info("Process $name released $released of $resourceType")
        return released
    }

    /**
     * 检查是否所有资源需求都已满足
     */
    fun hasResourceSatisfied(): Boolean {
        return resourceInfo.isFinished()
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

        // 更新资源持有时间
        resourceInfo.allocation.forEach { (resourceType, amount) ->
            if (amount > 0) {
                val currentHoldTime = resourceInfo.resourceHoldTime[resourceType] ?: 0
                resourceInfo.resourceHoldTime[resourceType] = currentHoldTime + consumeTime
            }
        }
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
        $resourceInfo
        Memory Usage: ${memoryInfo.getTotalMemoryUsage()} bytes
        """.trimIndent()
    }

}
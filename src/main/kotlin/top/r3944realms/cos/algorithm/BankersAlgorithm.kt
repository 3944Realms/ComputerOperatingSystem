package top.r3944realms.cos.algorithm

import top.r3944realms.cos.data.ProcessControlBlock
import top.r3944realms.cos.software.Logger

class BankersAlgorithm {
    private var available: MutableMap<String, Int> = mutableMapOf()
    private val processes: MutableList<ProcessControlBlock> = mutableListOf()
    private val resourceTypes: MutableList<String> = mutableListOf()

    /**
     * 初始化银行家算法系统
     */
    fun initializeSystem(
        initialResources: Map<String, Int>,
        processList: List<ProcessControlBlock>
    ) {
        available.clear()
        available.putAll(initialResources)
        processes.clear()
        processes.addAll(processList)
        resourceTypes.clear()
        resourceTypes.addAll(initialResources.keys)

        Logger.info("=== Banker's Algorithm System Initialization ===")
        Logger.info("Available resources: $available")
        Logger.info("Resource types: $resourceTypes")
        Logger.info("Process count: ${processes.size}")
    }

    /**
     * 安全检查算法
     */
    fun isSafeState(): Boolean {
        val work = available.toMutableMap()
        val finish = BooleanArray(processes.size) { false }
        val safeSequence = mutableListOf<ProcessControlBlock>()

        var found: Boolean
        do {
            found = false
            for (i in processes.indices) {
                if (!finish[i] && canSatisfy(processes[i], work)) {
                    // 分配资源给进程 i
                    processes[i].resourceInfo.allocation.forEach { (resourceType, amount) ->
                        work[resourceType] = (work[resourceType] ?: 0) + amount
                    }
                    safeSequence.add(processes[i])
                    finish[i] = true
                    found = true
                    processes[i].resourceInfo.inSafeState = true
                }
            }
        } while (found)

        // 检查是否所有进程都完成
        val isSafe = finish.all { it }

        if (isSafe) {
            Logger.info("System is in a safe state!")
            Logger.info("Safe sequence: ")
            safeSequence.forEachIndexed { index, process ->
                Logger.info(process.name)
                if (index < safeSequence.size - 1) Logger.info(" -> ")
            }
            Logger.info("")
        } else {
            Logger.info("System is in an unsafe state!")
            // 标记不安全的进程
            processes.forEachIndexed { index, process ->
                if (!finish[index]) {
                    process.resourceInfo.inSafeState = false
                }
            }
        }

        return isSafe
    }

    /**
     * 检查工作向量是否能满足进程需求
     */
    private fun canSatisfy(process: ProcessControlBlock, work: Map<String, Int>): Boolean {
        return process.resourceInfo.need.all { (resourceType, needAmount) ->
            val availableAmount = work[resourceType] ?: 0
            needAmount <= availableAmount
        }
    }

    /**
     * 资源请求算法
     */
    fun requestResources(process: ProcessControlBlock, requests: Map<String, Int>): Boolean {
        Logger.info("\n=== ${process.name} requesting resources: $requests ===")

        // 检查请求是否超过需求
        for ((resourceType, amount) in requests) {
            val currentNeed = process.resourceInfo.need[resourceType] ?: 0
            if (amount > currentNeed) {
                Logger.info("Error: ${process.name} requested more $resourceType than its maximum need! (requested: $amount, need: $currentNeed)")
                return false
            }
        }

        // 检查请求是否超过可用资源
        for ((resourceType, amount) in requests) {
            val currentAvailable = available[resourceType] ?: 0
            if (amount > currentAvailable) {
                Logger.info("Error: Requested $resourceType exceeds system available resources! (requested: $amount, available: $currentAvailable)")
                process.resourceInfo.waitingForResources.addAll(requests.keys)
                return false
            }
        }

        // 尝试分配资源
        Logger.info("\nAttempting to allocate resources to ${process.name}...")

        // 保存状态以便回滚
        val oldAvailable = available.toMutableMap()
        val oldAllocation = process.resourceInfo.allocation.toMutableMap()
        val oldNeed = process.resourceInfo.need.toMutableMap()

        // 模拟分配
        for ((resourceType, amount) in requests) {
            available[resourceType] = (available[resourceType] ?: 0) - amount
            process.resourceInfo.allocation[resourceType] = (process.resourceInfo.allocation[resourceType] ?: 0) + amount
            process.resourceInfo.need[resourceType] = (process.resourceInfo.need[resourceType] ?: 0) - amount
        }

        process.resourceInfo.waitingForResources.clear()

        Logger.info("State after allocation:")
        printState()

        // 进行安全检查
        if (isSafeState()) {
            Logger.info("Resource allocation successful! System remains in safe state.")

            // 标记请求为已授予
            requests.forEach { (resourceType, amount) ->
                process.resourceInfo.grantResource(resourceType, amount)
            }

            return true
        } else {
            Logger.info("Resource allocation would lead to unsafe state, allocation rejected!")

            // 回滚分配
            available = oldAvailable
            process.resourceInfo.allocation.clear()
            process.resourceInfo.allocation.putAll(oldAllocation)
            process.resourceInfo.need.clear()
            process.resourceInfo.need.putAll(oldNeed)
            process.resourceInfo.waitingForResources.addAll(requests.keys)

            Logger.info("Restored to state before allocation.")
            return false
        }
    }

    /**
     * 释放资源
     */
    fun releaseResources(process: ProcessControlBlock, releases: Map<String, Int>): Boolean {
        Logger.info("\n=== ${process.name} releasing resources: $releases ===")

        // 检查释放是否超过分配
        for ((resourceType, amount) in releases) {
            val currentAllocation = process.resourceInfo.allocation[resourceType] ?: 0
            if (amount > currentAllocation) {
                Logger.info("Error: ${process.name} trying to release more $resourceType than allocated! (release: $amount, allocated: $currentAllocation)")
                return false
            }
        }

        // 执行释放
        for ((resourceType, amount) in releases) {
            val releasedAmount = process.resourceInfo.releaseResource(resourceType, amount)
            available[resourceType] = (available[resourceType] ?: 0) + releasedAmount
        }

        Logger.info("Resources released successfully.")
        printState()

        // 检查系统是否进入安全状态
        isSafeState()

        return true
    }

    /**
     * 添加新进程到系统
     */
    fun addProcess(process: ProcessControlBlock): Boolean {
        // 检查进程的资源需求是否超过系统总资源
        val totalResources = getTotalResources()
        process.resourceInfo.maxDemand.forEach { (resourceType, maxDemand) ->
            val totalResource = totalResources[resourceType] ?: 0
            if (maxDemand > totalResource) {
                Logger.info("Error: Process ${process.name} max demand for $resourceType exceeds system total! (demand: $maxDemand, total: $totalResource)")
                return false
            }
        }

        processes.add(process)
        Logger.info("Process ${process.name} added to system.")

        // 检查系统是否仍然安全
        return isSafeState()
    }

    /**
     * 移除进程并释放其资源
     */
    fun removeProcess(processId: Int): Boolean {
        val process = processes.find { it.id.pid == processId }
        if (process == null) {
            Logger.info("Error: Process with ID $processId not found!")
            return false
        }

        // 释放进程持有的所有资源
        val allocations = process.resourceInfo.allocation.toMap()
        releaseResources(process, allocations)

        processes.remove(process)
        Logger.info("Process ${process.name} removed from system.")

        return true
    }

    /**
     * 获取系统总资源（可用 + 已分配）
     */
    fun getTotalResources(): Map<String, Int> {
        val total = available.toMutableMap()

        processes.forEach { process ->
            process.resourceInfo.allocation.forEach { (resourceType, amount) ->
                total[resourceType] = (total[resourceType] ?: 0) + amount
            }
        }

        return total
    }

    /**
     * 显示当前系统状态
     */
    fun printState() {
        Logger.info("\n=== Current Banker's Algorithm System State ===")

        Logger.info("Available resources:")
        Logger.info("Available: $available")

        Logger.info("\nProcess Resource Status:")
        processes.forEach { process ->
            Logger.info("${process.name}:")
            Logger.info("  Max Demand: ${process.resourceInfo.maxDemand}")
            Logger.info("  Allocation: ${process.resourceInfo.allocation}")
            Logger.info("  Need: ${process.resourceInfo.need}")
            Logger.info("  Waiting For: ${process.resourceInfo.waitingForResources}")
            Logger.info("  Safe State: ${process.resourceInfo.inSafeState}")
            Logger.info("  Deadlock Detected: ${process.resourceInfo.deadlockDetected}")
        }

        Logger.info("\nSystem Summary:")
        Logger.info("Total Processes: ${processes.size}")
        Logger.info("Resource Types: $resourceTypes")
        Logger.info("Total Resources: ${getTotalResources()}")
    }

    /**
     * 检测死锁
     */
    fun detectDeadlock(): List<ProcessControlBlock> {
        val work = available.toMutableMap()
        val finish = processes.map { it.resourceInfo.isFinished() }.toBooleanArray()
        val deadlockedProcesses = mutableListOf<ProcessControlBlock>()

        var changed: Boolean
        do {
            changed = false
            for (i in processes.indices) {
                if (!finish[i] && canSatisfy(processes[i], work)) {
                    // 进程可以完成，释放其资源
                    processes[i].resourceInfo.allocation.forEach { (resourceType, amount) ->
                        work[resourceType] = (work[resourceType] ?: 0) + amount
                    }
                    finish[i] = true
                    processes[i].resourceInfo.deadlockDetected = false
                    changed = true
                }
            }
        } while (changed)

        // 标记死锁的进程
        processes.forEachIndexed { index, process ->
            if (!finish[index]) {
                process.resourceInfo.deadlockDetected = true
                deadlockedProcesses.add(process)
                Logger.info("Deadlock detected for process: ${process.name}")
            }
        }

        if (deadlockedProcesses.isEmpty()) {
            Logger.info("No deadlock detected.")
        } else {
            Logger.info("Deadlock detected involving processes: ${deadlockedProcesses.map { it.name }}")
        }

        return deadlockedProcesses
    }

    /**
     * 获取所有进程
     */
    fun getProcesses(): List<ProcessControlBlock> {
        return processes.toList()
    }

    /**
     * 获取可用资源
     */
    fun getAvailableResources(): Map<String, Int> {
        return available.toMap()
    }

    /**
     * 获取资源类型
     */
    fun getResourceTypes(): List<String> {
        return resourceTypes.toList()
    }
}
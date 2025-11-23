package top.r3944realms.cos.software

import top.r3944realms.cos.algorithm.BankersAlgorithm
import top.r3944realms.cos.algorithm.PriorityLevel
import top.r3944realms.cos.algorithm.SchedulingPolicy
import top.r3944realms.cos.config.YAMLConfigLoader
import top.r3944realms.cos.data.ProcessControlBlock
import top.r3944realms.cos.data.ProcessState
import top.r3944realms.cos.hardware.CPURegisters
import top.r3944realms.cos.model.BAConfigModel
import java.io.File

object BASystem {
    private var banker: BankersAlgorithm? = null
    private var processMap: Map<Int, ProcessControlBlock> = emptyMap()
    private var config: BAConfigModel.CompleteConfig? = null
    private var currentRound: Int = 0
    private var systemTime: Long = 0

    /**
     * 从 YAML 配置文件加载并初始化银行家算法系统
     */
    fun initialize(configPath: String = "bankers_system.yaml") {
        Logger.info("=== Banker's Algorithm System Initialization ===")

        // 加载配置
        config = if (File(configPath).exists()) {
            YAMLConfigLoader.loadConfig(configPath, BAConfigModel.CompleteConfig::class.java)
        } else {
            // 从资源目录加载
            YAMLConfigLoader.loadConfigFromResource(configPath, BAConfigModel.CompleteConfig::class.java)
        }

        banker = BankersAlgorithm()

        // 创建进程
        processMap = createProcessesFromConfig(config!!)

        // 初始化银行家算法系统
        banker!!.initializeSystem(
            initialResources = config!!.resources.available,
            processList = processMap.values.toList()
        )

        Logger.info("Banker's Algorithm System initialized with ${processMap.size} processes")
        Logger.info("Resource types: ${config!!.resources.types}")
        Logger.info("Available resources: ${config!!.resources.available}")
        Logger.info("Total simulation rounds: ${config!!.simulation.totalRounds}")
    }

    fun reset() {
        config = null
        banker = null
        processMap = emptyMap()
        currentRound = 0
        systemTime = 0
    }

    fun currentTimeMillis(): Long = systemTime

    fun currentRound(): Int = currentRound

    /**
     * 运行银行家算法模拟
     */
    fun runSimulation() {
        val currentConfig = config ?: throw IllegalStateException("System not initialized. Call initialize() first.")

        Logger.info("\n=== Starting Banker's Algorithm Simulation ===")

        repeat(currentConfig.simulation.totalRounds) { round ->
            currentRound = round + 1
            systemTime += currentConfig.simulation.timeSpeed

            Logger.info("\n*** Simulation Round $currentRound ***")

            // 打印当前系统状态
            banker!!.printState()

            // 处理该回合的事件
            processEvents(currentRound, currentConfig)

            // 执行死锁检测（如果启用）
            if (currentConfig.simulation.enableDeadlockDetection) {
                val deadlockedProcesses = banker!!.detectDeadlock()
                if (deadlockedProcesses.isNotEmpty()) {
                    Logger.warn("*** DEADLOCK DETECTED in round $currentRound ***")
                    Logger.warn("Deadlocked processes: ${deadlockedProcesses.map { it.name }}")
                }
            }

            // 安全检查（如果启用）
            if (currentConfig.simulation.enableSafetyCheck) {
                val isSafe = banker!!.isSafeState()
                if (!isSafe) {
                    Logger.warn("*** SYSTEM IN UNSAFE STATE in round $currentRound ***")
                }
            }

            // 模拟时间推进
            Thread.sleep(currentConfig.simulation.timeSpeed)
        }

        Logger.info("\n=== Banker's Algorithm Simulation Completed ===")
    }

    /**
     * 手动请求资源
     */
    fun requestResources(processId: Int, requests: Map<String, Int>): Boolean {
        val process = processMap[processId]
        return if (process != null) {
            banker!!.requestResources(process, requests)
        } else {
            Logger.error("Process with ID $processId not found!")
            false
        }
    }

    /**
     * 手动释放资源
     */
    fun releaseResources(processId: Int, releases: Map<String, Int>): Boolean {
        val process = processMap[processId]
        return if (process != null) {
            banker!!.releaseResources(process, releases)
        } else {
            Logger.error("Process with ID $processId not found!")
            false
        }
    }

    /**
     * 添加新进程到系统
     */
    fun addProcess(processConfig: BAConfigModel.ProcessConfig): Boolean {
        val process = createProcessFromConfig(processConfig)
        processMap = processMap + (processConfig.id to process)
        return banker!!.addProcess(process)
    }

    /**
     * 移除进程
     */
    fun removeProcess(processId: Int): Boolean {
        val success = banker!!.removeProcess(processId)
        if (success) {
            processMap = processMap - processId
        }
        return success
    }

    /**
     * 获取系统统计信息
     */
    fun printStatistics() {
        Logger.info("\n=== Final Banker's Algorithm Statistics ===")

        val totalResources = banker!!.getTotalResources()
        Logger.info("Total System Resources: $totalResources")
        Logger.info("Available Resources: ${banker!!.getAvailableResources()}")

        processMap.values.forEach { process ->
            Logger.info("\n${process.name}:")
            Logger.info("  Resource Allocation: ${process.resourceInfo.allocation}")
            Logger.info("  Max Demand: ${process.resourceInfo.maxDemand}")
            Logger.info("  Current Need: ${process.resourceInfo.need}")
            Logger.info("  Waiting For: ${process.resourceInfo.waitingForResources}")
            Logger.info("  Safe State: ${process.resourceInfo.inSafeState}")
            Logger.info("  Deadlock Detected: ${process.resourceInfo.deadlockDetected}")
            Logger.info("  Total Requests: ${process.resourceInfo.requestHistory.size}")
            Logger.info("  Resource Hold Time: ${process.resourceInfo.resourceHoldTime}")
        }

        // 安全检查结果
        val isSafe = banker!!.isSafeState()
        Logger.info("\nSystem Safety Status: ${if (isSafe) "SAFE" else "UNSAFE"}")
    }

    /**
     * 获取系统状态快照
     */
    fun getSystemState(): BAConfigModel.SystemStateSnapshot {
        val processes = banker!!.getProcesses()
        val processStates = processes.map { process ->
            BAConfigModel.ProcessStateSnapshot(
                id = process.id.pid,
                name = process.name,
                state = process.state.name,
                allocation = process.resourceInfo.allocation,
                maxDemand = process.resourceInfo.maxDemand,
                need = process.resourceInfo.need,
                waitingFor = process.resourceInfo.waitingForResources.toList(),
                finished = process.resourceInfo.isFinished()
            )
        }

        val isSafe = banker!!.isSafeState()

        return BAConfigModel.SystemStateSnapshot(
            round = currentRound,
            available = banker!!.getAvailableResources(),
            processStates = processStates,
            isSafe = isSafe,
            safeSequence = if (isSafe) processes.map { it.id.pid } else null,
            deadlockDetected = processes.any { it.resourceInfo.deadlockDetected }
        )
    }

    /**
     * 获取特定进程的信息
     */
    fun getProcessInfo(processId: Int): ProcessControlBlock? {
        return processMap[processId]
    }

    /**
     * 获取所有进程列表
     */
    fun getAllProcesses(): List<ProcessControlBlock> {
        return processMap.values.toList()
    }

    /**
     * 获取银行家算法实例（用于高级操作）
     */
    fun getBanker(): BankersAlgorithm? {
        return banker
    }

    private fun createProcessesFromConfig(config: BAConfigModel.CompleteConfig): Map<Int, ProcessControlBlock> {
        return config.processes.associate { processConfig ->
            processConfig.id to createProcessFromConfig(processConfig)
        }
    }

    private fun createProcessFromConfig(processConfig: BAConfigModel.ProcessConfig): ProcessControlBlock {
        return ProcessControlBlock(
            id = ProcessControlBlock.ProcessIdentification(processConfig.id),
            name = processConfig.name,
            state = ProcessState.valueOf(processConfig.state),
            registers = CPURegisters()
        ).apply {
            initializeMemoryLayout()

            // 设置调度参数
            setSchedulingParameters(
                totalNeedTime = processConfig.scheduling.totalNeedTime,
                timeSlice = processConfig.scheduling.timeSlice,
                priority = PriorityLevel.valueOf(processConfig.scheduling.priority),
                preemptable = processConfig.scheduling.preemptable,
                policy = SchedulingPolicy.valueOf(processConfig.scheduling.policy)
            )

            // 设置资源参数
            setResourceParameters(
                resourceTypes = config!!.resources.types,
                maxDemands = processConfig.resources.maxDemand,
                initialAllocations = processConfig.resources.allocation
            )
        }
    }

    private fun processEvents(round: Int, config: BAConfigModel.CompleteConfig) {
        config.simulation.events.filter { it.round == round }.forEach { event ->
            when (event.action) {
                "REQUEST" -> {
                    val process = processMap[event.processId]
                    if (process != null && event.resources != null) {
                        val success = banker!!.requestResources(process, event.resources)
                        Logger.info(">>> Event: ${if (success) "Granted" else "Denied"} resource request for ${process.name}: ${event.resources}")
                    } else {
                        Logger.error(">>> Event: Invalid resource request event - process ${event.processId} not found or no resources specified")
                    }
                }
                "RELEASE" -> {
                    val process = processMap[event.processId]
                    if (process != null && event.resources != null) {
                        val success = banker!!.releaseResources(process, event.resources)
                        Logger.info(">>> Event: Resource release for ${process.name}: ${event.resources} - ${if (success) "Success" else "Failed"}")
                    } else {
                        Logger.error(">>> Event: Invalid resource release event - process ${event.processId} not found or no resources specified")
                    }
                }
                "ADD_PROCESS" -> {
                    event.processConfig?.let { processConfig ->
                        val success = addProcess(processConfig)
                        Logger.info(">>> Event: Add process ${processConfig.name} - ${if (success) "Success" else "Failed"}")
                    }
                }
                "REMOVE_PROCESS" -> {
                    if (event.processId != null) {
                        val success = removeProcess(event.processId)
                        Logger.info(">>> Event: Remove process ${event.processId} - ${if (success) "Success" else "Failed"}")
                    }
                }
                else -> {
                    Logger.warn(">>> Event: Unknown action '${event.action}' for process ${event.processId} at round $round")
                }
            }
        }
    }

    /**
     * 执行安全检查并返回结果
     */
    fun performSafetyCheck(): BAConfigModel.BankersResult {
        val isSafe = banker!!.isSafeState()
        val processes = banker!!.getProcesses()

        return BAConfigModel.BankersResult(
            isSafe = isSafe,
            safeSequence = if (isSafe) processes.map { it.id.pid } else null,
            availableAfter = banker!!.getAvailableResources(),
            allocationAfter = processes.associate { it.id.pid to it.resourceInfo.allocation },
            needAfter = processes.associate { it.id.pid to it.resourceInfo.need },
            message = if (isSafe) "System is in a safe state" else "System is in an unsafe state"
        )
    }

//    /**
//     * 执行死锁检测并返回结果
//     */
//    fun performDeadlockDetection(): BAConfigModel.DeadlockDetectionResult {
//        val deadlockedProcesses = banker!!.detectDeadlock()
//
//        // 构建资源等待图 -- 有点复杂，不予实现
//        val resourceWaitGraph = mutableMapOf<Int, List<Int>>()
//        banker!!.getProcesses().forEach { process ->
//            if (process.resourceInfo.waitingForResources.isNotEmpty()) {
//                resourceWaitGraph[process.id.pid] = emptyList()
//            }
//        }
//
//        return BAConfigModel.DeadlockDetectionResult(
//            deadlockDetected = deadlockedProcesses.isNotEmpty(),
//            deadlockedProcesses = deadlockedProcesses.map { it.id.pid },
//            availableResources = banker!!.getAvailableResources(),
//            resourceWaitGraph = resourceWaitGraph
//        )
//    }
}
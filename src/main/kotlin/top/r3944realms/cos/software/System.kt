package top.r3944realms.cos.software

import top.r3944realms.cos.algorithm.AdvancedProcessScheduler
import top.r3944realms.cos.algorithm.PriorityLevel
import top.r3944realms.cos.algorithm.SchedulingPolicy
import top.r3944realms.cos.config.YAMLConfigLoader
import top.r3944realms.cos.data.ProcessControlBlock
import top.r3944realms.cos.data.ProcessState
import top.r3944realms.cos.hardware.CPURegisters
import top.r3944realms.cos.model.CompleteConfig
import java.io.File

object System {
    private var scheduler: AdvancedProcessScheduler? = null
    private var processMap: Map<Int, ProcessControlBlock> = emptyMap()
    private var config: CompleteConfig? = null
    private var savePath: String? = null

    /**
     * 从 YAML 配置文件加载并初始化系统
     */
    fun initialize(configPath: String = "system.yaml") {
        Logger.info("=== COS System Initialization ===")

        // 加载配置
        config = if (File(configPath).exists()) {
            YAMLConfigLoader.loadConfig(configPath, CompleteConfig::class.java)
        } else {
            // 从资源目录加载
            YAMLConfigLoader.loadConfigFromResource(configPath, CompleteConfig::class.java)
        }
        scheduler = AdvancedProcessScheduler(config!!.simulation.timeSpeed)

        // 创建进程
        processMap = createProcessesFromConfig(config!!)

        Logger.info("System initialized with ${processMap.size} processes")
        Logger.info("Total simulation rounds: ${config!!.simulation.totalRounds}")
    }
    fun reset() {
        config = null
        scheduler = null
        processMap = emptyMap()
        savePath = null
    }

    fun currentTimeMillis(): Long? = scheduler?.systemTime()

    /**
     * 运行调度模拟
     */
    fun runSimulation() {
        val currentConfig = config ?: throw IllegalStateException("System not initialized. Call initialize() first.")

        Logger.info("\n=== Starting Simulation ===")

        repeat(currentConfig.simulation.totalRounds) { round ->
            Logger.info("\n*** Scheduling Round ${round + 1} ***")
            scheduler?.printSchedulerStatus()
            scheduler?.schedule()

            // 处理该回合的事件
            processEvents(round, currentConfig)
        }

        Logger.info("\n=== Simulation Completed ===")
    }

    /**
     * 获取系统统计信息
     */
    fun printStatistics() {
        Logger.info("\n=== Final Process Statistics ===")
        processMap.values.forEach { process ->
            Logger.info("${process.name}:")
            Logger.info("  Total CPU Time: ${process.timeUsed}ms")
            Logger.info("  Total Wait Time: ${process.schedulingInfo.timeInQueue}ms")
            Logger.info("  Average Burst: ${"%.2f".format(process.schedulingInfo.averageCpuBurst)}ms")
            Logger.info("  Final Priority: ${process.schedulingInfo.dynamicPriority}")
            Logger.info("  Queue Level: ${process.schedulingInfo.queueLevel}")
        }
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


    private fun createProcessesFromConfig(config: CompleteConfig): Map<Int, ProcessControlBlock> {
        return config.processes.associate { processConfig ->
            val pcb = ProcessControlBlock(
                id = ProcessControlBlock.ProcessIdentification(processConfig.id),
                name = processConfig.name,
                state = ProcessState.valueOf(processConfig.state),
                registers = CPURegisters()
            ).apply {
                initializeMemoryLayout()
                setSchedulingParameters(
                    totalNeedTime = processConfig.scheduling.totalNeedTime,
                    timeSlice = processConfig.scheduling.timeSlice,
                    priority = PriorityLevel.valueOf(processConfig.scheduling.priority),
                    preemptable = processConfig.scheduling.preemptable,
                    policy = SchedulingPolicy.valueOf(processConfig.scheduling.policy)
                )
            }
            processConfig.id to pcb
        }
    }

    private fun processEvents(round: Int, config: CompleteConfig) {
        config.simulation.events.filter { it.round == round }.forEach { event ->
            when (event.action) {
                "ADD_PROCESS" -> {
                    val process = processMap[event.processId]
                    process?.let {
                        scheduler?.addProcess(it)
                        Logger.info(">>> Event: Added process ${it.name} at round $round")
                    }
                }
                "TERMINATE_PROCESS" -> {
                    Logger.info(">>> Event: Terminate process ${event.processId} at round $round")
                    //TODO:? scheduler.terminateProcess(event.processId)
                }
                "CHANGE_PRIORITY" -> {
                    Logger.info(">>> Event: Change priority of process ${event.processId} at round $round")
                    //TODO:? scheduler.changePriority(event.processId, PriorityLevel.valueOf(event.newPriority!!))
                }
                else -> {
                    Logger.info(">>> Event: ${event.action} for process ${event.processId} at round $round")
                }
            }
        }
    }
}
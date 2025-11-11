package top.r3944realms.cos.algorithm

import top.r3944realms.cos.algorithm.SchedulingPolicy.*
import top.r3944realms.cos.data.ProcessControlBlock
import top.r3944realms.cos.data.ProcessState
import top.r3944realms.cos.hardware.CPURegisters
import top.r3944realms.cos.software.Logger
import java.util.LinkedList

/**
 * 增强的进程调度器 - 支持多种调度算法
 */
class AdvancedProcessScheduler(var timeSpeed: Long = 10) {
    private val readyQueues = Array(5) { LinkedList<ProcessControlBlock>() } // 多级队列
    private var currentProcess: ProcessControlBlock? = null
    private var lastProcess: ProcessControlBlock? = null
    private var currentRegisters: CPURegisters? = null
    private var systemTime: Long = 0
    fun systemTime(): Long = systemTime
    /**
     * 添加进程到合适的就绪队列
     */
    fun addProcess(process: ProcessControlBlock) {
        val queueLevel = process.schedulingInfo.queueLevel.coerceIn(0, readyQueues.size - 1)
        readyQueues[queueLevel].add(process)
        process.state = ProcessState.ACTIVE_READY
        Logger.info("Added process ${process.name} to ready queue level $queueLevel")
    }

    /**
     * 执行调度决策
     */
    fun schedule(timeSpeed: Long = this.timeSpeed): ProcessControlBlock? {

//        if(remainedSchedule) {
//            // 只在一次系统刻中更新一次 会导致队列实际等待时间会有所偏差 -> 应该执行后更新队列等待时间
//            updateQueueTimes(timeSpeed)
//        }
        //检查进程抢占可否, 进程是否执行完
        val nextProcess = selectNextProcess()
        // 无下一进程或下一进程有且非目前进程
        if (nextProcess == null || nextProcess != currentProcess || (nextProcess.policy == RP && nextProcess.schedulingInfo.timeSliceExpired)) {
            contextSwitch(nextProcess)
        }
        //若目前进程
        currentProcess?.let { process ->
            if (process.state != ProcessState.TERMINATED) {
                // 模拟进程执行
                simulateProcessExecution(process)
                val actualUseTime = process.updateSchedulingStats(systemTime, timeSpeed)
                systemTime += actualUseTime

                // 检查是否需要重新调度
                if (process.schedulingInfo.timeSliceExpired || process.schedulingInfo.needsReschedule) {
                    handleTimeSliceExpired(process)
                }
                // 执行后更新
                updateQueueTimes(actualUseTime.toLong())
            } else {
                Logger.info("Terminate process ${process.name}")
                currentProcess = null
                currentRegisters = null
            }
        }
         if (currentProcess == null) {
             systemTime += timeSpeed
             updateQueueTimes(timeSpeed)
         }

        return currentProcess
    }

    /**
     * 进程选择逻辑
     */
    private fun selectNextProcess(): ProcessControlBlock? {
        // 首先检查是否有进程需要被抢占
        currentProcess?.let { current ->
            if (current.schedulingInfo.shouldPreempt(systemTime, hasHigherPriorityProcess(current), findNextAlgorithm() == RP)) {
                Logger.info("Preempting ${current.name} due to higher priority process")
                current.markForReschedule() //标记为重新调度
                return findHighestPriorityProcess()
            } else if (current.schedulingInfo.remainingNeedTime > 0) {
                if (current.schedulingInfo.timeSliceExpired) {
                    if (findNextAlgorithm() != RP) {
                        return currentProcess
                    }
                    else {
                        current.markForReschedule()
                        return findHighestPriorityProcess()
                    }
                }
                return currentProcess
            } else {
                current.state = ProcessState.TERMINATED //设置为销毁
            }
        }
        return findHighestPriorityProcess()
    }

    private fun findNextAlgorithm() : SchedulingPolicy = if(readyQueues.isNotEmpty() && readyQueues.first().isNotEmpty()) readyQueues.first().first().policy else FCFS
    /**
     * 查找最高优先级的进程
     */
    private fun findHighestPriorityProcess(): ProcessControlBlock? {
        for (queue in readyQueues) {
            if (queue.isNotEmpty()) {
                return when (queue.first().policy) {
                    FCFS -> {
                        // FCFS: 选择队列中第一个进程
                        queue.first()
                    }
                    RP -> {
                        // RR: 轮转选择，将当前进程移到队列末尾
                        currentProcess?.let { current ->
                            if (current in queue) {
                                queue.remove(current)
                                queue.add(current)
                            }
                        }
                        queue.first()  // 问题：没有从队列中移除选中的进程
                    }
                    PR -> {
                        // 优先级: 选择动态优先级最高的进程（数值最小的）
                        queue.minByOrNull { it.schedulingInfo.dynamicPriority } ?: queue.first()
                    }
                    SJF -> {
                        queue.minByOrNull { it.schedulingInfo.remainingNeedTime } ?: queue.first()
                    }
                    else -> queue.first()
                }
            }
        }
        return null
    }

    /**
     * 检查是否有更高优先级的进程
     */
    private fun hasHigherPriorityProcess(current: ProcessControlBlock): Boolean {
        val currentPriority = current.schedulingInfo.dynamicPriority

        for (queue in readyQueues) {
            for (process in queue) {
                if (process.schedulingInfo.dynamicPriority < currentPriority) {
                    return true
                }
            }
        }
        return false
    }
    //上下文切换时 检查当前线程是否执行完
    private fun contextSwitch(nextProcess: ProcessControlBlock?) {
        val current = currentProcess
        Logger.info("\n=== Context Switch at time $systemTime ===")

        current?.let {
            if (it.state != ProcessState.TERMINATED) {
                it.state = ProcessState.ACTIVE_READY
                currentRegisters?.let { it1 -> it.saveRegisters(it1) }
                // 只有时间片耗尽或明确标记重新调度时才放回队列
                if ((findNextAlgorithm() == RP && it.schedulingInfo.timeSliceExpired) || it.schedulingInfo.needsReschedule) {
                    addProcess(it)
                    Logger.info("Saved context of ${it.name} and returned to ready queue")
                }
                else {
                    Logger.info("Saved context of ${it.name}")
                }
            }
        }
        nextProcess?.let {
            lastProcess = currentProcess
            currentProcess = it
            // 从就绪队列中移除即将运行的进程
            removeFromReadyQueues(it)
            it.state = ProcessState.RUNNING
            currentRegisters = it.loadRegisters()
            it.schedulingInfo.resetTimeSlice()

            Logger.info("Loaded context of ${it.name}")
            Logger.info("Switched from ${current?.name ?: "IDLE"} to ${it.name}")
        }
        Logger.info("=== Context Switch End ===")

    }

    /**
     * 从所有就绪队列中移除进程
     */
    private fun removeFromReadyQueues(process: ProcessControlBlock) {
        readyQueues.forEach { queue ->
            queue.remove(process)
        }
    }

    private fun handleTimeSliceExpired(process: ProcessControlBlock) {
        Logger.info("Time slice expired for process ${process.name}")

        when (process.policy) {
            MF ->   {
                process.demotePriority()
            }
            RP -> {}
            else -> {
                process.schedulingInfo.resetTimeSlice()
            }
        }
        process.markForReschedule()
    }

    private fun updateQueueTimes(timeSpeed: Long) {
        readyQueues.forEachIndexed { level, queue ->
            queue.forEach { process ->
                process.schedulingInfo.timeInQueue += timeSpeed

                // 防止饥饿：在低优先级队列中等待时间过长则提升优先级
                if (level > 0 && process.schedulingInfo.timeInQueue > 2000) {
                    Logger.info("Anti-starvation: boosting ${process.name} from queue level $level")
                    process.boostPriority()
                    // 移动到更高优先级队列
                    queue.remove(process)
                    addProcess(process)
                }
            }
        }
    }

    private fun simulateProcessExecution(process: ProcessControlBlock) {
        Logger.info("Executing process: ${process.name}")
        currentRegisters?.let{
            it.rax += 1
            it.rbx = it.rax * 2
            it.programCounter += 4
            it.stackPointer -= 8
            it.flags.updateAfterCompare(it.rax, 10)
        }

    }

    fun printSchedulerStatus() {
        Logger.info("\n=== Scheduler Status at time $systemTime ===")
        var totalProcesses = 0
        readyQueues.forEachIndexed { level, queue ->
            Logger.info("Queue Level $level (${queue.size} processes):")
            queue.forEachIndexed { index, process ->
                Logger.info("  ${index + 1}. ${process.name} - DynPrio: ${process.schedulingInfo.dynamicPriority}, " +
                        "Static: ${process.schedulingInfo.staticPriority}, " +
                        "Wait: ${process.schedulingInfo.timeInQueue}ms")
            }
            totalProcesses += queue.size
        }
        Logger.info("Total ready processes: $totalProcesses")
        lastProcess?.let {
            Logger.info("****")
            Logger.info("Last Process: ${it.name}")
            Logger.info("Remained Need Time: ${it.schedulingInfo.remainingNeedTime}ms")
            Logger.info("****")
        } ?: Logger.info("Last Process: IDLE")
        currentProcess?.let {
            Logger.info("####")
            Logger.info("Current Process: ${it.name}")
            Logger.info("Remained Need Time: ${it.schedulingInfo.remainingNeedTime}ms")
            Logger.info("Time Slice Remaining: ${it.schedulingInfo.timeSliceRemaining}ms")
            Logger.info("####")
        } ?: Logger.info("Current Process: IDLE")
    }
}
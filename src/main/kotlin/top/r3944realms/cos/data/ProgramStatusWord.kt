package top.r3944realms.cos.data

import top.r3944realms.cos.software.Logger

/**
 * 程序状态字 - 控制运行环境
 */
data class ProgramStatusWord(
    var interruptEnabled: Boolean = true,    // 中断使能
    var supervisorMode: Boolean = false,     // 特权模式
    var trapFlag: Boolean = false,           // 单步执行
    var directionFlag: Boolean = false       // 字符串操作方向
) {
    fun copy() : ProgramStatusWord {
        return ProgramStatusWord(interruptEnabled, supervisorMode, trapFlag, directionFlag)
    }
    fun enableInterrupts() {
        interruptEnabled = true
        Logger.debug("Interrupts enabled")
    }

    fun disableInterrupts() {
        interruptEnabled = false
        Logger.debug("Interrupts disabled")
    }

    fun enterSupervisorMode() {
        supervisorMode = true
        Logger.debug("Entered supervisor mode")
    }

    fun enterUserMode() {
        supervisorMode = false
        Logger.debug("Entered user mode")
    }

    override fun toString(): String {
        return "PSW: IE=$interruptEnabled Supervisor=$supervisorMode Trap=$trapFlag Direction=$directionFlag"
    }
}

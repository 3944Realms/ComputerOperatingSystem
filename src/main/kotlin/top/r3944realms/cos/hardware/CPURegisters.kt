package top.r3944realms.cos.hardware

import top.r3944realms.cos.data.ProgramStatusWord

/**
 * CPU 寄存器组
 */
data class CPURegisters(
    //  通用
    var rax: Long = 0,  // 累加器
    var rbx: Long = 0,  // 基地寄存器
    var rcx: Long = 0,  // 计数器
    var rdx: Long = 0,  // 数据寄存器

    // 堆栈
    var stackPointer: Long = 0, // 堆栈指针
    var basePointer: Long = 0,  // 基址指针

    // 程序计数器
    var programCounter: Long = 0,

    // 标志寄存器
    var flags: FlagRegister = FlagRegister(),

    // 程序状态字
    var programStatusWord: ProgramStatusWord = ProgramStatusWord()
) {
    fun copy(): CPURegisters {
        return CPURegisters(
            rax, rbx, rcx, rdx,
            stackPointer, basePointer,
            programCounter,
            flags.copy(),
            programStatusWord.copy()
        )
    }
    override fun toString(): String {
        return """
        CPU Registers:
          General: RAX=0x${rax.toString(16)} RBX=0x${rbx.toString(16)} 
                   RCX=0x${rcx.toString(16)} RDX=0x${rdx.toString(16)}
          Stack:   SP=0x${stackPointer.toString(16)} BP=0x${basePointer.toString(16)}
          Control: PC=0x${programCounter.toString(16)}
          $flags
          $programStatusWord
        """.trimIndent()
    }
}

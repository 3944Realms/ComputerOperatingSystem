package top.r3944realms.cos.hardware

/**
 * 标志寄存器 - 存储条件判断结果
 */
data class FlagRegister(
    var zeroFlag: Boolean = false,      // ZF - 零标志
    var carryFlag: Boolean = false,     // CF - 进位标志
    var signFlag: Boolean = false,      // SF - 符号标志
    var overflowFlag: Boolean = false,  // OF - 溢出标志
    var parityFlag: Boolean = false     // PF - 奇偶标志
) {
    fun copy(): FlagRegister {
        return FlagRegister(zeroFlag, carryFlag, signFlag, overflowFlag, parityFlag)
    }

    fun updateAfterCompare(a: Long, b: Long) {
        val result = a - b
        zeroFlag = (result == 0L)
        signFlag = (result < 0)
        carryFlag = (a < b)
    }

    fun updateAfterAdd(a: Long, b: Long, result: Long) {
        zeroFlag = (result == 0L)
        signFlag = (result < 0)
        // 简化的溢出检测
        overflowFlag = (a > 0 && b > 0 && result < 0) ||
                (a < 0 && b < 0 && result > 0)
    }

    override fun toString(): String {
        return "Flags: ZF=$zeroFlag CF=$carryFlag SF=$signFlag OF=$overflowFlag PF=$parityFlag"
    }
}

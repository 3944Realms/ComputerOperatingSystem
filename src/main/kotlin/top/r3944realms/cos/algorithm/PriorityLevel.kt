package top.r3944realms.cos.algorithm

/**
 * 优先级级别
 */
enum class PriorityLevel(val value: Int, val description: String) {
    REAL_TIME(0, "实时优先级"),
    HIGH(1, "高优先级"),
    NORMAL(2, "普通优先级"),
    LOW(3, "低优先级"),
    IDLE(4, "空闲优先级");

    companion object {
        fun fromValue(value: Int): PriorityLevel {
            return entries.find { it.value == value } ?: NORMAL
        }
    }
}
package top.r3944realms.cos.data

/**
 * 页表项 - 包含段类型信息
 */
data class PageTableEntry(
    val virtualPage: Int,
    var physicalFrame: Int = -1,
    var present: Boolean = false,
    var readable: Boolean = false,
    var writable: Boolean = false,
    var executable: Boolean = false,
    var accessed: Boolean = false,
    var dirty: Boolean = false,
    var userMode: Boolean = false,

    var segmentType: SegmentType = SegmentType.UNKNOWN
) {
    enum class SegmentType {
        TEXT,    // 代码段
        DATA,    // 数据段
        BSS,     // BSS段
        HEAP,    // 堆段
        STACK,   // 栈段
        UNKNOWN  // 未知段
    }

    override fun toString(): String {
        return "PTE[VP=$virtualPage -> PF=$physicalFrame, Type=$segmentType, P=$present, R=$readable, W=$writable, X=$executable]"
    }
}
package top.r3944realms.cos.data

/**
 * 进程内存管理信息 - 包含完整的内存段布局
 */
data class MemoryManagementInfo(
    // 代码段地址
    var textSegment: Long = 0,
    var textSegmentSize: Long = 0,

    // 数据段 - 用来存放程序中已初始化的全局变量的内存区域
    var dataSegment: Long = 0,
    var dataSegmentSize: Long = 0,

    // BSS 段 - 用来存放程序中未初始化的全局变量的内存区域
    var bssSegment: Long = 0,
    var bssSegmentSize: Long = 0,

    // 堆内存区域
    var heapStart: Long = 0,
    var heapEnd: Long = 0,

    // 栈内存区域
    var stackStart: Long = 0,
    var stackEnd: Long = 0,

    // 内存保护标志
    var textReadOnly: Boolean = true,
    var dataReadOnly: Boolean = false,
    var bssReadOnly: Boolean = false
) {
    fun getTotalMemoryUsage(): Long {
        return textSegmentSize + dataSegmentSize + bssSegmentSize +
                (heapEnd - heapStart) + (stackEnd - stackStart)
    }

    fun isValidTextAddress(address: Long): Boolean {
        return address in textSegment until (textSegment + textSegmentSize)
    }

    fun isValidDataAddress(address: Long): Boolean {
        return address in dataSegment until (dataSegment + dataSegmentSize)
    }

    fun isValidBssAddress(address: Long): Boolean {
        return address in bssSegment until (bssSegment + bssSegmentSize)
    }

    fun isValidHeapAddress(address: Long): Boolean {
        return address in heapStart until heapEnd
    }

    fun isValidStackAddress(address: Long): Boolean {
        return address in stackStart until stackEnd
    }

    override fun toString(): String {
        return """
        Memory Layout:
          Text:   0x${textSegment.toString(16)}-0x${(textSegment + textSegmentSize).toString(16)} (${textSegmentSize} bytes, ${if (textReadOnly) "RO" else "RW"})
          Data:   0x${dataSegment.toString(16)}-0x${(dataSegment + dataSegmentSize).toString(16)} (${dataSegmentSize} bytes)
          BSS:    0x${bssSegment.toString(16)}-0x${(bssSegment + bssSegmentSize).toString(16)} (${bssSegmentSize} bytes)
          Heap:   0x${heapStart.toString(16)}-0x${heapEnd.toString(16)} (${heapEnd - heapStart} bytes)
          Stack:  0x${stackStart.toString(16)}-0x${stackEnd.toString(16)} (${stackEnd - stackStart} bytes)
          Total:  ${getTotalMemoryUsage()} bytes
        """.trimIndent()
    }
}
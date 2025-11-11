package top.r3944realms.cos.data

import top.r3944realms.cos.software.Logger

class PageTable(val processId: Int, val memoryInfo: MemoryManagementInfo, pageCount: Int = 1024) {
    val entries = Array(pageCount) { PageTableEntry(it) }
    var basePhysicalAddress: Long = 0

    /**
     * 根据内存段信息自动设置页表权限
     */
    fun setupMemorySegments() {
        // 映射代码段
        mapSegment(
            startAddress = memoryInfo.textSegment,
            size = memoryInfo.textSegmentSize,
            permissions = if (memoryInfo.textReadOnly) "rx" else "rwx",
            segmentType = PageTableEntry.SegmentType.TEXT
        )

        // 映射数据段
        mapSegment(
            startAddress = memoryInfo.dataSegment,
            size = memoryInfo.dataSegmentSize,
            permissions = "rw",
            segmentType = PageTableEntry.SegmentType.DATA
        )

        // 映射BSS段
        mapSegment(
            startAddress = memoryInfo.bssSegment,
            size = memoryInfo.bssSegmentSize,
            permissions = "rw",
            segmentType = PageTableEntry.SegmentType.BSS
        )

        // 映射堆段
        mapSegment(
            startAddress = memoryInfo.heapStart,
            size = memoryInfo.heapEnd - memoryInfo.heapStart,
            permissions = "rw",
            segmentType = PageTableEntry.SegmentType.HEAP
        )

        // 映射栈段
        mapSegment(
            startAddress = memoryInfo.stackStart,
            size = memoryInfo.stackEnd - memoryInfo.stackStart,
            permissions = "rw",
            segmentType = PageTableEntry.SegmentType.STACK
        )
    }

    private fun mapSegment(startAddress: Long, size: Long, permissions: String, segmentType: PageTableEntry.SegmentType) {
        if (size <= 0) return

        val pageSize = 4096
        val startPage = (startAddress / pageSize).toInt()
        val endPage = ((startAddress + size - 1) / pageSize).toInt()

        for (page in startPage..endPage) {
            if (page in entries.indices) {
                // 模拟分配物理帧（实际系统中需要物理内存管理）
                val physicalFrame = page + 0x100 // 简单映射
                mapPage(page, physicalFrame, permissions, segmentType)
            }
        }

        Logger.debug("Mapped $segmentType segment: 0x${startAddress.toString(16)}-0x${(startAddress + size).toString(16)}")
    }

    fun mapPage(virtualPage: Int, physicalFrame: Int, permissions: String = "rw", segmentType: PageTableEntry.SegmentType = PageTableEntry.SegmentType.UNKNOWN) {
        if (virtualPage in entries.indices) {
            entries[virtualPage].apply {
                this.physicalFrame = physicalFrame
                this.segmentType = segmentType
                present = true
                readable = 'r' in permissions
                writable = 'w' in permissions
                executable = 'x' in permissions
            }
        }
    }

    /**
     * 地址转换，包含段类型检查
     */
    fun translateAddress(virtualAddress: Long, accessType: String = "read"): Long? {
        val pageSize = 4096
        val virtualPage = (virtualAddress / pageSize).toInt()
        val offset = virtualAddress % pageSize

        if (virtualPage in entries.indices) {
            val entry = entries[virtualPage]
            if (entry.present) {
                // 权限检查
                when (accessType) {
                    "read" -> if (!entry.readable) {
                        Logger.debug("Read violation at VA:0x${virtualAddress.toString(16)} in ${entry.segmentType}")
                        return null
                    }
                    "write" -> if (!entry.writable) {
                        Logger.debug("Write violation at VA:0x${virtualAddress.toString(16)} in ${entry.segmentType}")
                        return null
                    }
                    "execute" -> if (!entry.executable) {
                        Logger.debug("Execute violation at VA:0x${virtualAddress.toString(16)} in ${entry.segmentType}")
                        return null
                    }
                }

                entry.accessed = true
                if (accessType == "write") entry.dirty = true

                val physicalAddress = entry.physicalFrame.toLong() * pageSize + offset
                Logger.debug("Translated VA:0x${virtualAddress.toString(16)} -> PA:0x${physicalAddress.toString(16)} (${entry.segmentType})")
                return physicalAddress
            }
        }
        Logger.debug("Page fault at VA:0x${virtualAddress.toString(16)}")
        return null
    }

    fun printPageTable() {
        Logger.debug("\n=== Page Table for Process $processId ===")
        Logger.debug(memoryInfo.toString())
        entries.take(20).forEach { if (it.present) Logger.debug(it.toString()) }
        val presentCount = entries.count { it.present }
        Logger.debug("Total: $presentCount pages mapped / ${entries.size} total")
    }
}
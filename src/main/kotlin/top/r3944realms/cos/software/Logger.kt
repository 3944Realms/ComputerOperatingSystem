package top.r3944realms.cos.software

import java.io.File
import java.io.PrintWriter
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Logger {
    private var logToFile: Boolean = false
    private var logFile: File? = null
    private var printWriter: PrintWriter? = null
    private var logLevel: LogLevel = LogLevel.INFO

    enum class LogLevel(val level: Int) {
        DEBUG(0),
        INFO(1),
        WARN(2),
        ERROR(3)
    }

    /**
     * 初始化日志系统
     * @param logToFile 是否写入文件
     * @param filePath 日志文件路径，如果为null则使用默认路径
     * @param level 日志级别
     */
    fun initialize(
        logToFile: Boolean = false,
        filePath: String? = null,
        level: LogLevel = LogLevel.INFO
    ) {
        Logger.logToFile = logToFile
        logLevel = level

        if (logToFile) {
            try {
                val logFile = if (filePath != null) {
                    File(filePath)
                } else {
                    // 默认日志文件：logs/cos_yyyy-MM-dd_HH-mm-ss.log
                    val logsDir = File("logs")
                    if (!logsDir.exists()) logsDir.mkdirs()

                    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
                    File(logsDir, "cos_$timestamp.log")
                }

                // 确保父目录存在
                logFile.parentFile?.mkdirs()

                Logger.logFile = logFile
                printWriter = PrintWriter(FileWriter(logFile, true))

                info("Logger initialized. Log file: ${logFile.absolutePath}")
            } catch (e: Exception) {
                java.lang.System.err.println("Failed to initialize file logger: ${e.message}")
                Logger.logToFile = false
            }
        }
    }

    /**
     * 写入一行 DEBUG 级别日志
     */
    fun debug(message: String) {
        log(LogLevel.DEBUG, message)
    }

    /**
     * 写入一行 INFO 级别日志
     */
    fun info(message: String) {
        log(LogLevel.INFO, message)
    }

    /**
     * 写入一行 WARN 级别日志
     */
    fun warn(message: String) {
        log(LogLevel.WARN, message)
    }

    /**
     * 写入一行 ERROR 级别日志
     */
    fun error(message: String) {
        log(LogLevel.ERROR, message)
    }

    /**
     * 写入一行 ERROR 级别日志（带异常）
     */
    fun error(message: String, exception: Exception) {
        log(LogLevel.ERROR, "$message - ${exception.message}")
        exception.printStackTrace()

        if (logToFile) {
            printWriter?.apply {
                exception.printStackTrace(this)
                flush()
            }
        }
    }

    /**
     * 写入格式化日志
     */
    fun infof(format: String, vararg args: Any) {
        info(format.format(*args))
    }

    fun debugf(format: String, vararg args: Any) {
        debug(format.format(*args))
    }

    fun warnf(format: String, vararg args: Any) {
        warn(format.format(*args))
    }

    fun errorf(format: String, vararg args: Any) {
        error(format.format(*args))
    }

    /**
     * 写入分隔线
     */
    fun separator(char: Char = '=', length: Int = 50) {
        val separator = char.toString().repeat(length)
        info(separator)
    }

    /**
     * 写入带标题的分隔线
     */
    fun separator(title: String, char: Char = '=', length: Int = 50) {
        val titleWithPadding = " $title "
        val totalLength = maxOf(length, titleWithPadding.length + 4) // 确保最小长度
        val availableLength = totalLength - titleWithPadding.length
        val sideLength = availableLength / 2

        val separator = if (availableLength >= 2) {
            // 正常情况：标题两边都有分隔符
            "${char.toString().repeat(sideLength)}$titleWithPadding${char.toString().repeat(sideLength)}"
        } else {
            // 标题太长，直接显示标题
            titleWithPadding.trim()
        }

        // 确保分隔线长度一致
        val finalSeparator = if (separator.length < totalLength) {
            separator + char.toString().repeat(totalLength - separator.length)
        } else {
            separator
        }

        info(finalSeparator)
    }

    /**
     * 获取当前日志文件路径
     */
    fun getLogFilePath(): String? {
        return logFile?.absolutePath
    }

    /**
     * 获取当前日志级别
     */
    fun getLogLevel(): LogLevel {
        return logLevel
    }

    /**
     * 检查是否启用了文件日志
     */
    fun isFileLoggingEnabled(): Boolean {
        return logToFile
    }

    /**
     * 刷新日志缓冲区
     */
    fun flush() {
        printWriter?.flush()
    }

    /**
     * 关闭日志系统
     */
    fun close() {
        info("Logger shutting down...")
        printWriter?.close()
        printWriter = null
        logToFile = false
    }

    // 私有方法
    private fun log(level: LogLevel, message: String) {
        if (level.level < logLevel.level) return

        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val threadName = Thread.currentThread().name
        val logMessage = message

        // 输出到控制台
        when (level) {
            LogLevel.ERROR -> java.lang.System.err.println(logMessage)
            LogLevel.WARN -> java.lang.System.err.println(logMessage)
            else -> println(logMessage)
        }

        // 输出到文件
        if (logToFile) {
            printWriter?.apply {
                println(logMessage)
                flush()
            }
        }
    }
}
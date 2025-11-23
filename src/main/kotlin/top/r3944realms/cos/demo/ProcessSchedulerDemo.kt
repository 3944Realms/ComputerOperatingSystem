package top.r3944realms.cos.demo

import top.r3944realms.cos.software.Logger
import top.r3944realms.cos.software.PSSystem
import java.io.File

object ProcessSchedulerDemo {
    fun run() {
        println("=== COS Process Scheduler Demo ===")
        runInteractiveDemo()
    }

    /**
     * 交互式演示
     */
    private fun runInteractiveDemo() {
        println("\n=== Interactive Mode ===")

        while (true) {
            println("\nOptions:")
            println("1. Run default simulation")
            println("2. Run FSFC,SJF,RP,PR simulations")
            println("3. Run with custom config")
            println("4. Show process details")
            println("5. Reset system")
            println("6. Exit")
            print("Choose an option (1-5): ")

            when (readlnOrNull()?.trim()) {
                "1" -> {
                    // 使用默认配置，日志文件名为 default_simulation.log
                    runSimulationWithConfig("ps/ps_system.yaml", "default_simulation")
                }

                "2" -> {
                    runBatchSimulations()
                }

                "3" -> {
                    print("Enter config file path: ")
                    val configPath = readlnOrNull()?.trim() ?: "config.yaml"
                    runCustomConfigSimulation(configPath)
                }

                "4" -> {
                    showProcessDetails()
                }

                "5" -> {
                    PSSystem.reset()
                    Logger.close()
                    println("System reset complete")
                }

                "6" -> {
                    println("Exiting...")
                    return
                }

                else -> println("Invalid option")
            }
        }
    }

    /**
     * 运行自定义配置的模拟
     */
    private fun runCustomConfigSimulation(configPath: String) {
        try {
            // 从配置文件名提取日志文件名
            val configFile = File(configPath)
            val logFileName = if (configFile.exists()) {
                // 去掉扩展名作为日志文件名
                val baseName = configFile.nameWithoutExtension
                "${baseName}_simulation.log"
            } else {
                // 如果是资源文件，使用配置路径作为基础名
                val baseName = configPath.substringAfterLast('/').substringBeforeLast('.')
                if (baseName == configPath) "custom_simulation.log" else "${baseName}_simulation.log"
            }

            runSimulationWithConfig(configPath, logFileName)

        } catch (e: Exception) {
            println("Error: ${e.message}")
            Logger.error("Failed to run custom config: ${e.message}")
        }
    }

    /**
     * 使用指定配置运行模拟
     */
    private fun runSimulationWithConfig(configPath: String, logFileBaseName: String) {
        // 关闭之前的日志器
        Logger.close()

        // 初始化新的日志器
        val logFilePath = "logs/$logFileBaseName"
        Logger.initialize(
            logToFile = true,
            filePath = logFilePath,
            level = Logger.LogLevel.INFO
        )

        Logger.separator("Starting Simulation: $configPath")
        Logger.info("Log file: $logFilePath")

        try {
            PSSystem.reset()
            PSSystem.initialize(configPath)
            Logger.info("System initialized successfully with config: $configPath")

            PSSystem.runSimulation()
            Logger.info("Simulation completed successfully")

            PSSystem.printStatistics()
            Logger.info("Statistics printed")

            println("\nSimulation completed! Log saved to: $logFilePath")

        } catch (e: Exception) {
            Logger.error("Simulation failed with config: $configPath", e)
            println("Error: ${e.message}")
            println("Check log file for details: $logFilePath")
        } finally {
            Logger.separator("Simulation End: $configPath")
        }
    }

    /**
     * 显示进程详情
     */
    private fun showProcessDetails() {
        val processes = PSSystem.getAllProcesses()
        if (processes.isEmpty()) {
            println("No processes loaded. Please run a simulation first.")
            return
        }

        println("\nProcess Details:")
        processes.forEach { process ->
            println(
                "${process.name} (ID: ${process.id.pid}): " +
                        "State=${process.state}, " +
                        "CPU Time=${process.timeUsed}ms, " +
                        "Priority=${process.schedulingInfo.dynamicPriority}"
            )
        }

        // 同时记录到日志
        Logger.info("Displayed process details for ${processes.size} processes")
    }

    /**
     * 批量运行多个配置
     */
    private fun runBatchSimulations() {
        val configFiles = listOf(
            "ps/sjf.yaml",
            "ps/pr.yaml",
            "ps/rp.yaml",
            "ps/fcfs.yaml"
        )

        configFiles.forEach { configFile ->
            if (File(configFile).exists() ||
                PSSystem::class.java.getResource("/$configFile") != null
            ) {
                println("\nRunning simulation with: $configFile")
                runCustomConfigSimulation(configFile)
            } else {
                println("Config file not found: $configFile")
            }
        }
    }
}
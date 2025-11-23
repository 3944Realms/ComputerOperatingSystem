package top.r3944realms.cos.demo

import top.r3944realms.cos.software.Logger
import top.r3944realms.cos.software.BASystem
import java.io.File

object BankAlgorithmDemo {
    fun run() {
        println("=== COS Banker's Algorithm Demo ===")
        runInteractiveDemo()
    }

    /**
     * 交互式演示
     */
    private fun runInteractiveDemo() {
        println("\n=== Banker's Algorithm Interactive Mode ===")

        while (true) {
            println("\nOptions:")
            println("1. Run default simulation")
            println("2. Run all defined simulations")
            println("3. Run with custom config")
            println("4. Manual resource operations")
            println("5. Show system status")
            println("6. Reset system")
            println("7. Exit")
            print("Choose an option (1-7): ")

            when (readlnOrNull()?.trim()) {
                "1" -> {
                    runSimulationWithConfig("ba/ba_system.yaml", "default_bankers_simulation")
                }
                "2" -> {
                    runBatchSimulations()
                }
                "3" -> {
                    print("Enter config file path: ")
                    val configPath = readlnOrNull()?.trim() ?: "bankers_config.yaml"
                    runCustomConfigSimulation(configPath)
                }
                "4" -> {
                    runManualOperations()
                }
                "5" -> {
                    showSystemStatus()
                }
                "6" -> {
                    BASystem.reset()
                    Logger.close()
                    println("System reset complete")
                }
                "7" -> {
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
            val configFile = File(configPath)
            val logFileName = if (configFile.exists()) {
                val baseName = configFile.nameWithoutExtension
                "${baseName}_simulation.log"
            } else {
                val baseName = configPath.substringAfterLast('/').substringBeforeLast('.')
                if (baseName == configPath) "custom_bankers_simulation.log" else "${baseName}_simulation.log"
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

        Logger.separator("Starting Banker's Algorithm Simulation: $configPath")
        Logger.info("Log file: $logFilePath")

        try {
            BASystem.reset()
            BASystem.initialize(configPath)
            Logger.info("Banker's Algorithm System initialized successfully with config: $configPath")

            BASystem.runSimulation()
            Logger.info("Banker's Algorithm Simulation completed successfully")

            BASystem.printStatistics()
            Logger.info("Statistics printed")

            println("\nSimulation completed! Log saved to: $logFilePath")

        } catch (e: Exception) {
            Logger.error("Banker's Algorithm Simulation failed with config: $configPath", e)
            println("Error: ${e.message}")
            println("Check log file for details: $logFilePath")
        } finally {
            Logger.separator("Banker's Algorithm Simulation End: $configPath")
        }
    }

    /**
     * 运行安全检查场景
     */
    private fun runSafetyCheckScenarios() {
        val scenarios = listOf(
            "safe_scenario.yaml",
            "unsafe_scenario.yaml",
            "edge_case_safe.yaml"
        )

        println("\n=== Running Safety Check Scenarios ===")

        scenarios.forEach { configFile ->
            if (File(configFile).exists() ||
                BASystem::class.java.getResource("/$configFile") != null
            ) {
                println("\nRunning safety scenario: $configFile")
                runCustomConfigSimulation(configFile)

                // 执行额外的安全检查
                val safetyResult = BASystem.performSafetyCheck()
                println("Safety Check Result: ${if (safetyResult.isSafe) "SAFE" else "UNSAFE"}")
                if (safetyResult.isSafe) {
                    println("Safe Sequence: ${safetyResult.safeSequence}")
                }
            } else {
                println("Config file not found: $configFile")
            }
        }
    }

//    /**
//     * 运行死锁场景
//     */
//    private fun runDeadlockScenarios() {
//        val scenarios = listOf(
//            "deadlock_scenario.yaml",
//            "no_deadlock_scenario.yaml",
//            "resource_contention.yaml"
//        )
//
//        println("\n=== Running Deadlock Detection Scenarios ===")
//
//        scenarios.forEach { configFile ->
//            if (File(configFile).exists() ||
//                BASystem::class.java.getResource("/$configFile") != null
//            ) {
//                println("\nRunning deadlock scenario: $configFile")
//                runCustomConfigSimulation(configFile)
//
//                // 执行死锁检测
//                val deadlockResult = BASystem.performDeadlockDetection() // 过于复杂，不予实现
//                println("Deadlock Detection Result: ${if (deadlockResult.deadlockDetected) "DEADLOCK DETECTED" else "NO DEADLOCK"}")
//                if (deadlockResult.deadlockDetected) {
//                    println("Deadlocked Processes: ${deadlockResult.deadlockedProcesses}")
//                }
//            } else {
//                println("Config file not found: $configFile")
//            }
//        }
//    }

    /**
     * 手动资源操作
     */
    private fun runManualOperations() {
        if (BASystem.getAllProcesses().isEmpty()) {
            println("No processes loaded. Please run a simulation first.")
            return
        }

        println("\n=== Manual Resource Operations ===")

        while (true) {
            println("\nManual Operations:")
            println("1. Request resources")
            println("2. Release resources")
            println("3. Perform safety check")
            println("4. Show current state")
            println("5. Back to main menu")
            print("Choose an option (1-6): ")

            when (readlnOrNull()?.trim()) {
                "1" -> {
                    manualRequestResources()
                }
                "2" -> {
                    manualReleaseResources()
                }
                "3" -> {
                    val result = BASystem.performSafetyCheck()
                    println("Safety Check: ${if (result.isSafe) "SAFE" else "UNSAFE"}")
                    if (result.isSafe) {
                        println("Safe Sequence: ${result.safeSequence}")
                    }
                }
                "4" -> {
                    showSystemStatus()
                }
                "5" -> {
                    return
                }
                else -> println("Invalid option")
            }
        }
    }

    /**
     * 手动请求资源
     */
    private fun manualRequestResources() {
        val processes = BASystem.getAllProcesses()
        println("\nAvailable Processes:")
        processes.forEach { process ->
            println("${process.id.pid}: ${process.name} (Allocation: ${process.resourceInfo.allocation}, Need: ${process.resourceInfo.need})")
        }

        print("Enter process ID: ")
        val processId = readlnOrNull()?.toIntOrNull()
        if (processId == null || processes.none { it.id.pid == processId }) {
            println("Invalid process ID")
            return
        }

        print("Enter resource types (comma separated, e.g., CPU,Memory,IO): ")
        val resourceTypes = readlnOrNull()?.split(',')?.map { it.trim() } ?: emptyList()

        val requests = mutableMapOf<String, Int>()
        resourceTypes.forEach { resourceType ->
            print("Enter amount for $resourceType: ")
            val amount = readlnOrNull()?.toIntOrNull() ?: 0
            if (amount > 0) {
                requests[resourceType] = amount
            }
        }

        if (requests.isNotEmpty()) {
            val success = BASystem.requestResources(processId, requests)
            println("Resource request ${if (success) "SUCCESSFUL" else "FAILED"}")
        } else {
            println("No valid requests provided")
        }
    }

    /**
     * 手动释放资源
     */
    private fun manualReleaseResources() {
        val processes = BASystem.getAllProcesses()
        println("\nAvailable Processes:")
        processes.forEach { process ->
            println("${process.id.pid}: ${process.name} (Allocation: ${process.resourceInfo.allocation})")
        }

        print("Enter process ID: ")
        val processId = readlnOrNull()?.toIntOrNull()
        if (processId == null || processes.none { it.id.pid == processId }) {
            println("Invalid process ID")
            return
        }

        val process = BASystem.getProcessInfo(processId)
        if (process == null) {
            println("Process not found")
            return
        }

        println("Current allocations: ${process.resourceInfo.allocation}")

        print("Enter resource types to release (comma separated): ")
        val resourceTypes = readlnOrNull()?.split(',')?.map { it.trim() } ?: emptyList()

        val releases = mutableMapOf<String, Int>()
        resourceTypes.forEach { resourceType ->
            val currentAllocation = process.resourceInfo.allocation[resourceType] ?: 0
            if (currentAllocation > 0) {
                print("Enter amount to release for $resourceType (max: $currentAllocation): ")
                val amount = readlnOrNull()?.toIntOrNull() ?: 0
                if (amount > 0 && amount <= currentAllocation) {
                    releases[resourceType] = amount
                }
            } else {
                println("Process has no allocation of $resourceType")
            }
        }

        if (releases.isNotEmpty()) {
            val success = BASystem.releaseResources(processId, releases)
            println("Resource release ${if (success) "SUCCESSFUL" else "FAILED"}")
        } else {
            println("No valid releases provided")
        }
    }

    /**
     * 显示系统状态
     */
    private fun showSystemStatus() {
        if (BASystem.getAllProcesses().isEmpty()) {
            println("No processes loaded. Please run a simulation first.")
            return
        }

        val state = BASystem.getSystemState()

        println("\n=== Banker's Algorithm System Status ===")
        println("Current Round: ${state.round}")
        println("Available Resources: ${state.available}")
        println("System Safety: ${if (state.isSafe) "SAFE" else "UNSAFE"}")
        if (state.isSafe) {
            println("Safe Sequence: ${state.safeSequence}")
        }
        println("Deadlock Detected: ${state.deadlockDetected}")

        println("\nProcess States:")
        state.processStates.forEach { processState ->
            println("${processState.name} (ID: ${processState.id}):")
            println("  State: ${processState.state}")
            println("  Allocation: ${processState.allocation}")
            println("  Max Demand: ${processState.maxDemand}")
            println("  Need: ${processState.need}")
            println("  Waiting For: ${processState.waitingFor}")
            println("  Finished: ${processState.finished}")
        }

        // 同时记录到日志
        Logger.info("Displayed system status for ${state.processStates.size} processes")
    }

    /**
     * 批量运行多个配置
     */
    private fun runBatchSimulations() {
        val configFiles = listOf(
            "ba/ba_safe.yaml",
            "ba/ba_unsafe.yaml",
            "ba/ba_deadlock.yaml",
            "ba/ba_complex.yaml"
        )

        println("\n=== Running Batch Banker's Algorithm Simulations ===")

        configFiles.forEach { configFile ->
            if (File(configFile).exists() ||
                BASystem::class.java.getResource("/$configFile") != null
            ) {
                println("\nRunning simulation with: $configFile")
                runCustomConfigSimulation(configFile)
            } else {
                println("Config file not found: $configFile")
            }
        }
    }
}
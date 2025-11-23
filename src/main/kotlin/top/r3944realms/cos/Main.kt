package top.r3944realms.cos

import top.r3944realms.cos.demo.BankAlgorithmDemo
import top.r3944realms.cos.demo.ProcessSchedulerDemo
import top.r3944realms.cos.software.Logger

fun main() {
    println("\n=== Interactive Mode ===")

    while (true) {
        println("\nOptions:")
        println("1. Run Process Scheduler Demo")
        println("2. Run Banker's Algorithm Demo")
        println("3. Exit")
        print("Choose an option (1-3): ")

        when (readlnOrNull()?.trim()) {
            "1" -> {
                ProcessSchedulerDemo.run()
            }
            "2" -> {
                BankAlgorithmDemo.run()
            }
            "3" -> {
                println("Exiting...")
                Logger.close()
                return
            }
            else -> println("Invalid option")
        }
    }
}
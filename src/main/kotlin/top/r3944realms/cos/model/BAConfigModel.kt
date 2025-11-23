package top.r3944realms.cos.model

import com.fasterxml.jackson.annotation.JsonProperty

class BAConfigModel {
    data class CompleteConfig(
        val simulation: SimulationConfig,
        val resources: ResourceConfig,
        val processes: List<ProcessConfig>
    )

    data class SimulationConfig(
        @JsonProperty("totalRounds")
        val totalRounds: Int,

        @JsonProperty("timeSpeed")
        val timeSpeed: Long = 1000,

        @JsonProperty("autoMode")
        val autoMode: Boolean = false,

        @JsonProperty("enableDeadlockDetection")
        val enableDeadlockDetection: Boolean = true,

        @JsonProperty("enableSafetyCheck")
        val enableSafetyCheck: Boolean = true,

        @JsonProperty("events")
        val events: List<SimulationEvent> = emptyList()
    )

    data class ResourceConfig(
        @JsonProperty("types")
        val types: List<String>,

        @JsonProperty("available")
        val available: Map<String, Int>,

        @JsonProperty("descriptions")
        val descriptions: Map<String, String> = emptyMap(),

        @JsonProperty("total")
        val total: Map<String, Int>? = null
    )

    data class SimulationEvent(
        @JsonProperty("round")
        val round: Int,

        @JsonProperty("action")
        val action: String, // "REQUEST", "RELEASE", "ADD_PROCESS", "REMOVE_PROCESS"

        @JsonProperty("processId")
        val processId: Int? = null,

        @JsonProperty("resourceType")
        val resourceType: String? = null,

        @JsonProperty("amount")
        val amount: Int? = null,

        @JsonProperty("resources")
        val resources: Map<String, Int>? = null, // 多资源请求

        @JsonProperty("processConfig")
        val processConfig: ProcessConfig? = null
    )

    data class ProcessConfig(
        @JsonProperty("id")
        val id: Int,

        @JsonProperty("name")
        val name: String,

        @JsonProperty("state")
        val state: String = "ACTIVE_READY",

        @JsonProperty("scheduling")
        val scheduling: SchedulingConfig,

        @JsonProperty("resources")
        val resources: ProcessResourceConfig
    )

    data class SchedulingConfig(
        @JsonProperty("totalNeedTime")
        val totalNeedTime: Int,

        @JsonProperty("timeSlice")
        val timeSlice: Int,

        @JsonProperty("priority")
        val priority: String,

        @JsonProperty("policy")
        val policy: String,

        @JsonProperty("preemptable")
        val preemptable: Boolean,

        @JsonProperty("arrivalTime")
        val arrivalTime: Int = 0
    )

    data class ProcessResourceConfig(
        @JsonProperty("maxDemand")
        val maxDemand: Map<String, Int>,

        @JsonProperty("allocation")
        val allocation: Map<String, Int> = emptyMap(),

        @JsonProperty("need")
        val need: Map<String, Int>? = null // 可选，不提供则自动计算
    )

    // 银行家算法结果模型
    data class BankersResult(
        @JsonProperty("isSafe")
        val isSafe: Boolean,

        @JsonProperty("safeSequence")
        val safeSequence: List<Int>?,

        @JsonProperty("availableAfter")
        val availableAfter: Map<String, Int>,

        @JsonProperty("allocationAfter")
        val allocationAfter: Map<Int, Map<String, Int>>,

        @JsonProperty("needAfter")
        val needAfter: Map<Int, Map<String, Int>>,

        @JsonProperty("message")
        val message: String
    )

    // 死锁检测结果
    data class DeadlockDetectionResult(
        @JsonProperty("deadlockDetected")
        val deadlockDetected: Boolean,

        @JsonProperty("deadlockedProcesses")
        val deadlockedProcesses: List<Int>,

        @JsonProperty("availableResources")
        val availableResources: Map<String, Int>,

        @JsonProperty("resourceWaitGraph")
        val resourceWaitGraph: Map<Int, List<Int>>
    )

    // 系统状态快照
    data class SystemStateSnapshot(
        @JsonProperty("round")
        val round: Int,

        @JsonProperty("available")
        val available: Map<String, Int>,

        @JsonProperty("processStates")
        val processStates: List<ProcessStateSnapshot>,

        @JsonProperty("isSafe")
        val isSafe: Boolean,

        @JsonProperty("safeSequence")
        val safeSequence: List<Int>?,

        @JsonProperty("deadlockDetected")
        val deadlockDetected: Boolean = false
    )

    data class ProcessStateSnapshot(
        @JsonProperty("id")
        val id: Int,

        @JsonProperty("name")
        val name: String,

        @JsonProperty("state")
        val state: String,

        @JsonProperty("allocation")
        val allocation: Map<String, Int>,

        @JsonProperty("maxDemand")
        val maxDemand: Map<String, Int>,

        @JsonProperty("need")
        val need: Map<String, Int>,

        @JsonProperty("waitingFor")
        val waitingFor: List<String>,

        @JsonProperty("finished")
        val finished: Boolean
    )
}
package top.r3944realms.cos.model

import com.fasterxml.jackson.annotation.JsonProperty
class PSDConfigModels {
    data class CompleteConfig(
        val simulation: SimulationConfig,
        val processes: List<ProcessConfig>
    )

    data class SimulationConfig(
        @JsonProperty("totalRounds")
        val totalRounds: Int,

        @JsonProperty("timeSpeed")
        val timeSpeed: Long,

        @JsonProperty("events")
        val events: List<SimulationEvent> = emptyList()
    )

    data class SimulationEvent(


        @JsonProperty("round")
        val round: Int,

        @JsonProperty("action")
        val action: String,

        @JsonProperty("processId")
        val processId: Int? = null,

        @JsonProperty("newPriority")
        val newPriority: String? = null,

        @JsonProperty("timeSlice")
        val timeSlice: Int? = null
    )

    data class ProcessConfig(
        @JsonProperty("id")
        val id: Int,

        @JsonProperty("name")
        val name: String,

        @JsonProperty("state")
        val state: String,

        @JsonProperty("scheduling")
        val scheduling: SchedulingConfig
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
        val preemptable: Boolean
    )

}

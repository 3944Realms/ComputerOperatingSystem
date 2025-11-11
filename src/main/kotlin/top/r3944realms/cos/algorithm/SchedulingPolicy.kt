package top.r3944realms.cos.algorithm

enum class SchedulingPolicy {
    FCFS,        // 先来先服务
    RP,          // 时间片轮转
    PR,          // 优先级调度
    SJF,         // 短作业优先
    HRR,         // 高响应比优先
    MF,          // 多级反馈队列
}
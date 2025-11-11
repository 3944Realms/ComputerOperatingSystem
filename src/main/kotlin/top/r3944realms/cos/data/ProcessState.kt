package top.r3944realms.cos.data

enum class ProcessState {
    NEW,
    RUNNING,
    STATIC_READY,
    ACTIVE_READY,
    STATIC_BLOCKED,
    ACTIVE_BLOCKED,
    TERMINATED
}
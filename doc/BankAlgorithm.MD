# COS Bank's Algorithm 使用文档

## 概述

COSBank's Algorithm 是一个基于 Kotlin 开发的银行家算法的模拟场景。系统提供交互式操作界面和详细的日志记录功能。

## 系统特性
- ✅ 自定义资源和资源操作
- ✅ 多进程调度模拟
- ✅ YAML 配置文件驱动
- ✅ 交互式命令行界面
- ✅ 日志记录
- ✅ 实时状态监控

## 快速开始

### 1. 运行系统

```bash
# 在 IDEA 中直接运行 Main.kt
# 或使用 Gradle
./gradlew run
```

### 2. 基本使用流程

1. 启动程序后进入交互式菜单
2. 选择 "2. Run Banker's Algorithm Demo"
3. 选择 "1. Run default simulation" 运行默认配置
4. 查看控制台输出和生成的日志文件

## 配置文件说明

### 配置文件结构

```yaml
# bankers_config_schema.yaml
simulation:
  # 模拟设置部分
  totalRounds: 5                    # 总模拟轮数：只运行5轮，适合快速演示
  timeSpeed: 1000                   # 时间速度：每轮间隔1000毫秒（1秒）
  autoMode: false                   # 自动模式：关闭，需要手动控制或事件驱动
  enableDeadlockDetection: true     # 死锁检测：启用，系统会自动检测死锁
  enableSafetyCheck: true           # 安全检查：启用，每次资源分配前进行安全检查

  # 事件序列：定义在特定轮次发生的系统事件
  events:
    # 第1轮：进程0请求资源
    - round: 1
      action: "REQUEST"             # 动作类型：资源请求
      processId: 0                  # 目标进程：进程ID 0
      resources:                    # 请求的资源数量
        CPU: 1                      # 请求1个CPU核心
        Memory: 0                   # 不请求内存
        IO: 0                       # 不请求IO设备

    # 第2轮：进程1请求资源  
    - round: 2
      action: "REQUEST"
      processId: 1
      resources:
        CPU: 0                      # 不请求CPU
        Memory: 2                   # 请求2个内存块
        IO: 0                       # 不请求IO设备

    # 第3轮：进程0释放资源
    - round: 3
      action: "RELEASE"             # 动作类型：资源释放
      processId: 0
      resources:
        CPU: 1                      # 释放1个CPU核心
        Memory: 0                   # 不释放内存
        IO: 0                       # 不释放IO设备

    # 第4轮：动态添加新进程
    - round: 4
      action: "ADD_PROCESS"         # 动作类型：添加新进程
      processConfig:                # 新进程的完整配置
        id: 5                       # 新进程ID：5
        name: "NewProcess"          # 进程名称
        state: "ACTIVE_READY"       # 初始状态：就绪状态
        scheduling:                 # 调度参数配置
          totalNeedTime: 50         # 总需要执行时间：50单位
          timeSlice: 50             # 时间片长度：50单位
          priority: "NORMAL"        # 优先级：普通
          policy: "PR"              # 调度策略：优先级调度
          preemptable: true         # 是否可抢占：是
          arrivalTime: 10           # 到达时间：第10单位
        resources:                  # 资源需求配置
          maxDemand:                # 最大资源需求
            CPU: 3                  # 最多需要3个CPU核心
            Memory: 2               # 最多需要2个内存块
            IO: 2                   # 最多需要2个IO设备
          allocation:               # 初始资源分配（全部为0，表示尚未分配）
            CPU: 0
            Memory: 0
            IO: 0

resources:
  # 系统资源定义部分
  types: ["CPU", "Memory", "IO"]    # 资源类型列表：CPU、内存、IO三种资源

  available:                        # 初始可用资源数量
    CPU: 30                         # 可用CPU核心：30个
    Memory: 30                      # 可用内存块：30个
    IO: 20                          # 可用IO设备：20个

  descriptions:                     # 资源描述信息（可选）
    CPU: "Processor Cores"          # CPU描述：处理器核心
    Memory: "Memory Blocks"         # 内存描述：内存块
    IO: "I/O Devices"               # IO描述：输入输出设备

  total:                            # 系统总资源量（可选）
    CPU: 8                          # 总CPU核心：8个
    Memory: 8                       # 总内存块：8个
    IO: 4                           # 总IO设备：4个

processes:
  # 初始进程列表
  - id: 0                           # 进程ID：0
    name: "Process-0"               # 进程名称
    state: "ACTIVE_READY"           # 初始状态：就绪状态
    scheduling:                     # 调度参数
      totalNeedTime: 100            # 总需要执行时间：100单位
      timeSlice: 100                # 时间片长度：100单位
      priority: "NORMAL"            # 优先级：普通
      policy: "PR"                  # 调度策略：优先级调度
      preemptable: true             # 是否可抢占：是
      arrivalTime: 0                # 到达时间：0（初始进程）
    resources:                      # 资源配置
      maxDemand:                    # 最大资源需求
        CPU: 7                      # 最多需要7个CPU核心
        Memory: 5                   # 最多需要5个内存块
        IO: 3                       # 最多需要3个IO设备
      allocation:                   # 初始资源分配
        CPU: 0                      # 已分配CPU：0个
        Memory: 1                   # 已分配内存：1个
        IO: 0                       # 已分配IO：0个

  - id: 1                           # 进程ID：1
    name: "Process-1"               # 进程名称
    state: "ACTIVE_READY"           # 初始状态：就绪状态
    scheduling:
      totalNeedTime: 80             # 总需要执行时间：80单位
      timeSlice: 80                 # 时间片长度：80单位
      priority: "HIGH"              # 优先级：高
      policy: "PR"                  # 调度策略：优先级调度
      preemptable: true             # 是否可抢占：是
      arrivalTime: 0                # 到达时间：0
    resources:
      maxDemand:
        CPU: 3                      # 最多需要3个CPU核心
        Memory: 2                   # 最多需要2个内存块
        IO: 2                       # 最多需要2个IO设备
      allocation:
        CPU: 2                      # 已分配CPU：2个
        Memory: 0                   # 已分配内存：0个
        IO: 0                       # 已分配IO：0个
```

### 支持的事件类型

- `REQUEST` - 进程请求资源
- `RELEASE` - 进程释放资源
- `ADD_PROCESS` - 添加进程到调度器
- `REMOVE_PROCESS` - 从调度器中移除进程

### 支持的进程状态（暂无用处）

- `NEW` - 新建
- `RUNNING` - 运行中
- `ACTIVE_READY` - 活动就绪
- `ACTIVE_BLOCKED` - 活动阻塞
- `STATIC_READY` - 静态就绪
- `STATIC_BLOCKED` - 静态阻塞
- `TERMINATED` - 终止

### 支持的调度策略（暂无用处）

- `SJF` - 短作业优先
- `FCFS` - 先来先服务
- `RP` - 时间片轮转
- `PR` - 优先级调度

### 支持的优先级级别（暂无用处）

- `REAL_TIME` - 实时优先级
- `HIGH` - 高优先级
- `NORMAL` - 普通优先级
- `LOW` - 低优先级

## 交互式菜单使用

### 菜单选项

```
1. Run default simulation         # 运行默认配置
2. Run FSFC,SJF,RP,PR simulations # 运行一遍内置的进程调度模拟
3. Run with custom config         # 使用自定义配置
4. Show process details           # 显示进程详情
5. Reset system                   # 重置系统
6. Exit                           # 退出程序
```

### 选项详解

#### 1. 运行默认模拟
- 使用 `config.yaml` 配置文件
- 生成日志文件：`logs/default_simulation.log`
- 自动显示最终统计信息

#### 2. 使用自定义配置
- 提示输入配置文件路径
- 支持绝对路径和相对路径
- 自动根据配置文件名生成日志文件
- 示例：
  ```
  Enter config file path: config_sjf.yaml
  # 生成日志：logs/config_sjf_simulation.log
  
  Enter config file path: scenarios/high_priority.yaml  
  # 生成日志：logs/high_priority_simulation.log
  ```

#### 3. 显示进程详情
- 显示所有已加载进程的详细信息
- 包括：进程名、ID、状态、CPU时间、优先级等

#### 4. 重置系统
- 清空当前调度器状态
- 关闭日志文件
- 准备新的模拟运行

#### 5. 退出程序
- 安全关闭所有资源
- 退出应用程序

## 日志系统

### 日志级别

- `DEBUG` - 调试信息（最详细）
- `INFO` - 一般信息（默认）
- `WARN` - 警告信息
- `ERROR` - 错误信息

### 日志文件位置

- 默认位置：`logs/` 目录
- 命名规则：`{配置名}_simulation.log`
- 示例：
    - `logs/default_simulation.log`
    - `logs/config_sjf_simulation.log`
    - `logs/high_priority_simulation.log`

### 日志格式

```
Starting simulation
Process p-1 added to scheduler
Simulation error: Config file not found
```

## 配置示例

### 示例1：基本SJF调度

```yaml
# config_sjf.yaml
simulation:
  totalRounds: 20
  timeSpeed: 10
  events:
    - round: 0 # 在初次轮转调度时添加进程（ID: 1001）
      action: "ADD_PROCESS"
      processId: 1001
    - round: 0  # 在初次轮转调度时添加进程（ID: 1002）
      action: "ADD_PROCESS"
      processId: 1002
    - round: 5 # 在第6次轮转调度时添加进程（ID: 1003）
      action: "ADD_PROCESS" 
      processId: 1003

processes:
  - id: 1001  # 创建短作业活动就绪进程（ID: 1001）
    name: "short-job"
    state: "ACTIVE_READY"
    scheduling:
      totalNeedTime: 15
      timeSlice: 5
      priority: "HIGH"
      policy: "SJF"
      preemptable: false

  - id: 1002 # 创建短作业活动就绪进程（ID: 1002）
    name: "medium-job"
    state: "ACTIVE_READY"
    scheduling:
      totalNeedTime: 30
      timeSlice: 10
      priority: "NORMAL"
      policy: "SJF"
      preemptable: false
```

### 示例2：优先级调度

```yaml
# config_priority.yaml
simulation:
  totalRounds: 25
  events:
    - round: 2
      action: "ADD_PROCESS"
      processId: 1003
      newPriority: "HIGH"

processes:
  - id: 1001
    name: "realtime-process"
    state: "ACTIVE_READY"
    scheduling:
      totalNeedTime: 40
      timeSlice: 10
      priority: "REAL_TIME"
      policy: "RP"
      preemptable: false

  - id: 1002
    name: "background-job"
    state: "ACTIVE_READY"
    scheduling:
      totalNeedTime: 60
      timeSlice: 15
      priority: "LOW"
      policy: "RP"
      preemptable: false
```

## 输出说明

### 控制台输出

```
Logger initialized. Log file: XXXXX\logs\default_bankers_simulation
== Starting Banker's Algorithm Simulation: ba/ba_system.yaml ==
Log file: logs/default_bankers_simulation
=== Banker's Algorithm System Initialization ===
Process Process-0 scheduling: priority=NORMAL, timeSlice=100ms, preemptable=true, policy=PR
Process Process-0 resources: maxDemand={CPU=7, Memory=5, IO=3}, allocation={CPU=0, Memory=1, IO=0}
Process Process-1 scheduling: priority=HIGH, timeSlice=80ms, preemptable=true, policy=PR
Process Process-1 resources: maxDemand={CPU=3, Memory=2, IO=2}, allocation={CPU=2, Memory=0, IO=0}
=== Banker's Algorithm System Initialization ===        # 初始系统状态
Available resources: {CPU=30, Memory=30, IO=20}         # 可用资源
Resource types: [CPU, Memory, IO]                       # 资源类型
Process count: 2                                        
Banker's Algorithm System initialized with 2 processes
Resource types: [CPU, Memory, IO]
Available resources: {CPU=30, Memory=30, IO=20}
Total simulation rounds: 5
Banker's Algorithm System initialized successfully with config: ba/ba_system.yaml 

=== Starting Banker's Algorithm Simulation ===

*** Simulation Round 1 ***                # 模拟第一轮

=== Current Banker's Algorithm System State ===
Available resources:
Available: {CPU=30, Memory=30, IO=20}

Process Resource Status:                  # 进程状态
Process-0:
  Max Demand: {CPU=7, Memory=5, IO=3}     # 最大需求
  Allocation: {CPU=0, Memory=1, IO=0}     # 已分配资源
  Need: {CPU=7, Memory=4, IO=3}           # 所需资源
  Waiting For: []                         # 等待进程
  Safe State: true
  Deadlock Detected: false                # 是否死锁
Process-1:
  Max Demand: {CPU=3, Memory=2, IO=2}
  Allocation: {CPU=2, Memory=0, IO=0}
  Need: {CPU=1, Memory=2, IO=2}
  Waiting For: []
  Safe State: true
  Deadlock Detected: false

System Summary:
Total Processes: 2
Resource Types: [CPU, Memory, IO]
Total Resources: {CPU=32, Memory=31, IO=20}

=== Process-0 requesting resources: {CPU=1, Memory=0, IO=0} ===

Attempting to allocate resources to Process-0...
State after allocation:

=== Current Banker's Algorithm System State ===
Available resources:
Available: {CPU=29, Memory=30, IO=20}

Process Resource Status:
Process-0:
  Max Demand: {CPU=7, Memory=5, IO=3}
  Allocation: {CPU=1, Memory=1, IO=0}
  Need: {CPU=6, Memory=4, IO=3}
  Waiting For: []
  Safe State: true
  Deadlock Detected: false
Process-1:
  Max Demand: {CPU=3, Memory=2, IO=2}
  Allocation: {CPU=2, Memory=0, IO=0}
  Need: {CPU=1, Memory=2, IO=2}
  Waiting For: []
  Safe State: true
  Deadlock Detected: false

System Summary:
Total Processes: 2
Resource Types: [CPU, Memory, IO]
Total Resources: {CPU=32, Memory=31, IO=20}
System is in a safe state!
Safe sequence: 
Process-0
 -> 
Process-1

Resource allocation successful! System remains in safe state.
>>> Event: Granted resource request for Process-0: {CPU=1, Memory=0, IO=0}
No deadlock detected.
System is in a safe state!
Safe sequence:  # 安全序列
Process-0
 -> 
Process-1

```

### 统计信息输出

```
=== Banker's Algorithm Simulation Completed ===
Banker's Algorithm Simulation completed successfully

=== Final Banker's Algorithm Statistics ===
Total System Resources: {CPU=33, Memory=33, IO=20}
Available Resources: {CPU=30, Memory=28, IO=20}

Process-0:
  Resource Allocation: {CPU=1, Memory=1, IO=0}
  Max Demand: {CPU=7, Memory=5, IO=3}
  Current Need: {CPU=6, Memory=4, IO=3}
  Waiting For: []
  Safe State: true
  Deadlock Detected: false
  Total Requests: 0
  Resource Hold Time: {CPU=0, Memory=0, IO=0}

Process-1:
  Resource Allocation: {CPU=2, Memory=4, IO=0}
  Max Demand: {CPU=3, Memory=2, IO=2}
  Current Need: {CPU=1, Memory=-2, IO=2}
  Waiting For: []
  Safe State: true
  Deadlock Detected: false
  Total Requests: 0
  Resource Hold Time: {CPU=0, Memory=0, IO=0}

NewProcess:
  Resource Allocation: {CPU=0, Memory=0, IO=0}
  Max Demand: {CPU=3, Memory=2, IO=2}
  Current Need: {CPU=3, Memory=2, IO=2}
  Waiting For: []
  Safe State: true
  Deadlock Detected: false
  Total Requests: 0
  Resource Hold Time: {CPU=0, Memory=0, IO=0}
System is in a safe state!
Safe sequence: 
Process-0
 -> 
Process-1
 -> 
NewProcess


System Safety Status: SAFE
Statistics printed
== Banker's Algorithm Simulation End: ba/ba_system.yaml ==
```

### 调试技巧

1. 设置日志级别为 `DEBUG` 获取详细信息
2. 检查生成的日志文件了解运行详情
3. 使用选项3查看进程状态确认配置加载正确

## 版本信息

- 当前版本：1.0.0
- 开发语言：Kotlin
- 依赖管理：Gradle
- 配置文件格式：YAML
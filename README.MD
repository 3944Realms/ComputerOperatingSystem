# COS Process Scheduler 使用文档

## 概述

COS Process Scheduler 是一个基于 Kotlin 开发的进程调度模拟系统，支持多种调度算法和配置化的模拟场景。系统提供交互式操作界面和详细的日志记录功能。

## 系统特性

- ✅ 多进程调度模拟
- ✅ 支持多种调度算法（SJF、FCFS、优先级调度等）
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
2. 选择 "1. Run default simulation" 运行默认配置
3. 查看控制台输出和生成的日志文件

## 配置文件说明

### 配置文件结构

```yaml
# config.yaml
simulation:
  totalRounds: 25          # 总调度轮数
  timeSpeed: 10            # 每轮调度最大时间
  events:                  # 系统事件列表
    - round: 0             # 事件发生的回合
      action: "ADD_PROCESS" # 事件类型
      processId: 1002      # 涉及的进程ID

processes:                 # 进程定义列表
  - id: 1001               # 进程ID
    name: "p-1"            # 进程名称
    state: "ACTIVE_READY"  # 初始状态
    scheduling:            # 调度参数
      totalNeedTime: 50    # 总需要时间
      timeSlice: 10        # 时间片大小
      priority: "REAL_TIME" # 优先级
      policy: "SJF"        # 调度策略
      preemptable: false   # 是否可抢占（目前请设置为false，抢占开启有BUG）
```

### 支持的事件类型

- `ADD_PROCESS` - 添加进程到调度器
- `TERMINATE_PROCESS` - 终止进程（待实现）
- `CHANGE_PRIORITY` - 改变进程优先级（待实现）

### 支持的进程状态

- `ACTIVE_READY` - 活动就绪
- `BLOCKED` - 阻塞
- `TERMINATED` - 终止

### 支持的调度策略

- `SJF` - 短作业优先
- `FCFS` - 先来先服务
- `RP` - 时间片轮转
- `PR` - 优先级调度

### 支持的优先级级别

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
*** Scheduling Round 1 ***
[Scheduler Status]
- Running: p-1
- Ready: p-2, p-3
- Blocked: None

>>> Event: Added process p-2 at round 1
```

### 统计信息输出

```
=== Final Process Statistics ===
p-1:
  Total CPU Time: 50ms
  Total Wait Time: 5ms
  Average Burst: 8.33ms
  Final Priority: REAL_TIME
  Queue Level: 0
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
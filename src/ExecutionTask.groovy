class ExecutionTask {
  ExecutionTask executionTaskParent
  String processName
  String componentId
  String processId
  String executionId

  ExecutionTask(String processName, String componentId, String processId, String executionId) {
    this.processName = processName
    this.componentId = componentId
    this.processId = processId
    this.executionId = executionId
  }
  
  String getProcessName() {
    return this.processName
  }

  void setParent(ExecutionTask executionTaskParent) {
    this.executionTaskParent = executionTaskParent
  }

  ExecutionTask getParent() {
    return this.executionTaskParent
  }

  String getTopLevelComponentId() {
    return "component_0"
  }

  String getTopLevelExecutionId() {
    return "exec_0"
  }

  String getTopLevelProcessId() {
    return "process_0"
  }

  String getComponentId() {
    return this.componentId
  }

  String getProcessId() {
    return this.componentId
  }

  String getExecutionId() {
    return this.executionId
  }

  String getExecutionMode() {
    return "VIM"
  }

  String getSource() {
    return "USER"
  }

  String getRerunMode() {
    return "NONE"
  }
}


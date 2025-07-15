import java.util.logging.Logger;

class ExecutionManager {
  static ExecutionUtilHelper executionUtil
  static ExecutionTask executionTask

  static void setExecutionUtil(ExecutionUtilHelper executionUtil) {
    this.executionUtil = executionUtil
  }

  static ExecutionUtilHelper getCurrent() {
    return this.executionUtil
  }

}


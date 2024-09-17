import java.util.logging.Logger;

class ExecutionManager {
  static ExecutionUtilHelper executionUtil

  ExecutionManager(ExecutionUtilHelper executionUtil) {
    this.executionUtil = executionUtil
  }
  
  static ExecutionUtilHelper getCurrent() {
    return this.executionUtil
  }

}


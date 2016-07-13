package ch.icclab.cyclops.consume.command;

import ch.icclab.cyclops.consume.AbstractConsumer;
import ch.icclab.cyclops.util.loggers.CommandLogger;

/**
 * Author: Skoviera
 * Created: 14/04/16
 * Description: Commands consumer
 */
public class CommandConsumer extends AbstractConsumer {

    private ExecutionStatus status;
    public class ExecutionStatus {
        private Boolean executed;
        private String message;

        public ExecutionStatus(Boolean executed) {
            this.executed = executed;
            this.message = "";
        }

        public ExecutionStatus(Boolean executed, String message) {
            this.executed = executed;
            this.message = message;
        }

        public Boolean wasExecuted() {
            return executed;
        }

        public String getMessage() {
            return message;
        }
    }

    @Override
    public void consume(String content) {

        // automatic mapping based on type field
        Command command = CommandMapping.fromJson(content);

        if (command != null) {
            try {
                command.execute();

                status = new ExecutionStatus(true, String.format("[OK] command \"%s\" successfully executed", command.get_class()));
                CommandLogger.log(status.getMessage());
            } catch (Exception e) {
                status = new ExecutionStatus(false, String.format("[ERROR] command \"%s\" failed during execution: %s", command.get_class(), e.getMessage()));
                CommandLogger.log(status.getMessage());
            }
        } else {
            status = new ExecutionStatus(false, "[ERROR] Unknown command or invalid JSON");
            CommandLogger.log(status.getMessage());
        }
    }

    public ExecutionStatus getStatus() {
        return status;
    }
}

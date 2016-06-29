package ch.icclab.cyclops.consume.command;

import ch.icclab.cyclops.consume.AbstractConsumer;
import ch.icclab.cyclops.util.loggers.CommandLogger;

import java.util.List;

/**
 * Author: Skoviera
 * Created: 14/04/16
 * Description: Commands consumer
 */
public class CommandConsumer extends AbstractConsumer {

    @Override
    protected void consume(String content) {

        // automatic mapping based on type field
        Command command = CommandMapping.fromJson(content);

        if (command != null) {
            try {
                command.execute();
                CommandLogger.log(String.format("[OK] command \"%s\" successfully executed", command.get_class()));
            } catch (Exception e) {
                CommandLogger.log(String.format("[ERROR] command \"%s\" couldn't be executed: %s", command.get_class(), e.getMessage()));
            }
        } else {
            CommandLogger.log("Received message doesn't contain a valid command");
        }
    }
}

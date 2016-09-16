package ch.icclab.cyclops.consume.data;

import ch.icclab.cyclops.consume.AbstractConsumer;
import ch.icclab.cyclops.executor.TaskExecutor;
import ch.icclab.cyclops.load.model.PublisherCredentials;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Author: Skoviera
 * Created: 14/04/16
 * Description: Event consumer
 */
public class DataConsumer extends AbstractConsumer {
    final static Logger logger = LogManager.getLogger(DataConsumer.class.getName());

    private PublisherCredentials publisherSettings;
    private String defaultMeasurementName;
    private TaskExecutor executor;


    public DataConsumer(String name, PublisherCredentials settings) {
        this.defaultMeasurementName = name;
        this.publisherSettings = settings;
        this.executor = TaskExecutor.getInstance();
    }

    @Override
    public void consume(String content) {
        executor.addTask(new DataProcess(content, publisherSettings, defaultMeasurementName));
    }
}

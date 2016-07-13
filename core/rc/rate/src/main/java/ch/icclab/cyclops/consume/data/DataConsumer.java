package ch.icclab.cyclops.consume.data;

import ch.icclab.cyclops.consume.AbstractConsumer;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.PublisherCredentials;
import ch.icclab.cyclops.load.model.RatingPreferences;
import ch.icclab.cyclops.publish.Messenger;
import com.google.gson.Gson;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Author: Skoviera
 * Created: 14/04/16
 * Description: Event consumer
 */
public class DataConsumer extends AbstractConsumer {
    final static Logger logger = LogManager.getLogger(DataConsumer.class.getName());

    private PublisherCredentials publisherSettings;
    private RatingPreferences rating;
    private Properties properties;

    public DataConsumer(PublisherCredentials settings) {
        this.publisherSettings = settings;
        this.properties = Loader.getSettings().getProperties();
        this.rating = Loader.getSettings().getRatingPreferences();
    }

    @Override
    protected void consume(String content) {
        try {
            // try to map it as array
            List<Map> array = new Gson().fromJson(content, List.class);

            // make sure there is something to be rated at all
            if (array != null && !array.isEmpty()) {
                // now apply rates
                List<Map> rated = rateAndCharge(array);

                // push it to the next step
                publishOrBroadcast(rated);
            }

        } catch (Exception ignored) {
            try {
                // this means it was not an array to begin with, just a simple object
                Map obj = new Gson().fromJson(content, Map.class);

                if (obj != null) {
                    // apply rate
                    Map rated = rateAndCharge(obj);

                    publishOrBroadcast(Collections.singletonList(rated));
                }
            } catch (Exception ignoredAgain) {}
        }
    }

    /**
     * Rate and Charge an UDR record
     * @param obj as Map
     * @return object or null
     */
    private Map rateAndCharge(Map obj) {
        // make sure we have usage field present
        if (obj.containsKey(rating.getUsageField())) {

            // normalise usage object
            Double usage = getUsage(obj.get(rating.getUsageField()));

            // find correct rate or use default one
            Double rate = getRate(obj);

            // calculate charge
            Double charge = usage * rate;

            // put rate and charge back
            obj.put(rating.getRateField(), rate);
            obj.put(rating.getChargeField(), charge);

            // add string prefix to charge record
            if (obj.containsKey(RatingPreferences.CLASS_FIELD_NAME)) {
                obj.put(RatingPreferences.CLASS_FIELD_NAME, String.format("%s%s", obj.get(RatingPreferences.CLASS_FIELD_NAME), rating.getChargeSuffix()));
            }

            return obj;
        } else {
            return null;
        }
    }

    /**
     * Rate and Charge list of UDR records
     * @param list to be rated
     * @return rated list
     */
    private List<Map> rateAndCharge(List<Map> list) {
        List<Map> ratedList = new ArrayList<>();

        // iterate and rate all objects
        for (Map obj: list) {

            // rate the object
            Map rated = rateAndCharge(obj);

            // only add to the list if it was successfully rated
            if (rated != null) {
                ratedList.add(rated);
            }
        }

        return ratedList;
    }

    private Double getUsage(Object obj) {
        return (obj instanceof Number)? (Double) obj : NumberUtils.toDouble((String) obj, 0);
    }

    private Double getRate(Map obj) {
        return (obj.containsKey(RatingPreferences.CLASS_FIELD_NAME))? NumberUtils.toDouble(properties.getProperty((String) obj.get(RatingPreferences.CLASS_FIELD_NAME)), rating.getDefaultRate()): rating.getDefaultRate();
    }

    private void publishOrBroadcast(List<Map> list) {
        Messenger messenger = Messenger.getInstance();

        if (list != null && !list.isEmpty()) {
            if (publisherSettings.dispatchInsteadOfBroadcast()) {

                // some initialisation
                String defaultKey = publisherSettings.getPublisherDefaultRoutingKeyIfMissing();
                Map<String, List<Object>> dispatch = new HashMap<>();

                // let's iterate over individual maps
                for (Map map: list) {
                    String routingKey = (String) map.get(RatingPreferences.CLASS_FIELD_NAME);
                    if (routingKey == null || routingKey.isEmpty()) {
                        routingKey = defaultKey;
                    }

                    // check whether this list exists
                    if (dispatch.containsKey(routingKey)) {

                        // find appropriate list
                        List<Object> tmp = dispatch.get(routingKey);

                        // add new object to it
                        tmp.add(map);

                        // put it back
                        dispatch.put(routingKey, tmp);
                    } else {
                        List<Object> tmp = new ArrayList<>();
                        tmp.add(map);
                        dispatch.put(routingKey, tmp);
                    }
                }

                // finally dispatch it over RabbitMQ
                for (Map.Entry<String, List<Object>> entry: dispatch.entrySet()) {
                    messenger.publish(entry.getValue(), entry.getKey());
                }

            } else {
                messenger.broadcast(list);
            }
        }

    }
}

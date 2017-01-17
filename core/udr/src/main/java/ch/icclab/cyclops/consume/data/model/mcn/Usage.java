package ch.icclab.cyclops.consume.data.model.mcn;

import ch.icclab.cyclops.consume.data.DataMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Author: Oleksii
 * Created: 01/06/16
 * Description: OpenStackEvent UDR measurement definition
 */
public class Usage implements DataMapping {
    @Override
    public String getTimeField() { return "time"; }

    @Override
    public TimeUnit getTimeUnit() { return TimeUnit.MILLISECONDS; }

    @Override
    public List<String> getTagNames() {
        List<String> list = new ArrayList<>();
        list.add("account");
        list.add("metadata.source");
        return list;
    }

    @Override
    public Map preProcess(Map original) { return original; }

    @Override
    public Boolean shouldPublish() { return false; }

    @Override
    public Boolean doNotBroadcastButRoute() { return false; }
}


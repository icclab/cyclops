package ch.icclab.cyclops.timeseries;

/**
 * Created by lexxito on 01.02.17.
 */
public class InfluxDBHealth {
    private InfluxDBClient influxDBClient;
    private boolean statusOfLastSave;
    private static InfluxDBHealth singleton = new InfluxDBHealth();

    private InfluxDBHealth(){
        influxDBClient= new InfluxDBClient();
        statusOfLastSave = true;
    }

    public static InfluxDBHealth getInstance() { return singleton; }

    public boolean isHealthy(){
        try{
            influxDBClient.ping();
        } catch (Exception ignored){
            return false;
        }
        return statusOfLastSave;
    }

    public void setStatusOfLastSave(boolean status){
        statusOfLastSave = status;
    }


}

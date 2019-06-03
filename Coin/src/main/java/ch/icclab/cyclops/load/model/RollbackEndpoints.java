package ch.icclab.cyclops.load.model;

public class RollbackEndpoints {
    // These fields correspond with the configuration file
    private String udrendpoint;
    private String cdrendpoint;

    public String getUdrendpoint() {
        return udrendpoint;
    }

    public void setUdrendpoint(String udrendpoint) {
        this.udrendpoint = udrendpoint;
    }

    public String getCdrendpoint() {
        return cdrendpoint;
    }

    public void setCdrendpoint(String cdrendpoint) {
        this.cdrendpoint = cdrendpoint;
    }

    public String getBillingendpoint() {
        return billingendpoint;
    }

    public void setBillingendpoint(String billingendpoint) {
        this.billingendpoint = billingendpoint;
    }

    private String billingendpoint;

}

package ch.icclab.cyclops.consume.command;
/*
 * Copyright (c) 2017. Zuercher Hochschule fuer Angewandte Wissenschaften
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import ch.icclab.cyclops.consume.command.customerdb.Credentials;
import ch.icclab.cyclops.dao.BillRun;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.publish.APICaller;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.timeseries.DbAccess;
import com.google.gson.Gson;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 31.07.17
 * Description: Generate bills for all accounts in the customer-database
 */
public class GenerateAllBills extends Command{
    // mandatory fields
    private Long time_from;
    private Long time_to;

    /**
     * Organization representation when querying customer database API
     */
    private class Org {
        private String orgName;
        private String url;

        public Org() {
        }

        public String getOrgName() {
            return orgName;
        }
        public void setOrgName(String orgName) {
            this.orgName = orgName;
        }

        public String getUrl() {
            return url;
        }
        public void setUrl(String url) {
            this.url = url;
        }
    }

    /**
     * Organization representation when querying detailed object
     */
    private class OrgDetail {
        private String orgName;
        private String billingId;

        private class Department {
            private String departmentName;

            private class Tenant {
                private String tenantName;
                private String tenantId;

                public Tenant() {
                }

                public String getTenantName() {
                    return tenantName;
                }
                public void setTenantName(String tenantName) {
                    this.tenantName = tenantName;
                }

                public String getTenantId() {
                    return tenantId;
                }
                public void setTenantId(String tenantId) {
                    this.tenantId = tenantId;
                }
            }
            private List<Tenant> tenantList;

            public Department() {
            }

            public String getDepartmentName() {
                return departmentName;
            }
            public void setDepartmentName(String departmentName) {
                this.departmentName = departmentName;
            }

            public List<Tenant> getTenantList() {
                return tenantList;
            }
            public void setTenantList(List<Tenant> tenantList) {
                this.tenantList = tenantList;
            }
        }
        private List<Department> departmentList;

        public OrgDetail() {
        }

        public String getOrgName() {
            return orgName;
        }
        public void setOrgName(String orgName) {
            this.orgName = orgName;
        }

        public String getBillingId() {
            return billingId;
        }
        public void setBillingId(String billingId) {
            this.billingId = billingId;
        }

        public List<Department> getDepartmentList() {
            return departmentList;
        }
        public void setDepartmentList(List<Department> departmentList) {
            this.departmentList = departmentList;
        }
    }

    /**
     * Representation of list of Resellers and their customers
     */
    public class Reseller {
        private String id;
        private String name;

        public class Customer {
            private String name;
            private String id;

            public Customer(String name, String id) {
                this.name = name;
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public String getId() {
                return id;
            }
        }
        private List<Customer> customers;

        public Reseller(String name, String id) {
            this.id = id;
            this.name = name;
            this.customers = new ArrayList<>();
        }

        public void addCustomer(String name, String id) {
            this.customers.add(new Customer(name, id));
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public List<Customer> getCustomers() {
            return customers;
        }
    }

    /**
     * Routing key for dispatching commands to itself
     */
    private class RoutingKey {
        private String PublishToSelf = "SelfPublish";

        public RoutingKey() {
        }

        public String getPublishToSelf() {
            return PublishToSelf;
        }

        public void setPublishToSelf(String publishToSelf) {
            PublishToSelf = publishToSelf;
        }
    }

    @Override
    Status execute() {
        Status status = new Status();

        // sanity checks first
        if (time_from == null || time_to == null || time_from < 0L || time_to < time_from) {
            status.setClientError("Invalid FROM and TO (unit to be in milliseconds)");
            return status;
        }

        BillRun run = new BillRun();

        // get credentials from the config file
        Credentials credentials = Loader.extractProperties(Credentials.class);
        if (credentials == null || !credentials.isValid()) {
            status.setServerError("Missing Customer DB credentials");
            return status;
        }

        // query for list of resellers and their customers
        List<Reseller> resellers = queryCustomerDatabase(credentials);
        if (resellers == null || resellers.isEmpty()) {
            status.setServerError("No resellers present in the customer database");
            return status;
        }

        // set list of resellers
        run.setData(new Gson().toJson(resellers));

        // persist this bill run
        DbAccess.DatabaseResult result = new DbAccess().storePersistentObjectReturningId(run, BillRun.ID_FIELD, BillRun.class);
        if (result.getPersistenceStatus() != DbAccess.PersistenceStatus.OK) {
            status.setServerError("Couldn't persist Bill run meta data");
            return status;
        }

        // update run ID
        run = (BillRun) result.getReturnValue();

        // create list of GenerateBill commands
        List<GenerateBill> commands = new ArrayList<>();
        for (Reseller reseller: resellers) {

            // get list of customer IDs
            List<String> customerIDs = new ArrayList<>();
            reseller.getCustomers().forEach(customer -> customerIDs.add(customer.getId()));

            // construct reseller map with list of consumer IDs
            HashMap<String, List<String>> request = new HashMap<>();
            request.put(reseller.getId(), customerIDs);

            // create
            commands.add(new GenerateBill(time_from, time_to, run.getId(), request));
        }

        if (commands.isEmpty()) {
            status.setServerError("Couldn't generate list of Commands");
            return status;
        }

        // publish the list of commands to the RabbitMQ
        RoutingKey key = Loader.extractProperties(RoutingKey.class);
        for (Command command: commands) {
            if (!Messenger.publish(command, key.getPublishToSelf())) {
                status.setServerError("Couldn't publish one of generated commands to the RabbitMQ");
                return status;
            }
        }

        status.setSuccessful(String.format("Bill run %d: requesting %d bills for %d - %d period", run.getId(), commands.size(), time_from, time_to));

        return status;
    }

    /**
     * Query Customer Database for list of Resellers and their customers
     * @param credentials to connect with
     * @return list or null
     */
    private List<Reseller> queryCustomerDatabase(Credentials credentials) {
        try {
            List<Reseller> resellers = new ArrayList<>();
            APICaller caller = new APICaller();

            String host = String.format("http://%s:%s", credentials.getCustomerDatabaseHost(), credentials.getCustomerDatabasePort());

            // query for list of resellers
            URL orgListUrl = new URL(String.format("%s/api/org/", host));
            List<Org> orgList = caller.get(orgListUrl).getAsListOfThisType(Org.class);

            // iterate over the list
            for (Org org: orgList) {
                URL orgUrl = new URL(String.format("%s%s", host, org.getUrl()));
                OrgDetail detail = caller.get(orgUrl).getAsThisType(OrgDetail.class);

                // we have data available
                if (detail != null) {
                    Reseller reseller = new Reseller(detail.getOrgName(), detail.getBillingId());
                    List<OrgDetail.Department> departmentList = detail.getDepartmentList();

                    // check validity of department list and iterate over them
                    if (departmentList != null && !departmentList.isEmpty()) {
                        for (OrgDetail.Department department: departmentList) {

                            // extract list of tenants
                            List<OrgDetail.Department.Tenant> tenantList = department.getTenantList();
                            if (tenantList != null && !tenantList.isEmpty()) {
                                for (OrgDetail.Department.Tenant tenant: tenantList) {
                                    // we care only about Tenant Name and its ID
                                    String name = tenant.getTenantName();
                                    String id = tenant.getTenantId();

                                    // double check them
                                    if (name != null && !name.isEmpty() && id != null && !id.isEmpty()) {
                                        reseller.addCustomer(name, id);
                                    }
                                }
                            }
                        }
                    }

                    resellers.add(reseller);
                }
            }

            return resellers;

        } catch (Exception e) {
            return null;
        }
    }
}

package ch.icclab.cyclops.endpoint;
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

import ch.icclab.cyclops.consume.command.GenerateAllBills;
import ch.icclab.cyclops.dao.Bill;
import ch.icclab.cyclops.dao.BillRun;
import ch.icclab.cyclops.dao.PersistentObject;
import ch.icclab.cyclops.timeseries.DbAccess;
import ch.icclab.cyclops.util.loggers.RESTLogger;
import org.jooq.SelectQuery;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.Get;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jooq.impl.DSL.inline;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 16.05.17
 * Description: GET Bill Runs
 */
public class BillRunEndpoint extends AbstractEndpoint {
    @Override
    public List<String> getRoutes() {
        List<String> list = new ArrayList<>();

        list.add("/billrun");
        list.add("/billruns");
        list.add("/billrun/{id}");

        return list;
    }

    private Integer id = null;

    /**
     * This method is invoked in order to get command from API URL
     */
    public void doInit() {
        try {
            id = Integer.parseInt((String) getRequestAttributes().get("id"));
        } catch (Exception e) {
            id = null;
        }
    }

    @Get("json")
    public Response getBills() {
        if (id == null) return processList();
        else return processGet(id);
    }

    private Response processList() {
        // prepare response
        Response response = getResponse();
        HTTPOutput output = null;

        // create a select query
        DbAccess db = new DbAccess();
        SelectQuery select = db.createSelectFrom(BillRun.TABLE, BillRun.ID_FIELD, BillRun.TIME_FIELD);

        // fetch from database
        List<BillRun> runs = db.fetchUsingSelectStatement(select, BillRun.class);

        if (runs == null) {
            output = new HTTPOutput(Status.SERVER_ERROR_INTERNAL, "Could not fetch Bill runs, most likely database is down");
            response = output.prepareResponse(response);
        } else {
            output = new HTTPOutput(String.format("Fetched %d Bill Runs", runs.size()), runs);
            response = output.prepareResponse(response, false);
        }

        RESTLogger.log(String.format("%s %s", getRoutes(), output.toString()));

        return response;
    }

    private Response processGet(Integer id) {
        // prepare response
        Response response = getResponse();
        HTTPOutput output = null;

        // create a select query
        DbAccess db = new DbAccess();
        SelectQuery select = db.createSelectFrom(BillRun.TABLE);
        select.addConditions(BillRun.ID_FIELD.eq(inline(id)));

        // fetch the bill from database
        List<BillRun> runs = db.fetchUsingSelectStatement(select, BillRun.class);
        if (runs == null) {
            output = new HTTPOutput(Status.SERVER_ERROR_INTERNAL, String.format("Could not fetch Bill run %d, most likely database is down", id));
            response = output.prepareResponse(response);
        } else if (runs.isEmpty()) {
            output = new HTTPOutput(Status.CLIENT_ERROR_NOT_FOUND, String.format("Could not find Bill run %d", id));
            response = output.prepareResponse(response);
        } else {
            // transform Bill Run's data field time_from PGObject time_to Map
            BillRun run = applyDataToResellerTransformation(runs.get(0));

            // search for bills that have this ID
            SelectQuery selectBills = db.createSelectFrom(Bill.TABLE, Bill.ID_FIELD, Bill.ACCOUNT_FIELD, Bill.CHARGE_FIELD, Bill.CURRENCY_FIELD);
            selectBills.addConditions(Bill.RUN_FIELD.eq(inline(id)));
            List<Bill> bills = db.fetchUsingSelectStatement(selectBills, Bill.class);

            // merge these two tables to figure out what bills are already present
            Map data = mergeBillRunWithBills(run, bills);
            run.setData(data);

            output = new HTTPOutput(String.format("Fetched Bill run %d", id), run);
            response = output.prepareResponse(response, false);
        }

        RESTLogger.log(String.format("%s %s", getRoutes(), output.toString()));

        return response;
    }

    /**
     * Zip two lists into one hashmap
     * @param run bill run object
     * @param bills list
     * @return map <reseller, text>
     */
    private Map<String, String> mergeBillRunWithBills(BillRun run, List<Bill> bills) {
        HashMap<String, String> map = new HashMap<>();

        try {
            List<GenerateAllBills.Reseller> resellers = (List<GenerateAllBills.Reseller>) run.getData();
            resellers.forEach(reseller -> map.put(reseller.getId(), "No bill present"));
            bills.forEach(bill -> map.put(bill.getAccount(), String.format("%.2f %s", bill.getCharge(), bill.getCurrency())));
        } catch (Exception ignored) {
        }
        return map;
    }

    /**
     * Map Bill Run's data JSON as list of Resellers
     * @param run data
     * @return updated bill run
     */
    private BillRun applyDataToResellerTransformation(BillRun run) {
        run.setData(PersistentObject.PGObjectFieldToMap(run.getData(), GenerateAllBills.Reseller.class));
        return run;
    }
}

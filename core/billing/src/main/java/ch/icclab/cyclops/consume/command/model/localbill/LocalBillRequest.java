package ch.icclab.cyclops.consume.command.model.localbill;
/*
 * Copyright (c) 2016. Zuercher Hochschule fuer Angewandte Wissenschaften
 *  All Rights Reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may
 *     not use this file except in compliance with the License. You may obtain
 *     a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *     WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *     License for the specific language governing permissions and limitations
 *     under the License.
 */

import ch.icclab.cyclops.consume.command.Command;
import ch.icclab.cyclops.timeseries.BatchPointsContainer;
import ch.icclab.cyclops.timeseries.InfluxDBClient;
import ch.icclab.cyclops.timeseries.QueryBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Author: Skoviera
 * Created: 27/06/16
 * Description: Command representing Local bill request
 */
public class LocalBillRequest extends Command {

    private static String ACCOUNT_FIELD = "account";

    private String subject;
    private List<String> include;
    private Long from;
    private Long to;

    // SLAs, discounts and
    private Map<String, Double> sla;
    private Map<String, Double> coupons;
    private Double discount;
    private Double vat;

    @Override
    protected void execute() {
        // create local bill
        LocalBill invoice = new LocalBill(subject, from, to);

        // only process this message when subject and include are valid
        if (subject != null && !subject.isEmpty() && include != null && !include.isEmpty()) {
            // iterate over all included items
            for (String cdr: include) {
                // only if we got valid cdr request
                if (!cdr.isEmpty()) {

                    // create query
                    QueryBuilder query = prepareQuery(cdr);

                    // execute query
                    List<LocalBillCDRMapping> response = InfluxDBClient.getInstance().executeQueryAndMapItToClass(query, LocalBillCDRMapping.class);

                    // add it to container
                    if (response != null && !response.isEmpty()) {
                        invoice.addToBill(response, cdr);
                    }
                }
            }
        }

        invoice.applySLA(sla);
        invoice.applyCoupons(coupons);
        invoice.calculateAmount();
        invoice.applyDiscount(discount);
        invoice.applyVAT(vat);

        // proceed with storing this invoice to database (even if it's empty)
        InfluxDBClient.getInstance().persistSinglePoint(invoice.toDBPoint());
    }

    /**
     * Create query for InfluxDB
     * @param name as subject
     * @return QueryBuilder
     */
    private QueryBuilder prepareQuery(String name) {
        // build query for included measurement and subject
        QueryBuilder query = new QueryBuilder(name).where(ACCOUNT_FIELD, subject);

        // if from is valid
        if (from != null && from > 0) {
            query.timeFrom(from, TimeUnit.SECONDS);
        }

        // to is valid
        if (to != null && to > 0) {
            query.timeTo(to, TimeUnit.SECONDS);
        }

        return query;
    }
}

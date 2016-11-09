package ch.icclab.cyclops.consume.command.model.generic.local;
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

import ch.icclab.cyclops.util.BeanList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Author: Skoviera
 * Created: 09/09/16
 * Description: Generic bill container
 */
public class Bill {
    private String _class = getClass().getSimpleName();
    private String account;
    private Long time;
    private Long from;
    private Long to;
    private Double charge;
    private List<ChargeData> records;

    public static List<String> getTagNames() {
        return Collections.singletonList("account");
    }

    public static String getTimeFieldName() {
        return "time";
    }

    public static TimeUnit getTimeUnit() {
        return TimeUnit.MILLISECONDS;
    }

    public Bill(String account, Long from, Long to) {
        this.account = account;
        this.time = System.currentTimeMillis();
        this.from = from;
        this.to = to;
        this.charge = 0d;
        this.records = new ArrayList<>();
    }

    /**
     * Process individual Charge Records from list of CDRs
     * @param cdrs
     */
    public void addAndProcessCDRs(List<CDR> cdrs) {
        if (cdrs != null && !cdrs.isEmpty()) {
            List<ChargeData> data = new ArrayList<>();

            // flatten the structure and get all ChargeData records
            cdrs.stream().filter(cdr -> cdr.getData() != null).forEach(item -> data.addAll(BeanList.populate(item.getData(), ChargeData.class)));

            // iterate over charge data records
            for (ChargeData record: data) {

                int index = records.indexOf(record);

                if (index >= 0) {
                    // update found record
                    ChargeData tmp = records.get(index);
                    tmp.setCharge(tmp.getCharge() + record.getCharge());
                    tmp.setUsage(tmp.getUsage() + record.getUsage());
                } else {
                    // add new record
                    records.add(record);
                }

                // update overall counter
                charge += record.getCharge();
            }
        }
    }
}

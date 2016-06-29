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

import ch.icclab.cyclops.load.model.InfluxDBCredentials;
import ch.icclab.cyclops.timeseries.RemoveNullValues;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.Gson;
import org.influxdb.dto.Point;
import org.joda.time.DateTime;

import java.util.*;

/**
 * Author: Skoviera
 * Created: 28/06/16
 * Description:
 */
public class LocalBill {
    private String account;
    private Double amount = 0d;
    private Long from;
    private Long to;
    private List<BillEntry> bills = new ArrayList<>();

    private Double discount;
    private Double amountBeforeDiscount;
    public void applyDiscount(Double percentage) {
        if (percentage != null && percentage > 0d) {
            discount = percentage;
            amountBeforeDiscount = amount;
            amount = amount - amount * percentage / 100;
            if (amount < 0d) {
                amount = 0d;
            }
        }
    }

    private Double VAT;
    private Double amountBeforeVAT;
    public void applyVAT(Double percentage) {
        if (percentage != null && percentage > 0) {
            VAT = percentage;
            amountBeforeVAT = amount;
            amount = amount + amount * percentage / 100;
            if (amount < 0d) {
                amount = 0d;
            }
        }
    }

    public void calculateAmount() {
        for (BillEntry bill: bills) {
            Double counter = bill.amount;

            if (bill.sla_discount != null) {
                counter = counter - bill.sla_discount;
            }

            if (bill.coupon_discount != null) {
                counter = counter - bill.coupon_discount;
            }

            Double finalised = (counter > 0)? counter: 0;
            if (!Objects.equals(bill.amount, finalised)) {
                bill.original_amount = bill.amount;
                bill.amount = finalised;
            }

            amount += bill.amount;
        }
    }

    public void applySLA(Map<String, Double> slas) {
        if (slas != null && !slas.isEmpty()) {
            // process all SLA violations
            for (Map.Entry sla: slas.entrySet()) {

                // find appropriate bill
                for (BillEntry bill: bills) {
                    if (bill.getBill().equals(sla.getKey())) {
                        bill.applySLAViolation((Double) sla.getValue());
                        break;
                    }
                }
            }
        }
    }

    public void applyCoupons(Map<String, Double> coupons) {
        if (coupons != null && !coupons.isEmpty()){
            // process all coupons and apply them to correct bills
            for (Map.Entry coupon: coupons.entrySet()) {

                // find appropriate bill
                for (BillEntry bill: bills) {
                    if (bill.getBill().equals(coupon.getKey())) {
                        bill.applyCoupon((Double) coupon.getValue());
                        break;
                    }
                }
            }
        }
    }

    private class BillEntry {
        private String bill;
        private Double amount = 0d;
        private Double sla_percentage;
        private Double sla_discount;
        private Double coupon_discount;
        private Double original_amount;

        public BillEntry(String bill, LocalBillCDRMapping cdr) {
            this.bill = bill;

            addCdrToStatement(cdr);
        }

        public void addCdrToStatement(LocalBillCDRMapping cdr) {
            amount = amount + cdr.getCharge();
        }

        public String getBill() {
            return bill;
        }

        public void applySLAViolation(Double percentage) {
            sla_percentage = percentage;
            sla_discount = amount * sla_percentage / 100;
            if (sla_discount < 0d) {
                sla_discount = 0d;
            } else if (sla_discount > amount){
                sla_discount = amount;
            }
        }

        public void applyCoupon(Double value) {
            coupon_discount = value;
        }
    }

    public LocalBill(String account, Long from, Long to) {
        this.account = account;

        this.from = (from != null && from > 0)? from : 0;
        this.to = (to != null && to > this.from) ? to : DateTime.now().getMillis() / 1000;
    }

    /**
     * Calculate and prepare a bill based on list of CDRs
     * @param cdrs to be taken into account
     */
    public void addToBill(List<LocalBillCDRMapping> cdrs, String clazz) {
        // iterate over all cdr records
        for (LocalBillCDRMapping cdr: cdrs) {
            // have we seen this bill before?
            Boolean found = false;
            for (BillEntry entry: bills) {
                if (entry.getBill().equals(clazz)) {
                    entry.addCdrToStatement(cdr);
                    found = true;
                }
            }

            if (!found) {
                // it's a new bill entry
                BillEntry entry = new BillEntry(clazz, cdr);
                bills.add(entry);
            }
        }
    }

    public Point.Builder toDBPoint() {
        if (bills.isEmpty()) {
            bills = null;
        }

        // first translate it into json
        String json = new Gson().toJson(this);

        // now flatten it properly
        Map<String, Object> flat = JsonFlattener.flattenAsMap(json);

        // let's delete account as we will use it as tag and not field
        flat.remove("account");

        // just in case remove all null values
        Map valid = RemoveNullValues.fromMap(flat);

        return Point.measurement(this.getClass().getSimpleName()).tag("account", account).fields(valid);
    }

    //======= Getters and Setters
    public String getAccount() {
        return account;
    }
    public void setAccount(String account) {
        this.account = account;
    }

    public Double getAmount() {
        return amount;
    }
    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public List<BillEntry> getBills() {
        return bills;
    }
    public void setBills(List<BillEntry> bills) {
        this.bills = bills;
    }

    public Long getFrom() {
        return from;
    }
    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }
    public void setTo(Long to) {
        this.to = to;
    }
}

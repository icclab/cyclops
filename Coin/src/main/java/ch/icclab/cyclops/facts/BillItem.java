package ch.icclab.cyclops.facts;
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

import java.util.*;

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 01.06.17
 * Description: Bill item with link to its parent and children nodes
 */
public class BillItem {
    private transient BillItem parent;
    private double charge = 0;
    private Object data;

    /**
     * Constructor for the root element
     * @param children List or Map
     * @param records charge
     */
    public BillItem(Object children, Map<String, List<Charge>> records) {
        processBeingNodeElement(children, records);
    }

    /**
     * Constructor for a node element
     * @param children List or Map
     * @param records charge
     * @param parent link
     */
    public BillItem(Object children, Map<String, List<Charge>> records, BillItem parent) {
        this.parent = parent;
        processBeingNodeElement(children, records);
    }

    /**
     * Constructor for a leaf element
     * @param records list of charge data
     * @param parent link
     */
    public BillItem(List<Charge> records, BillItem parent) {
        this.parent = parent;
        processBeingLeafElement(records);
    }

    /**
     * Constructor for a leaf element
     * @param records list of charge data
     */
    public BillItem(List<Charge> records) {
        processBeingLeafElement(records);
    }

    /**
     * Process being a node element
     * @param children list or map
     * @param records charge
     */
    private void processBeingNodeElement(Object children, Map<String, List<Charge>> records) {
        Map<String, BillItem> container = new HashMap<>();

        // an array of children
        if (children instanceof List) {
            List list = (List) children;

            // iterate over all of them
            for (Object item: list) {
                // end leaf
                if (item instanceof String) {
                    String name = (String) item;
                    container.put(name, new BillItem(records.get(name), this));
                } else if (item instanceof Map) {
                    Map.Entry<String, Object> entry = (Map.Entry<String, Object>) ((Map) item).entrySet().iterator().next();
                    String name = entry.getKey();
                    container.put(name, new BillItem(entry.getValue(), records, this));
                }
            }
        } else if (children instanceof Map) {
            // iterate over individual accounts
            Iterator<Map.Entry<String, Object>> it = ((Map) children).entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                String name = entry.getKey();
                container.put(name, new BillItem(entry.getValue(), records, this));
            }
        }

        // attach result to the data
        if (!container.isEmpty()) data = container;
    }

    /**
     * Process being a leaf element
     * @param records charge
     */
    private void processBeingLeafElement(List<Charge> records) {
        // means that there is something to process
        if (records != null && !records.isEmpty()) {
            List<Charge> items = new ArrayList<>();

            // go over the list and fill Bill Item object
            for (Charge item: records) {
                // update the charge
                charge += item.getCharge();

                // we don't care about these fields anymore
                item.setAccount(null);
                item.setCurrency(null);

                // add it to the list
                items.add(item);
            }

            // assign the list of records to data field
            data = items;

            // recursively update parents with the calculated charge difference
            if (parent != null) parent.update(charge);
        }
    }

    /**
     * Recursively update charge
     * @param add to charge
     */
    public void update(double add) {
        charge += add;

        if (parent != null) parent.update(add);
    }

    /**
     * Is the bill item a root element?
     * @return true/false
     */
    public boolean isRoot() {
        return parent == null;
    }

    public double getCharge() {
        return charge;
    }
    public Object getData() {
        return data;
    }
}

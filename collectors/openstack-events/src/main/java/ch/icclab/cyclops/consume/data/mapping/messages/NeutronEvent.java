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
package ch.icclab.cyclops.consume.data.mapping.messages;

/**
 * Author: Oleksii Serhiienko
 * Updated on: 26-Aug-16
 * Description: This class holds the Neutron event data
 */
public class NeutronEvent {
    private String event_type;
    private String _context_timestamp;
    private String _context_tenant_id;
    private Payload payload;

    public String getEvent_type() {
        return event_type;
    }

    public String get_context_timestamp() {
        return _context_timestamp;
    }

    public String get_context_tenant_id() {
        return _context_tenant_id;
    }

    public Payload getPayload() {
        return payload;
    }

    public class Payload  {
        String floatingip_id;
        FloatingIP floatingip;

        public String getFloatingip_id() {
            return floatingip_id;
        }

        public FloatingIP getFloatingip() {
            return floatingip;
        }

        public class FloatingIP {
            String id;
            String floating_ip_address;
            public String getId() {
                return id;
            }
            public String getFloating_ip_address() { return floating_ip_address; }

        }
    }
}

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
    public String getEvent_type() {
        return event_type;
    }

    public void setEvent_type(String event_type) {
        this.event_type = event_type;
    }

    public String get_context_timestamp() {
        return _context_timestamp;
    }

    public void set_context_timestamp(String _context_timestamp) {
        this._context_timestamp = _context_timestamp;
    }

    public String get_context_tenant_name() {
        return _context_tenant_name;
    }

    public void set_context_tenant_name(String _context_tenant_name) {
        this._context_tenant_name = _context_tenant_name;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public String event_type;
    public String _context_timestamp;
    public String _context_tenant_name;
    public Payload payload;
    public class Payload  {
        public String getFloatingip_id() {
            return floatingip_id;
        }

        public void setFloatingip_id(String floatingip_id) {
            this.floatingip_id = floatingip_id;
        }

        public String floatingip_id;

        public FloatingIP getFloatingip() {
            return floatingip;
        }

        public void setFloatingip(FloatingIP floatingip) {
            this.floatingip = floatingip;
        }

        public FloatingIP floatingip;
        public class FloatingIP {
            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }
            public String id;
        }
    }
}

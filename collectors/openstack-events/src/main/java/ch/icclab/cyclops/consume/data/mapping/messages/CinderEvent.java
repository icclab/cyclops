package ch.icclab.cyclops.consume.data.mapping.messages;

/*
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
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

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Oleksii Serhiienko
 * Updated on: 26-Aug-16
 * Description: This class holds the Neutron event data
 */
public class CinderEvent {
    @SerializedName("oslo.message")
    private String osloMessage;
    private Gson mapper = new Gson();

    public class OsloMessage {
        private String event_type;
        private String timestamp;
        private String _context_tenant;
        private Payload payload;

        public String get_context_tenant() {
            return _context_tenant;
        }

        public String getEvent_type() {
            return event_type;
        }

        public String get_timestamp() {
            return timestamp;
        }

        public Payload getPayload() {
            return payload;
        }

        public class Payload  {
            private Double size;
            private String volume_id;
            private String display_name;
            private ArrayList<InstanceAttachment> volume_attachment;

            public ArrayList<InstanceAttachment> getVolume_attachment() { return volume_attachment; }

            public Double getSize() {
                return size;
            }

            public String getVolume_id() {
                return volume_id;
            }

            public String getDisplay_name() { return display_name; }

            public class InstanceAttachment{
                private String instance_uuid;

                public String getInstance_uuid() { return instance_uuid; }
            }
        }
    }
    public OsloMessage getOsloMessage() {
        return mapper.fromJson(osloMessage, OsloMessage.class);
    }
}
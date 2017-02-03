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

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Author: Oleksii
 * Date: 01/04/2016
 * Description: This class holds the NovaEvent data
 */
public class NovaEvent {

    @SerializedName("oslo.message")
    private String osloMessage;
    private Gson mapper = new Gson();

    public class OsloMessage {
        public Args getArgs() {
            return args;
        }

        Args args;
        String _context_project_id;
        String _context_timestamp;

        public String get_context_project_id() {
            return _context_project_id;
        }

        public String get_context_timestamp() { return _context_timestamp; }

        public class Args {
            ObjInst objinst;

            public String getObjmethod() {
                return objmethod;
            }

            String objmethod;

            public Kwargs getKwargs() {
                return kwargs;
            }

            Kwargs kwargs;

            public ObjInst getObjinst() {
                return objinst;
            }

            public class ObjInst {

                @SerializedName("nova_object.name")
                private String nova_objectName;

                public Nova_objectData getNova_objectData() {
                    return nova_objectData;
                }

                public String getNova_objectName() {
                    return nova_objectName;
                }

                @SerializedName("nova_object.data")
                private Nova_objectData nova_objectData;

                public class Nova_objectData{
                    String uuid;
                    String display_name;
                    Double memory_mb;
                    Double vcpus;
                    Double root_gb;
                    Double ephemeral_gb;
                    SystemMetadata system_metadata;

                    public String getUuid(){ return uuid; }

                    public String getDisplay_name(){ return display_name; }

                    public Double getMemory_mb(){ return memory_mb; }

                    public Double getVcpus(){ return vcpus; }

                    public Double getEphemeral_gb() { return ephemeral_gb; }

                    public Double getRoot_gb() { return root_gb; }

                    public SystemMetadata getSystem_metadata(){ return system_metadata; }

                    public class SystemMetadata {
                        String image_description;

                        String image_base_image_ref;

                        String image_os_version;

                        public String getImage_description() { return image_description; }

                        public String getImage_os_version() { return image_os_version; }

                        public String getImage_base_image_ref() { return image_base_image_ref; }

                    }
                }
            }

            public class Kwargs {
                Object expected_task_state;

                public Object getExpected_task_state() {
                    return expected_task_state;
                }
            }
        }
    }
    public OsloMessage getOsloMessage() {
        return mapper.fromJson(osloMessage, OsloMessage.class);
    }
}


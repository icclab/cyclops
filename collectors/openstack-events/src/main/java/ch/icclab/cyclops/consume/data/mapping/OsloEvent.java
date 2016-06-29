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

package ch.icclab.cyclops.consume.data.mapping;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Author: Oleksii
 * Date: 01/04/2016
 * Description: This class holds the OsloEvent data
 */
public class OsloEvent {

    @SerializedName("oslo.message")
    private String osloMessage;
    private Gson mapper = new Gson();

    public class OsloMessage {
        public Args getArgs() {
            return args;
        }


        Args args;
        String _context_user_name;

        public String get_context_user_name() {
            return _context_user_name;
        }

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
                    Double memory_mb;
                    Double vcpus;

                    public String getUuid(){ return uuid; }

                    public Double getMemory_mb(){ return memory_mb; }

                    public Double getVcpus(){ return vcpus;}

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


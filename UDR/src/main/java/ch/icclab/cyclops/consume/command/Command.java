package ch.icclab.cyclops.consume.command;
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

/**
 * Author: Martin Skoviera (linkedin.com/in/skoviera)
 * Created: 07/03/16
 * Description: Pojo object for automatic GSON mapping of Commands
 */
public abstract class Command {
    static String FIELD_FOR_MAPPING = "command";
    private String command;

    public enum Execution {
        SUCCESS, SERVER_ERROR, CLIENT_ERROR, NOT_SET
    }

    public static class Status {
        private Execution executed;
        private String description;
        private Object output;

        public Status() {
            executed = Execution.NOT_SET;
            description = "Command either not executed, or did not explicitly return an execution status";
            output = null;
        }

        public void setServerError(String error) {
            executed = Execution.SERVER_ERROR;
            description = error;
        }

        public void setClientError(String error) {
            executed = Execution.CLIENT_ERROR;
            description = error;
        }

        public void setSuccessful(String reason) {
            executed = Execution.SUCCESS;
            description = reason;
        }
        public void setSuccessful(String reason, Object object) {
            executed = Execution.SUCCESS;
            description = reason;
            output = object;
        }

        public boolean hadServerError() {
            return executed == Execution.SERVER_ERROR;
        }
        public boolean hadClientError() {
            return executed == Execution.CLIENT_ERROR;
        }
        public boolean hasSucceeded() {
            return executed == Execution.SUCCESS;
        }

        public String getDescription() {
            return description;
        }
        public Object getOutput() {
            return output;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s", executed, description);
        }
    }

    /**
     * Every command has to implement execute method
     * @return Status of the executed
     */
    abstract Status execute();

    //===== Getters and Setters
    String getCommand() {
        return command;
    }
    void setCommand(String command) {
        this.command = command;
    }
}

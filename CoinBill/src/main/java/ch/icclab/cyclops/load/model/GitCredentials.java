package ch.icclab.cyclops.load.model;
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
public class GitCredentials {
    // These fields correspond with the configuration file
    private String gitRepo;
    private String gitUsername;
    private String gitPassword;
    private String gitProjectPath;

    //==== Getters and Setters
    public String getGitRepo() {
        return gitRepo;
    }
    public void setGitRepo(String gitRepo) {
        this.gitRepo = gitRepo;
    }

    public String getGitUsername() {
        return gitUsername;
    }
    public void setGitUsername(String gitUsername) {
        this.gitUsername = gitUsername;
    }

    public String getGitPassword() {
        return gitPassword;
    }
    public void setGitPassword(String gitPassword) {
        this.gitPassword = gitPassword;
    }

    public String getGitProjectPath() {
        return gitProjectPath;
    }
    public void setGitProjectPath(String gitProjectPath) {
        this.gitProjectPath = gitProjectPath;
    }

}

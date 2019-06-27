package ch.icclab.cyclops.endpoint;
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

import ch.icclab.cyclops.persistence.DatabaseException;
import ch.icclab.cyclops.persistence.orm.InstanceORM;
import ch.icclab.cyclops.publish.Messenger;
import ch.icclab.cyclops.resource.InstanceResource;
import ch.icclab.cyclops.rule.RuleException;
import ch.icclab.cyclops.rule.RuleManagement;
import ch.icclab.cyclops.util.PrettyGson;
import ch.icclab.cyclops.util.loggers.TimelineLogger;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.load.model.GitCredentials;
import ch.icclab.cyclops.load.model.RollbackEndpoints;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.RepositoryFileApi;
import org.gitlab4j.api.TagsApi;
import org.gitlab4j.api.models.RepositoryFile;
import org.gitlab4j.api.models.Tag;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.lang.*;

public class NewRuleEndpoint extends ServerResource {
    // logger
    final static Logger logger = LogManager.getLogger(RuleEndpoint.class.getName());
    GitCredentials credentials = Loader.getSettings().getGitCredentials();
    RollbackEndpoints endpoints = Loader.getSettings().getRollbackEndpoints();
    private String repo = credentials.getGitRepo();
    private String fileName = "Static rule for ram";
    private String user = credentials.getGitUsername();
    private String password = credentials.getGitPassword();
    private String projectPath = credentials.getGitProjectPath();
    private String billingurl = endpoints.getBillingendpoint();

    private class FlushUDRs {
        private String command;
        private Long time_from;
        private Long time_to;

        public FlushUDRs(Long time_from, Long time_to) {
            this.command = getClass().getSimpleName();
            this.time_from = time_from;
            this.time_to = time_to;
        }
    }

    private class DeleteCDRs {
        private String command;
        private Long time_from;
        private Long time_to;

        public DeleteCDRs(Long time_from, Long time_to) {
            this.command = getClass().getSimpleName();
            this.time_from = time_from;
            this.time_to = time_to;
        }
    }

    private class DeleteBills {
        private String command;
        private Long time_from;
        private Long time_to;

        public DeleteBills(Long time_from, Long time_to) {
            this.command = getClass().getSimpleName();
            this.time_from = time_from;
            this.time_to = time_to;
        }
    }

    private class GenerateBill {
        private String command;
        private Long time_from;
        private Long time_to;
        private String request;

        public GenerateBill(Long time_from, Long time_to, String request) {
            this.command = getClass().getSimpleName();
            this.time_from = time_from;
            this.time_to = time_to;
            this.request = request;
        }
    }

    @Post
    public String processPostRequest(Representation entity) {
        // will be filled once dispatched
        String response = "";

        try {
            String payload = entity.getText();
            System.out.println("Representation entity: " + payload);
            JsonArray touchedFiles = getTouchedFiles(payloadToGson(payload));
            for(JsonElement element : touchedFiles ) {


                GitLabApi gitLabApi = GitLabApi.login(repo, user, password);
                RepositoryFileApi repositoryFileApi = gitLabApi.getRepositoryFileApi();
                RepositoryFile repositoryFile = repositoryFileApi.getFile(getProjectId(payload), element.getAsString(), getProjectRef(payload));
                String newRule = repositoryFile.getDecodedContentAsString();

                InstanceResource resource = new InstanceResource();
                try {
                    resource.removeInstances(element.getAsString());
                } catch (RuleException | DatabaseException e) {
                    e.printStackTrace();
                }

                try {
                    response = processAdd(newRule);
                    //Code to roll back CDRs and bills:

                    //Step 1: Delete affected CDRs:

                    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                    Long time_from = getTime(payload);
                    Long time_to = System.currentTimeMillis();
                    DeleteCDRs deleteCDRs = new DeleteCDRs(time_from,time_to);
                    Messenger.publish(deleteCDRs,"CDR");

                    //Step 2: Retrieve affected Bills (time window and account):

                    HttpGet getrequest = new HttpGet( billingurl + "bill?time_from=" + time_from.toString());
                    HttpResponse result = httpClient.execute(getrequest);

                    String json = EntityUtils.toString(result.getEntity(), "UTF-8");
                    JsonArray touchedBills = getTouchedBills(json);

                    //Step3 : Delete affected Bills:

                    DeleteBills deleteBills = new DeleteBills(time_from,time_to);
                    Messenger.publish(deleteBills,"Billing");

                    //Step4: Flush UDRs:

                    FlushUDRs flushUDRs = new FlushUDRs(time_from,time_to);
                    Messenger.publish(flushUDRs,"UDR");
                    Thread.sleep(500);

                    //Step5: Generate Bill (use time window and account of retrieved bills):

                    for (int i = 0; i < touchedBills.size(); i++) {
                        JsonObject bill = touchedBills.get(i).getAsJsonObject();
                        Long bill_from = bill.get("time_from").getAsLong();
                        Long bill_to = bill.get("time_to").getAsLong();
                        String bill_account = bill.get("account").getAsString();
                        GenerateBill generateBill = new GenerateBill(bill_from,bill_to,bill_account);
                        Messenger.publish(generateBill,"Billing");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println("Added/updated rule file content:\n" + newRule);
            }
        }  catch (GitLabApiException | IOException e) {
            e.printStackTrace();
        }

        return response;
    }
    @Get
    public String processGetRequest(Representation entity) {
        String response = "";
        List<String> tagNmaesList = new ArrayList<>();
        try {
            GitLabApi gitLabApi = GitLabApi.login(repo, user, password);
            TagsApi tagsApi = gitLabApi.getTagsApi();
            List<Tag> tagsList = tagsApi.getTags(projectPath);
            for (Tag tag :
                    tagsList) {
                tagNmaesList.add(tag.getName());
            }
            response = PrettyGson.toJson(tagNmaesList);
        } catch (GitLabApiException e) {
            e.printStackTrace();
        }
        return response;
    }
    private int getProjectId(String query) throws IOException {
        JsonObject pushEvent = payloadToGson(query);
        JsonElement projectId = pushEvent.get("project_id");
        return projectId.getAsInt();
    }
    private Long getTime(String query) throws IOException {
        JsonObject pushEvent = payloadToGson(query);
        JsonElement time = pushEvent.get("time_from");
        return time.getAsLong();
    }
    private String getProjectRef(String query) throws IOException {
        JsonObject pushEvent = payloadToGson(query);
        JsonElement projectId = pushEvent.get("ref");
        return projectId.getAsString();
    }

    private JsonArray getTouchedBills(String query) throws IOException {
        JsonObject pushEvent = payloadToGson(query);
        JsonArray data = pushEvent.get("data").getAsJsonArray();
        return data;
    }
    private JsonObject payloadToGson(String payload) throws UnsupportedEncodingException {
        String formattedPayload = URLDecoder.decode(payload, "utf-8").replaceFirst("payload=", "");
        Gson gson = new Gson();
        return gson.fromJson(formattedPayload, JsonObject.class);
    }
    private JsonArray getTouchedFiles(JsonObject jsonObject){
        JsonArray result = new JsonArray();
        JsonArray commits = jsonObject.get("commits").getAsJsonArray();
        for (JsonElement element :
                commits) {
            result.addAll(element.getAsJsonObject().get("added").getAsJsonArray());
            result.addAll(element.getAsJsonObject().get("modified").getAsJsonArray());
        }
        return result;
    }
    /**
     * Process ADD command and store provided template
     * @param entity as template
     * @return string message
     */
    public String processAdd(String entity) throws Exception {
        InstanceResource resource = new InstanceResource();

        InstanceORM instance = resource.addRule(entity);
        String response = String.format("Rule \"%s\" added as id %d", instance.getName(), instance.getId());
        logger.trace(response);

        // also log it into timeline
        TimelineLogger.log(response);

        // in case that user asked to execute rules now
        String execute = getQueryValue("execute");

        // in case user wants rule instance to be executed immediately
        if (execute != null && Boolean.valueOf(execute)) {
            RuleManagement management = RuleManagement.getInstance();

            // execute rules based on present facts
            List list = management.fireAllRulesNow();

            // optionally return the response
            if (list != null) {
                if (!list.isEmpty()) response = PrettyGson.toJson(list);
                else response = String.format("Rule \"%s\" executed and added as id %d", instance.getName(), instance.getId());
            }
        }

        return response;
    }

}

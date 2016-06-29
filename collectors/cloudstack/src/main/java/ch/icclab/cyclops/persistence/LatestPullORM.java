package ch.icclab.cyclops.persistence;
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

import javax.persistence.*;

/**
 * Author: Skoviera
 * Created: 23/05/16
 * Description:
 */
@Entity
public class LatestPullORM {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    public LatestPullORM() {
    }

    public LatestPullORM(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    private Long timeStamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}

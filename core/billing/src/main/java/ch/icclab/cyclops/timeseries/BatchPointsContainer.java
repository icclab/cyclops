package ch.icclab.cyclops.timeseries;
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

import ch.icclab.cyclops.load.model.InfluxDBCredentials;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Skoviera
 * Created: 28/06/16
 * Description: Batch session for InfluxDB
 */
public class BatchPointsContainer {
    // if you ever run addPoint in parallel stream, make sure points are encapsulated with Collections.synchronizedList()
    private List<Point.Builder> points = new ArrayList<>();

    public void addPoint(Point.Builder builder) {
        points.add(builder.addField(InfluxDBCredentials.COUNTER_FIELD_NAME, true));
    }

    public BatchPoints getPoints() {
        // get empty container
        BatchPoints container = new InfluxDBClient().getEmptyContainer();

        // iterate over points
        for (Point.Builder builder : points) {
            // build the point
            container.point(builder.build());
        }

        return container;
    }

    public Integer size() {
        return points.size();
    }

    public Point.Builder getFirstPoint() {
        return (size() > 0) ? points.get(0): null;
    }
}

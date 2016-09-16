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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Author: Skoviera
 * Created: 17/05/16
 * Description: Query Builder for InfluxDB SQL
 */
public class QueryBuilder {

    private final static String COLUMN_TIME = "time";
    private final static String GREATER_EQUAL = ">=";
    private final static String LOWER_EQUAL = "<=";
    private final static String GREATER_THAN = ">";
    private final static String LOWER_THAN = "<";

    private String measurement = "";
    private List<String> selectedFields = new ArrayList<>();
    private List<String> whereFields = new ArrayList<>();
    private List<String> groupByFields = new ArrayList<>();
    private Integer limit = null;
    private Integer offset = null;
    private ORDER order = ORDER.ASC;
    private enum ORDER {
        ASC, DESC
    }

    private TYPE type;
    private enum TYPE {
        SELECT, MEASUREMENTS
    }

    /**
     * Iteratively build Query
     * @param mes to be selected on
     */
    public QueryBuilder(String mes) {
        type = TYPE.SELECT;
        measurement = addDoubleQuotes(mes);
    }

    /**
     * Internally create a QueryBuilder of certain type
     * @param type of builder
     */
    private QueryBuilder(TYPE type){
        this.type = type;
    }


    /**
     * Get QueryBuilder for list of Measurements
     * @return QueryBuilder
     */
    public static QueryBuilder getMeasurementsQuery() {
        return new QueryBuilder(TYPE.MEASUREMENTS);
    }

    /**
     * Finalise Query
     * @return String
     */
    public String build() {
        String query = null;

        switch (type) {
            case SELECT:
                query = buildSelect();
                break;

            case MEASUREMENTS:
                query = buildShowMeasurements();
                break;
        }

        return query;
    }

    /**
     * Build SELECT command
     * @return String
     */
    private String buildSelect() {
        StringBuilder command = new StringBuilder(String.format("SELECT %s FROM %s", getSelect(selectedFields), measurement));

        // add WHERE clause
        if (!whereFields.isEmpty()) {
            command.append(String.format(" WHERE %s", getWhere(whereFields)));
        }

        // add GROUP BY clause
        if (!groupByFields.isEmpty()){
            command.append(String.format(" GROUP BY %s", getGroupBy(groupByFields)));
        }

        // if ORDERED BY DESC
        if (order == ORDER.DESC) {
            command.append(" ORDER BY time DESC");
        }

        // add LIMIT clause
        if (limit != null && limit > 0) {
            command.append(String.format(" LIMIT %d", limit));
        }

        // add SEEK clause
        if (offset != null && offset > 0) {
            command.append(String.format(" OFFSET %d", offset));
        }

        return command.toString();
    }

    /**
     * Build query for showing list of measurements
     * @return String
     */
    private String buildShowMeasurements() {
        return "SHOW MEASUREMENTS";
    }

    /**
     * Add fields to SELECT
     * @param fields to be selected
     * @return QueryBuilder
     */
    public QueryBuilder select(String ... fields) {
        for (String field: fields) {
            selectedFields.add(addDoubleQuotes(field));
        }
        return this;
    }

    /**
     * Add COUNT to selected fields
     * @param field to perform counting on
     * @return QueryBuilder
     */
    public QueryBuilder count(String field) {
        selectedFields.add(String.format("COUNT(%s)", addDoubleQuotes(field)));
        return this;
    }

    /**
     * Add SUM to selected fields
     * @param field to perform summing on
     * @return QueryBuilder
     */
    public QueryBuilder sum(String field) {
        selectedFields.add(String.format("SUM(%s)", addDoubleQuotes(field)));
        return this;
    }

    /**
     * Reset list of selected fields
     * @return QueryBuilder
     */
    public QueryBuilder resetSelectClause() {
        selectedFields.clear();
        return this;
    }

    /**
     * Access fields separated with comma
     * @param list to work with
     * @return string list of fields
     */
    private String getSelect(List<String> list) {
        return (list.isEmpty())? "*": String.join(",", list);
    }

    /**
     * Construct content for WHERE clause
     * @param list to work with
     * @return string
     */
    private String getWhere(List<String> list) {
        return (list.isEmpty())? "": String.join(" AND ", list);
    }

    /**
     * Internally build normalised WHERE statement
     * @param key field/tag
     * @param delimiter condition
     * @param value to be used
     * @return QueryBuilder
     */
    private QueryBuilder appendToWhere(String key, String delimiter, Object value) {
        String rightSide;

        // make sure we are adding numbers and string correctly
        if (value instanceof Number) {
            rightSide = value.toString();
        } else {
            rightSide = checkSingleQuotes((String) value);
        }

        // add it to WHERE
        whereFields.add(String.format("%s %s %s", addDoubleQuotes(key), delimiter, rightSide));

        return this;
    }
    public QueryBuilder where(String key, String delimiter, Object value){
        return appendToWhere(key, delimiter, value);
    }

    public QueryBuilder where(String key, Object value){
        return appendToWhere(key, "=", value);
    }

    public QueryBuilder and(String key, String delimiter, Object value){
        return appendToWhere(key, delimiter, value);
    }
    public QueryBuilder and(String key, Object value){
        return appendToWhere(key, "=", value);
    }

    /**
     * Add TIME FROM constraint
     * @param time mark
     * @param unit of time
     * @return QueryBuilder
     */
    public QueryBuilder timeFrom(Long time, TimeUnit unit) {
        return time(time, unit, GREATER_EQUAL);
    }
    public QueryBuilder afterTime(Long time, TimeUnit unit) {
        return time(time, unit, GREATER_THAN);
    }


    /**
     * Add TIME TO constraint
     * @param time mark
     * @param unit of time
     * @return QueryBuilder
     */
    public QueryBuilder timeTo(Long time, TimeUnit unit) {
        return time(time, unit, LOWER_EQUAL);
    }
    public QueryBuilder beforeTime(Long time, TimeUnit unit) {
        return time(time, unit, LOWER_THAN);
    }

    private QueryBuilder time(Long time, TimeUnit unit, String delimiter) {
        String from = String.format("%d%s", time, getTimeDurationLetter(unit));
        whereFields.add(String.format("%s %s %s", addDoubleQuotes(COLUMN_TIME), delimiter, from));
        return this;
    }

    /**
     * GROUP BY clause
     * @param keys to group by
     * @return QueryBuilder
     */
    public QueryBuilder groupBy(String ... keys) {
        for (String key : keys) {
            groupByFields.add(addDoubleQuotes(key));
        }
        return this;
    }

    /**
     * Access GroupBy fields
     * @param list to be used
     * @return String or null
     */
    private String getGroupBy(List<String> list) {
        return String.join(",", list);
    }


    /**
     * Default ORDER is ASC, change it to DESC
     * @return QueryBuilder
     */
    public QueryBuilder orderDesc() {
        order = ORDER.DESC;
        return this;
    }

    /**
     * LIMIT clause
     * @param lmt as Integer
     * @return QueryBuilder
     */
    public QueryBuilder limit(Integer lmt) {
        limit = lmt;
        return this;
    }

    /**
     * OFFSET clause
     * @param off as Integer
     * @return QueryBuilder
     */
    public QueryBuilder offset(Integer off) {
        offset = off;
        return this;
    }

    /**
     * InfluxDB SQL queries have to have double quotes if they contain spaces
     * @param original string
     * @return normalised string
     */
    private String addDoubleQuotes(String original) {
        return (!original.startsWith("\""))? String.format("\"%s\"", original): original;
    }

    /**
     * InfluxDB SQL queries have to have single quotes around values
     * @param original string
     * @return normalised string
     */
    private String checkSingleQuotes(String original) {
        return (!original.startsWith("'"))? String.format("'%s'", original): original;
    }

    /**
     * Get TimeUnit's abbreviation for InfluxDB
     * @param unit to be used
     * @return String
     */
    private String getTimeDurationLetter(TimeUnit unit) {
        switch (unit) {
            case NANOSECONDS:
                return "u";
            case MICROSECONDS:
                return "u";
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            case MINUTES:
                return "m";
            case HOURS:
                return "h";
            case DAYS:
                return "d";
            default:
                return "s";
        }
    }
}

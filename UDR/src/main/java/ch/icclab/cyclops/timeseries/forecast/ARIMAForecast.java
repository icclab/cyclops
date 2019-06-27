package ch.icclab.cyclops.timeseries.forecast;
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
import com.workday.insights.timeseries.arima.Arima;
import com.workday.insights.timeseries.arima.struct.ArimaParams;
/**
 * Author: Panagiotis Gkikopoulos
 * Created: 11/06/2019
 * Description: Generate forecast using ARIMA model
 */
public class ARIMAForecast {
    public double[] getForecast(double[] metrics, int p, int d, int q, int P, int D, int Q, int m, int forecastSize){
        return Arima.forecast_arima(metrics,forecastSize,new ArimaParams(p, d, q, P, D, Q, m)).getForecast();
    }
}

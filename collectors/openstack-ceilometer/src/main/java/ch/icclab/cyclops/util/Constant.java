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
package ch.icclab.cyclops.util;

import java.util.HashMap;
import java.util.Map;

public class Constant {

    // Full meter selection support keyword
    public static final String FULL_METER_SELECTION = "*";

    // Ceilometer field name
    public static final String CEILOMETER_METER_NAME = "name";

    // Graph type constants
    public static final String GAUGE_GRAPH = "gauge";
    public static final String HISTOGRAM_GRAPH = "histogram";
    public static final String NUMBER_GRAPH = "number";

    // Ceilometer Meter names mapping map
    public static final Map<String, String> METER_NAMES = initializeMeternameMap();
    public static final String modelPath = "ch.icclab.cyclops.model.ceilometerMeasurements.";
    public static final String CEILOMETER_CUMULATIVE_METER = "cumulative";

    // Initialization of the metername map
    private static Map<String, String> initializeMeternameMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("cpu", modelPath + "OpenStackCeilometerCpu");
        map.put("cpu.delta", modelPath + "OpenStackCeilometerCpuDelta");
        map.put("cpu_util", modelPath + "OpenStackCeilometerCpuUtil");
        map.put("disk.allocation", modelPath + "OpenStackCeilometerDiskAllocation");
        map.put("disk.capacity", modelPath + "OpenStackCeilometerDiskCapacity");
        map.put("disk.device.allocation", modelPath + "OpenStackCeilometerDiskDeviceAllocation");
        map.put("disk.device.capacity", modelPath + "OpenStackCeilometerDiskDeviceCapacity");
        map.put("disk.device.read.bytes", modelPath + "OpenStackCeilometerDiskDeviceReadBytes");
        map.put("disk.device.read.bytes.rate", modelPath + "OpenStackCeilometerDiskDeviceReadBytesRate");
        map.put("disk.device.read.requests", modelPath + "OpenStackCeilometerDiskDeviceReadRequests");
        map.put("disk.device.read.requests.rate", modelPath + "OpenStackCeilometerDiskDeviceReadRequestsRate");
        map.put("disk.device.usage", modelPath + "OpenStackCeilometerDiskDeviceUsage");
        map.put("disk.device.write.bytes", modelPath + "OpenStackCeilometerDiskDeviceWriteBytes");
        map.put("disk.device.write.bytes.rate", modelPath + "OpenStackCeilometerDiskDeviceWriteBytesRate");
        map.put("disk.device.write.requests", modelPath + "OpenStackCeilometerDiskDeviceWriteRequests");
        map.put("disk.device.write.requests.rate", modelPath + "OpenStackCeilometerDiskDeviceWriteRequestsRate");
        map.put("disk.read.bytes", modelPath + "OpenStackCeilometerDiskReadBytes");
        map.put("disk.read.bytes.rate", modelPath + "OpenStackCeilometerDiskReadBytesRate");
        map.put("disk.read.requests", modelPath + "OpenStackCeilometerDiskReadRequests");
        map.put("disk.read.requests.rate", modelPath + "OpenStackCeilometerDiskReadRequestsRate");
        map.put("disk.usage", modelPath + "OpenStackCeilometerDiskUsage");
        map.put("disk.write.bytes", modelPath + "OpenStackCeilometerDiskWriteBytes");
        map.put("disk.write.bytes.rate", modelPath + "OpenStackCeilometerDiskWriteBytesRate");
        map.put("disk.write.requests", modelPath + "OpenStackCeilometerDiskWriteRequests");
        map.put("disk.write.requests.rate", modelPath + "OpenStackCeilometerDiskWriteRequestsRate");
        map.put("image", modelPath + "OpenStackCeilometerImage");
        map.put("image.download", modelPath + "OpenStackCeilometerImageDownload");
        map.put("image.serve", modelPath + "OpenStackCeilometerImageServe");
        map.put("image.size", modelPath + "OpenStackCeilometerImageSize");
        map.put("instance", modelPath + "OpenStackCeilometerInstance");
        map.put("ip.floating", modelPath + "OpenStackCeilometerIpFloating");
        map.put("memory.resident", modelPath + "OpenStackCeilometerMemoryResident");
        map.put("memory.usage", modelPath + "OpenStackCeilometerMemoryUsage");
        map.put("network.incoming.bytes", modelPath + "OpenStackCeilometerNetworkIncomingBytes");
        map.put("network.incoming.bytes.rate", modelPath + "OpenStackCeilometerNetworkIncomingBytesRate");
        map.put("network.incoming.packets", modelPath + "OpenStackCeilometerNetworkIncomingPackets");
        map.put("network.incoming.packets.rate", modelPath + "OpenStackCeilometerNetworkIncomingPacketsRate");
        map.put("network.outgoing.bytes", modelPath + "OpenStackCeilometerNetworkOutgoingBytes");
        map.put("network.outgoing.bytes.rate", modelPath + "OpenStackCeilometerNetworkOutgoingBytesRate");
        map.put("network.outgoing.packets", modelPath + "OpenStackCeilometerNetworkOutgoingPackets");
        map.put("network.outgoing.packets.rate", modelPath + "OpenStackCeilometerNetworkOutgoingPacketsRate");
        map.put("storage.objects", modelPath + "OpenStackCeilometerStorageObjects");
        map.put("storage.objects.containers", modelPath + "OpenStackCeilometerStorageObjectsContainers");
        map.put("storage.objects.size", modelPath + "OpenStackCeilometerStorageObjectsSize");
        return map;
    }

}

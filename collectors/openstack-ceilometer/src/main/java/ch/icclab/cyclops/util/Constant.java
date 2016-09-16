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
        map.put("cpu", modelPath + "Cpu");
        map.put("cpu.delta", modelPath + "CpuDelta");
        map.put("cpu_util", modelPath + "CpuUtil");
        map.put("disk.allocation", modelPath + "DiskAllocation");
        map.put("disk.capacity", modelPath + "DiskCapacity");
        map.put("disk.device.allocation", modelPath + "DiskDeviceAllocation");
        map.put("disk.device.capacity", modelPath + "DiskDeviceCapacity");
        map.put("disk.device.read.bytes", modelPath + "DiskDeviceReadBytes");
        map.put("disk.device.read.bytes.rate", modelPath + "DiskDeviceReadBytesRate");
        map.put("disk.device.read.requests", modelPath + "DiskDeviceReadRequest");
        map.put("disk.device.read.requests.rate", modelPath + "DiskDeviceReadRequestRate");
        map.put("disk.device.usage", modelPath + "DiskDeviceUsage");
        map.put("disk.device.write.bytes", modelPath + "DiskDeviceWriteBytes");
        map.put("disk.device.write.bytes.rate", modelPath + "DiskDeviceWriteBytesRate");
        map.put("disk.device.write.requests", modelPath + "DiskDeviceWriteRequests");
        map.put("disk.device.write.requests.rate", modelPath + "DiskDeviceWriteRequestsRate");
        map.put("disk.read.bytes", modelPath + "DiskReadBytes");
        map.put("disk.read.bytes.rate", modelPath + "DiskReadBytesRate");
        map.put("disk.read.requests", modelPath + "DiskReadRequests");
        map.put("disk.read.requests.rate", modelPath + "DiskReadRequestsRate");
        map.put("disk.usage", modelPath + "DiskUsage");
        map.put("disk.write.bytes", modelPath + "DiskWriteBytes");
        map.put("disk.write.bytes.rate", modelPath + "DiskWriteBytesRate");
        map.put("disk.write.requests", modelPath + "DiskWriteRequests");
        map.put("disk.write.requests.rate", modelPath + "DiskWriteRequestsRate");
        map.put("image", modelPath + "Image");
        map.put("image.download", modelPath + "ImageDownload");
        map.put("image.serve", modelPath + "ImageServe");
        map.put("image.size", modelPath + "ImageSize");
        map.put("instance", modelPath + "Instance");
        map.put("ip.floating", modelPath + "IpFloating");
        map.put("memory.resident", modelPath + "MemoryResident");
        map.put("memory.usage", modelPath + "MemoryUsage");
        map.put("network.incoming.bytes", modelPath + "NetworkIncomingBytes");
        map.put("network.incoming.bytes.rate", modelPath + "NetworkIncomingBytesRate");
        map.put("network.incoming.packets", modelPath + "NetworkIncomingPackets");
        map.put("network.incoming.packets.rate", modelPath + "NetworkIncomingPacketsRate");
        map.put("network.outgoing.bytes", modelPath + "NetworkOutgoingBytes");
        map.put("network.outgoing.bytes.rate", modelPath + "NetworkOutgoingBytesRate");
        map.put("network.outgoing.packets", modelPath + "NetworkOutgoingPackets");
        map.put("network.outgoing.packets.rate", modelPath + "NetworkOutgoingPacketsRate");
        map.put("storage.objects", modelPath + "StorageObjects");
        map.put("storage.objects.containers", modelPath + "StorageObjectsContainers");
        map.put("storage.objects.size", modelPath + "StorageObjectsSize");
        return map;
    }

}

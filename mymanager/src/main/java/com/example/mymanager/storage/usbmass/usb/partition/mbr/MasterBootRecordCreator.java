package com.example.mymanager.storage.usbmass.usb.partition.mbr;


import androidx.annotation.Nullable;

import com.example.mymanager.storage.usbmass.usb.driver.BlockDeviceDriver;
import com.example.mymanager.storage.usbmass.usb.partition.PartitionTable;
import com.example.mymanager.storage.usbmass.usb.partition.PartitionTableFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by magnusja on 30/07/17.
 */

public class MasterBootRecordCreator implements PartitionTableFactory.PartitionTableCreator {
    @Nullable
    @Override
    public PartitionTable read(BlockDeviceDriver blockDevice) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Math.max(512, blockDevice.getBlockSize()));
        blockDevice.read(0, buffer);
        return MasterBootRecord.read(buffer);
    }
}

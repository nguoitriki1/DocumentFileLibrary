package com.example.mymanager.storage.usbmass.usb.partition.fs;


import androidx.annotation.Nullable;

import com.example.mymanager.storage.usbmass.usb.driver.BlockDeviceDriver;
import com.example.mymanager.storage.usbmass.usb.driver.ByteBlockDevice;
import com.example.mymanager.storage.usbmass.usb.fs.FileSystemFactory;
import com.example.mymanager.storage.usbmass.usb.partition.PartitionTable;
import com.example.mymanager.storage.usbmass.usb.partition.PartitionTableFactory;

import java.io.IOException;

/**
 * Created by magnusja on 30/07/17.
 */

public class FileSystemPartitionTableCreator implements PartitionTableFactory.PartitionTableCreator {
    @Nullable
    @Override
    public PartitionTable read(BlockDeviceDriver blockDevice) throws IOException {
        try {
            return new FileSystemPartitionTable(blockDevice,
                    FileSystemFactory.createFileSystem(null, new ByteBlockDevice(blockDevice)));
        } catch(FileSystemFactory.UnsupportedFileSystemException e) {
            return null;
        }
    }
}

package com.example.mymanager.storage.usbmass.usb.fs.fat32;


import com.example.mymanager.storage.usbmass.usb.driver.BlockDeviceDriver;
import com.example.mymanager.storage.usbmass.usb.fs.FileSystem;
import com.example.mymanager.storage.usbmass.usb.fs.FileSystemCreator;
import com.example.mymanager.storage.usbmass.usb.partition.PartitionTableEntry;

import java.io.IOException;

/**
 * Created by magnusja on 28/02/17.
 */

public class Fat32FileSystemCreator implements FileSystemCreator {

    @Override
    public FileSystem read(PartitionTableEntry entry, BlockDeviceDriver blockDevice) throws IOException {
        return Fat32FileSystem.read(blockDevice);
    }
}

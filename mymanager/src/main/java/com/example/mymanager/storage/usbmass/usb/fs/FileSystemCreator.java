package com.example.mymanager.storage.usbmass.usb.fs;


import androidx.annotation.Nullable;

import com.example.mymanager.storage.usbmass.usb.driver.BlockDeviceDriver;
import com.example.mymanager.storage.usbmass.usb.partition.PartitionTableEntry;

import java.io.IOException;

/**
 * Created by magnusja on 28/02/17.
 */

public interface FileSystemCreator {
    @Nullable
    FileSystem read(PartitionTableEntry entry, BlockDeviceDriver blockDevice) throws IOException;
}

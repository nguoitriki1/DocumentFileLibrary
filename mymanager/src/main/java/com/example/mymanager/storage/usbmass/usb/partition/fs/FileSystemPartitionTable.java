package com.example.mymanager.storage.usbmass.usb.partition.fs;

import android.util.Log;


import com.example.mymanager.storage.usbmass.usb.driver.BlockDeviceDriver;
import com.example.mymanager.storage.usbmass.usb.fs.FileSystem;
import com.example.mymanager.storage.usbmass.usb.partition.PartitionTable;
import com.example.mymanager.storage.usbmass.usb.partition.PartitionTableEntry;

import java.util.ArrayList;
import java.util.List;

public class FileSystemPartitionTable implements PartitionTable {

    private static final String TAG =  FileSystemPartitionTable.class.getSimpleName();

    List<PartitionTableEntry> entries = new ArrayList<>();

    public FileSystemPartitionTable(BlockDeviceDriver blockDevice, FileSystem fs) {
        Log.i(TAG, "Found a device without partition table, yay!");
        int totalNumberOfSectors = (int) fs.getCapacity() / blockDevice.getBlockSize();
        if (fs.getCapacity() % blockDevice.getBlockSize() != 0) {
            Log.w(TAG, "fs capacity is not multiple of block size");
        }
        entries.add(new PartitionTableEntry(fs.getType(), 0, totalNumberOfSectors));
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public List<PartitionTableEntry> getPartitionTableEntries() {
        return entries;
    }
}

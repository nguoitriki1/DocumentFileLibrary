package com.example.mymanager.storage.usbmass.usb.driver;


import java.io.IOException;
import java.nio.ByteBuffer;


public class ByteBlockDevice implements BlockDeviceDriver {

    private static final String TAG =  ByteBlockDevice.class.getSimpleName();

    private BlockDeviceDriver targetBlockDevice;
    private int logicalOffsetToAdd;
    private int blockSize;

    public ByteBlockDevice(BlockDeviceDriver targetBlockDevice, int logicalOffsetToAdd) {
        this.targetBlockDevice = targetBlockDevice;
        this.blockSize = targetBlockDevice.getBlockSize();
        this.logicalOffsetToAdd = logicalOffsetToAdd;
    }

    public ByteBlockDevice(BlockDeviceDriver targetBlockDevice) {
        this(targetBlockDevice, 0);
    }

    @Override
    public void init() throws IOException {
        targetBlockDevice.init();
    }

    @Override
    public void read(long byteOffset, ByteBuffer dest) throws IOException {
        long devOffset = byteOffset / blockSize + logicalOffsetToAdd;
        // TODO try to make this more efficient by for example making tmp buffer
        // global
        if (byteOffset % blockSize != 0) {
            //Log.w(TAG, "device offset " + offset + " not a multiple of block size");
            ByteBuffer tmp = ByteBuffer.allocate(blockSize);

            targetBlockDevice.read(devOffset, tmp);
            tmp.clear();
            tmp.position((int) (byteOffset % blockSize));
            int limit = Math.min(dest.remaining(), tmp.remaining());
            tmp.limit(tmp.position() + limit);
            dest.put(tmp);

            devOffset++;
        }

        if (dest.remaining() > 0) {
            ByteBuffer buffer;
            if (dest.remaining() % blockSize != 0) {
                //Log.w(TAG, "we have to round up size to next block sector");
                int rounded = blockSize - dest.remaining() % blockSize + dest.remaining();
                buffer = ByteBuffer.allocate(rounded);
                buffer.limit(rounded);
            } else {
                buffer = dest;
            }

            targetBlockDevice.read(devOffset, buffer);

            if (dest.remaining() % blockSize != 0) {
                System.arraycopy(buffer.array(), 0, dest.array(), dest.position(), dest.remaining());
            }

            dest.position(dest.limit());
        }
    }

    @Override
    public void write(long byteOffset, ByteBuffer src) throws IOException {
        long devOffset = byteOffset / blockSize + logicalOffsetToAdd;
        // TODO try to make this more efficient by for example making tmp buffer
        // global
        if (byteOffset % blockSize != 0) {
            //Log.w(TAG, "device offset " + offset + " not a multiple of block size");
            ByteBuffer tmp = ByteBuffer.allocate(blockSize);

            targetBlockDevice.read(devOffset, tmp);
            tmp.clear();
            tmp.position((int) (byteOffset % blockSize));
            int remaining = Math.min(tmp.remaining(), src.remaining());
            tmp.put(src.array(), src.position(), remaining);
            src.position(src.position() + remaining);
            tmp.clear();
            targetBlockDevice.write(devOffset, tmp);

            devOffset++;
        }

        if (src.remaining() > 0) {
            // TODO try to make this more efficient by for example only allocating
            // blockSize and making it global
            ByteBuffer buffer;
            if (src.remaining() % blockSize != 0) {
                //Log.w(TAG, "we have to round up size to next block sector");
                int rounded = blockSize - src.remaining() % blockSize + src.remaining();
                buffer = ByteBuffer.allocate(rounded);
                buffer.limit(rounded);

                // TODO: instead of just writing 0s at the end of the buffer do we need to read what
                // is currently on the disk and save that then?
                System.arraycopy(src.array(), src.position(), buffer.array(), 0, src.remaining());

                src.position(src.limit());
            } else {
                buffer = src;
            }

            targetBlockDevice.write(devOffset, buffer);
        }
    }

    @Override
    public int getBlockSize() {
        return targetBlockDevice.getBlockSize();
    }
} 
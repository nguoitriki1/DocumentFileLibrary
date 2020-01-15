package com.example.mymanager.storage.usbmass

import android.content.Context
import android.hardware.usb.*
import android.util.Log
import com.example.mymanager.storage.usbmass.usb.driver.BlockDeviceDriver
import com.example.mymanager.storage.usbmass.usb.driver.BlockDeviceDriverFactory
import com.example.mymanager.storage.usbmass.usb.fs.UsbFile
import com.example.mymanager.storage.usbmass.usb.partition.Partition
import com.example.mymanager.storage.usbmass.usb.partition.PartitionTable
import com.example.mymanager.storage.usbmass.usb.partition.PartitionTableEntry
import com.example.mymanager.storage.usbmass.usb.partition.PartitionTableFactory
import com.example.mymanager.storage.usbmass.usb.usb.UsbCommunicationFactory
import java.io.IOException
import java.util.*

class StorageUsbMass private constructor(
    private val usbManager: UsbManager,
    val usbDevice: UsbDevice,
    private val usbInterface: UsbInterface,
    private val inEndpoint: UsbEndpoint,
    private val outEndpoint: UsbEndpoint
) {
    val PATH_STORAGE = "/storage/";
    private var deviceConnection: UsbDeviceConnection? = null
    var blockDevice: BlockDeviceDriver? = null
    private var partitionTable: PartitionTable? = null
    private val partitions: MutableList<Partition> = ArrayList()
    private var isInit = false

    @Throws(IOException::class)
    fun init() {
        if (usbManager.hasPermission(usbDevice)) setupDevice() else {
            isInit = false
            throw IllegalStateException("Missing permission to access usb device: $usbDevice")
        }
        isInit = true
    }

    @Throws(IOException::class)
    private fun setupDevice() {
        Log.d(TAG, "setup device")
        deviceConnection = usbManager.openDevice(usbDevice)
        if (deviceConnection == null) {
            throw IOException("deviceConnection is null!")
        }
        val claim = deviceConnection!!.claimInterface(usbInterface, true)
        if (!claim) {
            throw IOException("could not claim interface!")
        }
        val communication = UsbCommunicationFactory.createUsbCommunication(
            deviceConnection,
            outEndpoint,
            inEndpoint
        )
        val b = ByteArray(1)
        deviceConnection!!.controlTransfer(161, 254, 0, usbInterface.id, b, 1, 5000)
        Log.i(TAG, "MAX LUN " + b[0].toInt())
        blockDevice = BlockDeviceDriverFactory.createBlockDevice(communication)
        blockDevice?.init()
        partitionTable = PartitionTableFactory.createPartitionTable(blockDevice)
        initPartitions()
    }

    @Throws(IOException::class)
    private fun initPartitions() {
        val partitionEntrys: Collection<PartitionTableEntry> =
            partitionTable!!.partitionTableEntries
        for (entry in partitionEntrys) {
            val partition =
                Partition.createPartition(
                    entry,
                    blockDevice
                )
            if (partition != null) {
                partitions.add(partition)
            }
        }
    }

    fun close() {
        Log.d(TAG, "close device")
        if (deviceConnection == null) return
        val release = deviceConnection!!.releaseInterface(usbInterface)
        if (!release) {
            Log.e(TAG, "could not release interface!")
        }
        deviceConnection!!.close()
        isInit = false
    }

    fun getPartitions(): List<Partition> {
        return partitions
    }

    companion object {
        private val TAG = StorageUsbMass::class.java.simpleName
        private const val INTERFACE_SUBCLASS = 6
        private const val INTERFACE_PROTOCOL = 80
        fun getMassStorageDevices(context: Context): Array<StorageUsbMass> {
            val usbManager =
                context.getSystemService(Context.USB_SERVICE) as UsbManager
            val result = ArrayList<StorageUsbMass>()
            for (device in usbManager.deviceList.values) {
                Log.i(TAG, "found usb device: $device")
                val interfaceCount = device.interfaceCount
                for (i in 0 until interfaceCount) {
                    val usbInterface = device.getInterface(i)
                    Log.i(
                        TAG,
                        "found usb interface: $usbInterface"
                    )
                    if (usbInterface.interfaceClass != UsbConstants.USB_CLASS_MASS_STORAGE || usbInterface.interfaceSubclass != INTERFACE_SUBCLASS || usbInterface.interfaceProtocol != INTERFACE_PROTOCOL
                    ) {
                        Log.i(
                            TAG,
                            "device interface not suitable!"
                        )
                        continue
                    }
                    val endpointCount = usbInterface.endpointCount
                    if (endpointCount != 2) {
                        Log.w(
                            TAG,
                            "inteface endpoint count != 2"
                        )
                    }
                    var outEndpoint: UsbEndpoint? = null
                    var inEndpoint: UsbEndpoint? = null
                    for (j in 0 until endpointCount) {
                        val endpoint = usbInterface.getEndpoint(j)
                        Log.i(
                            TAG,
                            "found usb endpoint: $endpoint"
                        )
                        if (endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                            if (endpoint.direction == UsbConstants.USB_DIR_OUT) {
                                outEndpoint = endpoint
                            } else {
                                inEndpoint = endpoint
                            }
                        }
                    }
                    if (outEndpoint == null || inEndpoint == null) {
                        Log.e(
                            TAG,
                            "Not all needed endpoints found!"
                        )
                        continue
                    }
                    result.add(
                        StorageUsbMass(
                            usbManager, device, usbInterface, inEndpoint,
                            outEndpoint
                        )
                    )
                }
            }
            return result.toTypedArray()
        }
    }

    fun getNameStorage(): String {
        return usbDevice.deviceName;
    }

    fun getPathStorage(): String {
        return PATH_STORAGE + usbDevice.deviceName;
    }

    fun getTotalStorage() : Long{
      return getPartitions()[0].fileSystem.capacity
    }

    fun getFreeSpace() : Long{
        return getPartitions()[0].fileSystem.freeSpace
    }

    fun getUsageSpace() : Long{
        return getTotalStorage() - getFreeSpace()
    }

    fun getDir() : UsbFile{
       return getPartitions()[0].fileSystem.rootDirectory
    }


}
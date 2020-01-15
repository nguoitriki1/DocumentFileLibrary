package com.example.mymanager.storage.usbmass

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.documentfile.provider.DocumentFile
import com.example.mymanager.hasOTGConnected

class StorageUsbManager(var context: Context) {
    val REQUEST_CODE_PERMISSION_USB_DEVICE = 501
    val ACTION_USB_PERMISSION = ".USB_PERMISSION"
    var documentFile: DocumentFile? = null
    val isUsbConnected: Boolean
        get() = hasOTGConnected(context)
    lateinit var storageDeviceUsbs : MutableList<StorageUsbMass>

    init {
        getListStorageDeviceUsb()
    }

    private fun getListStorageDeviceUsb(): MutableList<StorageUsbMass> {
        storageDeviceUsbs.clear()
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        for (usbDevice in usbManager.deviceList.values) {
            if (isPermissionDevice(usbManager, usbDevice)) {
                val usbMassStorageDevices = StorageUsbMass.getMassStorageDevices(context)
                for (usbMassStorageDevice in usbMassStorageDevices) {
                    try {
                        storageDeviceUsbs.addAll(usbMassStorageDevices)
                    } catch (e: Exception) {
                        throw Exception(e)
                    }
                }
            } else {
                requestPermission(usbManager, context, usbDevice)
            }
        }
        return storageDeviceUsbs
    }

    private fun isPermissionDevice(usbManager: UsbManager, usbDevice: UsbDevice?): Boolean {
        return usbManager.hasPermission(usbDevice)
    }

    private fun requestPermission(usbManager: UsbManager, context: Context, usbDevice: UsbDevice?) {
        usbManager.requestPermission(
            usbDevice,
            PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_PERMISSION_USB_DEVICE,
                Intent(context.applicationContext.getPackageName() + ACTION_USB_PERMISSION),
                0
            )
        )
    }

    fun getName(): String {
        if (documentFile != null) {
            return documentFile!!.name.toString()
        }
        return ""
    }



}
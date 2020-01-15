package com.example.mymanager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbManager
import android.os.storage.StorageManager
import java.lang.reflect.Array

fun getPathInternalStorage(context: Context): String {
    var internalPath: String? = null
    val mStorageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
    var storageVolumeClazz: Class<*>? = null
    try {
        storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
        val getVolumeList =
            mStorageManager.javaClass.getMethod("getVolumeList")
        val getPath = storageVolumeClazz.getMethod("getPath")
        val isRemovable = storageVolumeClazz.getMethod("isRemovable")
        val result = getVolumeList.invoke(mStorageManager)
        val length = Array.getLength(result)
        for (i in 0 until length) {
            val storageVolumeElement = Array.get(result, i)
            val path = getPath.invoke(storageVolumeElement) as String
            val removable = isRemovable.invoke(storageVolumeElement) as Boolean
            if (!removable) {
                internalPath = path
                break
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return internalPath.toString()
}

fun getPathExternalStorage(context: Context): MutableList<String> {
    var listPartSdCard: MutableList<String> = ArrayList<String>()
    val mStorageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
    var storageVolumeClazz: Class<*>? = null
    try {
        storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
        val getVolumeList =
            mStorageManager.javaClass.getMethod("getVolumeList")
        val getPath = storageVolumeClazz.getMethod("getPath")
        val isRemovable = storageVolumeClazz.getMethod("isRemovable")
        val result = getVolumeList.invoke(mStorageManager)
        val length = Array.getLength(result)
        for (i in 0 until length) {
            val storageVolumeElement = Array.get(result, i)
            val path = getPath.invoke(storageVolumeElement) as String
            val removable = isRemovable.invoke(storageVolumeElement) as Boolean
            if (removable) {
                listPartSdCard.add(path)
                break
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return listPartSdCard
}

fun isPermissionReadWriteStorageSystem(context: Context): Boolean {
    val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    val res: Int = context.checkCallingOrSelfPermission(permission)
    return res == PackageManager.PERMISSION_GRANTED
}

fun hasOTGConnected(context: Context): Boolean {
    return try {
        (context.getSystemService(Context.USB_SERVICE) as UsbManager).deviceList.any {
            it.value.getInterface(0).interfaceClass == UsbConstants.USB_CLASS_MASS_STORAGE
        }
    } catch (e: Exception) {
        false
    }
}

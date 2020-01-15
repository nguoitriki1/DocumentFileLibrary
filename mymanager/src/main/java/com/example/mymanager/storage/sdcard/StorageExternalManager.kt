package com.example.mymanager.storage.sdcard

import android.content.Context
import com.example.mymanager.getPathExternalStorage
import com.example.mymanager.isPermissionReadWriteStorageSystem
import java.io.File

class StorageExternalManager(context: Context) {
    var listStorageExternal: MutableList<StorageExternal>? = null

    init {
        if (isPermissionReadWriteStorageSystem(context)) {
            throw Exception("Storage permission is refuse")
        }
        listStorageExternal = ArrayList<StorageExternal>()
        val pathExternalStorage: MutableList<String> = getPathExternalStorage(context)
        pathExternalStorage.forEach {
            val storageExternal = StorageExternal(context)
            storageExternal.file = File(it)
            (listStorageExternal as ArrayList<StorageExternal>).add(storageExternal)
        }
    }

}
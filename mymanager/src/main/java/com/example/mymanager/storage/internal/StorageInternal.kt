package com.example.mymanager.storage.internal

import android.content.Context
import com.example.mymanager.getPathInternalStorage
import com.example.mymanager.isPermissionReadWriteStorageSystem
import com.example.mymanager.model.ItemStorage
import java.io.File


class StorageInternal(context: Context) {
    var file: File? = null

    init {
        if (isPermissionReadWriteStorageSystem(context)) {
            throw Exception("Storage permission is refuse")
        }
        this.file = File(getPathInternalStorage(context))
    }

    val path: String
        get() = file?.absolutePath.toString()
    val name: String?
        get() = file?.name
    val totalSpace: Long?
        get() = file?.totalSpace
    val freeSpace: Long?
        get() = file?.freeSpace
    val listItemStorage: MutableList<ItemStorage>
        get() = createItemStorage()

    private fun createItemStorage(): MutableList<ItemStorage> {
        val listItemStorage: MutableList<ItemStorage> = ArrayList<ItemStorage>()
        if (file?.exists()!!) {
            val listFiles = file!!.listFiles()
            listFiles.forEach {
                val file = ItemStorage(it, false);
                listItemStorage.add(file)
            }
        }
        return listItemStorage
    }
}
package com.example.mymanager.model

import java.io.File

class ItemStorage(var file: File, var isUsbMass: Boolean) {
    val path: String
        get() {
            if (!isUsbMass) {
                return file.absolutePath
            } else {
                return ""
            }
        }

    val listItemStorage: MutableList<ItemStorage>
        get() {
            if (!isUsbMass) {
                val listItemStorage: MutableList<ItemStorage> = ArrayList<ItemStorage>()
                if (file?.exists()!!) {
                    val listFiles = file!!.listFiles()
                    listFiles.forEach {
                        val file = ItemStorage(it, false);
                        listItemStorage.add(file)
                    }
                }
                return listItemStorage
            } else {
                return ArrayList<ItemStorage>()
            }
        }
}
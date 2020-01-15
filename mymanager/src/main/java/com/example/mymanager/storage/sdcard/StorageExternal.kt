package com.example.mymanager.storage.sdcard

import android.content.Context
import java.io.File

class StorageExternal(context: Context) {
    lateinit var file: File

    private fun isPermissionWriteSdCard(): Boolean {
        return file.canWrite()
    }
}
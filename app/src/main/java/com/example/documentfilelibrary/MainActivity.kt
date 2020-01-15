package com.example.documentfilelibrary

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mymanager.storage.usbmass.StorageUsbManager

class MainActivity : AppCompatActivity() {
    lateinit var storageUsb: StorageUsbManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storageUsb = StorageUsbManager(this);
        val storageDeviceUsbs = storageUsb.storageDeviceUsbs
        storageDeviceUsbs.get(0).usbDevice
        val intentFilter = IntentFilter()
        intentFilter.addAction(application.packageName + ".USB_PERMISSION")
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(broadcastReceiver, intentFilter)

    }

    private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null) {
                if (action.contains(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                    if (storageUsb.isUsbConnected){
                        Log.d("abc",storageUsb.getName())
                    }
                } else if (action.contains(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                    Log.d("abc",storageUsb.isUsbConnected.toString())
                }
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }


}

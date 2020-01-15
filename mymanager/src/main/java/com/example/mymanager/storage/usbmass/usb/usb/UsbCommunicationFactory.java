package com.example.mymanager.storage.usbmass.usb.usb;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;


/**
 * Created by magnusja on 21/12/16.
 */

public class UsbCommunicationFactory {

    public enum UnderlyingUsbCommunication {
        USB_REQUEST_ASYNC,
        DEVICE_CONNECTION_SYNC
    }

    private static final String TAG =  UsbCommunicationFactory.class.getSimpleName();

    private static UnderlyingUsbCommunication underlyingUsbCommunication = UnderlyingUsbCommunication.DEVICE_CONNECTION_SYNC;

    public static UsbCommunication createUsbCommunication(UsbDeviceConnection deviceConnection, UsbEndpoint outEndpoint, UsbEndpoint inEndpoint) {

        return new  UsbRequestCommunication(deviceConnection, outEndpoint, inEndpoint);
    }

    public static void setUnderlyingUsbCommunication(UnderlyingUsbCommunication underlyingUsbCommunication) {
         UsbCommunicationFactory.underlyingUsbCommunication = underlyingUsbCommunication;
    }
}

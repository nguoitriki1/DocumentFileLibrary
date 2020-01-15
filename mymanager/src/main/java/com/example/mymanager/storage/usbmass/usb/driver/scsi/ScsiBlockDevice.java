/*
 * (C) Copyright 2014 mjahnen <jahnen@in.tum.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.example.mymanager.storage.usbmass.usb.driver.scsi;

import android.util.Log;


import com.example.mymanager.storage.usbmass.usb.driver.BlockDeviceDriver;
import com.example.mymanager.storage.usbmass.usb.driver.scsi.commands.CommandBlockWrapper;
import com.example.mymanager.storage.usbmass.usb.driver.scsi.commands.CommandStatusWrapper;
import com.example.mymanager.storage.usbmass.usb.driver.scsi.commands.ScsiInquiry;
import com.example.mymanager.storage.usbmass.usb.driver.scsi.commands.ScsiInquiryResponse;
import com.example.mymanager.storage.usbmass.usb.driver.scsi.commands.ScsiRead10;
import com.example.mymanager.storage.usbmass.usb.driver.scsi.commands.ScsiReadCapacity;
import com.example.mymanager.storage.usbmass.usb.driver.scsi.commands.ScsiReadCapacityResponse;
import com.example.mymanager.storage.usbmass.usb.driver.scsi.commands.ScsiTestUnitReady;
import com.example.mymanager.storage.usbmass.usb.driver.scsi.commands.ScsiWrite10;
import com.example.mymanager.storage.usbmass.usb.usb.UsbCommunication;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class ScsiBlockDevice implements BlockDeviceDriver {

	private static final String TAG =  ScsiBlockDevice.class.getSimpleName();

	private UsbCommunication usbCommunication;
	private ByteBuffer outBuffer;
	private ByteBuffer cswBuffer;

	private int blockSize;
	private int lastBlockAddress;

	private ScsiWrite10 writeCommand = new ScsiWrite10();
	private ScsiRead10 readCommand = new ScsiRead10();
	private CommandStatusWrapper csw = new CommandStatusWrapper();

	public ScsiBlockDevice(UsbCommunication usbCommunication) {
		this.usbCommunication = usbCommunication;
		outBuffer = ByteBuffer.allocate(31);
		cswBuffer = ByteBuffer.allocate(CommandStatusWrapper.SIZE);
	}


	@Override
	public void init() throws IOException {
		ByteBuffer inBuffer = ByteBuffer.allocate(36);
		ScsiInquiry inquiry = new ScsiInquiry((byte) inBuffer.array().length);
		transferCommand(inquiry, inBuffer);
		inBuffer.clear();
		// TODO support multiple luns!
		ScsiInquiryResponse inquiryResponse = ScsiInquiryResponse.read(inBuffer);
		Log.d(TAG, "inquiry response: " + inquiryResponse);

		if (inquiryResponse.getPeripheralQualifier() != 0
				|| inquiryResponse.getPeripheralDeviceType() != 0) {
			throw new IOException("unsupported PeripheralQualifier or PeripheralDeviceType");
		}

		ScsiTestUnitReady testUnit = new ScsiTestUnitReady();
		if (!transferCommand(testUnit, null)) {
			Log.w(TAG, "unit not ready!");
		}

		ScsiReadCapacity readCapacity = new ScsiReadCapacity();
		inBuffer.clear();
		transferCommand(readCapacity, inBuffer);
		inBuffer.clear();
		ScsiReadCapacityResponse readCapacityResponse = ScsiReadCapacityResponse.read(inBuffer);
		blockSize = readCapacityResponse.getBlockLength();
		lastBlockAddress = readCapacityResponse.getLogicalBlockAddress();

		Log.i(TAG, "Block size: " + blockSize);
		Log.i(TAG, "Last block address: " + lastBlockAddress);
	}


	private boolean transferCommand(CommandBlockWrapper command, ByteBuffer inBuffer)
			throws IOException {
		byte[] outArray = outBuffer.array();
		Arrays.fill(outArray, (byte) 0);

		outBuffer.clear();
		command.serialize(outBuffer);
		outBuffer.clear();

		int written = usbCommunication.bulkOutTransfer(outBuffer);
		if (written != outArray.length) {
			throw new IOException("Writing all bytes on command " + command + " failed!");
		}

		int transferLength = command.getdCbwDataTransferLength();
		int read = 0;
		if (transferLength > 0) {

			if (command.getDirection() == CommandBlockWrapper.Direction.IN) {
				do {
					read += usbCommunication.bulkInTransfer(inBuffer);
				} while (read < transferLength);

				if (read != transferLength) {
					throw new IOException("Unexpected command size (" + read + ") on response to "
							+ command);
				}
			} else {
				written = 0;
				do {
					written += usbCommunication.bulkOutTransfer(inBuffer);
				} while (written < transferLength);

				if (written != transferLength) {
					throw new IOException("Could not write all bytes: " + command);
				}
			}
		}


		// expecting csw now
		cswBuffer.clear();
		read = usbCommunication.bulkInTransfer(cswBuffer);
		if (read != CommandStatusWrapper.SIZE) {
			throw new IOException("Unexpected command size while expecting csw");
		}
		cswBuffer.clear();

		csw.read(cswBuffer);
		if (csw.getbCswStatus() != CommandStatusWrapper.COMMAND_PASSED) {
			throw new IOException("Unsuccessful Csw status: " + csw.getbCswStatus());
		}

		if (csw.getdCswTag() != command.getdCbwTag()) {
			throw new IOException("wrong csw tag!");
		}

		return csw.getbCswStatus() == CommandStatusWrapper.COMMAND_PASSED;
	}

	/**
	 * This method reads from the device at the specific device offset. The
	 * devOffset specifies at which block the reading should begin. That means
	 * the devOffset is not in bytes!
	 */
	@Override
	public synchronized void read(long devOffset, ByteBuffer dest) throws IOException {
		//long time = System.currentTimeMillis();
		if (dest.remaining() % blockSize != 0) {
			throw new IllegalArgumentException("dest.remaining() must be multiple of blockSize!");
		}

		readCommand.init((int) devOffset, dest.remaining(), blockSize);
		//Log.d(TAG, "reading: " + read);

		transferCommand(readCommand, dest);
		dest.position(dest.limit());

		//Log.d(TAG, "read time: " + (System.currentTimeMillis() - time));
	}

	/**
	 * This method writes from the device at the specific device offset. The
	 * devOffset specifies at which block the writing should begin. That means
	 * the devOffset is not in bytes!
	 */
	@Override
	public synchronized void write(long devOffset, ByteBuffer src) throws IOException {
		//long time = System.currentTimeMillis();
		if (src.remaining() % blockSize != 0) {
			throw new IllegalArgumentException("src.remaining() must be multiple of blockSize!");
		}

		writeCommand.init((int) devOffset, src.remaining(), blockSize);
		//Log.d(TAG, "writing: " + write);

		transferCommand(writeCommand, src);
		src.position(src.limit());

		//Log.d(TAG, "write time: " + (System.currentTimeMillis() - time));
	}

	@Override
	public int getBlockSize() {
		return blockSize;
	}
}

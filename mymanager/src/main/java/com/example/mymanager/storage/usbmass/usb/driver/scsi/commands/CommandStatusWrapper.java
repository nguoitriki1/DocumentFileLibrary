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

package com.example.mymanager.storage.usbmass.usb.driver.scsi.commands;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class represents the command status wrapper (CSW) in the SCSI
 * transparent command set standard, which is transmitted from the device to the
 * host after the data phase (if any).
 * 
 * @author mjahnen
 * 
 */
public class CommandStatusWrapper {

	/**
	 * SCSI command has successfully been executed.
	 */
	public static final int COMMAND_PASSED = 0;

	public static final int COMMAND_FAILED = 1;
	/**
	 * SCSI command could not be executed, host should issue a mass storage
	 * reset.
	 */
	public static final int PHASE_ERROR = 2;

	/**
	 * Every CSW has the same size.
	 */
	public static final int SIZE = 13;

	private static final String TAG = CommandStatusWrapper.class.getSimpleName();

	private static final int D_CSW_SIGNATURE = 0x53425355;

	private int dCswSignature;
	private int dCswTag;
	private int dCswDataResidue;
	private byte bCswStatus;

	/**
	 * Reads command block wrapper from the specified buffer and stores it into this object.
	 * 
	 * @param buffer
	 *            The data where the command block wrapper is located.
	 */
	public void read(ByteBuffer buffer) {
		buffer.order(ByteOrder.LITTLE_ENDIAN);

		dCswSignature = buffer.getInt();
		if (dCswSignature != D_CSW_SIGNATURE) {
			Log.e(TAG, "unexpected dCSWSignature " + dCswSignature);
		}
		dCswTag = buffer.getInt();
		dCswDataResidue = buffer.getInt();
		bCswStatus = buffer.get();
	}


	public int getdCswTag() {
		return dCswTag;
	}

	/**
	 * Returns the amount of bytes which has not been processed yet in the data
	 * phase.
	 * 
	 * @return The amount of bytes.
	 */
	public int getdCswDataResidue() {
		return dCswDataResidue;
	}

	public byte getbCswStatus() {
		return bCswStatus;
	}
}

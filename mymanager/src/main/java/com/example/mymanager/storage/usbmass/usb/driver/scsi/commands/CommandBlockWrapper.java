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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class represents the command block wrapper (CBW) which is always wrapped
 * around a specific SCSI command in the SCSI transparent command set standard.
 * <p>
 * Every SCSI command shall extend this class, call the constructor
 * {@link #CommandBlockWrapper(int, Direction, byte, byte)} with the desired
 * information. When transmitting the command, the
 * {@link #serialize(ByteBuffer)} method has to be called!
 * 
 * @author mjahnen
 * 
 */
public abstract class CommandBlockWrapper {

	/**
	 * The direction of the data phase of the SCSI command.
	 * 
	 * @author mjahnen
	 * 
	 */
	public enum Direction {
		/**
		 * Means from device to host (Android).
		 */
		IN,
		/**
		 * Means from host (Android) to device.
		 */
		OUT,
		/**
		 * There is no data phase
		 */
		NONE
	}

	private static final int D_CBW_SIGNATURE = 0x43425355;

	private int dCbwTag;
	protected int dCbwDataTransferLength;
	private byte bmCbwFlags;
	private byte bCbwLun;
	private byte bCbwcbLength;
	private Direction direction;


	protected CommandBlockWrapper(int transferLength, Direction direction, byte lun,
			byte cbwcbLength) {
		dCbwDataTransferLength = transferLength;
		this.direction = direction;
		if (direction == Direction.IN)
			bmCbwFlags = (byte) 0x80;
		bCbwLun = lun;
		bCbwcbLength = cbwcbLength;
	}

	/**
	 * Serializes the command block wrapper for transmission.
	 * <p>
	 * This method should be called in every subclass right before the specific
	 * SCSI command serializes itself to the buffer!
	 * 
	 * @param buffer
	 *            The buffer were the serialized data should be copied to.
	 */
	public void serialize(ByteBuffer buffer) {
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(D_CBW_SIGNATURE);
		buffer.putInt(dCbwTag);
		buffer.putInt(dCbwDataTransferLength);
		buffer.put(bmCbwFlags);
		buffer.put(bCbwLun);
		buffer.put(bCbwcbLength);
	}


	public int getdCbwTag() {
		return dCbwTag;
	}


	public void setdCbwTag(int dCbwTag) {
		this.dCbwTag = dCbwTag;
	}

	/**
	 * Returns the amount of bytes which should be transmitted in the data
	 * phase.
	 * 
	 * @return The length in bytes.
	 */
	public int getdCbwDataTransferLength() {
		return dCbwDataTransferLength;
	}


	public Direction getDirection() {
		return direction;
	}

}

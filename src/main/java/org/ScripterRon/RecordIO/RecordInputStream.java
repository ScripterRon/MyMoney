/**
 * Copyright 2005-2014 Ronald W Hoffman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ScripterRon.RecordIO;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;

/**
 * The RecordInputStream class provides record-based input using a file
 * input stream.
 */
public final class RecordInputStream extends FileInputStream {

    /**
     * Class instance variables
     */
    private byte[]  stream=null;            // Buffered input stream
    private int     size=8192;              // Size of the stream array
    private int     next=0;                 // Offset to next input byte
    private int     residualLength=0;       // Residual input length
    private boolean endOfData=false;        // End of data reached

    /**
     * Constructor for an input record stream where the input file is
     * specified as a File object
     *
     * @param       file            Input file
     * @exception   IOException     Unable to open input file
     */
    public RecordInputStream(File file) throws IOException {
        super(file);

        //
        // Allocate the stream buffer
        //
        stream = new byte[size];
    }

    /**
     * Constructor for an input record stream where the input file is
     * specified as a String filename
     *
     * @param       filename        Input filename
     * @exception   IOException     Unable to open input file
     */
    public RecordInputStream(String filename) throws IOException {
        super(filename);

        //
        // Allocate the stream buffer
        //
        stream = new byte[size];
    }

    /**
     * Fill the input buffer
     *
     * @exception   IOException     Unable to read from input file
     */
    private void fillBuffer() throws IOException {

        //
        // Move a partial record to the beginning of the stream buffer
        //
        if (residualLength != 0 && next != 0)
            System.arraycopy(stream, next, stream, 0, residualLength);

        next = 0;

        //
        // Fill the buffer unless we are at the end of the file
        //
        if (!endOfData) {
            int offset = next + residualLength;

            if (offset < size) {
                int length = read(stream, offset, size-offset);
                if (length > 0)
                    residualLength += length;
                else
                    endOfData = true;
            }
        }
    }

    /**
     * Read the next record.  The return value will be null if the end of
     * file has been reached.
     *
     * @return                      New byte[] array containing the record
     * @exception   IOException     Unable to read from input file
     * @exception   EOFException    Premature end of file
     * @exception   StreamCorruptedException  Invalid record descriptor
     */
    public byte[] readRecord() throws IOException {

        //
        // Stop now if we have reached the end of the input file and
        // all of the buffered data has been processed
        //
        if (endOfData && residualLength == 0)
            return null;

        //
        // Fill the stream buffer if we do not have at least 4 bytes available
        //
        if (residualLength < 4) {
            fillBuffer();

            if (residualLength == 0)
                return null;

            if (residualLength < 4)
                throw new EOFException("Partial record encountered");
        }

        //
        // Read the record descriptor
        //
        int length = (((int)stream[next]&0xff)<<24) |
                            (((int)stream[next+1]&0xff)<<16) |
                            (((int)stream[next+2]&0xff)<<8) |
                            ((int)stream[next+3]&0xff);
        next += 4;
        residualLength -= 4;

        if (length < 1)
            throw new StreamCorruptedException("Record length "+length+" is not valid");

        //
        // Read the record data
        //
        byte[] record = new byte[length];
        int offset = 0;
        int segment;

        while (length != 0) {
            segment = Math.min(length, residualLength);

            if (segment != 0) {
                System.arraycopy(stream, next, record, offset, segment);
                offset += segment;
                length -= segment;
                next += segment;
                residualLength -= segment;
            }

            if (length != 0) {
                fillBuffer();
                if (endOfData)
                    throw new EOFException("Partial record encountered");
            }
        }

        return record;
    }
}

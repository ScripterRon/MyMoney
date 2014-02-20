package RecordIO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * The RecordOutputStream class provides record-based input using a file
 * output stream.
 */
public final class RecordOutputStream extends FileOutputStream {

    /**
     * Class instance variables
     */
    private byte[]  stream=null;            // Buffered output stream
    private int     size=8192;              // Size of the stream array
    private int     next=0;                 // Offset to next output byte
    private boolean streamOpen=false;       // TRUE if stream is open

    /**
     * Constructor for an output record stream where the output file is
     * specified as a File object
     *
     * @param       file            Output file
     * @exception   IOException     Unable to open output file
     */
    public RecordOutputStream(File file) throws IOException {
        super(file);

        //
        // Allocate the stream buffer
        //
        stream = new byte[size];

        //
        // Indicate the stream is open
        //
        streamOpen = true;
    }

    /**
     * Constructor for an output record stream where the output file is
     * specified as a String filename
     *
     * @param       filename        Output filename
     * @exception   IOException     Unable to open output file
     */
    public RecordOutputStream(String filename) throws IOException {
        super(filename);

        //
        // Allocate the stream buffer
        //
        stream = new byte[size];

        //
        // Indicate the stream is open
        //
        streamOpen = true;
    }

    /**
     * Write the next record.
     *
     * @param       record          Record data
     * @exception   IOException     Unable to write to output file
     * @exception   IllegalArgumentException  Record length is not valid
     */
    public void writeRecord(byte[] record) throws IOException {
        int length = record.length;

        //
        // The record length must be a positive value
        //
        if (length < 1)
            throw new IllegalArgumentException("Record length "+length+" is not valid");

        //
        // Flush the output buffer if there are fewer than 4 bytes remaining
        //
        if (size-next < 4)
            flush();

        //
        // Write the record descriptor
        //
        stream[next++] = (byte)(length>>24);
        stream[next++] = (byte)(length>>16);
        stream[next++] = (byte)(length>>8);
        stream[next++] = (byte)length;

        //
        // Write the record data
        //
        int offset = 0;
        int segment;

        while (offset < length) {
            segment = Math.min(size-next, length-offset);

            if (segment != 0) {
                System.arraycopy(record, offset, stream, next, segment);
                next += segment;
                offset += segment;
            }

            if (offset < length)
                flush();
        }
    }

    /**
     * Flush the output buffer
     *
     * @exception   IOException     Unable to write to output file
     */
    public void flush() throws IOException {
        if (next != 0)
            write(stream, 0, next);

        next = 0;
    }

    /**
     * Close the output file
     *
     * @exception   IOException     Unable to write to output file
     */
    public void close() throws IOException {
        flush();
        super.close();
        streamOpen = false;
    }

    /**
     * Flush the stream buffer when the object is released
     *
     * @exception   IOException     Unable to write to output file
     */
    protected void finalize() throws IOException {
        if (streamOpen)
            close();

        super.finalize();
    }
}

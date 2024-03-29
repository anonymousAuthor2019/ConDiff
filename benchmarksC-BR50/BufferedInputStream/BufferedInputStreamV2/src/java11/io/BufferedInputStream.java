/*
 * @(#)BufferedInputStream.java	1.37 01/11/29
 *
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java11.io;

/**
 * A <code>BufferedInputStream</code> adds
 * functionality to another input stream-namely,
 * the ability to buffer the input and to
 * support the <code>mark</code> and <code>reset</code>
 * methods. When  the <code>BufferedInputStream</code>
 * is created, an internal buffer array is
 * created. As bytes  from the stream are read
 * or skipped, the internal buffer is refilled
 * as necessary  from the contained input stream,
 * many bytes at a time. The <code>mark</code>
 * operation  remembers a point in the input
 * stream and the <code>reset</code> operation
 * causes all the  bytes read since the most
 * recent <code>mark</code> operation to be
 * reread before new bytes are  taken from
 * the contained input stream.
 *
 * @author  Arthur van Hoff
 * @version 1.36, 08/18/98
 * @since   JDK1.0
 */
public
class BufferedInputStream extends FilterInputStream {

    private static int defaultBufferSize = 2048;

    /**
     * The internal buffer array where the data is stored. When necessary,
     * it may be replaced by another array of
     * a different size.
     */
    protected byte buf[];

    /**
     * The index one greater than the index of the last valid byte in 
     * the buffer. 
     * This value is always
     * in the range <code>0</code> through <code>buf.length</code>;
     * elements <code>buf[0]</code>  through <code>buf[count-1]
     * </code>contain buffered input data obtained
     * from the underlying  input stream.
     */
    protected int count;

    /**
     * The current position in the buffer. This is the index of the next 
     * character to be read from the <code>buf</code> array. 
     * <p>
     * This value is always in the range <code>0</code>
     * through <code>count</code>. If it is less
     * than <code>count</code>, then  <code>buf[pos]</code>
     * is the next byte to be supplied as input;
     * if it is equal to <code>count</code>, then
     * the  next <code>read</code> or <code>skip</code>
     * operation will require more bytes to be
     * read from the contained  input stream.
     *
     * @see     java12.io.BufferedInputStream#buf
     */
    protected int pos;
    
    /**
     * The value of the <code>pos</code> field at the time the last 
     * <code>mark</code> method was called.
     * <p>
     * This value is always
     * in the range <code>-1</code> through <code>pos</code>.
     * If there is no marked position in  the input
     * stream, this field is <code>-1</code>. If
     * there is a marked position in the input
     * stream,  then <code>buf[markpos]</code>
     * is the first byte to be supplied as input
     * after a <code>reset</code> operation. If
     * <code>markpos</code> is not <code>-1</code>,
     * then all bytes from positions <code>buf[markpos]</code>
     * through  <code>buf[pos-1]</code> must remain
     * in the buffer array (though they may be
     * moved to  another place in the buffer array,
     * with suitable adjustments to the values
     * of <code>count</code>,  <code>pos</code>,
     * and <code>markpos</code>); they may not
     * be discarded unless and until the difference
     * between <code>pos</code> and <code>markpos</code>
     * exceeds <code>marklimit</code>.
     *
     * @see     java12.io.BufferedInputStream#mark(int)
     * @see     java12.io.BufferedInputStream#pos
     */
    protected int markpos = -1;

    /**
     * The maximum read ahead allowed after a call to the 
     * <code>mark</code> method before subsequent calls to the 
     * <code>reset</code> method fail. 
     * Whenever the difference between <code>pos</code>
     * and <code>markpos</code> exceeds <code>marklimit</code>,
     * then the  mark may be dropped by setting
     * <code>markpos</code> to <code>-1</code>.
     *
     * @see     java12.io.BufferedInputStream#mark(int)
     * @see     java12.io.BufferedInputStream#reset()
     */
    protected int marklimit;

    /**
     * Check to make sure that this stream has not been closed
     */
    private void ensureOpen() throws IOException {
	if (in == null)
	    throw new IOException("Stream closed");
    }

    /**
     * Creates a <code>BufferedInputStream</code>
     * and saves its  argument, the input stream
     * <code>in</code>, for later use. An internal
     * buffer array is created and  stored in <code>buf</code>.
     *
     * @param   in   the underlying input stream.
     */
    public BufferedInputStream(InputStream in) {
	this(in, defaultBufferSize);
    }

    /**
     * Creates a <code>BufferedInputStream</code>
     * with the specified buffer size,
     * and saves its  argument, the input stream
     * <code>in</code>, for later use.  An internal
     * buffer array of length  <code>size</code>
     * is created and stored in <code>buf</code>.
     *
     * @param   in     the underlying input stream.
     * @param   size   the buffer size.
     * @exception IllegalArgumentException if size <= 0.
     */
    public BufferedInputStream(InputStream in, int size) {
	super(in);
        if (size <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
	buf = new byte[size];
    }

    /**
     * Fills the buffer with more data, taking into account
     * shuffling and other tricks for dealing with marks.
     * Assumes that it is being called by a synchronized method.
     * This method also assumes that all data has already been read in,
     * hence pos > count.
     */
    public void fill() throws IOException {
        int var0 = 0;
	if (markpos < 0)
	    pos = 0;		/* no mark: throw away the buffer */
	else if (pos >= buf.length)	/* no room left in buffer */
	    if (markpos > 0) {	/* can throw away early part of the buffer */
		int sz = pos - markpos;
		System.arraycopy(buf, markpos, buf, 0, sz);
		pos = sz;
		markpos = 0;
	    } else if (buf.length >= marklimit) {
		markpos = -1;	/* buffer got too big, invalidate mark */
		pos = 0;	/* drop buffer contents */
	    } else {		/* grow buffer */
		int nsz = pos * 2;
		if (nsz > marklimit)
		    nsz = marklimit;
		byte nbuf[] = new byte[nsz];
		System.arraycopy(buf, 0, nbuf, 0, pos);
		buf = nbuf;
	    }
        count = pos;
	    int n = in.read(buf, pos, buf.length - pos);
        if (n > 0)
            count = n + pos;
    }

    /**
     * See
     * the general contract of the <code>read</code>
     * method of <code>InputStream</code>.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java12.io.FilterInputStream#in
     */
    public  int read() throws IOException {
        synchronized(this){
        ensureOpen();
	if (pos >= count) {
	    fill();
	    if (pos >= count)
		return -1;
	}
	return buf[pos++] & 0xff;
}
    }

    /**
     * Read characters into a portion of an array, reading from the underlying
     * stream at most once if necessary.
     */
    private int read1(byte[] b, int off, int len) throws IOException {
	int avail = count - pos;
	if (avail <= 0) {
	    /* If the requested length is at least as large as the buffer, and
	       if there is no mark/reset activity, do not bother to copy the
	       bytes into the local buffer.  In this way buffered streams will
	       cascade harmlessly. */
	    if (len >= buf.length && markpos < 0) {
		return in.read(b, off, len);
	    }
	    fill();
	    avail = count - pos;
	    if (avail <= 0) return -1;
	}
	int cnt = (avail < len) ? avail : len;
	System.arraycopy(buf, pos, b, off, cnt);
	pos += cnt;
	return cnt;
    }

    /**
     * Reads bytes from this byte-input stream into the specified byte array,
     * starting at the given offset.
     *
     * <p> This method implements the general contract of the corresponding
     * <code>{@link InputStream#read(byte[], int, int) read}</code> method of
     * the <code>{@link InputStream}</code> class.  As an additional
     * convenience, it attempts to read as many bytes as possible by repeatedly
     * invoking the <code>read</code> method of the underlying stream.  This
     * iterated <code>read</code> continues until one of the following
     * conditions becomes true: <ul>
     *
     *   <li> The specified number of bytes have been read,
     *
     *   <li> The <code>read</code> method of the underlying stream returns
     *   <code>-1</code>, indicating end-of-file, or
     *
     *   <li> The <code>available</code> method of the underlying stream
     *   returns zero, indicating that further input requests would block.
     *
     * </ul> If the first <code>read</code> on the underlying stream returns
     * <code>-1</code> to indicate end-of-file then this method returns
     * <code>-1</code>.  Otherwise this method returns the number of bytes
     * actually read.
     *
     * <p> Subclasses of this class are encouraged, but not required, to
     * attempt to read as many bytes as possible in the same fashion.
     *
     * @param      b     destination buffer.
     * @param      off   offset at which to start storing bytes.
     * @param      len   maximum number of bytes to read.
     * @return     the number of bytes read, or <code>-1</code> if the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     */
    public int read(byte b[], int off, int len)
	throws IOException
    {
                synchronized(this){

        ensureOpen();
	if ((off < 0) || (off > b.length) || (len < 0) ||
            ((off + len) > b.length) || ((off + len) < 0)) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
	    return 0;
	}

	int n = read1(b, off, len);
	if (n <= 0) return n;
	while ((n < len) && (in.available() > 0)) {
	    int n1 = read1(b, off + n, len - n);
	    if (n1 <= 0) break;
	    n += n1;
	}
	return n;
}

    }

    /**
     * See the general contract of the <code>skip</code>
     * method of <code>InputStream</code>.
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  IOException  if an I/O error occurs.
     */
    public long skip(long n) throws IOException {
                synchronized(this){

        ensureOpen();
	if (n <= 0) {
	    return 0;
	}
	long avail = count - pos;
     
        if (avail <= 0) {
            // If no mark position set then don't keep in buffer
            if (markpos <0) 
                return in.skip(n);
            
            // Fill in buffer to save bytes for reset
            fill();
            avail = count - pos;
            if (avail <= 0)
                return 0;
        }
        
        long skipped = (avail < n) ? avail : n;
        pos += skipped;
        return skipped;
    }
    }

    /**
     * Returns the number of bytes that can be read from this input 
     * stream without blocking. 
     * <p>
     * The <code>available</code> method of 
     * <code>BufferedInputStream</code> returns the sum of the the number 
     * of bytes remaining to be read in the buffer 
     * (<code>count&nbsp;- pos</code>) 
     * and the result of calling the <code>available</code> method of the 
     * underlying input stream. 
     *
     * @return     the number of bytes that can be read from this input
     *             stream without blocking.
     * @exception  IOException  if an I/O error occurs.
     * @see        java12.io.FilterInputStream#in
     */
    public synchronized int available() throws IOException {
        ensureOpen();
	return (count - pos) + in.available();
    }

    /** 
     * See the general contract of the <code>mark</code>
     * method of <code>InputStream</code>.
     *
     * @param   readlimit   the maximum limit of bytes that can be read before
     *                      the mark position becomes invalid.
     * @see     java12.io.BufferedInputStream#reset()
     */
    public synchronized void mark(int readlimit) {
	marklimit = readlimit;
	markpos = pos;
    }

    /**
     * See the general contract of the <code>reset</code>
     * method of <code>InputStream</code>.
     * <p>
     * If <code>markpos</code> is <code>-1</code>
     * (no mark has been set or the mark has been
     * invalidated), an <code>IOException</code>
     * is thrown. Otherwise, <code>pos</code> is
     * set equal to <code>markpos</code>.
     *
     * @exception  IOException  if this stream has not been marked or
     *               if the mark has been invalidated.
     * @see        java12.io.BufferedInputStream#mark(int)
     */
    public synchronized void reset() throws IOException {
        ensureOpen();
	if (markpos < 0)
	    throw new IOException("Resetting to invalid mark");
	pos = markpos;
    }

    /**
     * Tests if this input stream supports the <code>mark</code> 
     * and <code>reset</code> methods. The <code>markSupported</code> 
     * method of <code>BufferedInputStream</code> returns 
     * <code>true</code>. 
     *
     * @return  a <code>boolean</code> indicating if this stream type supports
     *          the <code>mark</code> and <code>reset</code> methods.
     * @see     java12.io.InputStream#mark(int)
     * @see     java12.io.InputStream#reset()
     */
    public boolean markSupported() {
	return true;
    }

    /**
     * Closes this input stream and releases any system resources 
     * associated with the stream. 
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void close() throws IOException {
        if (in == null)
            return;
        in.close();
        in = null;
        buf = null;
    }
}

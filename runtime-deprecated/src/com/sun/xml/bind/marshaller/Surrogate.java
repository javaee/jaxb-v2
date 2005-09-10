/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.xml.bind.marshaller;

import java.nio.CharBuffer;
import java.nio.charset.CoderResult;


/**
 * Utility class for dealing with surrogates.
 *
 * @author Mark Reinhold
 * @version 1.11, 03/01/23
 */

class Surrogate {

    private Surrogate() { }

    // UTF-16 surrogate-character ranges
    //
    public static final char MIN_HIGH = '\uD800';
    public static final char MAX_HIGH = '\uDBFF';
    public static final char MIN_LOW  = '\uDC00';
    public static final char MAX_LOW  = '\uDFFF';
    public static final char MIN = MIN_HIGH;
    public static final char MAX = MAX_LOW;

    // Range of UCS-4 values that need surrogates in UTF-16
    //
    public static final int UCS4_MIN = 0x10000;
    public static final int UCS4_MAX = (1 << 20) + UCS4_MIN - 1;

    /**
     * Tells whether or not the given UTF-16 value is a high surrogate.
     */
    public static boolean isHigh(int c) {
	return (MIN_HIGH <= c) && (c <= MAX_HIGH);
    }

    /**
     * Tells whether or not the given UTF-16 value is a low surrogate.
     */
    public static boolean isLow(int c) {
	return (MIN_LOW <= c) && (c <= MAX_LOW);
    }

    /**
     * Tells whether or not the given UTF-16 value is a surrogate character,
     */
    public static boolean is(int c) {
	return (MIN <= c) && (c <= MAX);
    }

    /**
     * Tells whether or not the given UCS-4 character must be represented as a
     * surrogate pair in UTF-16.
     */
    public static boolean neededFor(int uc) {
	return (uc >= UCS4_MIN) && (uc <= UCS4_MAX);
    }

    /**
     * Returns the high UTF-16 surrogate for the given UCS-4 character.
     */
    public static char high(int uc) {
	return (char)(0xd800 | (((uc - UCS4_MIN) >> 10) & 0x3ff));
    }

    /**
     * Returns the low UTF-16 surrogate for the given UCS-4 character.
     */
    public static char low(int uc) {
	return (char)(0xdc00 | ((uc - UCS4_MIN) & 0x3ff));
    }

    /**
     * Converts the given surrogate pair into a 32-bit UCS-4 character.
     */
    public static int toUCS4(char c, char d) {
	return (((c & 0x3ff) << 10) | (d & 0x3ff)) + 0x10000;
    }

    /**
     * Surrogate parsing support.  Charset implementations may use instances of
     * this class to handle the details of parsing UTF-16 surrogate pairs.
     */
    public static class Parser {

	public Parser() { }

	private int character;		// UCS-4
	private CoderResult error = CoderResult.UNDERFLOW;
	private boolean isPair;

	/**
	 * Returns the UCS-4 character previously parsed.
	 */
	public int character() {
	    return character;
	}

	/**
	 * Tells whether or not the previously-parsed UCS-4 character was
	 * originally represented by a surrogate pair.
	 */
	public boolean isPair() {
	    return isPair;
	}

	/**
	 * Returns the number of UTF-16 characters consumed by the previous
	 * parse.
	 */
	public int increment() {
	    return isPair ? 2 : 1;
	}

	/**
	 * If the previous parse operation detected an error, return the object
	 * describing that error.
	 */
	public CoderResult error() {
	    return error;
	}

	/**
	 * Returns an unmappable-input result object, with the appropriate
	 * input length, for the previously-parsed character.
	 */
	public CoderResult unmappableResult() {
	    return CoderResult.unmappableForLength(isPair ? 2 : 1);
	}

	/**
	 * Parses a UCS-4 character from the given source buffer, handling
	 * surrogates.
	 *
	 * @param  c    The first character
	 * @param  in   The source buffer, from which one more character
	 *              will be consumed if c is a high surrogate
	 *
	 * @returns  Either a parsed UCS-4 character, in which case the isPair()
	 *           and increment() methods will return meaningful values, or
	 *           -1, in which case error() will return a descriptive result
	 *           object
	 */
	public int parse(char c, CharBuffer in) {
	    if (Surrogate.isHigh(c)) {
		if (!in.hasRemaining()) {
		    error = CoderResult.UNDERFLOW;
		    return -1;
		}
		char d = in.get();
		if (Surrogate.isLow(d)) {
		    character = toUCS4(c, d);
		    isPair = true;
		    error = null;
		    return character;
		}
		error = CoderResult.malformedForLength(1);
		return -1;
	    }
	    if (Surrogate.isLow(c)) {
		error = CoderResult.malformedForLength(1);
		return -1;
	    }
	    character = c;
	    isPair = false;
	    error = null;
	    return character;
	}

	/**
	 * Parses a UCS-4 character from the given source buffer, handling
	 * surrogates.
	 *
	 * @param  c    The first character
	 * @param  ia   The input array, from which one more character
	 *              will be consumed if c is a high surrogate
	 * @param  ip   The input index
	 * @param  il   The input limit
	 *
	 * @returns  Either a parsed UCS-4 character, in which case the isPair()
	 *           and increment() methods will return meaningful values, or
	 *           -1, in which case error() will return a descriptive result
	 *           object
	 */
	public int parse(char c, char[] ia, int ip, int il) {
	    if (Surrogate.isHigh(c)) {
		if (il - ip < 2) {
		    error = CoderResult.UNDERFLOW;
		    return -1;
		}
		char d = ia[ip + 1];
		if (Surrogate.isLow(d)) {
		    character = toUCS4(c, d);
		    isPair = true;
		    error = null;
		    return character;
		}
		error = CoderResult.malformedForLength(1);
		return -1;
	    }
	    if (Surrogate.isLow(c)) {
		error = CoderResult.malformedForLength(1);
		return -1;
	    }
	    character = c;
	    isPair = false;
	    error = null;
	    return character;
	}

    }

    /**
     * Surrogate generation support.  Charset implementations may use instances
     * of this class to handle the details of generating UTF-16 surrogate
     * pairs.
     */
    public static class Generator {

	public Generator() { }

	private CoderResult error = CoderResult.OVERFLOW;

	/**
	 * If the previous generation operation detected an error, return the
	 * object describing that error.
	 */
	public CoderResult error() {
	    return error;
	}

	/**
	 * Generates one or two UTF-16 characters to represent the given UCS-4
	 * character.
	 *
	 * @param  uc   The UCS-4 character
	 * @param  len  The number of input bytes from which the UCS-4 value
	 *              was constructed (used when creating result objects)
	 * @param  dst  The destination buffer, to which one or two UTF-16
	 *              characters will be written
	 *
	 * @returns  Either a positive count of the number of UTF-16 characters
	 *           written to the destination buffer, or -1, in which case
	 *           error() will return a descriptive result object
	 */
	public int generate(int uc, int len, CharBuffer dst) {
	    if (uc <= 0xffff) {
		if (Surrogate.is(uc)) {
		    error = CoderResult.malformedForLength(len);
		    return -1;
		}
		if (dst.remaining() < 1) {
		    error = CoderResult.OVERFLOW;
		    return -1;
		}
		dst.put((char)uc);
		error = null;
		return 1;
	    }
	    if (uc < Surrogate.UCS4_MIN) {
		error = CoderResult.malformedForLength(len);
		return -1;
	    }
	    if (uc <= Surrogate.UCS4_MAX) {
		if (dst.remaining() < 2) {
		    error = CoderResult.OVERFLOW;
		    return -1;
		}
		dst.put(Surrogate.high(uc));
		dst.put(Surrogate.low(uc));
		error = null;
		return 2;
	    }
	    error = CoderResult.unmappableForLength(len);
	    return -1;
	}

	/**
	 * Generates one or two UTF-16 characters to represent the given UCS-4
	 * character.
	 *
	 * @param  uc   The UCS-4 character
	 * @param  len  The number of input bytes from which the UCS-4 value
	 *              was constructed (used when creating result objects)
	 * @param  da   The destination array, to which one or two UTF-16
	 *              characters will be written
	 * @param  dp   The destination position
	 * @param  dl   The destination limit
	 *
	 * @returns  Either a positive count of the number of UTF-16 characters
	 *           written to the destination buffer, or -1, in which case
	 *           error() will return a descriptive result object
	 */
	public int generate(int uc, int len, char[] da, int dp, int dl) {
	    if (uc <= 0xffff) {
		if (Surrogate.is(uc)) {
		    error = CoderResult.malformedForLength(len);
		    return -1;
		}
		if (dl - dp < 1) {
		    error = CoderResult.OVERFLOW;
		    return -1;
		}
		da[dp] = (char)uc;
		error = null;
		return 1;
	    }
	    if (uc < Surrogate.UCS4_MIN) {
		error = CoderResult.malformedForLength(len);
		return -1;
	    }
	    if (uc <= Surrogate.UCS4_MAX) {
		if (dl - dp < 2) {
		    error = CoderResult.OVERFLOW;
		    return -1;
		}
		da[dp] = Surrogate.high(uc);
		da[dp + 1] = Surrogate.low(uc);
		error = null;
		return 2;
	    }
	    error = CoderResult.unmappableForLength(len);
	    return -1;
	}

    }

}

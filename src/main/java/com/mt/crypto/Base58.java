package com.mt.crypto;

import java.util.Arrays;

/**
 * Base58 is a way to encode Bitcoin addresses (or arbitrary data) as
 * alphanumeric strings.
 * <p>
 * Note that this is not the same base58 as used by Flickr, which you may find
 * referenced around the Internet.
 * <p>
 * You may want to consider working with
 * {@code org.bitcoinj.core.EncodedPrivateKey} instead, which adds support for
 * testing the prefix and suffix bytes commonly found in addresses.
 * <p>
 * Satoshi explains: why base-58 instead of standard base-64 encoding?
 * <ul>
 * <li>Don't want 0OIl characters that look the same in some fonts and could be
 * used to create visually identical looking account numbers.</li>
 * <li>A string with non-alphanumeric characters is not as easily accepted as an
 * account number.</li>
 * <li>E-mail usually won't line-break if there's no punctuation to break
 * at.</li>
 * <li>Doubleclicking selects the whole number as one word if it's all
 * alphanumeric.</li>
 * </ul>
 * <p>
 * However, note that the encoding/decoding runs in O(n&sup2;) time, so it is
 * not useful for large data.
 * <p>
 * The basic idea of the encoding is to treat the data bytes as a large number
 * represented using base-256 digits, convert the number to be represented using
 * base-58 digits, preserve the exact number of leading zeros (which are
 * otherwise lost during the mathematical operations on the numbers), and
 * finally represent the resulting base-58 digits as alphanumeric ASCII
 * characters.
 */

public class Base58 {

	private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
	private static final char ENCODED_ZERO = ALPHABET[0];
	private static final int[] INDEXES = new int[128];

	static {
		Arrays.fill(INDEXES, -1);
		for (int i = 0; i < ALPHABET.length; i++) {
			INDEXES[ALPHABET[i]] = i;
		}
	}

	private Base58() {
		throw new IllegalStateException("Base58 was not designed to be instantiated");
	}

	/**
	 * Encodes the given bytes as a base58 string (no checksum is appended).
	 *
	 * @param input the bytes to encode
	 * @return the base58-encoded string
	 */
	public static String encode(byte[] input) {
		if (input.length == 0) {
			return "";
		}
		// Count leading zeros.
		int zeros = 0;
		while (zeros < input.length && input[zeros] == 0) {
			++zeros;
		}
		// Convert base-256 digits to base-58 digits (plus conversion to ASCII
		// characters)
		input = Arrays.copyOf(input, input.length); // since we modify it in-place
		char[] encoded = new char[input.length * 2]; // upper bound
		int outputStart = encoded.length;
		for (int inputStart = zeros; inputStart < input.length;) {
			encoded[--outputStart] = ALPHABET[divmod(input, inputStart, 256, 58)];
			if (input[inputStart] == 0) {
				++inputStart; // NOSONAR optimization - skip leading zeros
			}
		}
		// Preserve exactly as many leading encoded zeros in output as there were
		// leading zeros in input.
		while (outputStart < encoded.length && encoded[outputStart] == ENCODED_ZERO) {
			++outputStart;
		}
		while (--zeros >= 0) {
			encoded[--outputStart] = ENCODED_ZERO;
		}
		// Return encoded string (including encoded leading zeros).
		return new String(encoded, outputStart, encoded.length - outputStart);
	}

	/**
	 * Divides a number, represented as an array of bytes each containing a single
	 * digit in the specified base, by the given divisor. The given number is
	 * modified in-place to contain the quotient, and the return value is the
	 * remainder.
	 *
	 * @param number     the number to divide
	 * @param firstDigit the index within the array of the first non-zero digit
	 *                   (this is used for optimization by skipping the leading
	 *                   zeros)
	 * @param base       the base in which the number's digits are represented (up
	 *                   to 256)
	 * @param divisor    the number to divide by (up to 256)
	 * @return the remainder of the division operation
	 */
	private static byte divmod(byte[] number, int firstDigit, int base, int divisor) {
		// this is just long division which accounts for the base of the input digits
		int remainder = 0;
		for (int i = firstDigit; i < number.length; i++) {
			int digit = number[i] & 0xFF;
			int temp = remainder * base + digit;
			number[i] = (byte) (temp / divisor);
			remainder = temp % divisor;
		}
		return (byte) remainder;
	}
}
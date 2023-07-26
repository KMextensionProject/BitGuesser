package com.mt.crypto;

import static java.lang.System.arraycopy;

public class Bech32 {

	private static final String CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";
	private static final int[] GENERATORS = { 0x3b6a57b2, 0x26508e6d, 0x1ea119fa, 0x3d4233dd, 0x2a1462b3 };

	private Bech32() {
		throw new IllegalStateException("This class was not designed to be instantiated");
	}

	public static String encode(String humanReadablePart, byte[] data) {
		byte[] hrp = humanReadablePart.getBytes();
		byte[] checksum = createChecksum(hrp, data);
		byte[] combined = new byte[checksum.length + data.length];

		arraycopy(data, 0, combined, 0, data.length);
		arraycopy(checksum, 0, combined, data.length, checksum.length);

		byte[] xlat = new byte[combined.length];
		for (int i = 0; i < combined.length; i++) {
			xlat[i] = (byte) CHARSET.charAt(combined[i]);
		}

		byte[] result = new byte[hrp.length + xlat.length + 1];
		arraycopy(hrp, 0, result, 0, hrp.length);
		arraycopy(new byte[] { 0x31 }, 0, result, hrp.length, 1);
		arraycopy(xlat, 0, result, hrp.length + 1, xlat.length);

		return new String(result);
	}

	private static int polymod(byte[] values) {
		int chk = 1;
		for (byte b : values) {
			byte top = (byte) (chk >> 0x19);
			chk = b & 0xFF ^ ((chk & 0x1ffffff) << 5); // altered by 
			for (int i = 0; i < 5; i++) {
				chk ^= ((top >> i) & 1) == 1 ? GENERATORS[i] : 0;
			}
		}
		return chk;
	}

	private static byte[] hrpExpand(byte[] hrp) {
		byte[] buf1 = new byte[hrp.length];
		byte[] buf2 = new byte[hrp.length];
		byte[] mid = new byte[1];

		for (int i = 0; i < hrp.length; i++) {
			buf1[i] = (byte) (hrp[i] >> 5);
		}
		mid[0] = 0x00;
		for (int i = 0; i < hrp.length; i++) {
			buf2[i] = (byte) (hrp[i] & 0x1f);
		}

		byte[] ret = new byte[(hrp.length * 2) + 1];
		arraycopy(buf1, 0, ret, 0, buf1.length);
		arraycopy(mid, 0, ret, buf1.length, mid.length);
		arraycopy(buf2, 0, ret, buf1.length + mid.length, buf2.length);

		return ret;
	}

	private static byte[] createChecksum(byte[] hrp, byte[] data) {
		byte[] zeroes = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		byte[] expanded = hrpExpand(hrp);
		byte[] values = new byte[zeroes.length + expanded.length + data.length];

		arraycopy(expanded, 0, values, 0, expanded.length);
		arraycopy(data, 0, values, expanded.length, data.length);
		arraycopy(zeroes, 0, values, expanded.length + data.length, zeroes.length);

		int polymod = polymod(values) ^ 1;
		byte[] ret = new byte[6];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = (byte) ((polymod >> 5 * (5 - i)) & 0x1f);
		}

		return ret;
	}

	public static class Pair<L, R> {

		private L l;
		private R r;

		private Pair() { }

		public static <L, R> Pair<L, R> of(L l, R r) {
			Pair<L, R> pair = new Pair<>();
			pair.l = l;
			pair.r = r;
			return pair;
		}

		public R getRight() {
			return r;
		}

		public L getLeft() {
			return l;
		}
	}
}
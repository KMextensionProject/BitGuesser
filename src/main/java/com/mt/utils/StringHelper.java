package com.mt.utils;

/**
 *
 * @author mkrajcovic
 */
public class StringHelper {

	private StringHelper() {
		throw new IllegalStateException("StringHelper was not designed to be instantiated");
	}

	/**
	 *
	 * @param what
	 * @param howMany
	 * @param delimiter
	 * @return
	 */
	public static String repeat(String what, int howMany, String delimiter) {
		if (what == null || what.isEmpty()) {
			return what;
		}
		if (howMany < 0) {
			throw new IllegalArgumentException("Cannot pass in a negative number");
		}
		boolean hasDelimiter = delimiter != null && !delimiter.isEmpty(); 
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < howMany; i++) {
			sb.append(what);
			if (hasDelimiter) {
				sb.append(delimiter);
			}
		}
		return hasDelimiter ? sb.deleteCharAt(sb.length() - 1).toString() : sb.toString();
	}

	/**
	 *
	 * @param howMany
	 * @param toCut
	 * @return
	 */
	public static String keepFirst(int howMany, String toCut) {
		if (toCut == null || toCut.isEmpty() || howMany > toCut.length()) {
			return toCut;
		}
		if (howMany < 0) {
			throw new IllegalArgumentException("Cannot pass in a negative number");
		}
		return toCut.substring(0, howMany) + (toCut.length() > howMany ? "..." : "");
	}
}

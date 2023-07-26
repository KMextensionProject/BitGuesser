package com.mt.utils;

/**
 *
 * @author mkrajcovic
 */
public class StringHelper {

	/**
	 *
	 * @param what
	 * @param howMany
	 * @param delimiter
	 * @return
	 */
	public static String repeat(String what, int howMany, String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < howMany; i++) {
			sb.append(what).append(delimiter);
		}
		return sb.deleteCharAt(sb.length() - 1).toString();
	}

	/**
	 *
	 * @param howMany
	 * @param toCut
	 * @return
	 */
	public static String keepFirst(int howMany, String toCut) {
		return toCut.substring(0, howMany) + (toCut.length() > howMany ? "..." : "");
	}

}

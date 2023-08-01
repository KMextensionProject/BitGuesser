package com.mt.crypto;

import static java.lang.System.arraycopy;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.util.encoders.Hex;

import com.mt.core.ApplicationFailure;

/**
 * This class is responsible for generating a valid Bitcoin Native Segregated
 * Witness wallet and provide access to all its keys even in a raw form directly
 * through KeyPair holder object.<br>
 *
 * @author mkrajcovic
 */
public final class BtcNativeSegWitAddressGenerator extends BtcLegacyAddressGenerator {

	/**
	 * Generates a valid SegWit or the Bech32 Bitcoin wallet address
	 * from the given long public key by following these steps:<br>
	 * <ul>
	 *  <li>decode public key from hex to byte representation</li>
	 *  <li>apply SHA-256 to the public key</li>
	 *  <li>apply RIPEMD-160 on the hashed public key</li>
	 *  <li>create SegWit script hash from it with bit conversion and 0x00 version byte prepended</li>
	 *  <li>apply Bech32 encoding with main net human readable part</li>
	 * </ul>
	 */
	@Override
	public String getAddress(String publicKey) throws NoSuchAlgorithmException {
		byte[] publicKeyBytes = Hex.decode(publicKey);
		byte[] sha256Hash = sha256(publicKeyBytes);
		byte[] ripemd160Hash = ripemd160(sha256Hash);
		byte[] scriptHash = createSegWitScriptHash(ripemd160Hash);

		return Bech32.encode("bc", scriptHash);
	}

	private byte[] sha256(byte[] data) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		return digest.digest(data);
	}

	private byte[] ripemd160(byte[] data) {
		RIPEMD160Digest digest = new RIPEMD160Digest();
		digest.update(data, 0, data.length);
		byte[] output = new byte[digest.getDigestSize()];
		digest.doFinal(output, 0);
		return output;
	}

	private byte[] createSegWitScriptHash(byte[] ripemd160Hash) {
		byte[] prog = convertBits(ripemd160Hash, 8, 5, true);
		byte[] scriptHash = new byte[1 + prog.length];

		arraycopy(new byte[] { 0x00 }, 0, scriptHash, 0, 1); // version 0 for SegWit
		arraycopy(prog, 0, scriptHash, 1, prog.length);

		return scriptHash;
	}

	private byte[] convertBits(byte[] data, int fromBits, int toBits, boolean pad) {
		int acc = 0;
		int bits = 0;
		int maxv = (1 << toBits) - 1;
		List<Byte> ret = new ArrayList<>();

		for (Byte value : data) {
			short b = (short) (value & 0xff);

			if (b < 0 || (b >> fromBits) > 0) {
				throw new ApplicationFailure("Invalid byte value: " + b);
			}

			acc = (acc << fromBits) | b;
			bits += fromBits;
			while (bits >= toBits) {
				bits -= toBits;
				ret.add((byte) ((acc >> bits) & maxv));
			}
		}

		if (pad && (bits > 0)) {
			ret.add((byte) ((acc << (toBits - bits)) & maxv));
		} else if (bits >= fromBits || (byte) ((acc << (toBits - bits)) & maxv) != 0) {
			throw new ApplicationFailure("Error during bit conversion [bits=" + bits + ", return bytes=" + ret + "]");
		}

		byte[] retArray = new byte[ret.size()];
		for (int i = 0; i < retArray.length; i++) {
			retArray[i] = ret.get(i).byteValue(); 
		}

		return retArray;
	}
}

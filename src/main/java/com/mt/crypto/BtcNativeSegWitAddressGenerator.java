package com.mt.crypto;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Arrays;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

/**
 * 
 * @author mkrajcovic
 */
public class BtcNativeSegWitAddressGenerator implements CryptoAddressGenerator {

	private static CryptoAddressGenerator legacyGenerator = BtcLegacyAddressGenerator.getInstance();

	private static final BtcNativeSegWitAddressGenerator INSTANCE = new BtcNativeSegWitAddressGenerator();

	private BtcNativeSegWitAddressGenerator() {
		// intentionally empty
	}

	/**
	 * @return the singleton instance of this class
	 */
	public static final BtcNativeSegWitAddressGenerator getInstance() {
		return INSTANCE;
	}

	@Override
	public KeyPair generateAsymetricKeyPair() throws GeneralSecurityException {
		return legacyGenerator.generateAsymetricKeyPair();
	}

	@Override
	public String getPublicKey(KeyPair keyPair) throws InvalidKeyException {
		return legacyGenerator.getPublicKey(keyPair);
	}

	@Override
	public String getPrivateKey(KeyPair keyPair) throws InvalidKeyException {
		return legacyGenerator.getPrivateKey(keyPair);
	}

	@Override
	public String getAddress(String publicKey) throws NoSuchAlgorithmException {
		// Parse the long public key string into a byte array
		byte[] publicKeyBytes = Hex.decode(publicKey);

		// Compute the SHA-256 hash of the public key
		byte[] sha256Hash = sha256(publicKeyBytes);

		// Compute the RIPEMD-160 hash of the SHA-256 hash
		byte[] ripemd160Hash = ripemd160(sha256Hash);

		// TODO: zbavit sa oboch util tried, rozbit len to co treba sem do metod
		// TODO: od ripemd160Hash dole, vsetko prec
		try {
			String address = SegwitAddressUtil.getInstance().encode("bc".getBytes(), (byte) 0x00, ripemd160Hash);
			System.out.println(address);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Create the SegWit script hash
		byte[] scriptHash = createSegWitScriptHash(ripemd160Hash); // uz ani toto nerobim

		// Create the SegWit address
		// String segWitAddress = encodeSegWitAddress(scriptHash);

		// return segWitAddress;
		return null;
	}

	private static byte[] sha256(byte[] data) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		return digest.digest(data);
	}

	private static byte[] ripemd160(byte[] data) {
		RIPEMD160Digest digest = new RIPEMD160Digest();
		digest.update(data, 0, data.length);
		byte[] output = new byte[digest.getDigestSize()];
		digest.doFinal(output, 0);
		return output;
	}

	private static byte[] createSegWitScriptHash(byte[] ripemd160Hash) {
		byte[] scriptHash = new byte[ripemd160Hash.length + 2];
		scriptHash[0] = (byte) 0x00; // Version 0 for SegWit
		scriptHash[1] = (byte) ripemd160Hash.length; // Push length
		System.arraycopy(ripemd160Hash, 0, scriptHash, 2, ripemd160Hash.length);
		return scriptHash;
	}

	public static void main(String[] args) throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		CryptoAddressGenerator gen = BtcNativeSegWitAddressGenerator.getInstance();
		KeyPair kp = gen.generateAsymetricKeyPair();
		String publicKey = gen.getPublicKey(kp);
		System.out.println(gen.getAddress(publicKey));
	}
	
	private static byte[] convertBits(byte[] input, int fromBits, int toBits, boolean pad) {
		int acc = 0;
		int bits = 0;
		int maxv = (1 << toBits) - 1;
		int max_acc = (1 << (fromBits + toBits - 1)) - 1;
		byte[] result = new byte[(input.length * fromBits + toBits - 1) / toBits];
		int index = 0;

		for (byte value : input) {
			acc = ((acc << fromBits) | (value & 0xFF));
			bits += fromBits;
			while (bits >= toBits) {
				bits -= toBits;
				result[index] = (byte) ((acc >> bits) & maxv);
				++index;
			}
		}

		if (pad) {
			if (bits > 0) {
				result[index] = (byte) ((acc << (toBits - bits)) & maxv);
				++index;
			}
		} else if (bits >= fromBits || ((acc << (toBits - bits)) & maxv) != 0) {
			return null;
		}

		return Arrays.copyOf(result, index);
	}
}

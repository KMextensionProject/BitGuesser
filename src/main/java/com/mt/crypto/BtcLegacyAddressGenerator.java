package com.mt.crypto;

import static java.lang.System.arraycopy;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;

/**
 * This class is responsible for generating a valid Bitcoin wallet compatible
 * before the Segwit update and provide access to all its keys even in a raw
 * form directly through KeyPair holder object.<br>
 *
 * @author mkrajcovic
 */
public final class BtcLegacyAddressGenerator implements CryptoAddressGenerator {

	private static final BtcLegacyAddressGenerator INSTANCE = new BtcLegacyAddressGenerator();

	private BtcLegacyAddressGenerator() {
		// intentionally empty
	}

	/**
	 * @return the singleton instance of this class
	 */
	public static final BtcLegacyAddressGenerator getInstance() {
		return INSTANCE;
	}

	/**
	 * Generates KeyPair by applying ECDSA algorithm to the private key.
	 * Here, the secp256k1 curve is used as by the Bitcoin protocol.
	 *
	 * @return KeyPair - holder containing public and private key
	 */
	@Override
	public KeyPair generateAsymetricKeyPair() throws NoSuchProviderException,
													 NoSuchAlgorithmException,
													 InvalidAlgorithmParameterException {
		/*
		 * An elliptic curve is a curve defined by the equation y² = x³ + ax + b
		 * with a chosen a and b.
		 *
		 * By applying the ECDSA to the private key, we get a 64-byte integer.
		 * This consists of two 32-byte integers that represent the X and Y of
		 * the point on the elliptic curve, concatenated together.
		 */
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
		ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
		keyGen.initialize(ecSpec);

		return keyGen.generateKeyPair();
	}

	/**
	 * Retrieves the generated public key from the KeyPair holder applying
	 * concatenation of X and Y EC coordinates together with 0x04 prepended to
	 * represent a full valid Bitcoin public key.
	 *
	 * @return non compressed public key
	 * @throws InvalidKeyException
	 *             when a key with insufficient length gets produced
	 */
	@Override
	public String getPublicKey(KeyPair keyPair) throws InvalidKeyException {
		ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
		ECPoint ecPoint = publicKey.getW();
		// FIXME: this coordinate sometimes results in 61/60 digit number which is not valid
		String x = adjustTo64(ecPoint.getAffineX().toString(16));
		String y = adjustTo64(ecPoint.getAffineY().toString(16));
		return "04" + x + y;
	}

	/**
	 * Retrieves the generated private key from the KeyPair holder.
	 *
	 * @throws InvalidKeyException
	 *             when a key with insufficient length gets produced
	 */
	@Override
	public String getPrivateKey(KeyPair keyPair) throws InvalidKeyException {
		ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();
		return adjustTo64(privateKey.getS().toString(16));
	}

	private String adjustTo64(String key) throws InvalidKeyException {
		switch(key.length()) {
		case 62:
			return "00" + key;
		case 63:
			return "0" + key;
		case 64:
			return key;
		default :
			throw new InvalidKeyException("Invalid key: " + key + " length: " + key.length() + " (expected one of 62, 63, 64)");
		}
	}

	/**
	 * Generates a valid Bitcoin wallet address from the given public key by
	 * following these steps:<br>
	 * <ul>
	 * 	<li>apply SHA-256 to the public key</li>
	 * 	<li>apply RIPEMD-160 on the hashed public key</li>
	 * 	<li>add version byte of 0x00 at the beginning of the hash</li>
	 * 	<li>apply SHA-256 twice on the result of the previous operation</li>
	 * 	<li>add first 4 bytes of the second hashing at the end of the RIPEMD-160 hash</li>
	 * 	<li>apply Base58 on the resulting 25-byte address</li>
	 * </ul>
	 */
	@Override
	public String getAddress(String publicKey) throws NoSuchProviderException, NoSuchAlgorithmException {
		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
		byte[] shaPubKey = sha256.digest(publicKey.getBytes(UTF_8));

		MessageDigest ripeMD160 = MessageDigest.getInstance("RipeMD160", "BC");
		byte[] ripeMDPubKey = ripeMD160.digest(shaPubKey);

		byte[] ripeMDFinal = new byte[ripeMDPubKey.length + 1];
		ripeMDFinal[0] = 0;
		arraycopy(ripeMDPubKey, 0, ripeMDFinal, 1, ripeMDPubKey.length);

		// 4 bytes appended is the checksum
		byte[] shaFinal = sha256.digest(sha256.digest(ripeMDFinal));
		byte[] address = new byte[25];
		arraycopy(ripeMDFinal, 0, address, 0, 21);
		arraycopy(shaFinal, 0, address, 21, 4);

		return Base58.encode(address);
	}

}

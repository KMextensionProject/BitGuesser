package com.mt.crypto;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * 1. creating a public key with ECDSA
 * 2. encrypting the key with SHA-256 and RIPEMD-160 
 * 3. calculating the checksum with double SHA-256
 * 4. encoding the key with Base58.
 *
 * @author mkrajcovic
 */
public class BitWalletGenerator implements CryptoAddressGenerator {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	// TODO: split to smaller methods

	@Override
	public String getAddress(String publicKey) throws GeneralSecurityException {
		// compress public key
		byte[] pubBytes = new byte[publicKey.length() / 2];
		for (int i = 0; i < publicKey.length() / 2; i += 2) {
			pubBytes[i / 2] = (byte) ((Character.digit(publicKey.charAt(i), 16) << 4) + Character.digit(publicKey.charAt(i + 1), 16));
		}

		/*
		 * What we need to do here is to apply SHA-256 to the public key, and
		 * then apply RIPEMD-160 to the result. The order is important.
		 */
		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
		byte[] s1 = sha256.digest(pubBytes);

		MessageDigest ripeMD160 = MessageDigest.getInstance("RipeMD160", "BC");
		byte[] r1 = ripeMD160.digest(s1);

		byte[] r2 = new byte[r1.length + 1];
		r2[0] = 0;
		for (int i = 0; i < r1.length; i++) {
			r2[i + 1] = r1[i];
		}

		/*
		 * To calculate the checksum of the key, we need to apply SHA-256 twice
		 * and then take first 4 bytes of the result.
		 */
		byte[] s2 = sha256.digest(r2);
		byte[] s3 = sha256.digest(s2);
		byte[] a1 = new byte[25];
		for (int i = 0; i < r2.length; i++) {
			a1[i] = r2[i];
		}
		for (int i = 0; i < 4; i++) {
			a1[21 + i] = s3[i];
		}

		/*
		 * A compressed Bitcoin address is Main net encrypted public key +
		 * checksum
		 */
		return Base58.encode(a1);
	}

	@Override
	public KeyPair generateAsymetricKeyPair() throws GeneralSecurityException {
		/*
		 * The first thing we need to do is to apply the ECDSA or Elliptic Curve
		 * Digital Signature Algorithm to our private key. An elliptic curve is
		 * a curve defined by the equation y² = x³ + ax + b with a chosen a and
		 * b. There is a whole family of such curves that are widely known and
		 * used. Bitcoin uses the secp256k1 curve.
		 *
		 * By applying the ECDSA to the private key, we get a 64-byte integer.
		 * This consists of two 32-byte integers that represent the X and Y of
		 * the point on the elliptic curve, concatenated together.
		 */
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC"); // eliptic curve algorithm
		ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
		keyGen.initialize(ecSpec);

		return keyGen.generateKeyPair();
	}

	private String adjustTo64(String key) {
		switch(key.length()) {
		case 62:
			return "00" + key;
		case 63:
			return "0" + key;
		case 64:
			return key;
		default :
			// TODO: replace by more specific exception so it can be handled above meaningfully
			throw new IllegalArgumentException("Invalid key: " + key + " length: " + key.length() + " (expected one of 62, 63, 64)");
		}
	}

	@Override
	public String getPrivateKey(KeyPair keyPair) {
		return adjustTo64(((ECPrivateKey)keyPair.getPrivate()).getS().toString(16));
	}

	/**
	 * @return long public key
	 */
	@Override
	public String getPublicKey(KeyPair keyPair) throws GeneralSecurityException {
		/*
		 * Once we’re done with the ECDSA, all we need to do is to add the bytes
		 * 0x04 at the start of our public key. The result is a full Bitcoin
		 * public key.
		 */
		ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
		ECPoint ecPoint = publicKey.getW();
		String x = adjustTo64(ecPoint.getAffineX().toString(16));
		// FIXME: this Y coordinate sometimes results in 61 digit number which is not valid
		// internally it is stored fine, but fetching it produces wrong output
		String y = adjustTo64(ecPoint.getAffineY().toString(16));
		return "04" + x + y;
	}

}

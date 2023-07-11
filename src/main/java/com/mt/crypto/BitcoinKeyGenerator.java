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
 *
 * @author mkrajcovic
 */
public class BitcoinKeyGenerator implements CryptoAddressGenerator {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	@Override
	public String getAddress(String publicKey) {
		return null;
	}

	@Override
	public KeyPair generateAsymetricKeyPair() throws GeneralSecurityException {
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
			throw new IllegalArgumentException("Invalid key: " + key);
		}
	}

	@Override
	public String extractPrivateKey(KeyPair keyPair) {
		return adjustTo64(((ECPrivateKey)keyPair.getPrivate()).getS().toString(16));
	}

	@Override
	public String extractPublicKey(KeyPair keyPair) throws GeneralSecurityException {
		ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
		ECPoint ecPoint = publicKey.getW();
		String x = adjustTo64(ecPoint.getAffineX().toString(16));
		String y = adjustTo64(ecPoint.getAffineY().toString(16));
		String bcPub = "04" + x + y;

		byte[] bytes = new byte[bcPub.length() / 2];
		for (int i = 0; i < bcPub.length() / 2; i += 2) {
			bytes[i / 2] = (byte) ((Character.digit(bcPub.charAt(i), 16) << 4) + Character.digit(bcPub.charAt(i+1), 16));
		}
		MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
		byte[] s1 = sha256.digest(bytes);

		MessageDigest ripeMD160 = MessageDigest.getInstance("RipeMD160", "BC");
		byte[] r1 = ripeMD160.digest(s1);

		byte[] r2 = new byte[r1.length + 1];
		r2[0] = 0;
		for (int i = 0; i < r1.length; i++) {
			r2[i + 1] = r1[i];
		}

		byte[] s2 = sha256.digest(r2);
		byte[] s3 = sha256.digest(s2);
		byte[] a1 = new byte[25];
		for (int i = 0; i < r2.length; i++) {
			a1[i] = r2[i];
		}
		for (int i = 0; i < 4; i++) {
			a1[21 + i] = s3[i];
		}

		return Base58.encode(a1);
	}
	
	public static void main(String[] args) throws Exception {
//		rename this class
		BitcoinKeyGenerator btcGenerator = new BitcoinKeyGenerator();

		KeyPair keyPair = btcGenerator.generateAsymetricKeyPair();
		String publicKey = btcGenerator.extractPublicKey(keyPair);
		String privateKey = btcGenerator.extractPrivateKey(keyPair);

		System.out.println("address: " + btcGenerator.getAddress(publicKey));
		System.out.println("public key: " + publicKey);
		System.out.println("private key: " + privateKey);
	}

}

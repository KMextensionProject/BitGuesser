package com.mt.crypto;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.util.Arrays;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;

/**
 *
 * @author mkrajcovic
 */
public class BtcSegWitAddressGenerator implements CryptoAddressGenerator {

	// kept for delegating on private and public key generation since these processes are same
	private static CryptoAddressGenerator legacyGenerator = BtcLegacyAddressGenerator.getInstance();

	private static final BtcSegWitAddressGenerator INSTANCE = new BtcSegWitAddressGenerator();

	private BtcSegWitAddressGenerator() {
		// intentionally empty
	}

	/**
	 * @return the singleton instance of this class
	 */
	public static final BtcSegWitAddressGenerator getInstance() {
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
	public String getAddress(String publicKey) throws GeneralSecurityException {
		return generateSegWitAddress(publicKey.getBytes());
	}

	private String generateSegWitAddress(byte[] longPublicKey) {
		// Step 1: Calculate the SHA-256 hash of the long public key
		SHA256Digest sha256Digest = new SHA256Digest();
		byte[] sha256Hash = new byte[sha256Digest.getDigestSize()];
		sha256Digest.update(longPublicKey, 0, longPublicKey.length);
		sha256Digest.doFinal(sha256Hash, 0);

		// Step 2: Calculate the RIPEMD-160 hash of the SHA-256 hash
		RIPEMD160Digest ripemd160Digest = new RIPEMD160Digest();
		byte[] publicKeyHash = new byte[ripemd160Digest.getDigestSize()];
		ripemd160Digest.update(sha256Hash, 0, sha256Hash.length);
		ripemd160Digest.doFinal(publicKeyHash, 0);

		// Step 3: Create a SegWit address with the version byte (0x05 for
		// mainnet) and the public key hash
		byte[] versionAndData = new byte[publicKeyHash.length + 1];
		versionAndData[0] = 0x05; // Version byte for mainnet SegWit address (0x05 for mainnet, 0xC4 for testnet)
		System.arraycopy(publicKeyHash, 0, versionAndData, 1, publicKeyHash.length);

		// Step 4: Calculate the SHA-256 hash of the version byte and data
		sha256Digest.update(versionAndData, 0, versionAndData.length);
		sha256Digest.doFinal(sha256Hash, 0);

		// Step 5: Calculate the SHA-256 hash of the previous SHA-256 hash
		sha256Digest.update(sha256Hash, 0, sha256Hash.length);
		sha256Digest.doFinal(sha256Hash, 0);

		// Step 6: Take the first 4 bytes of the second SHA-256 hash, which will be the checksum
		byte[] checksum = Arrays.copyOfRange(sha256Hash, 0, 4);

		// Step 7: Concatenate the version byte, data, and checksum
		byte[] dataWithChecksum = new byte[versionAndData.length + 4];
		System.arraycopy(versionAndData, 0, dataWithChecksum, 0, versionAndData.length);
		System.arraycopy(checksum, 0, dataWithChecksum, versionAndData.length, 4);

		// Step 8: Encode the data with checksum in Base58Check encoding
		return Base58.encode(dataWithChecksum);
	}

}

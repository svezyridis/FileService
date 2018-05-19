package crypto;

import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

import zookeeper.Configuration;

public class Validator {

	public static boolean validateHMAC(String fileID, String userID, String validTill, String hmachash) throws GeneralSecurityException, java.io.UnsupportedEncodingException {
		byte[] hmac=Base64.getDecoder().decode(hmachash);
		SecretKeySpec hks = new SecretKeySpec(Base64.getDecoder().decode("boubis12"), "HmacSHA256");
		Mac m = Mac.getInstance("HmacSHA256");
		m.init(hks);
		byte[] calcmac = m.doFinal(Base64.getDecoder().decode(fileID+userID+validTill));
		if (Arrays.equals(hmac, calcmac))
		return true;
		return false;
	}

	public static boolean validateTime(String validTill) {
		// TODO Auto-generated method stub
		return true;
	}

}

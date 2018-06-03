package crypto;

import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.*;

import zookeeper.Configuration;

public class Validator {

	public static boolean validateHMAC(String fileID, String userID, String validTill, String hmachash) throws GeneralSecurityException, java.io.UnsupportedEncodingException {
		SecretKeySpec hks = new SecretKeySpec(Base64.getDecoder().decode(Configuration.getSharedKey()), "HmacSHA256");
		Mac m = Mac.getInstance("HmacSHA256");
		m.init(hks);
		byte[] calcmac = m.doFinal((fileID+userID+validTill).getBytes("UTF-8"));
		String calchmac=Base64.getEncoder().encodeToString(calcmac);
		if (hmachash.equals(calchmac))
		return true;
		return false;
	}

	public static boolean validateTime(String validTill) {
		if(((int)(System.currentTimeMillis()/1000)-Integer.parseInt(validTill))>60) {
			return false;
			
		}
		return true;
	}

}

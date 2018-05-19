package api;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import crypto.Validator;

public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fileid="1";
		String userid="savvas";
		String validtill="now";
		
		SecretKeySpec hks = new SecretKeySpec(Base64.getDecoder().decode("boubis12"), "HmacSHA256");
		Mac m;
		try {
			m = Mac.getInstance("HmacSHA256");
			m.init(hks);
			byte[] hmac = m.doFinal(Base64.getDecoder().decode(fileid+userid+validtill));
			String hmachash=Base64.getEncoder().encodeToString(hmac);
			if(Validator.validateHMAC(fileid, userid, validtill, hmachash)) {
				System.out.println("OK");
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	

	}

}

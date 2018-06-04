package api;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.*;
import javax.servlet.http.*; 
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import java.security.*;
import java.security.spec.*;
import java.util.*;
import org.json.*;

import crypto.Validator;
import zookeeper.Configuration;

/**
 * Servlet implementation class FileServiceApi
 */
@MultipartConfig
@WebServlet("/FileServiceApi")
public class FileServiceApi extends HttpServlet {
	public static String getRepositoryPath() {
		// outside of webapp!!
		return Configuration.getRootPath();
	}
	
	private int counter = 0;
	private static final long serialVersionUID = 1L;
	
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FileServiceApi() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String fileID=request.getParameter("fileid");
		System.out.println(fileID);
		String userID=request.getParameter("userid");
		System.out.println(userID);
		String validTill=request.getParameter("validtill");
		System.out.println(validTill);
		String hmac=request.getParameter("hmac");
		System.out.println(hmac);

		try {
			if(Validator.validateHMAC(fileID,userID,validTill,hmac)&&Validator.validateTime(validTill)) {
				
				
				File file = new File(getRepositoryPath()+"/"+userID+"/"+fileID);
				String mimetype=Files.probeContentType(file.toPath());
				response.setContentType(mimetype);
				FileInputStream fis = null;
				fis = new FileInputStream(file);
				BufferedImage image=ImageIO.read(fis);
				System.out.println(mimetype);
			
				String format=Configuration.getMimeToExtensionMap().get(mimetype);
				System.out.println(format);
			    ImageIO.write(image, format, response.getOutputStream());
			    Configuration.getStats().incReads();
				return;
			
			}
		} catch (GeneralSecurityException e) {
			 
			 
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");
		PrintWriter out = response.getWriter();
		String fileID=request.getParameter("fileid");
		String userID=request.getParameter("userid");
		System.out.println(userID);
		String validTill=request.getParameter("validtill");
		System.out.println(validTill);
		String hmac=request.getParameter("hmac");
		System.out.println(hmac);
		
		try {
			if(Validator.validateHMAC(fileID,userID,validTill,hmac)&&Validator.validateTime(validTill)) {
				Part filePart = request.getPart("file"); // Retrieves <input type="file" name="file">
			    String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
			    ImageInputStream fileContent = ImageIO.createImageInputStream(filePart.getInputStream());
			    Iterator<ImageReader> readers = ImageIO.getImageReaders(fileContent);
			    if (readers.hasNext()) {
			        ImageReader reader = readers.next();
			    String format=reader.getFormatName();
			    BufferedImage image=ImageIO.read(fileContent);
			    File outputfile = new File(getRepositoryPath()+"/"+userID+"/"+fileID);
			    outputfile.mkdirs();
			    ImageIO.write(image, format, outputfile);
			    JSONObject resJSON=new JSONObject();
				resJSON.put("error", "");
				out.print(resJSON.toString());
				Configuration.getStats().incWrites();
				return;
			    }
			    JSONObject resJSON=new JSONObject();
				resJSON.put("error", "unsupported file");
				out.print(resJSON.toString());
				return;
			
			}
			JSONObject resJSON=new JSONObject();
			resJSON.put("error", "invalid hmac");
			out.print(resJSON.toString());
			return;
		} catch (GeneralSecurityException e) {
			JSONObject resJSON=new JSONObject();
			resJSON.put("error", e.getMessage());
			out.print(resJSON.toString());
			e.printStackTrace();
			return;
		}
		catch (IllegalArgumentException e) {
			JSONObject resJSON=new JSONObject();
			resJSON.put("error", e.getMessage());
			out.print(resJSON.toString());
			e.printStackTrace();
			return;
		}
		
	}

}

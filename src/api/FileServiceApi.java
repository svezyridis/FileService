package api;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.awt.image.BufferedImage;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*; 
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.imageio.ImageIO;

import java.security.*;
import java.security.spec.*;
import java.util.*;
import org.json.*;

import crypto.Validator;

/**
 * Servlet implementation class FileServiceApi
 */
@WebServlet("/FileServiceApi")
public class FileServiceApi extends HttpServlet {
	public static String getRepositoryPath() {
		// outside of webapp!!
		return "/tmp/";
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
		response.setContentType("image/jpg");
		String fileID=request.getParameter("fileid");
		String userID=request.getParameter("userid");
		String validTill=request.getParameter("validtill");
		String hmac=request.getParameter("hmac");	

		try {
			if(Validator.validateHMAC(fileID,userID,validTill,hmac)&&Validator.validateTime(validTill)) {
				File file = new File("/home/boubis12/Downloads/image.jpg");
				FileInputStream fis = null;
				fis = new FileInputStream(file);
				BufferedImage image=ImageIO.read(fis);
			    ImageIO.write(image, "JPG", response.getOutputStream());
				return;
			
			}
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		doGet(request, response);
	}

}

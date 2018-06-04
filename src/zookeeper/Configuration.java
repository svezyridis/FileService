package zookeeper;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.json.JSONObject;

import api.Stats;




/**
 * Application Lifecycle Listener implementation class Zooconf
 *
 */
@WebListener
public class Configuration implements ServletContextListener {
	private static  List<String> zookeeperIPs = new ArrayList<String>();
	private String host="";
	private static  String myip;
	private static String identifier;
	private static String sharedkey;
	private static String name;
	private static String strgpath;
	private static String zoouser; 
	private static String zoopass; 
	private  ZooKeeper zoo;
	private static int timeStarted;
	private static  Map<String,String> mimeTypeToFileExtension=initializeMap();
	private static Stats statistics;
	
	final CountDownLatch connectedSignal = new CountDownLatch(1);
	private static String rootpath;
	private static Configuration ConfInstance = null;
	
	public static String getSharedKey() {
		Configuration instance = getInstance();
		return instance.sharedkey;
	}
	public static int getTimeStarted() {
		Configuration instance = getInstance();
		return instance.timeStarted;
	}
	public static Stats getStats() {
		Configuration instance = getInstance();
		return instance.statistics;
	}
	public static Map<String,String> getMimeToExtensionMap() {
		Configuration instance = getInstance();
		return mimeTypeToFileExtension;
	}
	
	private static Map<String, String> initializeMap() {
		 Map<String,String> myMap = new HashMap<String,String>();
	        myMap.put("image/jpeg", "JPG");
	        myMap.put("image/png", "PNG");
	        myMap.put("image/bmp", "BMP");
	        myMap.put("image/gif", "GIF");
	        myMap.put("image/svg+xml", "SVG");
	        return myMap;
	}

	public static String getMyIdentifier() {
		Configuration instance = getInstance();
		return instance.identifier;
	}
	public static String getRootPath() {
		Configuration instance = getInstance();
		return instance.rootpath;
	}
	
	public static String getMyIP() {
		Configuration instance = getInstance();
		return instance.myip;
	}
	
	public static String getZookeeperIPs(){
		Configuration instance = getInstance();
		
		boolean first=true;
		for (String ip:zookeeperIPs) {
			if (first) {
				instance.host+=ip;
				first =false;
			}
			else
				instance.host=instance.host+","+ip;				
		}
		return instance.host;
	}
	
	private ZooKeeper zooConnect() throws IOException,InterruptedException {
		System.out.println("start zooConnect on "+getZookeeperIPs());
		
		ZooKeeper zk = new ZooKeeper(getZookeeperIPs(), 3000, new Watcher() {
			@Override
			public void process(WatchedEvent we) {
				if (we.getState() == KeeperState.SyncConnected) {
					connectedSignal.countDown();
				}
			}
		});
		connectedSignal.await();
		
		zk.addAuthInfo("digest", new String(zoouser+":"+zoopass).getBytes());
		
		System.out.println("finished zooConnect");

		return zk;
	}

	public static ZooKeeper getZooConnection() {
		Configuration instance = getInstance();
		return instance.zoo;
	}
	

	public void ReadConfigurationFile() {
		Configuration instance=getInstance();
		URL resource = getClass().getResource("/");
		String path = resource.getPath();
		path = path.replace("WEB-INF/classes/", "conf/config.xml");
		//Read configuration file
	        try {
	            File inputFile = new File(path);
	            SAXBuilder saxBuilder = new SAXBuilder();
	            Document document = saxBuilder.build(inputFile);
	            System.out.println("Root element :" + document.getRootElement().getName());
	            Element classElement = document.getRootElement();
	            System.out.println("----------------------------");
	            Element setting=classElement.getChild("zookeeper");
	            System.out.println("\nCurrent Element :" 
		                  + setting.getName());
	            List<Element>ips=setting.getChildren("zooip");
	            for(Element ip:ips) {
	            	instance.zookeeperIPs.add(ip.getValue().toString());
	                
	            	
	            }
	            instance.strgpath=setting.getChild("storageservicepath").getValue();
	            instance.zoouser=setting.getChild("zoouser").getValue();
	            instance.zoopass=setting.getChild("zoopass").getValue();
	            instance.identifier=classElement.getChild("identifier").getValue();    
	            instance.sharedkey=classElement.getChild("sharedkey").getValue();
	            instance.myip= classElement.getChild("hostname").getValue();
	            instance.rootpath= classElement.getChild("rootpath").getValue();
				System.out.println(instance.myip);
	        

	            
	         } catch(JDOMException e) {
	            e.printStackTrace();
	         } catch(IOException ioe) {
	            ioe.printStackTrace();
	         }
		
	}

	public void PublishService(ServletContextEvent sce) {
		Configuration instance=getInstance();
		ACL acl = null;
		try {
			String base64EncodedSHA1Digest = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA1").digest((zoouser+":"+zoopass).getBytes()));
			acl = new ACL(ZooDefs.Perms.ALL, new Id("digest",zoouser+":" + base64EncodedSHA1Digest));
		}
		catch (NoSuchAlgorithmException ex) {
			System.err.println("destroy NoSuchAlgorithmException");
		}
		
			
		
	       JSONObject configJSON=new JSONObject();
	       configJSON.put("URL", instance.myip);
	       configJSON.put("key", instance.sharedkey);
	       configJSON.put("id", instance.identifier);
	           
	       System.out.println("publishing service");  
	       try {
			Stat stat = instance.zoo.exists(strgpath, false);
			if(stat==null) {
				System.out.println("Node does not exist, creating node");
				instance.zoo.create(strgpath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
			}
			instance.zoo.create(strgpath+"/"+identifier, configJSON.toString().getBytes(),Arrays.asList(acl),
					CreateMode.EPHEMERAL);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static Configuration getInstance() {
		if (ConfInstance == null) {
			ConfInstance = new Configuration();
		}
		return ConfInstance;
	}

	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		 System.err.println("Dirservice Context destroyed");
		 Configuration instance = getInstance();
			try {
				if (instance.zoo != null) {
					instance.zoo.close();
				}
			}
			catch ( InterruptedException ex) {
				System.err.println("destroy InterruptedException");
			}

		
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		Configuration instance = getInstance();
		instance.ReadConfigurationFile();
	
		try {
			instance.zoo = instance.zooConnect();
			instance.PublishService(sce);	  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		instance.timeStarted=(int)(System.currentTimeMillis()/1000);
		instance.statistics=new Stats();

	    }
	
		

	
}

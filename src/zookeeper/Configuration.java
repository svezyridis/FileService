package zookeeper;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
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
	
	final CountDownLatch connectedSignal = new CountDownLatch(1);
	private static Configuration ConfInstance = null;
	
	public static String getSharedKey() {
		Configuration instance = getInstance();
		return instance.sharedkey;
	}
	
	public static String getMyIdentifier() {
		Configuration instance = getInstance();
		return instance.identifier;
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
		
		//zk.addAuthInfo("digest", new String(zoouser+":"+zoopass).getBytes());
		
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
	        

	            
	         } catch(JDOMException e) {
	            e.printStackTrace();
	         } catch(IOException ioe) {
	            ioe.printStackTrace();
	         }
		
	}

	public void PublishService(ServletContextEvent sce) {
		Configuration instance=getInstance();
		ACL acl = new ACL();
		try {
			String base64EncodedSHA1Digest = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA1").
					digest((zoopass).getBytes()));
			acl.setPerms(ZooDefs.Perms.ALL);
			acl.setId(new Id("digest",zoouser+":"+base64EncodedSHA1Digest));
		}
		catch (NoSuchAlgorithmException ex) {
			System.err.println("destroy NoSuchAlgorithmException");
		}
		List<ACL> aclList=new ArrayList<ACL>();
		aclList.add(acl);
		
	       try {
			instance.myip= InetAddress.getLocalHost().toString();
			instance.myip=instance.myip+"/"+sce.getServletContext().getServletContextName();
			System.out.println(instance.myip);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			instance.zoo.create(strgpath+"/"+identifier, configJSON.toString().getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,
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

	    }
	
		

	
}

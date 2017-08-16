package com.dc.appengine.router.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class RouterConfig {
	//
	private static Log log = LogFactory.getLog(RouterConfig.class);
	private static RouterConfig config = null;
	private HashMap<String, String> props = new HashMap<String, String>();
	private PropertiesConfiguration propConfiguration = new  PropertiesConfiguration();
	private static String instroot = null;
	private RouterConfig(){
		init();
	}
	public static RouterConfig getInstance(){
		if(config == null){
			synchronized(RouterConfig.class){
				if(config == null){
					config = new RouterConfig();
				}
			}
		}
		return config;
	}
	private void init(){
		Properties properties = new Properties();
		if (instroot == null) {
			instroot = System.getProperty("com.dc.install_path");
		}
		// if (instroot == null)
		// instroot = System.getProperty("com.dc.install_path");
		if (!isAbsolutePath(instroot)) {
			instroot = new File(instroot).getAbsolutePath();
		}
		FileInputStream fin = null;
		FileInputStream fin1 = null;
		if (instroot == null) {
			if(log.isErrorEnabled()){
				log.error("can not get properties: com.dc.install_path");
			}
		}
		try {
			//fin与fin1所指向相同的文件
			fin = new FileInputStream(new File(instroot, "config.properties"));
			properties.load(fin);
			
			fin1 = new FileInputStream(new File(instroot, "config.properties"));
			propConfiguration.load(fin1);
		} catch (Exception ex) {
			if (log.isErrorEnabled()) {
				log.error("Load system config error.", ex);
			}
		} finally {
			try {
				fin.close();
			} catch (Exception e) {
			}
		}
		Enumeration en = properties.keys();
		while (en.hasMoreElements()) {
			String key = (String) en.nextElement();
			props.put(key, properties.getProperty(key));
		}
	}
	private static boolean isAbsolutePath(String url) {
		if (url == null || url.length() < 1) {
			return true;
		}
		if (url.startsWith(".")) {
			return false;
		}
		return true;

	}
	public String getProperty(String name) {
		return props.get(name);
	}
	
	public void setPoperValue(String key,String value){
		props.put(key, value);
		propConfiguration.setProperty(key, value);
	}
	
	public void updateConfig(){
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(new File(instroot, "config.properties"));
			Properties properties = new Properties();
			Iterator<String> en = props.keySet().iterator();
			while (en.hasNext()) {
				String key = (String) en.next();
				properties.put(key, props.get(key));
			}
			properties.store(fout, "update at " + new Date().getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(fout != null){
				try {
					fout.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fout = null;
			}
		}
	}
	
	public void delPoperValue(String key){
		props.remove(key);
		propConfiguration.clearProperty(key);
	}
	
	public void updateConfigWithComment(){
		File file = new File(instroot, "config.properties");
		try {
			propConfiguration.save(file);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

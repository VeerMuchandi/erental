package com.pinaka.eRental.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringEscapeUtils;


public class PropertyManager {
	
	    private static Properties props = new Properties();
	    private static ResourceBundle rb =  ResourceBundle.getBundle("ExceptionMessages");
	 
	    public PropertyManager(){
	        try {
	            loadProps();
	        } catch (IOException e) {
	            System.out.println("An Error Occurred while preparing properties. "+ e);
	        }
	    }
	 
	    private void loadProps() throws IOException{    
	        InputStream inputstream = this.getClass().getResourceAsStream("/config.properties");
	        props.load (inputstream); 
//	    	props.load(new FileInputStream("/src/main/resources/config.properties"));
//	    	props.load(new FileInputStream("/config.properties"));
	    }
	    
	   
	 
	    public static String getProp(String key){
	        return props.getProperty(key);
	    }
	    
	    public static String getMessage(String key, Object ... params) {
	    	return StringEscapeUtils.unescapeJava((MessageFormat.format(rb.getString(key), params)));
	    }
	

}

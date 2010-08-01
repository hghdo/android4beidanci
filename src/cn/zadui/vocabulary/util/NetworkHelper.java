package cn.zadui.vocabulary.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class NetworkHelper {
	public static final String G_BLOG="blogs";
	public static final String G_NEWS="news";
	public static final String G_WEB="web";
	
	public static final int CONNECT_TIMEOUT=20*1000;
	public static final int READ_TIMEOUT=20*1000;
	
	public static final String XML_FORMAT="xml";
	public static final String JSON_FORMAT="json";
	//public static final String HOST_ROOT="http://172.29.1.67:3000/";
	public static final String HOST_ROOT="http://17ttxs.com/";
	
	public static String courseListUrl(String format){
		if (format==null) format=XML_FORMAT;
		return HOST_ROOT+"courses."+format;
	}
	
	public static String lookup(String format,String word,String srcLang,String toLang){
		if (format==null) format=XML_FORMAT;
		StringBuilder sb=new StringBuilder();
		sb.append(HOST_ROOT);
		sb.append("lookup.");
		sb.append(format);
		sb.append("?word="+word);
		if (srcLang!=null && srcLang.length()>0) sb.append("&src="+srcLang);
		if (toLang!=null && toLang.length()>0) sb.append("&to="+toLang);
		return sb.toString();
	}
	
	public static String dictCnLookupUrl(String word){
		StringBuilder sb=new StringBuilder();
		sb.append("http://api.dict.cn/ws.php?utf8=true&q=");
		try {
			sb.append(URLEncoder.encode(word,"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	public static String exampleUrl(String headword,String language,String timestamp){
		StringBuilder sb=new StringBuilder();
		sb.append(HOST_ROOT+"examples/fetch.json?hw=");
		try {
			sb.append(URLEncoder.encode(headword,"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return sb.toString();
		
	}
	
	public static String googleAjaxUrl(String headword,String language,String category){
		String key="ABQIAAAArzAJW5BfpaHTaZWzewF5mhQFszn0YzdxtVD0cBgE4oJ_cGGJlhSKkYLOu4kuiW7FChWck3f3pVEDoA";
		StringBuilder sb=new StringBuilder();
		sb.append("http://ajax.googleapis.com/ajax/services/search/");
		sb.append(category == null ? G_BLOG : category);
		sb.append("?v=1.0&rsz=8&q=");
		try {
			sb.append(URLEncoder.encode(headword,"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		sb.append("&key=");
		sb.append(key);
		return sb.toString();		
	}
		
	public static URLConnection buildUrlConnection(String url) throws IOException{
		URL u=new URL(url);
		HttpURLConnection con=(HttpURLConnection)u.openConnection();
		con.setConnectTimeout(NetworkHelper.CONNECT_TIMEOUT);
		con.setReadTimeout(NetworkHelper.READ_TIMEOUT);
		return con;
	}

}

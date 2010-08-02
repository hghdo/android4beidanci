package cn.zadui.vocabulary.model.dictionary;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;
import cn.zadui.vocabulary.model.Word;
import cn.zadui.vocabulary.util.NetworkHelper;

public class DictCnDict implements Dict {

	private static final String DEF_TAG="def";
	private static final String PRONETIC_TAG="pron";
	
	private XmlPullParser xpp;
	
	
	@Override
	public String getDictName() {
		return "NetDict";
	}

	//@Override
	public String lookup(String headword,String srcLang,String toLang) {
		Log.d("AAAAAAAAAAA",headword);
		StringBuilder sb=new StringBuilder();
		try {
			URL url=new URL(NetworkHelper.dictCnLookupUrl(headword));			
			InputStream in=url.openStream();			
			if (xpp==null){
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				xpp=factory.newPullParser();
			}
			xpp.setInput(in,"UTF-8");
			int eventType = xpp.getEventType();
			String tag="";
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType){
				case XmlPullParser.START_TAG:
					if (xpp.getName().equals(DEF_TAG)){
						tag=DEF_TAG;
					}else if(xpp.getName().equals(PRONETIC_TAG)){
						tag=PRONETIC_TAG;
					}
					break;
				case XmlPullParser.TEXT:
					if(tag.equals(DEF_TAG)){
						sb.append(xpp.getText());
						tag="";
					}else if (tag.equals(PRONETIC_TAG)){
						sb.append(xpp.getText());
						sb.append(System.getProperty("line.separator"));
						tag="";
					}
					break;
				}
				eventType = xpp.next();
			}
			in.close();			
		} catch (XmlPullParserException e) {
			return Dict.ERROR_WORD;
		} catch (IOException e) {
			e.printStackTrace();
			return Dict.ERROR_WORD;
		}
		return sb.toString();
	}

	@Override
	public Word lookup(String headword, String srcLang, String toLang,String nothing) {
		Word w=new Word(headword);
		try {
			URLConnection conn=NetworkHelper.buildUrlConnection(NetworkHelper.dictCnLookupUrl(headword));					
			InputStream in=conn.getInputStream();			
			if (xpp==null){
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				xpp=factory.newPullParser();
			}
			xpp.setInput(in,"UTF-8");
			int eventType = xpp.getEventType();
			String tag="";
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType){
				case XmlPullParser.START_TAG:
					if (xpp.getName().equals(DEF_TAG)){
						tag=DEF_TAG;
					}else if(xpp.getName().equals(PRONETIC_TAG)){
						tag=PRONETIC_TAG;
					}
					break;
				case XmlPullParser.TEXT:
					if(tag.equals(DEF_TAG)){
						w.setMeaning(xpp.getText());
						tag="";
					}else if (tag.equals(PRONETIC_TAG)){
						w.setPhonetic(xpp.getText());
						tag="";
					}
					break;
				}
				eventType = xpp.next();
			}
			in.close();	
		} catch (XmlPullParserException e) {
			w.setMeaning(Dict.ERROR_WORD);
		} catch (IOException e) {
			e.printStackTrace();
			w.setMeaning(Dict.ERROR_WORD);
		}
		return w;
	}

}

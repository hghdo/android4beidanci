package cn.zadui.vocabulary.model.dictionary;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

/**
 * Used to select proper dictionary
 * 
 * @author david
 * 
 */
public class DictFactory {
	
	private static Dict dict;
	
	public static Map<String,String> LangNames;
	
	static{
		LangNames=new HashMap<String,String>();
		LangNames.put("English", "en");
		LangNames.put("Chinese(Simplified)", "zh_CN");
		LangNames.put("Chinese(Traditional)", "zh_TW");
		LangNames.put("French", "fr");
		LangNames.put("Spanish", "es");
		LangNames.put("Russian", "ru");
		LangNames.put("German", "de");
	}
	
	

	public static Dict getDict(Context context,String srcLang,String toLang){
		if (dict!=null && dict.canSupport(srcLang,toLang)) return dict;
		
		if (SimpleDict.support(srcLang, toLang)){
			try {
				dict=SimpleDict.getInstance(null);
				return dict;
			} catch (IOException e) {
			}
		}
		
		if (DictCnDict.support(srcLang, toLang)){
			dict=new DictCnDict();
		}else if (GoogleDict.support(srcLang, toLang)){
			dict=new GoogleDict();
		}else{
			dict=new ErrorDict();
		}
		return dict;
	}
	
}

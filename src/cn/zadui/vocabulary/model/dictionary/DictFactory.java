package cn.zadui.vocabulary.model.dictionary;

import java.io.IOException;
import java.util.Locale;

import android.content.Context;

/**
 * Used to select proper dictionary
 * 
 * @author david
 * 
 */
public class DictFactory {

	public static Dict loadDict(Context context,String srcLang,String toLang){
		return networdDict(context);
//		if(srcLang.equals(Locale.CHINESE.getDisplayLanguage(Locale.ENGLISH)) && toLang.equals(Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH))){
//			return networdDict(context);
//		}else if (srcLang.equals(Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH)) && toLang.equals(Locale.CHINESE.getDisplayLanguage(Locale.ENGLISH))){
//			try {
//				return SimpleDict.getInstance(null);
//			} catch (IOException e) {
//				return networdDict(context);			
//			}
//		}else{
//			return new ErrorDict();
//		}
	}
	
	private static Dict networdDict(Context context){
		//TODO check network
		return new DictCnDict();
	}
	
	
}

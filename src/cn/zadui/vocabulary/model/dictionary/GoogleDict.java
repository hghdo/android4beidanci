package cn.zadui.vocabulary.model.dictionary;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import cn.zadui.vocabulary.model.Word;
import cn.zadui.vocabulary.util.NetworkHelper;

public class GoogleDict implements Dict {

	static Set<String> langSet=null;
	
	public static boolean support(String srcLang, String toLang) {
		if (langSet==null){
			langSet=new HashSet<String>();
			langSet.add(Locale.FRENCH.toString());
			langSet.add(Locale.GERMAN.toString());
			langSet.add(Locale.TRADITIONAL_CHINESE.toString());
			langSet.add("ru");
			langSet.add("es");
		}
		if (srcLang.equals(Locale.ENGLISH.toString())){
			return langSet.contains(toLang);
		}else if (toLang.equals(Locale.ENGLISH.toString())){
			return langSet.contains(srcLang);
		}else{
			return false;
		}
	}
	
	@Override
	public boolean canSupport(String srcLang, String toLang){
		return support(srcLang,toLang);
	}

	@Override
	public String getDictName() {
		return "Google dictionary [http://www.google.com/dictionary]";
	}

	@Override
	public Word lookup(String headword, String srcLang, String toLang) throws LookupException {
		Word w=new Word(headword);
		try {
			
			URLConnection con = NetworkHelper.buildUrlConnection(NetworkHelper.googleDictUrl(headword, srcLang, toLang));
			LineNumberReader reader=new LineNumberReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
			w.setPhonetic(reader.readLine());
			StringBuilder sb=new StringBuilder();
			char[] buffer=new char[100];
			int len=0;
			while((len=reader.read(buffer,0,100))!=-1){
				sb.append(buffer, 0, len);
			}
			w.setMeaning(sb.toString());
			return w;
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new LookupException(e.getMessage());
		}
	}

}

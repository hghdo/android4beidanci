package cn.zadui.vocabulary.model.dictionary;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URLConnection;

import cn.zadui.vocabulary.model.Word;
import cn.zadui.vocabulary.util.NetworkHelper;

public class GoogleDict implements Dict {

	
	@Override
	public boolean canSupport(String srcLang, String toLang) {
		return true;
	}

	@Override
	public String getDictName() {
		return "Google dictionary [http://www.google.com/dictionary]";
	}

	@Override
	public Word lookup(String headword, String srcLang, String toLang,
			String nothing) {
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
		}
		return null;
	}

}

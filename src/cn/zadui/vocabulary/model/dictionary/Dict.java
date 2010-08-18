package cn.zadui.vocabulary.model.dictionary;

import cn.zadui.vocabulary.model.Word;


public interface Dict {
	
	public static final String NET_ERROR="$NETWORK ERROR";
	
	public static final String ERROR_WORD="$ERROR OCCURRED";
	
	//public String lookup(String headword,String srcLang,String toLang);
	
	public Word lookup(String headword,String srcLang,String toLang);
	
	public String getDictName();
	
	public boolean canSupport(String srcLang,String toLang);
}

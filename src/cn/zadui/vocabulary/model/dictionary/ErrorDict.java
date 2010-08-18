package cn.zadui.vocabulary.model.dictionary;

import cn.zadui.vocabulary.model.Word;

public class ErrorDict implements Dict {

	@Override
	public String getDictName() {
		return "ErrorDict";
	}

	@Override
	public Word lookup(String headword, String srcLang, String toLang) {
		Word w=new Word(headword);
		w.setMeaning(Dict.ERROR_WORD);
		return w;
	}

	@Override
	public boolean canSupport(String srcLang, String toLang) {
		return false;
	}
	

	public static boolean support(String srcLang, String toLang) {
		return false;
	}
	

}

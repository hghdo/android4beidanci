package cn.zadui.vocabulary.model.dictionary;

import cn.zadui.vocabulary.model.Word;

public class ErrorDict implements Dict {

	//@Override
	public String lookup(String headword,String srcLang,String toLang) {
		return Dict.ERROR_WORD;
	}

	@Override
	public String getDictName() {
		return "ErrorDict";
	}

	@Override
	public Word lookup(String headword, String srcLang, String toLang,
			String nothing) {
		Word w=new Word(headword);
		w.setMeaning(Dict.ERROR_WORD);
		return w;
	}
	
	

}

package cn.zadui.vocabulary.model.dictionary;

import cn.zadui.vocabulary.model.Word;

/**
 * 
 * @author david
 *
 */
public abstract class NetDict implements Dict {

	@Override
	public abstract String getDictName();

	@Override
	public abstract Word lookup(String headword, String srcLang, String toLang,
			String nothing);

}

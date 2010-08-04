package cn.zadui.vocabulary.service;

import java.util.Locale;

import android.content.Intent;
import android.os.IBinder;
import cn.zadui.vocabulary.model.Word;
import cn.zadui.vocabulary.model.dictionary.Dict;
import cn.zadui.vocabulary.model.dictionary.DictFactory;

public class DictionaryService extends NetworkService {

	public static final String KEY_SRC_LANGUAGE="src";
	
	private String headword;
	private String srcLang;
	private Word word;
	
	public enum LookupState {
        LOOKUP, CANCELED;
    }	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		int action=intent.getExtras().getInt(NetworkService.KEY_ACTION);
		switch (action){
		case NetworkService.LOOKUP_ACTION:
			headword=intent.getExtras().getString(NetworkService.KEY_HEADWORD);
			srcLang=intent.getExtras().getString(KEY_SRC_LANGUAGE);
			(new LookupThread()).start();
			
//			Dict dict=DictFactory.loadDict(this, srcLang, Locale.getDefault().getDisplayLanguage(Locale.ENGLISH));
//			Word w=dict.lookup(headword,null,null,null);
//			stateListener.stateChanged(w);
			break;
		}
	}
	
	
	class LookupThread extends Thread{
		public void run(){	
			Dict dict=DictFactory.buildDict(DictionaryService.this, srcLang, Locale.getDefault().getDisplayLanguage(Locale.ENGLISH));
			word=dict.lookup(headword,null,null,null);
			stateListener.stateChanged(word);
		}
		
	}
	
}

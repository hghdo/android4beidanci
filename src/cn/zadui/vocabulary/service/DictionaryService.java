package cn.zadui.vocabulary.service;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import cn.zadui.vocabulary.model.Word;
import cn.zadui.vocabulary.model.dictionary.Dict;
import cn.zadui.vocabulary.model.dictionary.DictFactory;
import cn.zadui.vocabulary.storage.PrefStore;

public class DictionaryService extends NetworkService {

	static final String TAG="DictionaryService";
	public static final String KEY_SRC_LANGUAGE="src";
	
	private String headword;
	private String srcLang;
	private String toLang;
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
		toLang=DictFactory.LangNames.get(PrefStore.getMotherTongueCode(this));
		int action=intent.getExtras().getInt(NetworkService.KEY_ACTION);
		switch (action){
		case NetworkService.LOOKUP_ACTION:
			headword=intent.getExtras().getString(NetworkService.KEY_HEADWORD);
			srcLang=intent.getExtras().getString(KEY_SRC_LANGUAGE);
			srcLang=DictFactory.LangNames.get(srcLang);
			(new LookupThread()).start();
			
//			Dict dict=DictFactory.loadDict(this, srcLang, Locale.getDefault().getDisplayLanguage(Locale.ENGLISH));
//			Word w=dict.lookup(headword,null,null,null);
//			stateListener.stateChanged(w);
			break;
		}
	}
	
	
	class LookupThread extends Thread{
		public void run(){	
			Dict dict=DictFactory.getDict(DictionaryService.this, srcLang, toLang);
			Log.d(TAG,dict.getDictName());
			word=dict.lookup(headword,srcLang,toLang);
			stateListener.onServiceStateChanged(word);
		}
		
	}
	
}

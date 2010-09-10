package cn.zadui.vocabulary.service;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import cn.zadui.vocabulary.model.Word;
import cn.zadui.vocabulary.model.dictionary.Dict;
import cn.zadui.vocabulary.model.dictionary.DictFactory;
import cn.zadui.vocabulary.model.dictionary.LookupException;
import cn.zadui.vocabulary.storage.PrefStore;

public class DictionaryService extends NetworkService {

	static final String TAG="DictionaryService";
	public static final String KEY_SRC_LANGUAGE="src";
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		final String toLang=DictFactory.LangNames.get(PrefStore.getMotherTongueCode(this));
		int action=intent.getExtras().getInt(NetworkService.KEY_ACTION);
		switch (action){
		case NetworkService.LOOKUP_ACTION:
			final String headword=intent.getExtras().getString(NetworkService.KEY_HEADWORD);
			//final String srcLang=DictFactory.LangNames.get(intent.getExtras().getString(KEY_SRC_LANGUAGE));
			final String srcLang=intent.getExtras().getString(KEY_SRC_LANGUAGE);
			new Thread(){
				public void run(){
					Dict dict=DictFactory.getDict(DictionaryService.this, srcLang, toLang);
					Log.d(TAG,dict.getDictName());
					Word word;
					try {
						word = dict.lookup(headword,srcLang,toLang);
						stateListener.onServiceStateChanged(word,ServiceState.OK);
					} catch (LookupException e) {
						stateListener.onServiceStateChanged(null,ServiceState.GENERAL_ERROR);
					}
				}
			}.start();
			break;
		}
	}
	
}

package cn.zadui.vocabulary.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Locale;

import cn.zadui.vocabulary.model.Word;
import cn.zadui.vocabulary.model.dictionary.Dict;
import cn.zadui.vocabulary.model.dictionary.DictFactory;
import cn.zadui.vocabulary.util.NetworkHelper;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DictionaryService extends Service {

	public static final String KEY_ACTION="action";
	public static final int LOOKUP_ACTION=0;
	public static final int SELECTIVE_EXAMPLE_ACTION=1;
	public static final int GOOGLE_EXAMPLE_ACTION=2;
	public static final String KEY_HEADWORD="headword";
	public static final String KEY_SRC_LANGUAGE="src";
	
	public enum LookupState {
        LOOKUP, CANCELED;
    }	
	
	private static StateChangeListener stateListener;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		String headword;
		int action=intent.getExtras().getInt(KEY_ACTION);
		switch (action){
		case LOOKUP_ACTION:
			headword=intent.getExtras().getString(KEY_HEADWORD);
			String srcLang=intent.getExtras().getString(KEY_SRC_LANGUAGE);
			Dict dict=DictFactory.loadDict(this, srcLang, Locale.getDefault().getDisplayLanguage(Locale.ENGLISH));
			Word w=dict.lookup(headword,null,null,null);
			stateListener.stateChanged(w);
			break;
		case SELECTIVE_EXAMPLE_ACTION:
			headword=intent.getExtras().getString(KEY_HEADWORD);
			try {
				stateListener.stateChanged(
						getStringFromNetIO(
								NetworkHelper.buildUrlConnection(NetworkHelper.exampleUrl(headword, null, null))
								)
						);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case GOOGLE_EXAMPLE_ACTION:
			headword=intent.getExtras().getString(KEY_HEADWORD);
			try {
				stateListener.stateChanged(
						getStringFromNetIO(
								NetworkHelper.buildUrlConnection(NetworkHelper.googleAjaxUrl(headword, null, null))
								)
						);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
	}
	
	public interface StateChangeListener{
		public void stateChanged(Object result);
	} 
	
	public static void setStateChangeListener(StateChangeListener listener) {
		stateListener=listener;
	}
	
	private String getStringFromNetIO(URLConnection con){
		InputStream in=null;
		ByteArrayOutputStream out=null;
		String result="";
		try {
			in=con.getInputStream();
			out=new ByteArrayOutputStream();
			byte[] buf=new byte[1024*8];
			int readCount=0;
			while((readCount=in.read(buf))!=-1){
				out.write(buf, 0, readCount);
			}
			result = out.toString("UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(in!=null)in.close();
				if(out!=null)out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

}

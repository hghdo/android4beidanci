package cn.zadui.vocabulary.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NetworkService extends Service {

	public static final String KEY_ACTION="action";
	public static final int LOOKUP_ACTION=0;
	public static final String KEY_HEADWORD="headword";
	public static final int SELECTIVE_EXAMPLE_ACTION=1;
	public static final int GOOGLE_EXAMPLE_ACTION=2;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	
	public static void setStateChangeListener(StateChangeListener listener) {
		stateListener=listener;
	}
	
	protected static StateChangeListener stateListener=null;
}

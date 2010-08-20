package cn.zadui.vocabulary.service;

import java.io.IOException;

import android.content.Intent;
import android.os.IBinder;
import cn.zadui.vocabulary.util.NetworkHelper;

public class ExampleService extends NetworkService {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		super.onStart(intent, startId);
		final String headword;
		int action=intent.getExtras().getInt(NetworkService.KEY_ACTION);
		switch (action){
		case NetworkService.SELECTIVE_EXAMPLE_ACTION:
			headword=intent.getExtras().getString(NetworkService.KEY_HEADWORD);
			try {
				stateListener.onServiceStateChanged(
						NetworkHelper.getStringFromNetIO(
								NetworkHelper.buildUrlConnection(NetworkHelper.exampleUrl(headword, null, null))
								),
						ServiceState.OK
						);
			} catch (IOException e) {
				stateListener.onServiceStateChanged(null,ServiceState.GENERAL_ERROR);
			}
			break;
		case NetworkService.GOOGLE_EXAMPLE_ACTION:
			headword=intent.getExtras().getString(NetworkService.KEY_HEADWORD);
			new Thread(){
				public void run(){
					try {
						stateListener.onServiceStateChanged(
								NetworkHelper.getStringFromNetIO(
										NetworkHelper.buildUrlConnection(NetworkHelper.googleAjaxUrl(headword, null, null))
										),
								ServiceState.OK
								);
					} catch (IOException e) {
						stateListener.onServiceStateChanged(null,ServiceState.GENERAL_ERROR);
					}					
				}
			}.start();
			break;
		}
	}

}

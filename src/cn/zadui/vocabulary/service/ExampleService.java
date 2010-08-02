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
		String headword;
		int action=intent.getExtras().getInt(NetworkService.KEY_ACTION);
		switch (action){
		case NetworkService.SELECTIVE_EXAMPLE_ACTION:
			headword=intent.getExtras().getString(NetworkService.KEY_HEADWORD);
			try {
				stateListener.stateChanged(
						NetworkHelper.getStringFromNetIO(
								NetworkHelper.buildUrlConnection(NetworkHelper.exampleUrl(headword, null, null))
								)
						);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case NetworkService.GOOGLE_EXAMPLE_ACTION:
			headword=intent.getExtras().getString(NetworkService.KEY_HEADWORD);
			try {
				stateListener.stateChanged(
						NetworkHelper.getStringFromNetIO(
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

}

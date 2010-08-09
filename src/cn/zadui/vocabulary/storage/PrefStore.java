package cn.zadui.vocabulary.storage;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefStore {

	//public static final String PREFS_NAME="beidanci.prefs";
	// Global Preference keys
	public static final String SP_KEY_MOTHER_TONGUE="mother_tongue";
	public static final String SP_KEY_LAST_COURSE_STATUS_ID="last_course_status";
	
    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }
	
	public static long getCurrentCourseStatusId(Context ctx){
		return getSharedPreferences(ctx).getLong(SP_KEY_LAST_COURSE_STATUS_ID, 0L);
	}

	public static void saveCurrentCourseStatusId(Context ctx,long _id){
		SharedPreferences spSettings=getSharedPreferences(ctx);//.getSharedPreferences(PrefStore.PREFS_NAME, 0);
		SharedPreferences.Editor editor = spSettings.edit();
		editor.putLong(SP_KEY_LAST_COURSE_STATUS_ID, _id);
		editor.commit();
	}
	
	/**
	 * 
	 * @param ctx
	 * @return
	 */
	public static String getMotherTongueCode(Context ctx){
		String lang=getSharedPreferences(ctx).getString(SP_KEY_MOTHER_TONGUE, "en");
		if (lang.equals("en"))return lang;
		return lang.substring(lang.indexOf('[')+1,lang.indexOf(']'));
	}
}

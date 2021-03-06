package cn.zadui.vocabulary.storage;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cn.zadui.vocabulary.model.dictionary.DictFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefStore {

	//public static final String PREFS_NAME="beidanci.prefs";
	// Global Preference keys
	public static final String SP_KEY_MOTHER_TONGUE="mother_tongue";
	public static final String SP_KEY_SELECTED_COURSE_STATUS_ID="selected_course_status";
	public static final String SP_KEY_SELECTED_COURSE_TITLE="selected_course_name";
	public static final String SP_KEY_SELECTED_COURSE_KEY="selected_course_key";
	
    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }
	
//	public static long getSelectedCourseStatusId(Context ctx){
//		return getSharedPreferences(ctx).getLong(SP_KEY_SELECTED_COURSE_STATUS_ID, 0L);
//	}

//	public static void saveSelectedCourseStatusId(Context ctx,long _id){
//		SharedPreferences spSettings=getSharedPreferences(ctx);//.getSharedPreferences(PrefStore.PREFS_NAME, 0);
//		SharedPreferences.Editor editor = spSettings.edit();
//		editor.putLong(SP_KEY_SELECTED_COURSE_STATUS_ID, _id);
//		editor.commit();
//	}
	
	public static String getSelectedCourseTitle(Context ctx){
		return getSharedPreferences(ctx).getString(SP_KEY_SELECTED_COURSE_TITLE, "");
	}
	
	public static void saveSelectedCourseTitle(Context ctx,String title){
		SharedPreferences spSettings=getSharedPreferences(ctx);//.getSharedPreferences(PrefStore.PREFS_NAME, 0);
		SharedPreferences.Editor editor = spSettings.edit();
		editor.putString(SP_KEY_SELECTED_COURSE_TITLE, title);
		editor.commit();
		
	}
	
	public static String getSelectedCourseKey(Context ctx){
		return getSharedPreferences(ctx).getString(SP_KEY_SELECTED_COURSE_KEY, "");
	}
	
	public static void saveSelectedCourseKey(Context ctx,String key){
		SharedPreferences spSettings=getSharedPreferences(ctx);//.getSharedPreferences(PrefStore.PREFS_NAME, 0);
		SharedPreferences.Editor editor = spSettings.edit();
		editor.putString(SP_KEY_SELECTED_COURSE_KEY, key);
		editor.commit();
		
	}
	
	/**
	 * 
	 * @param ctx
	 * @return
	 */
	public static String getMotherTongueCode(Context ctx){
		String lang=getSharedPreferences(ctx).getString(SP_KEY_MOTHER_TONGUE,"initial");
		return lang;
//		if (lang.equals("en"))return lang;
//		return lang.substring(lang.indexOf('[')+1,lang.indexOf(']'));
	}
}

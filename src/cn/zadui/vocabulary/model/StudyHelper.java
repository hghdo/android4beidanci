package cn.zadui.vocabulary.model;

import cn.zadui.vocabulary.storage.StudyDbAdapter;

public class StudyHelper {

	public static void studyWord(String headword,StudyDbAdapter adapter){
		adapter.open();
	}
}

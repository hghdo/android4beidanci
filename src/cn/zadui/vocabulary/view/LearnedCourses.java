package cn.zadui.vocabulary.view;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.SimpleAdapter;
import cn.zadui.vocabulary.R;
import cn.zadui.vocabulary.storage.StudyDbAdapter;

public class LearnedCourses extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.learned_courses);
		
		int[] displayViews=new int[]{
				R.id.tv_selected_courses_course_name,
				R.id.tv_selected_courses_learned_count,
				R.id.tv_selected_courses_content_count
				//R.id.tv_unit_created_at,
				//R.id.tv_unit_next_exam_at,
				//R.id.tv_unit_exam_times
				};
		String[] columns=new String[]{	
				//StudyDbAdapter.KEY_ROWID,
				StudyDbAdapter.KEY_COURSE_NAME,
				StudyDbAdapter.KEY_LEARNED_CONTENT_COUNT,
				StudyDbAdapter.KEY_CONTENT_COUNT,
				//StudyDbAdapter.KEY_CREATED_AT,
				//StudyDbAdapter.KEY_NEXT_COMMON_EXAM_AT,
				//StudyDbAdapter.KEY_COMMON_EXAM_TIMES
				};
		StudyDbAdapter dbAdapter=new StudyDbAdapter(this);
		dbAdapter.open();
		LinkedList<Map<String,String>> list=dbAdapter.fetchCourseStatusList();
		Map<String,String> header=new HashMap<String,String>();
		SimpleAdapter adapter=new SimpleAdapter(this,list,R.layout.learned_courses_row,columns,displayViews);
		
		setListAdapter(adapter);
		dbAdapter.close();
	}

}

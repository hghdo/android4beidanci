package cn.zadui.vocabulary.view;

import java.io.File;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.zadui.vocabulary.R;
import cn.zadui.vocabulary.storage.CourseStatus;
import cn.zadui.vocabulary.storage.PrefStore;
import cn.zadui.vocabulary.storage.StudyDbAdapter;

public class Main extends Activity {

	private TextView tvCurrentCourse;
	private TextView tvCurrentProgressTitle;
	private Button btnLearn;
	private ImageButton btnSelectCourse;
	ProgressBar pb;
	CourseStatus status;
	StudyDbAdapter dbAdapter=null;
	
	static final String TAG="MMMMMMMMMMMMMMMMMMain";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		//TODO should move this initial checkDataDir method into {@link Application}
		checkDataDir();
		setContentView(R.layout.summary);
		dbAdapter=new StudyDbAdapter(this);
		dbAdapter.open();
		status=new CourseStatus(PrefStore.getSelectedCourseStatusId(this),dbAdapter);
		
		tvCurrentCourse=(TextView) findViewById(R.id.current_course_name);
		tvCurrentProgressTitle=(TextView) findViewById(R.id.tv_summary_pg_title);
		pb=(ProgressBar)findViewById(R.id.pb_summary_progress);
		btnLearn=(Button)this.findViewById(R.id.btn_learn);
		btnLearn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(v.getContext(), Study.class);
				startActivity(i);
			}
		});
		
		btnSelectCourse=(ImageButton)this.findViewById(R.id.btn_sel_course);
		btnSelectCourse.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent sc=new Intent(v.getContext(), CourseList.class);
				startActivityForResult(sc, LearnedCourses.SELECT_COURSE_REQUEST);
			}
		});

		ImageButton btnUnits=(ImageButton)findViewById(R.id.btn_units);
		btnUnits.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent units=new Intent(v.getContext(),Sections.class);
				startActivity(units);
			}
		});
		ImageButton btnLookup=(ImageButton)findViewById(R.id.btn_lookup);
		btnLookup.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent units=new Intent(v.getContext(),Lookup.class);
				startActivity(units);
				
			}
		});
		
		((ImageButton)findViewById(R.id.btn_settings)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent settings=new Intent(v.getContext(),Settings.class);
				startActivity(settings);
			}
		});
	}	
	
	@Override
	protected void onResume() {
		super.onResume();
		drawCourseSummary();
	}

	@Override
	protected void onDestroy() {
		dbAdapter.close();
		super.onDestroy();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode==LearnedCourses.SELECT_COURSE_REQUEST){
			if (resultCode==RESULT_OK){
				status=new CourseStatus(PrefStore.getSelectedCourseStatusId(this),dbAdapter);
			}
		}
	}
	
	private void drawCourseSummary(){
		if (status.isEmpty()){
			findViewById(R.id.ll_summary_study).setVisibility(View.GONE);
			tvCurrentCourse.setText(getResources().getString(R.string.select_course_first));
			return;
		}else{
			findViewById(R.id.ll_summary_study).setVisibility(View.VISIBLE);
			tvCurrentCourse.setText(status.getCourseName());
			pb.setProgress(status.getProgress());
			tvCurrentProgressTitle.setText(
					//getResources().getString(R.string.current_course_progress_title) +
					//" " +
					String.valueOf(status.getLearnedWordsCount()) + 
					"/" +
					String.valueOf(status.getContentCount())
			);		
		}
	}

	private void checkDataDir(){
		File f=new File(CourseStatus.DATA_DIR);
		if (f.exists() && f.isDirectory()) return;
		f.mkdirs();
	}
}

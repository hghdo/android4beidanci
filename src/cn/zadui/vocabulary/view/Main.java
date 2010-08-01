package cn.zadui.vocabulary.view;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.zadui.vocabulary.R;
import cn.zadui.vocabulary.storage.CourseStatus;

public class Main extends Activity {

	private TextView tvCurrentCourse;
	private TextView tvCurrentProgressTitle;
	private Button btnLearn;
	private ImageButton btnSelectCourse;
	SharedPreferences spSettings ;
	CourseStatus status;
	ProgressBar pb;
	
	static final int SELECT_COURSE_REQUEST=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		checkDataDir();
		setContentView(R.layout.summary);
		
		spSettings = getSharedPreferences(CourseStatus.PREFS_NAME, 0);
		status=new CourseStatus(spSettings);
		
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
				startActivityForResult(sc, SELECT_COURSE_REQUEST);
			}
		});

		ImageButton btnUnits=(ImageButton)findViewById(R.id.btn_units);
		btnUnits.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent units=new Intent(v.getContext(),Units.class);
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
	}	
	
	@Override
	protected void onResume() {
		super.onResume();
		status.refresh(spSettings);
		drawCourseSummary();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode==SELECT_COURSE_REQUEST){
			if (resultCode==RESULT_OK){
				status=new CourseStatus(spSettings);
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

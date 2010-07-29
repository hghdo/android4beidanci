package cn.zadui.vocabulary.view;


import android.app.ListActivity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import cn.zadui.vocabulary.R;
import cn.zadui.vocabulary.storage.CourseStatus;
import cn.zadui.vocabulary.storage.StudyDbAdapter;

public class Review extends ListActivity implements RadioGroup.OnCheckedChangeListener {

	SharedPreferences spSettings;
	CourseStatus status;
	StudyDbAdapter dbAdapter;
    private RadioGroup mRadioGroup;
    private long unitId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//spSettings = getSharedPreferences(Status.PREFS_NAME, 0);		
		//status=Status.getInstance(spSettings);
		unitId=getIntent().getExtras().getLong("id");
		
		setContentView(R.layout.review);
		
		mRadioGroup = (RadioGroup) findViewById(R.id.rg_review_filter);
		mRadioGroup.setOnCheckedChangeListener(this);
		
		findViewById(R.id.rb_review_forgot).setTag(StudyDbAdapter.UNIT_WORDS_FILTER_FORGOT_ONLY);
		findViewById(R.id.rb_not_mastered).setTag(StudyDbAdapter.UNIT_WORDS_FILTER_MASTERED_EXCLUDED);
		findViewById(R.id.rb_review_all).setTag(StudyDbAdapter.UNIT_WORDS_FILTER_ALL);
		
		dbAdapter=new StudyDbAdapter(this);
		dbAdapter.open();
		fillData();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		fillData();
	}

	@Override
	protected void onDestroy() {
		dbAdapter.close();
		super.onDestroy();
	}
	
	private void fillData(){
		int[] viewIds=new int[]{R.id.headword,R.id.meaning};
		String[] columns=new String[]{StudyDbAdapter.KEY_WORD,StudyDbAdapter.KEY_MEANING};
		Cursor cur=dbAdapter.fetchSectionWords(
				unitId,
				(Integer)findViewById(mRadioGroup.getCheckedRadioButtonId()).getTag()
				);
		startManagingCursor(cur);
		SimpleCursorAdapter listAdapter=new SimpleCursorAdapter(this,R.layout.review_row,cur,columns,viewIds);
		setListAdapter(listAdapter);
	}

}

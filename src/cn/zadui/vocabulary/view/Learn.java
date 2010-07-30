package cn.zadui.vocabulary.view;

import java.util.Locale;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.TextView;
import cn.zadui.vocabulary.R;
import cn.zadui.vocabulary.model.LearnCache;
import cn.zadui.vocabulary.model.Section;
import cn.zadui.vocabulary.model.Word;
import cn.zadui.vocabulary.model.course.Course;
import cn.zadui.vocabulary.model.course.SimpleCourse;
import cn.zadui.vocabulary.model.course.Course.EOFCourseException;
import cn.zadui.vocabulary.model.dictionary.Dict;
import cn.zadui.vocabulary.model.dictionary.DictFactory;
import cn.zadui.vocabulary.storage.CourseStatus;
import cn.zadui.vocabulary.storage.StudyDbAdapter;

/**
 * 
 * @author david
 *
 */
public class Learn extends Activity implements View.OnClickListener {

	private TextView tvHeadword;
	private TextView tvMeaning;
	private TextView tvPhonetic;
	private Button btnCloseUnit;
	private Button btnNext;
	private Button btnIgnore;
	private Button btnPrevious;
	private Course course;
	private Dict dict;
	SharedPreferences spSettings;
	CourseStatus status;
	private static final int MAX_UNSAVED_WORDS=5;
	LearnCache cache=new LearnCache(50,MAX_UNSAVED_WORDS);
	Word cw=null;
	StudyDbAdapter adapter=null;
	Section section=null;
	AlphaAnimation alpha;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		spSettings = getSharedPreferences(CourseStatus.PREFS_NAME, 0);		
		status=new CourseStatus(spSettings);
		
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.learn);
		setProgressBarVisibility(true);
		
		course=SimpleCourse.getInstance(status.getCourseFileName());
		//dict=SimpleDict.getInstance(status.getDictFileName());
		dict=DictFactory.loadDict(this,course.getLang(), Locale.getDefault().getDisplayLanguage(Locale.ENGLISH));
		
		adapter=new StudyDbAdapter(this);
		adapter.open();
		section=Section.obtainUnit(adapter,course.getName());
        
		tvHeadword=(TextView) findViewById(R.id.headword);
		tvMeaning=(TextView) findViewById(R.id.meaning);
		tvPhonetic=(TextView) findViewById(R.id.phonetic);
		btnNext=(Button)findViewById(R.id.btn_next_word);
		btnNext.setOnClickListener(this);
		btnIgnore=(Button)findViewById(R.id.btn_mastered_word);
		btnIgnore.setOnClickListener(this);
		//((Button)findViewById(R.id.btn_mastered_word)).setOnClickListener(this);
		btnPrevious=(Button)findViewById(R.id.btn_previous_word);
		btnPrevious.setOnClickListener(this);
		//((Button)findViewById(R.id.btn_previous_word)).setOnClickListener(this);
		btnCloseUnit=((Button)findViewById(R.id.btn_learn_close_unit));
		btnCloseUnit.setOnClickListener(this);
		
		
	}

	@Override
	protected void onPause() {
		status.saveCourseStatusToPreferences(spSettings);
		adapter.saveOrUpdateCourseStatus(status);
		section.saveUnsavedWords();
		super.onPause();
	}
	
	private void display(){
		int percentage=section.getWordsCount()*100/status.getUnitCreateStyleValue();
		if (percentage*100>=10000){
			setProgress(9999);
			btnCloseUnit.setEnabled(true);
		}else{
			setProgress(percentage*100);
		}
		setTitle("Unit Status: " +
				String.valueOf(section.getWordsCount()) +
				"/" +
				String.valueOf(status.getUnitCreateStyleValue()));
		alpha=new AlphaAnimation(0.1f,1.0f);
		alpha.setDuration(5000);
		tvMeaning.setAnimation(alpha);
		tvHeadword.setText(cw.getHeadword());
		tvMeaning.setText(cw.getMeaning());
		tvPhonetic.setText(cw.getPhonetic());
	}

	@Override
	public void onClick(View v) {
		try{
			if (v.getId()==R.id.btn_next_word){
				if (cache.hasNext()){
					cw=cache.forword();
				}else{
					// add current word.
					if (cw!=null) section.addWord(cw);
					// Fetch a new word from course.
					String headword=course.getContent(status.getNextContentOffset());
					//cw=new Word(headword,dict.lookup(headword,null,null));
					cw=dict.lookup(headword,null,null,null);
					// add new word to cache
					cache.add(cw);
					// update status
					status.increaseLearnedWordsCount();
					status.increaseNextContentOffset(headword.getBytes().length+course.getSeparator().length);
					status.setLastWord(headword);
				}
			}else if (v.getId()==R.id.btn_mastered_word){
				String headword=course.getContent(status.getNextContentOffset());
				//cw=new Word(headword,dict.lookup(headword,null,null));
				cw=dict.lookup(headword,null,null,null);
				status.increaseLearnedWordsCount();
				status.increaseNextContentOffset(headword.getBytes().length+course.getSeparator().length);
				status.setLastWord(headword);
			}else if(v.getId()==R.id.btn_previous_word){
				//if (!cache.hasNext() && cw!=null) cache.add(cw);
				cw=cache.back();
				if (cw==null) return;
			}else if(v.getId()==R.id.btn_learn_close_unit){
				section.closeUnit();
				finish();
				return;
			}
			display();
		}catch(EOFCourseException ex){
			section.closeUnit();
			finish();			
		}
	}

	@Override
	protected void onDestroy() {
		adapter.close();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		String lastWord=status.getLastWord();
		if (lastWord.equals(CourseStatus.AT_BEGINNING)){
			tvHeadword.setText(getResources().getString(R.string.begin_word));
			setProgress(0);
			setTitle(this.getResources().getString(R.string.learn_title));
		}else{
			//cw=new Word(lastWord,dict.lookup(lastWord,null,null));
			cw=dict.lookup(lastWord,null,null,null);
			display();
		}
		
	}

}

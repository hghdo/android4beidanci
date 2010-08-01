package cn.zadui.vocabulary.view;

import java.util.Locale;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
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
 * General screen which a tool bar at the bottom of the screen. 
 * The tool bar has buttons that can switch study mode as below:
 * Learn: display the headword/content, phonetic symbol(if available)  and meanings(if available) in the same screen
 * Spelling: let user try to spell the headword/content.
 * Examples: show usage examples of the headword. The examples may get from Internet
 * Lookup: let user lookup this headword or change the spelling to lookup similar words.
 * Also has a 
 * @author Huang Gehua
 *
 */
public class Study extends Activity implements View.OnClickListener {

	private static final int MAX_UNSAVED_WORDS=5;
	
	private ImageButton btnCloseUnit;
	private ImageButton btnNext;
	private ImageButton btnIgnore;
	private ImageButton btnPrevious;
	private Course course;
	
	Dict dict;
	SharedPreferences spSettings;
	CourseStatus status;
	
	
	LearnCache cache=new LearnCache(50,MAX_UNSAVED_WORDS);
	Word cw=null;
	StudyDbAdapter dbAdapter=null;
	Section section=null;

	// Learn controls
	private TextView tvHeadword;
	private TextView tvMeaning;
	private TextView tvPhonetic;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		spSettings = getSharedPreferences(CourseStatus.PREFS_NAME, 0);		
		status=new CourseStatus(spSettings);
		
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.study);
		setProgressBarVisibility(true);
		
		course=SimpleCourse.getInstance(status.getCourseFileName());
		dict=DictFactory.loadDict(this,course.getLang(), Locale.getDefault().getDisplayLanguage(Locale.ENGLISH));
		
		dbAdapter=new StudyDbAdapter(this);
		dbAdapter.open();
		section=Section.obtainUnit(dbAdapter,course.getName());
        
		tvHeadword=(TextView) findViewById(R.id.headword);
		tvMeaning=(TextView) findViewById(R.id.meaning);
		tvPhonetic=(TextView) findViewById(R.id.phonetic);
		btnNext=(ImageButton)findViewById(R.id.btn_next_word);
		btnNext.setOnClickListener(this);
		btnIgnore=(ImageButton)findViewById(R.id.btn_mastered_word);
		btnIgnore.setOnClickListener(this);
		btnPrevious=(ImageButton)findViewById(R.id.btn_previous_word);
		btnPrevious.setOnClickListener(this);
		btnCloseUnit=((ImageButton)findViewById(R.id.btn_learn_close_unit));
		btnCloseUnit.setOnClickListener(this);
		
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
		dbAdapter.close();
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
		//alpha=new AlphaAnimation(0.1f,1.0f);
		//alpha.setDuration(5000);
		//tvMeaning.setAnimation(alpha);
		tvHeadword.setText(cw.getHeadword());
		tvMeaning.setText(cw.getMeaning());
		tvPhonetic.setText(cw.getPhonetic());
	}
	
}

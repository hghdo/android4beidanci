package cn.zadui.vocabulary.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.InputFilter;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import cn.zadui.vocabulary.R;
import cn.zadui.vocabulary.model.LearnCache;
import cn.zadui.vocabulary.model.Section;
import cn.zadui.vocabulary.model.Word;
import cn.zadui.vocabulary.model.course.Course;
import cn.zadui.vocabulary.model.course.SimpleCourse;
import cn.zadui.vocabulary.model.course.Course.EOFCourseException;
import cn.zadui.vocabulary.service.DictionaryService;
import cn.zadui.vocabulary.service.ExampleService;
import cn.zadui.vocabulary.service.NetworkService;
import cn.zadui.vocabulary.service.StateChangeListener;
import cn.zadui.vocabulary.storage.CourseStatus;
import cn.zadui.vocabulary.storage.PrefStore;
import cn.zadui.vocabulary.storage.StudyDbAdapter;

/**
 * General screen which a tool bar at the bottom of the screen. 
 * The tool bar has buttons that can switch study mode as below:
 * Learn: display the headword/content, phonetic symbol(if available)  and meanings(if available) in the same screen
 * Spelling: let user try to spell the headword/content.
 * Examples: show usage examples of the headword. The examples may get from Internet
 * Lookup: let user lookup this headword or change the spelling to lookup similar words.
 * 
 * TODO Should be opened to review a word in {@link Review} activity
 * 
 * @author Huang Gehua
 *
 */
public class Study extends Activity implements View.OnClickListener,StateChangeListener {

	public static final int REVIEW=0;
	public static final int STUDY=1;
	
	private static final String TAG="Study";
	private static final int HANDLE_LOOKUP=0;
	private static final int HANDLE_EXAMPLE=1;
	
	private GestureDetector gestureDetector;
	private View vLearn;
	private View vExamples;
	private View vSpelling;
	
	private ProgressDialog progressDialog;
	
	private CourseStatus status;
	private Course course;
	private Word cw=null;
	private String exampleFor;
	private Section section=null;
	private StudyDbAdapter dbAdapter=null;
	private ArrayList<Map<String,CharSequence>> examples;
	private Handler serviceHandler;
	
	// Learn controls
	private TextView tvHeadword;
	private TextView tvMeaning;
	private TextView tvPhonetic;
	// Spelling controls
	private EditText etSpelling;
	private TextView tvSpellingMeaning;
	
	/**
	 * If is the "last word" then should not add this word into the study section, other wise 
	 * should add it in.
	 */
	private boolean isLastWord=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbAdapter=new StudyDbAdapter(this);
		dbAdapter.open();
		status=new CourseStatus(getIntent().getExtras().getString(StudyDbAdapter.KEY_COURSE_NAME),dbAdapter);
		PrefStore.saveSelectedCourseStatusId(this, status.getRowId());
		//status=new CourseStatus(PrefStore.getCurrentCourseStatusId(this),dbAdapter);
		
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.study);
		setProgressBarVisibility(true);
		
		gestureDetector = new GestureDetector(this,new MySimpleGestureListener());		
		vLearn=findViewById(R.id.learn_snip);
		vExamples=findViewById(R.id.examples_snip);
		vSpelling=findViewById(R.id.spelling_snip);
		bringViewToFront(vLearn);
		
		course=SimpleCourse.getInstance(status.getCourseFileName());
		section=Section.obtain(dbAdapter,course.getName());
        
		tvHeadword=(TextView) findViewById(R.id.headword);
		tvMeaning=(TextView) findViewById(R.id.meaning);
		tvPhonetic=(TextView) findViewById(R.id.phonetic);
		etSpelling=(EditText)findViewById(R.id.et_study_spell);
		tvSpellingMeaning=(TextView) findViewById(R.id.tv_study_spell_meaning);

		((ImageButton)findViewById(R.id.btn_next_word)).setOnClickListener(this);
		((ImageButton)findViewById(R.id.btn_mastered_word)).setOnClickListener(this);
		((ImageButton)findViewById(R.id.btn_previous_word)).setOnClickListener(this);
		((ImageButton)findViewById(R.id.btn_learn_close_section)).setOnClickListener(this);
		((ImageButton)findViewById(R.id.btn_learn_examples)).setOnClickListener(this);
		((ImageButton)findViewById(R.id.btn_learn_study)).setOnClickListener(this);
		((ImageButton)findViewById(R.id.btn_learn_spelling)).setOnClickListener(this);
		((ImageButton)findViewById(R.id.btn_study_spell_check)).setOnClickListener(this);
		
		serviceHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
            	switch (msg.what){
            	case HANDLE_LOOKUP:
            		fillLearnSnipViewContent(cw);
            		break;
            	case HANDLE_EXAMPLE:
    				fillExamplesSnipView();
    				break;
            	}
            }
		};
	}	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

	/**
	 * Callback for service. 
	 * TODO It could be smaller if move network task from Service to Runnable. 
	 */
	@Override
	public void onServiceStateChanged(Object result) {
		if (result instanceof Word){
			cw=(Word)result;
			if (!isLastWord){
				section.addWord(cw);
				isLastWord=false;
			}
			serviceHandler.sendEmptyMessage(HANDLE_LOOKUP);
		}else{
			String jstr=(String)result;
			Log.d(TAG, jstr);
			try {
				JSONObject jo=new JSONObject(jstr);
				JSONArray results=jo.getJSONObject("responseData").getJSONArray("results");
				if (examples==null) examples=new ArrayList<Map<String,CharSequence>>();
				else examples.clear();
				for(int i=0;i<results.length();i++){
					Map<String,CharSequence> ex=new HashMap<String,CharSequence>();
					ex.put("title",results.getJSONObject(i).getString("title"));
					ex.put("content", results.getJSONObject(i).getString("content"));
					ex.put("url", results.getJSONObject(i).getString("blogUrl"));				
					examples.add(ex);
				}
				serviceHandler.sendEmptyMessage(HANDLE_EXAMPLE);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onStart(){
		super.onStart();
		String lastWord=status.getLastWord();
		if (lastWord.equals(CourseStatus.AT_BEGINNING)){
			tvHeadword.setText(getResources().getString(R.string.begin_word));
			setProgress(0);
			setTitle(this.getResources().getString(R.string.learn_title));
		}else{
			isLastWord=true;
			fillLearnSnipViewHeadword(lastWord);
			runLookupService(lastWord);
		}
	}

	/**
	 * TODO Fix hard code text here
	 */
    @Override
    protected Dialog onCreateDialog(int id) {
    	progressDialog = new ProgressDialog(this);
    	switch (id){
    	case HANDLE_LOOKUP:
    		progressDialog.setMessage("Please wait while looking up...");
    		break;
    	case HANDLE_EXAMPLE:
    		progressDialog.setMessage("Please wait while download examples...");
    		break;
    	}
    	progressDialog.setIndeterminate(true);
    	progressDialog.setCancelable(true);
        return progressDialog;
    }

	@Override
	protected void onPause() {
		status.save(dbAdapter);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		dbAdapter.close();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v.getId()==R.id.btn_next_word){
			bringViewToFront(vLearn);
			nextContent();
		}else if (v.getId()==R.id.btn_mastered_word){
			bringViewToFront(vLearn);
			cw.review(dbAdapter, Word.MASTERED);
			nextContent();
		}else if(v.getId()==R.id.btn_previous_word){
			bringViewToFront(vLearn);
			previousContent();
		}else if (v.getId()==R.id.btn_learn_study){
			bringViewToFront(vLearn);
			return;
		}else if (v.getId()==R.id.btn_learn_spelling){
			bringViewToFront(vSpelling);
			tvSpellingMeaning.setText(cw.getMeaning());
			etSpelling.setFilters(new InputFilter[]{
					new InputFilter.LengthFilter(cw.getHeadword().length())
			});
			etSpelling.selectAll();
			etSpelling.setBackgroundColor(this.getResources().getColor(R.color.white));
			return;
		}else if (v.getId()==R.id.btn_learn_examples){
			bringViewToFront(vExamples);
			if (exampleFor!=null && exampleFor.equals(cw.getHeadword())) return;
			exampleFor=cw.getHeadword();
			runExampleService(cw.getHeadword());
			return;
		}else if(v.getId()==R.id.btn_learn_close_section){
			//TODO should give advice if the word amount of the section is too small.
			section.freeze();
			finish();
			return;
		}else if(v.getId()==R.id.btn_study_spell_check){
			etSpelling.selectAll();
			if (etSpelling.getText().toString().equals(cw.getHeadword())){
				etSpelling.setBackgroundColor(this.getResources().getColor(R.color.grey));
			}else{
				etSpelling.setBackgroundColor(this.getResources().getColor(R.color.red));
			}
		}
	}

	/**
	 * This will update the progress bar. 
	 * TODO What does the the progress bar indicate? The whole progress of the course? Or other else?
	 * @param headword
	 */
	private void fillLearnSnipViewHeadword(String headword){
		int percentage=section.getWordsCount()*100/status.getUnitCreateStyleValue();
		if (percentage*100>=10000){
			setProgress(9999); 
		}else{
			setProgress(percentage*100);
		}
		tvHeadword.setText(headword);
		tvPhonetic.setText("");
		tvMeaning.setText("");
	}
	
	private void fillLearnSnipViewContent(Word word){
		//TODO fix hard code text.
		setTitle("Study Status: " +
				String.valueOf(section.getWordsCount()) +
				"/" +
				String.valueOf(status.getUnitCreateStyleValue()));
		tvPhonetic.setText(word.getPhonetic());
		tvMeaning.setText(Html.fromHtml(word.getMeaning()));
		dismissDialog(HANDLE_LOOKUP);
	}
	
	private void fillExamplesSnipView(){
		ListView list=(ListView)findViewById(R.id.lv_study_example_list);
		int[] to = {R.id.tv_example_title,R.id.tv_example_content};
		String [] from = {"title","content"};
		SimpleAdapter adapter=new SimpleAdapter(this,examples,R.layout.examples_row,from,to);
		adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				((TextView)view).setText(Html.fromHtml(textRepresentation));
				return true;
			}
		});
		list.setAdapter(adapter);
		dismissDialog(HANDLE_EXAMPLE);
	}
	
	private void runLookupService(String headword){
		showDialog(HANDLE_LOOKUP);
		DictionaryService.setStateChangeListener(this);
		Intent i=new Intent();
		i.setClass(Study.this, DictionaryService.class);
		i.putExtra(NetworkService.KEY_ACTION, NetworkService.LOOKUP_ACTION);
		i.putExtra(NetworkService.KEY_HEADWORD, headword);
		//TODO set src language
		i.putExtra(DictionaryService.KEY_SRC_LANGUAGE, course.getLang());
		startService(i);
	}
	
	private void runExampleService(String headword){
		showDialog(HANDLE_EXAMPLE);
		ExampleService.setStateChangeListener(this);
		Intent i=new Intent();
		i.setClass(this, ExampleService.class);
		i.putExtra(NetworkService.KEY_ACTION, NetworkService.GOOGLE_EXAMPLE_ACTION);
		i.putExtra(NetworkService.KEY_HEADWORD, headword);
		startService(i);
	}
	
	private void bringViewToFront(View v){
		vLearn.setVisibility(View.GONE);
		vExamples.setVisibility(View.GONE);
		vSpelling.setVisibility(View.GONE);
		v.setVisibility(View.VISIBLE);
	}

	/**
	 * Get next content from the course and display it.
	 * If reach the end of the course then close the activity.
	 */
	private void nextContent() {
		Word newxtWord=section.next(cw);
		if (newxtWord!=null){
			cw=newxtWord;
			fillLearnSnipViewHeadword(cw.getHeadword());
			fillLearnSnipViewContent(cw);
		}else{
			try{
				String headword=course.getContent(status.getNextContentOffset());// Fetch a new word from course.
				isLastWord=false;
				fillLearnSnipViewHeadword(headword);
				status.change(headword,course.getSeparator().length);
				runLookupService(headword);
			}catch(EOFCourseException e){
				section.freeze();
				finish();			
			}			
		}
	}	

	private void previousContent() {
		Word pw=section.previous(cw);
		if (pw==null) {
			//TODO If there was no previous word in this section then pop a
			//dialog to inform user they can use {@link Review} activity to view more.
			Log.d(TAG,"At the begining of this section!");
			return;
		}
		cw=pw;
		fillLearnSnipViewHeadword(cw.getHeadword());
		fillLearnSnipViewContent(cw);
	}
	
	class MySimpleGestureListener extends SimpleOnGestureListener {
		
	    private static final int SWIPE_MIN_DISTANCE = 120;
	    private static final int SWIPE_MAX_OFF_PATH = 250;
	    private static final int SWIPE_THRESHOLD_VELOCITY = 200;		
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) return false;
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				bringViewToFront(vLearn);
				nextContent();
				return true;
			}else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				bringViewToFront(vLearn);
				previousContent();
				return true;
			}
			return false;
		}
	}	
}

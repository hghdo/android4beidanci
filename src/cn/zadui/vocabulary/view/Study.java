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
import android.util.Log;
import android.view.View;
import android.view.Window;
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
import cn.zadui.vocabulary.model.dictionary.Dict;
import cn.zadui.vocabulary.model.dictionary.DictFactory;
import cn.zadui.vocabulary.service.DictionaryService;
import cn.zadui.vocabulary.service.ExampleService;
import cn.zadui.vocabulary.service.NetworkService;
import cn.zadui.vocabulary.service.StateChangeListener;
import cn.zadui.vocabulary.storage.CourseStatus;
import cn.zadui.vocabulary.storage.StudyDbAdapter;

/**
 * General screen which a tool bar at the bottom of the screen. 
 * The tool bar has buttons that can switch study mode as below:
 * Learn: display the headword/content, phonetic symbol(if available)  and meanings(if available) in the same screen
 * Spelling: let user try to spell the headword/content.
 * Examples: show usage examples of the headword. The examples may get from Internet
 * Lookup: let user lookup this headword or change the spelling to lookup similar words.
 * 
 * @author Huang Gehua
 *
 */
public class Study extends Activity implements View.OnClickListener,StateChangeListener {

	private static final int MAX_UNSAVED_WORDS=5;
	private static final String TAG="Study";
	
	private View vLearn;
	private View vExamples;
	
	private ImageButton btnCloseUnit;
	private ImageButton btnNext;
	private ImageButton btnIgnore;
	private ImageButton btnPrevious;
	
	private ProgressDialog progressDialog;
    private static final int MAX_PROGRESS = 100;
	
	private SharedPreferences spSettings;
	private CourseStatus status;
	private Course course;
	private Dict dict;
	private LearnCache cache=new LearnCache(50,MAX_UNSAVED_WORDS);
	private Word cw=null;
	private Section section=null;
	private StudyDbAdapter dbAdapter=null;
	private ArrayList<Map<String,CharSequence>> examples;
	private Handler serviceHandler;
	

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
		
		vLearn=findViewById(R.id.learn_snip);
		vExamples=findViewById(R.id.examples_snip);
		vExamples.setVisibility(View.GONE);
		
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
		((ImageButton)findViewById(R.id.btn_learn_examples)).setOnClickListener(this);
		
		serviceHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
            	//TODO add example handler.
            	switch (msg.what){
            	case 1:
            		fillLearnSnipViewContent();
            		break;
            	}
        		if (progressDialog!=null) progressDialog.dismiss();;
            }
		};
		
	}	

	@Override
	public void onClick(View v) {
		try{
			if (v.getId()==R.id.btn_next_word){
				showView(vLearn);
				if (cache.hasNext()){
					cw=cache.forword();
					fillLearnSnipViewHeadword(cw.getHeadword());
					fillLearnSnipViewContent();
				}else{
					// add current word to section
					if (cw!=null) section.addWord(cw);
					// Fetch a new word from course.
					String headword=course.getContent(status.getNextContentOffset());
					fillLearnSnipViewHeadword(headword);
					updateCourseStatus(headword);
					showDialog(0);
					runLookupService(headword);
				}
			}else if (v.getId()==R.id.btn_mastered_word){
				showView(vLearn);
				String headword=course.getContent(status.getNextContentOffset());
				runLookupService(headword);
				updateCourseStatus(headword);
			}else if(v.getId()==R.id.btn_previous_word){
				showView(vLearn);
				// TODO select previous word from db.
				cw=cache.back();
				if (cw==null) return;
				fillLearnSnipViewHeadword(cw.getHeadword());
				fillLearnSnipViewContent();
			}else if (v.getId()==R.id.btn_learn_examples){
				showView(vExamples);
				ExampleService.setStateChangeListener(this);
				Intent i=new Intent();
				i.setClass(this, ExampleService.class);
				i.putExtra(NetworkService.KEY_ACTION, NetworkService.GOOGLE_EXAMPLE_ACTION);
				i.putExtra(NetworkService.KEY_HEADWORD, cw.getHeadword());
				startService(i);
				return;
			}else if(v.getId()==R.id.btn_learn_close_unit){
				section.closeUnit();
				finish();
				return;
			}
		}catch(EOFCourseException ex){
			section.closeUnit();
			finish();			
		}
	}

	@Override
	public void stateChanged(Object result) {
		cw=(Word)result;
		serviceHandler.sendEmptyMessage(0);
		/*
		if (result instanceof Word){
			cw=(Word)result;
			fillLearnSnipViewContent();
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
				fillExamplesSnipView();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if (progressDialog!=null) progressDialog.dismiss();;
		*/
	}

	@Override
	protected void onPause() {
		status.saveCourseStatusToPreferences(spSettings);
		dbAdapter.saveOrUpdateCourseStatus(status);
		section.saveUnsavedWords();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		dbAdapter.close();
		super.onDestroy();
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
			fillLearnSnipViewHeadword(lastWord);
			showDialog(0);
			runLookupService(lastWord);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

    @Override
    protected Dialog onCreateDialog(int id) {
    	progressDialog = new ProgressDialog(this);
    	progressDialog.setMessage("Please wait while loading...");
    	progressDialog.setIndeterminate(true);
    	progressDialog.setCancelable(true);
        return progressDialog;

    }
	
    /*
	private void fillLearnSnipView(){
		int percentage=section.getWordsCount()*100/status.getUnitCreateStyleValue();
		if (percentage*100>=10000){
			setProgress(9999);
			btnCloseUnit.setEnabled(true);
		}else{
			setProgress(percentage*100);
		}
		//TODO fix hard code text.
		setTitle("Unit Status: " +
				String.valueOf(section.getWordsCount()) +
				"/" +
				String.valueOf(status.getUnitCreateStyleValue()));
		//alpha=new AlphaAnimation(0.1f,1.0f);
		//alpha.setDuration(5000);
		//tvMeaning.setAnimation(alpha);
		tvHeadword.setText(cw.getHeadword());
		tvMeaning.setText(Html.fromHtml(cw.getMeaning()));
		tvPhonetic.setText(cw.getPhonetic());
	}
	*/
	
	private void fillLearnSnipViewHeadword(String headword){
		int percentage=section.getWordsCount()*100/status.getUnitCreateStyleValue();
		if (percentage*100>=10000){
			setProgress(9999);
			btnCloseUnit.setEnabled(true);
		}else{
			setProgress(percentage*100);
		}
		//TODO fix hard code text.
		setTitle("Unit Status: " +
				String.valueOf(section.getWordsCount()) +
				"/" +
				String.valueOf(status.getUnitCreateStyleValue()));
		tvHeadword.setText(headword);
	}
	
	private void fillLearnSnipViewContent(){
		tvPhonetic.setText(cw.getPhonetic());
		tvMeaning.setText(Html.fromHtml(cw.getMeaning()));
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
	}
	
	private void runLookupService(String headword){
		
		DictionaryService.setStateChangeListener(this);
		Intent i=new Intent();
		i.setClass(Study.this, DictionaryService.class);
		i.putExtra(NetworkService.KEY_ACTION, NetworkService.LOOKUP_ACTION);
		i.putExtra(NetworkService.KEY_HEADWORD, headword);
		//TODO set src language
		i.putExtra(DictionaryService.KEY_SRC_LANGUAGE, Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH));
		startService(i);
		//(new lookupThread(headword)).start();
	}

	private void updateCourseStatus(String headword) {
		status.increaseLearnedWordsCount();
		status.increaseNextContentOffset(headword.getBytes().length+course.getSeparator().length);
		status.setLastWord(headword);
	}
	
	private void showView(View v){
		vLearn.setVisibility(View.GONE);
		vExamples.setVisibility(View.GONE);
		v.setVisibility(View.VISIBLE);
	}
	
}

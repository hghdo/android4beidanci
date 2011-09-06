package cn.zadui.vocabulary.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.zadui.vocabulary.model.Word;
import cn.zadui.vocabulary.service.DictionaryService;
import cn.zadui.vocabulary.service.ExampleService;
import cn.zadui.vocabulary.service.NetworkService;
import cn.zadui.vocabulary.service.StateChangeListener;
import cn.zadui.vocabulary.service.NetworkService.ServiceState;
import cn.zadui.vocabulary.storage.StudyDbAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import cn.zadui.vocabulary.R;

public class Examples extends ListActivity implements StateChangeListener,View.OnClickListener {

	private Word word;
//	private StudyDbAdapter dbAdapter;
	
//	private TextView tvExamAnswer;
//	private View bottomBar;
//	private Button btnAnswer;
	
	private ArrayList<Map<String,CharSequence>> examples;
	
	private ProgressDialog progressDialog;

	private static final int HANDLE_EXAMPLE=1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.examples_snip);
		
//		//pb=(ProgressBar)findViewById(R.id.pb_example_progress);
//		setProgress(0);
//		
//		findViewById(R.id.btn_example_mastered).setOnClickListener(this);
//		findViewById(R.id.btn_example_mastered).setTag(Word.MASTERED);
//		findViewById(R.id.btn_example_pass).setOnClickListener(this);
//		findViewById(R.id.btn_example_pass).setTag(Word.PASS);
//		findViewById(R.id.btn_example_forgot).setOnClickListener(this);
//		findViewById(R.id.btn_example_forgot).setTag(Word.FORGOT);
						
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	private void fillExamples(){
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


	@Override
	public void onServiceStateChanged(Object result,ServiceState state){
		String jstr=(String)result;
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
			fillExamples();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onClick(View v) {

	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id){
		case HANDLE_EXAMPLE:
	    	progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("Please wait while download examples...");
	    	progressDialog.setIndeterminate(true);
	    	progressDialog.setCancelable(true);
	    	return progressDialog;
		}
        return new AlertDialog.Builder(Examples.this)
        //.setIcon(R.drawable.alert_dialog_icon)
        .setTitle(R.string.exam_finished)
        .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	Examples.this.finish();
            }
        })
        .create();

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

}




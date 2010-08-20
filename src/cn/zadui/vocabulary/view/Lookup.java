package cn.zadui.vocabulary.view;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.zadui.vocabulary.R;
import cn.zadui.vocabulary.model.Word;
import cn.zadui.vocabulary.model.dictionary.Dict;
import cn.zadui.vocabulary.model.dictionary.DictFactory;
import cn.zadui.vocabulary.service.DictionaryService;
import cn.zadui.vocabulary.service.NetworkService;
import cn.zadui.vocabulary.service.StateChangeListener;
import cn.zadui.vocabulary.service.NetworkService.ServiceState;

public class Lookup extends Activity implements StateChangeListener {

	// private Dict dict;
	SharedPreferences spSettings;
	Dict dict;
	private TextView tvResult;
	private EditText etHeadword;
	private TextView tvHeadword;
	private TextView tvPhonetic;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dict = DictFactory.getDict(this,
				Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH),
				Locale.CHINESE.getDisplayLanguage(Locale.ENGLISH));

		setContentView(R.layout.lookup);

		tvResult = (TextView) findViewById(R.id.tv_lookup_result);
		tvHeadword = (TextView) findViewById(R.id.tv_lookup_headword);
		tvPhonetic = (TextView) findViewById(R.id.tv_lookup_phonetic);
		etHeadword = (EditText) findViewById(R.id.et_lookup_headword);
		Button b = (Button) findViewById(R.id.btn_lookup);
		b.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String headword = etHeadword.getText().toString().trim();
				//TODO set src language
				runLookupService(headword,Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH));
				
			}
		});

	}
	
	private void runLookupService(String headword,String srcLang){
		
		String str="<span class='dct-tt'>书</span><span class='dct-tp'>&nbsp;&nbsp;&nbsp;</span><span class='dct-tp'>[shu]<span class='dct-tlb'>Pinyin</span></span>";
		Spanned span=Html.fromHtml(str);
		Log.d("SSSSSSSSSSSSSSSSS",span.toString());
		
		
		DictionaryService.setStateChangeListener(this);
		Intent i=new Intent();
		i.setClass(this, DictionaryService.class);
		i.putExtra(NetworkService.KEY_ACTION, NetworkService.LOOKUP_ACTION);
		i.putExtra(NetworkService.KEY_HEADWORD, headword);
		//TODO set src language
		i.putExtra(DictionaryService.KEY_SRC_LANGUAGE, Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH));
		startService(i);
	}

	@Override
	public void onServiceStateChanged(Object word,ServiceState state) {
		Word w = (Word)word;
		tvHeadword.setText(w.getHeadword());
		Typeface mFace=Typeface.createFromAsset(getAssets(), "font/SEGOEUI.TTF");
		tvPhonetic.setTypeface(mFace);
		tvPhonetic.setText(w.getPhonetic());
		tvResult.setText(w.getMeaning());
	}

}

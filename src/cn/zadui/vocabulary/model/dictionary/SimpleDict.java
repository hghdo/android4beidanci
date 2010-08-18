package cn.zadui.vocabulary.model.dictionary;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Locale;
import java.util.TreeMap;
import java.util.regex.Pattern;

import cn.zadui.vocabulary.model.Helper;
import cn.zadui.vocabulary.model.Word;
import cn.zadui.vocabulary.storage.CourseStatus;

import android.util.Log;

public class SimpleDict implements Dict {

	public static final String DICT_NAME_KEY="dict_name";
	public static final String TOP_SIZE_KEY="top_size";
	public static final String TOP_COUNT_KEY="top_count";
	public static final String SECOND_SIZE_KEY="second_size";
	public static final String SECTION_COUNT_KEY="section_count";
	public static final String WORDS_COUNT_KEY="word_count";


	private static Dict dict=null;
	
	private String dictFilePath="";
	private String dictName="";
	private int headSize=0;
	private int version=0;
	private int wordsCount=0;
	private int topSize=0;
	private int secSize=0;
	private int topCount=0;
	private int sectionCount=0;
	private int secOffset=0;
	private int dictOffset=0;
	private String langPair="";
	private TopIndex topIndex=null;
	private Cache cache=null;
	
	private byte[] buf=new byte[50];
	private byte[] buffer=new byte[8192];
	/**
	 * There are 2 bytes(short integer) before the heads means the head length.
	 */
	public static final int BEFORE_HEAD=2;
	/**
	 * DICT HEAD is the foremost bytes of a dictionary that stores common information.
	 */
	public static final int DICT_HEAD_SIZE=8;
	
	public static boolean support(String srcLang, String toLang) {
		return (
				srcLang.equals(Locale.ENGLISH.toString()) 
				&& 
				toLang.equals(Locale.SIMPLIFIED_CHINESE.toString())
				);
	}
	
	@Override
	public boolean canSupport(String srcLang, String toLang) {
		return support(srcLang, toLang);
	}
	
	/**
	 * If there are no dictionary was specified in the status then scan {@link CourseStatus.DATA_DIR data} 
	 * directory for dictionaries. If there are more than one dictionaries existed then use 
	 * the latest version.
	 * @param status
	 * @return
	 * @throws IOException 
	 */
	public static Dict getInstance(String filename) throws IOException{
		if (dict==null){
			if (filename==null || filename.length()==0){
				filename="lazyworm_ec.all";
			}
			dict=new SimpleDict(CourseStatus.DATA_DIR + filename);

		}
		return dict;
	}
	
	private SimpleDict(String dictPath) throws IOException{
		dictFilePath=dictPath;
		InputStream in=new FileInputStream(dictFilePath);
		readDictInfo(in);
		topIndex=new TopIndex(in,topSize,topCount);
		secOffset=SimpleDict.BEFORE_HEAD+headSize+topSize;
		dictOffset=secOffset+secSize;
		in.close();
		cache=new Cache(50);
	}
	
	@Override
	public Word lookup(String headword, String srcLang, String toLang) {
		return convert(
				headword,look(headword, srcLang, toLang)
				);
	}	
	
	private void readDictInfo(InputStream in) throws IOException{
		in.read(buf,0,2);
		headSize=Helper.byteArray2Short(buf,0);
		int point=0;
		String key="";
		String value="";
		for(int i=0;i<headSize;i++){
			char c=(char)in.read();
			if (c==':'){
				key=new String(buf,0,point,"UTF-8");
				point=0;
			}else if (c=='\n'){
				value=new String(buf,0,point,"UTF-8");
				point=0;
				if (key.equals(DICT_NAME_KEY)) dictName=value;
				else if (key.equals(TOP_SIZE_KEY)) topSize=Integer.valueOf(value);
				else if (key.equals(TOP_COUNT_KEY)) topCount=Integer.valueOf(value);
				else if (key.equals(SECOND_SIZE_KEY)) secSize=Integer.valueOf(value);
				else if (key.equals(SECTION_COUNT_KEY)) sectionCount=Integer.valueOf(value);
				else if (key.equals(WORDS_COUNT_KEY)) wordsCount=Integer.valueOf(value);
			}else{
				buf[point]=(byte)c;
				point++;
			}
		}
	}
	
	/**
	 * TODO validate the headword parameter before do lookup
	 */
	private String look(String headword,String srcLang,String toLang){
		Log.d("Go into lookup", headword);
		headword=headword.toLowerCase();
		String cacheResult=cache.lookup(headword);
		if (cacheResult!=null) return cacheResult;
		
		String result="Empty";

		int secItemOff=topIndex.getSecondOffset(headword);
		if (secItemOff==-1) return "Word not found in dictionary";
		int itemOff=secItemOff+secOffset;
//		Log.d("itemOff",String.valueOf(itemOff));
		try{
			RandomAccessFile ranIn=new RandomAccessFile(dictFilePath,"r");
//			Log.d("XXXXXXXXXXXXXXXXXXXXX before the skip action","FFFFFFFFFFFFFFF");
			ranIn.seek(itemOff);
			boolean found=false;
			String word="";
			String wordDictOffset="";
			int point=0;
			ranIn.read(buffer);
			for (int i=0;i<buffer.length;i++){
				char c=(char)buffer[i];
				if (c==':'){
					word=new String(buf,0,point,"UTF-8");
					point=0;
					if (headword.equals(word)) found=true;
					if (word.compareToIgnoreCase(headword) > 0) return "Word not found";
				}else if (c=='\n'){
					if (found) {
						wordDictOffset=new String(buf,0,point,"UTF-8");
						break;
					}
					point=0;
				}else{
					buf[point]=(byte)c;
					point++;
				}
			}
			ranIn.seek(dictOffset+Integer.valueOf(wordDictOffset));
			ranIn.read(buf,0,2);
			short sectionLenght=Helper.byteArray2Short(buf, 0);
			Log.d("sectionLenght",String.valueOf(sectionLenght));
			if (buf.length<sectionLenght)buf=new byte[sectionLenght];
			ranIn.read(buf,0,sectionLenght);
			ranIn.close();
			result=new String(buf,0,sectionLenght);
			cache.add(headword, result);
		}catch(IOException ex){
			result="ERRORS occurred!";
		}
		return result;
	}

	public String getDictName() {
		return dictName;
	}

	public int getWordsCount() {
		return wordsCount;
	}

	public String getLangPair() {
		return langPair;
	}	
	
	private static class Cache{
		private TreeMap<String,String> meanings=null;
		private int maxSize=50;
		public Cache(){
			meanings=new TreeMap<String,String>();
		}
		
		public Cache(int maxCacheSize){
			meanings=new TreeMap<String,String>();
			maxSize=maxCacheSize;
		}
		
		public void add(String w,String m){
			meanings.put(w, m);
			if (meanings.size()>maxSize) meanings.remove(meanings.firstKey());
		}
		
		public String lookup(String w){
			return meanings.get(w);
		}
	}
	
	private static class TopIndex{
		
		private byte[][]wordBytes;
		private byte[][]offBytes;
		
		
		public TopIndex(InputStream in,int topSize,int topCount) throws IOException{
			byte[] buffer=new byte[50];
			int point=0;

			wordBytes=new byte[topCount][];
			offBytes=new byte[topCount][];
			int count=0;
			char c;
			byte[] topBytes=new byte[topSize];
			in.read(topBytes);
			
			for (int i=0;i<topSize;i++){
				c=(char)topBytes[i];
				if (c==':'){
					byte[] tmp=new byte[point];
					System.arraycopy(buffer, 0, tmp, 0, tmp.length);
					wordBytes[count]=tmp;
					point=0;
				}else if (c=='\n'){
					byte[] tmp=new byte[point];
					System.arraycopy(buffer, 0, tmp, 0, tmp.length);
					offBytes[count++]=tmp;
					point=0;
				}else{
					buffer[point++]=(byte)c;
				}
			}
		}
		
		public int getSecondOffset(String headword){
			byte[] result=null;
			int starAt=0;
			int endAt=wordBytes.length/2;
			String middle=new String(wordBytes[(wordBytes.length/2)]);
			if (headword.compareToIgnoreCase(middle)>0){
				starAt=wordBytes.length/2;
				endAt=wordBytes.length;
			}
			String current="";
			//byte[] headwordBytes=headword.getBytes();
			for(;starAt<endAt;starAt++){
				current=new String(wordBytes[starAt]);
				if (current.compareToIgnoreCase(headword)==0){
					result = offBytes[starAt];
					break;
				}else if (current.compareToIgnoreCase(headword)>0){
					result = offBytes[starAt-1];
					break;
				}else{
				}
			}
			return result==null ? -1 : Integer.valueOf(new String(result));
		}
		
	}
	
	private Word convert(String headword,String result){
		Word w=new Word(headword);
		if (result.equals(Dict.ERROR_WORD)){
			w.setMeaning(Dict.ERROR_WORD);
			return w;
		}
		if (Pattern.matches("[a-zA-Z]+", headword) && result.startsWith("[")){
			w.setPhonetic(result.substring(0,result.indexOf('\n')));
			w.setMeaning(result.substring(result.indexOf('\n')+1));
		}else{
			w.setMeaning(result);
		}
		return w;
	}

	
}

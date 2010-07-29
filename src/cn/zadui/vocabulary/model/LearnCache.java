package cn.zadui.vocabulary.model;

import java.util.LinkedList;


public class LearnCache {
	public int maxCacheSize=50;
	//public int maxUnsavedSize=5;
	public int pointer=0;
	private LinkedList<Word> words;
	//private LinkedList<Word> unsaved;
	
	public LearnCache(int maxCacheSize,int maxUnsavedSize){
		words=new LinkedList<Word>();
		//unsaved=new LinkedList<Word>();
		this.maxCacheSize=maxCacheSize;
		//this.maxUnsavedSize=maxUnsavedSize;
	}
	
	public boolean hasNext(){
		return (pointer+1)<words.size();
	}
	
	public void add(Word w){
		words.addLast(w);
		//unsaved.addLast(w);
		if (words.size()>maxCacheSize) words.removeFirst();
		pointer=words.size()-1;
	}
	
	
	public Word forword(){
		return words.get(++pointer);
	}
	
	public Word back(){
		if (pointer>0) return words.get(--pointer);
		else if (pointer==0 && (words==null || words.size()<0)) return words.get(pointer);
		else return null;
	}

//	public LinkedList<Word> getUnsaved() {
//		return unsaved;
//	}
//	
//	public void clearUnsaved(){
//		unsaved.clear();
//	}
//	
//	public boolean isUnsavedFull(){
//		return unsaved.size()>maxUnsavedSize;
//	}
	
}

/**
 * 
 */
package cn.zadui.vocabulary.service;

import cn.zadui.vocabulary.service.NetworkService.ServiceState;

public interface StateChangeListener{
	public void onServiceStateChanged(Object result,ServiceState state);
}
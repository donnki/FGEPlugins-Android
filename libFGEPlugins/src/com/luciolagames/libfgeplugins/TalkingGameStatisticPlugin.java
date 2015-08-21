package com.luciolagames.libfgeplugins;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;

import com.tendcloud.tenddata.TDGAAccount;
import com.tendcloud.tenddata.TDGAItem;
import com.tendcloud.tenddata.TDGAMission;
import com.tendcloud.tenddata.TDGAVirtualCurrency;
import com.tendcloud.tenddata.TalkingDataGA;

public class TalkingGameStatisticPlugin {
	private Activity context;
//	private static final String APP_ID = "2850691E320321C073BA88D706228661";
//	private static final String APP_CHANNEL = "GooglePlay";
	
	
	public TalkingGameStatisticPlugin(Activity context){
		this.context = context;
		TalkingDataGA.init(context, context.getString(R.string.tdAppID), context.getString(R.string.tdAppChannel));
		TDGAAccount account = TDGAAccount.setAccount(TalkingDataGA.getDeviceId(context)); 
		account.setAccountType(TDGAAccount.AccountType.ANONYMOUS);
	}
	
	public void onResume(){
		TalkingDataGA.onResume(context);
	}
	
	public void onPause(){
		TalkingDataGA.onPause(context);
	}
	
	public static void onStatisticEvent(String eventId, String jsonParams){
		Map<String, String> map = new HashMap<String, String>();
		try {
			JSONObject object = new JSONObject(jsonParams);
			if(object != JSONObject.NULL) {
			    Iterator<String> keysItr = object.keys();
			    while(keysItr.hasNext()) {
			        String key = keysItr.next();
			        String value = object.getString(key);
			        map.put(key, value);
			    } 
			} 
		} catch (JSONException e) {
			e.printStackTrace(); 
		}
		TalkingDataGA.onEvent(eventId, map);		
	}
	
	public static void onStatisticMission(String missionId, int state, String param){
		if(state == 0){
			TDGAMission.onBegin(missionId);
		}else if(state == 1){
			TDGAMission.onCompleted(missionId);
		}else if(state == 2){
			TDGAMission.onFailed(missionId, param);
		}
	}
	
	public static void onStatisticReward(int amount, String reason){
		TDGAVirtualCurrency.onReward(amount, reason);
	}
	
	public static void onStatisticChargeRequest(String orderId, String iapId, int currencyAmount, String currencyType, int virtualCurrencyAmount, String paymentType){
		TDGAVirtualCurrency.onChargeRequest(orderId, iapId, currencyAmount, currencyType, virtualCurrencyAmount, paymentType);
	}
	
	public static void onStatisticChargeSuccess(String orderId){
		TDGAVirtualCurrency.onChargeSuccess(orderId);
	}
	
	public static void onStatisticPurchase(String item, int itemNumber, int priceinVirtualCurrency){
		TDGAItem.onPurchase(item, itemNumber, priceinVirtualCurrency);
	}
	
	public static void onUse(String item, int itemNumber){
		TDGAItem.onUse(item, itemNumber);
	}
}

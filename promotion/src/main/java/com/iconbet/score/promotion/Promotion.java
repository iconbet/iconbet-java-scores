package com.iconbet.score.promotion;

import static java.math.BigInteger.ZERO;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;

import com.eclipsesource.json.JsonValue;
import score.Address;
import score.Context;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;
import scorex.util.ArrayList;
import scorex.util.HashMap;

public class Promotion {
	protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);

	public static final String TAG = "Promotion";
	public static BigInteger TEN_18 = new BigInteger("1000000000000000000");
	public static int[] WAGER_WAR_PRIZE = new int[] {25, 20, 15, 10, 10, 6, 6, 3, 3, 2};

	private static final String _REWARDS_SCORE = "rewards_score";
	private static final String _DIVIDENDS_SCORE = "dividends_score";
	private static final String _TOTAL_PRIZES = "total_prizes";

	public VarDB<Address> _rewards_score = Context.newVarDB(_REWARDS_SCORE, Address.class);
	public VarDB<Address> _dividends_score = Context.newVarDB(_DIVIDENDS_SCORE, Address.class);
	public VarDB<BigInteger> _total_prizes = Context.newVarDB(_TOTAL_PRIZES, BigInteger.class);

	public Promotion(@Optional boolean _on_update_var){
		if(_on_update_var) {
			Context.println("updating contract only");
			onUpdate();
			return;
		}

		this._total_prizes.set(ZERO);

	}

	public void onUpdate() {
		Context.println("calling on update. "+TAG);
	}

	@EventLog(indexed=2)
	public void FundTransfer(String to, BigInteger amount, String note) {}

	@External(readonly=true)
	public String name() {
		return "ICONbet Promotion";
	}

	/*
    Sets the rewards core address. Only owner can set the address
    :param _score: Address of the rewards score
    :type _score: :class:`iconservice.base.address.Address`
    :return:
	 */
	@External
	public void set_rewards_score(Address _score) {
		if ( Context.getCaller().equals(Context.getOwner()) ){
			Context.println("setting reward score as: "+ _score);
			this._rewards_score.set(_score);
		}
	}

	/*
    Returns the Rewards score address
    :return: Address of the rewards score
    :rtype: :class:`iconservice.base.address.Address`
	 */
	@External(readonly=true)
	public Address get_rewards_score() {
		return this._rewards_score.getOrDefault(ZERO_ADDRESS);
	}

	/*
    Sets the dividends score address. Only owner can set the address
    :param _score: Address of the dividends score
    :type _score: :class:`iconservice.base.address.Address`
    :return:
	 */
	@External
	public void set_dividends_score(Address _score) {
		if ( Context.getCaller().equals(Context.getOwner()) ){
			Context.println("setting dividens score as: "+_score);
			this._dividends_score.set(_score);
		}
	}

	/*
    Returns the dividends score address
    :return: Address of the dividends score
    :rtype: :class:`iconservice.base.address.Address`
	 */
	@External(readonly=true)
	public Address get_dividends_score() {
		return this._dividends_score.getOrDefault(ZERO_ADDRESS);
	}

	/*
    Distributes the prizes it receive to the top 10 wagerers
    :return:
	 */
	@SuppressWarnings("unchecked")
	public void _distribute_prizes() {
		String json = Context.call(String.class, this._rewards_score.get(),  "get_daily_wager_totals");

		Context.println("json received: " + json);
		JsonObject wagerTotals = Json.parse(json).asObject();
		if(wagerTotals.get("yesterday").isNull()) {
			Context.println("no wagers found for Yestarday");
			return;
		}

		List<String> it = splitString(wagerTotals.get("yesterday").asString());
		Context.println(it.get(0));


		Map<String, Object>[] wagers = new Map[it.size()];

		for (int j = 0; j < it.size(); j++) {
			Context.println("reached in for " + TAG);
			Map<String, Object> wagerMap = makeMap(it.get(j));
 			wagers[j] = wagerMap;
			Context.println("wager found: " + wagers[j]);
		}

		mergeSort(wagers, 0, it.size() - 1);

		Context.println("sorted and reached here");

		Map<String, Object>[] topWagers = ArrayUtils.top(wagers, 10, false);
		Context.println("reached in for ......???.....");


		int totalPercent = 0;
		for(int i = topWagers.length-1 ; i >= 0 ; i-- ) {
			totalPercent = totalPercent + WAGER_WAR_PRIZE[i]; 
		}

		int i = 0;
		//TODO: test this logic in depth
		BigInteger totalPrizes = this._total_prizes.get();
		for (Map<String, Object> es: topWagers) {
			Context.println("reached in for ..............");
			String address =  (String) es.get("address");
			BigInteger prize = BigInteger.valueOf(WAGER_WAR_PRIZE[i]).multiply(totalPrizes).divide(BigInteger.valueOf(totalPercent));
			totalPercent -= WAGER_WAR_PRIZE[i];
			totalPrizes = totalPrizes.subtract(prize);
			try {
				Context.transfer(Address.fromString(address), prize);
				this.FundTransfer(address, prize, "Wager Wars prize distribution");
			}catch (Exception e) {
				Context.revert("Network problem. Prize not sent. Will try again later. Exception:"+ e.getMessage());
			}
			i++;
		}
		this._total_prizes.set(ZERO);
	}

	@Payable
	public void fallback() {
		Context.println("caller "+ Context.getCaller());
		if ( this._dividends_score.get()!= null && Context.getCaller().equals(this._dividends_score.get())){
			this._total_prizes.set(Context.getValue());
			this._distribute_prizes();
		}else {
			Context.revert("Funds can only be accepted from the dividends distribution contract");
		}
	}

	public List<String> splitString(String toBeSplitted){
		List<String> splitList = new ArrayList<>();
		String splitString = "";

		for (int i = 0 ; i < toBeSplitted.length(); i++){
			char charAtIndex = toBeSplitted.charAt(i);
			if (charAtIndex != ',' && charAtIndex != ' ' && charAtIndex != ']' && charAtIndex != '['){
				splitString += String.valueOf(charAtIndex);
			}
			else if (charAtIndex == ','){
				splitList.add(splitString);
				splitString = "";
			}
		}
		splitList.add(splitString);
		return splitList;
	}

	public Map<String, Object> makeMap(String stringToBeMapped){
		String splitString = "";
		String address = "";
		HashMap<String, Object> mapToReturn =  new HashMap<>();
		for (int i = 0; i < stringToBeMapped.length(); i++){
			char charAtIndex = stringToBeMapped.charAt(i);
			if (charAtIndex == ':'){
				address = splitString;
				splitString = "";
			}
			else if (charAtIndex != '{' && charAtIndex != '"' && charAtIndex != '}'){
				splitString += String.valueOf(charAtIndex);
			}
		}

		mapToReturn.put("address", address);
		mapToReturn.put("value", new BigInteger(splitString));
		Context.println("returned from make map: " + address + " " + splitString);
		return mapToReturn;
	}

	void mergeSort(Map<String, Object> array[], int left, int right) {
		if (left < right) {

			int mid = (left + right) / 2;

			mergeSort(array, left, mid);
			mergeSort(array, mid + 1, right);

			merge(array, left, mid, right);
		}
	}

	void merge(Map<String, Object> array[], int p, int q, int r) {

		int n1 = q - p + 1;
		int n2 = r - q;

		Map<String, Object> L[] = new Map[n1];
		Map<String, Object> M[] = new Map[n2];

		for (int i = 0; i < n1; i++)
			L[i] = array[p + i];
		for (int j = 0; j < n2; j++)
			M[j] = array[q + 1 + j];

		int i, j, k;
		i = 0;
		j = 0;
		k = p;

		while (i < n1 && j < n2) {
			if (((BigInteger)L[i].get("value")).compareTo((BigInteger) M[j].get("value")) >= 0) {
				array[k] = L[i];
				i++;
			} else {
				array[k] = M[j];
				j++;
			}
			k++;
		}

		while (i < n1) {
			array[k] = L[i];
			i++;
			k++;
		}

		while (j < n2) {
			array[k] = M[j];
			j++;
			k++;
		}
	}
}

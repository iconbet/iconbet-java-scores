package com.iconbet.score.reward;

import com.eclipsesource.json.JsonArray;
import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import com.sun.management.DiagnosticCommandMBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.stubbing.Answer;
import score.Address;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;


import static org.mockito.Mockito.*;

import score.Context;
import scorex.util.ArrayList;
import scorex.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class RewardDistributionTest extends TestBase{

	private static final ServiceManager sm = getServiceManager();
	private static final Account owner = sm.createAccount();
	private static final BigInteger totalSupply = BigInteger.valueOf(50000000000L);

	private static final String symbol = "TAP";

	private static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
	private static final Address dividendScore = Address.fromString("cx0000000000000000000000000000000000000001");
	private static final Address gameScore = Address.fromString("cx0000000000000000000000000000000000000002");
	private static final Address tapToken = Address.fromString("cx0000000000000000000000000000000000000003");
	private static final Address rewards = Address.fromString("cx0000000000000000000000000000000000000004");
	private static final Address ibpnpScore = Address.fromString("cx0000000000000000000000000000000000000005");
	private static final Address promo = Address.fromString("cx0000000000000000000000000000000000000006");

	private static final Address dice = Address.fromString("cx0000000000000000000000000000000000000007");
	private static final Address roulette = Address.fromString("cx0000000000000000000000000000000000000008");
	private static final Address blackjack = Address.fromString("cx0000000000000000000000000000000000000009");
	public static final String TAG = "REWARDS";

	private static final Account testingAccount = sm.createAccount();
	private static final Account testingAccount1 = sm.createAccount();
	private static final Account testingAccount2 = sm.createAccount();

	private static final Account tapStakeUser1 = sm.createAccount();
	private static final Account tapStakeUser2 = sm.createAccount();
	private static final Account tapStakeUser3 = sm.createAccount();
	private static final Account tapStakeUser4 = sm.createAccount();
	private static final Account revshareWallet = sm.createAccount();
	private static final Account notRevshareWallet = sm.createAccount();



	public static final BigInteger decimal = new BigInteger("1000000000000000000");
	private static Score rewardDistribution;
	private final SecureRandom secureRandom = new SecureRandom();
	private static MockedStatic<Context> contextMock;

	RewardDistribution scoreSpy;

	@BeforeAll
	public static void init() {
		owner.addBalance(symbol, totalSupply);
		contextMock = Mockito.mockStatic(Context.class, Mockito.CALLS_REAL_METHODS);
	}

	@BeforeEach
	public void setup() throws Exception {
		rewardDistribution = sm.deploy(owner, RewardDistribution.class, false);
		RewardDistribution instance = (RewardDistribution) rewardDistribution.getInstance();
		scoreSpy = spy(instance);
		rewardDistribution.setInstance(scoreSpy);
		long currentTime = System.currentTimeMillis() / 1000L;
		sm.getBlock().increase(currentTime / 2);
	}

	@Test
	void setScores(){
		contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());

		rewardDistribution.invoke(owner, "set_token_score", tapToken);
		rewardDistribution.invoke(owner, "set_dividends_score", dividendScore);
		rewardDistribution.invoke(owner, "set_game_score", gameScore);

		assertEquals(tapToken, rewardDistribution.call("get_token_score"));
		assertEquals(dividendScore, rewardDistribution.call("get_dividends_score"));
		assertEquals(gameScore, rewardDistribution.call("get_game_score"));
	}

	@Test
	void setScoresMethod(){
		contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());

		rewardDistribution.invoke(owner, "set_token_score", tapToken);
		rewardDistribution.invoke(owner, "set_dividends_score", dividendScore);
		rewardDistribution.invoke(owner, "set_game_score", gameScore);
	}

	@Test
	void setScoresNotOwner(){
		contextMock.when(() -> Context.getCaller()).thenReturn(testingAccount.getAddress());

		Executable setScoresNotOwner = () -> rewardDistribution.invoke(testingAccount, "set_token_score", tapToken);
		expectErrorMessage(setScoresNotOwner, "Reverted(0): " + TAG + ": Only owner can call this score");

		setScoresNotOwner = () -> rewardDistribution.invoke(testingAccount, "set_dividends_score", dividendScore);
		expectErrorMessage(setScoresNotOwner, "Reverted(0): " + TAG + ": Only owner can call this score");

		setScoresNotOwner = () -> rewardDistribution.invoke(testingAccount, "set_game_score", gameScore);
		expectErrorMessage(setScoresNotOwner, "Reverted(0): " + TAG + ": Only owner can call this score");
	}

	@Test
	void setScoresNotContractAddresses(){
		contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());

		Executable setScoresNotOwner = () -> rewardDistribution.invoke(owner, "set_token_score", testingAccount.getAddress());
		expectErrorMessage(setScoresNotOwner, "Reverted(0): " + TAG + ": The address is not a contract address");

		setScoresNotOwner = () -> rewardDistribution.invoke(owner, "set_dividends_score", testingAccount.getAddress());
		expectErrorMessage(setScoresNotOwner, "Reverted(0): " + TAG + ": The address is not a contract address");

		setScoresNotOwner = () -> rewardDistribution.invoke(owner, "set_game_score", testingAccount.getAddress());
		expectErrorMessage(setScoresNotOwner, "Reverted(0): " + TAG + ": The address is not a contract address");
	}

	@Test
	void accumulateWagers(){
		setScoresMethod();
		contextMock.when(() -> Context.getCaller()).thenReturn(gameScore);
		doReturn(BigInteger.valueOf(264000000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), any());
		doReturn(BigInteger.valueOf(10)).when(scoreSpy).callScore(eq(BigInteger.class), eq(gameScore), eq("get_batch_size"), any());
		doReturn(Boolean.FALSE).when(scoreSpy).callScore(eq(Boolean.class), eq(dividendScore), eq("distribute"));
		/*
		Accumulating wagers of different users
		 */
		rewardDistribution.invoke(owner, "accumulate_wagers", testingAccount.getAddress().toString(), BigInteger.valueOf(100).multiply(decimal), BigInteger.ONE);
		rewardDistribution.invoke(owner, "accumulate_wagers", testingAccount1.getAddress().toString(), BigInteger.valueOf(100).multiply(decimal), BigInteger.ONE);
		rewardDistribution.invoke(owner, "accumulate_wagers", testingAccount2.getAddress().toString(), BigInteger.valueOf(100).multiply(decimal), BigInteger.ONE);
		assertEquals(Boolean.FALSE, rewardDistribution.call("rewards_dist_complete"));
		assertEquals(BigInteger.valueOf(100).multiply(decimal), rewardDistribution.call("get_daily_wagers", testingAccount.getAddress().toString()));
		assertEquals(BigInteger.valueOf(100).multiply(decimal), rewardDistribution.call("get_daily_wagers", testingAccount1.getAddress().toString()));
		assertEquals(BigInteger.valueOf(100).multiply(decimal), rewardDistribution.call("get_daily_wagers", testingAccount2.getAddress().toString()));
		assertEquals(BigInteger.valueOf(300).multiply(decimal), rewardDistribution.call("get_todays_total_wagers"));
		String jsonString = (String) rewardDistribution.call("get_daily_wager_totals");
		JsonValue jsonValue = Json.parse(jsonString);
		System.out.println(jsonValue.asObject().get("today").asString().charAt(0));
		List<String> todayEntries = splitString(jsonValue.asObject().get("today").asString());
		JsonValue individualEntry0 = Json.parse(todayEntries.get(0));
		JsonValue individualEntry1 = Json.parse(todayEntries.get(1));
		JsonValue individualEntry2 = Json.parse(todayEntries.get(2));
		assertEquals(BigInteger.valueOf(100).multiply(decimal).toString(), individualEntry0.asObject().get(testingAccount.getAddress().toString()).asString());
		assertEquals(BigInteger.valueOf(100).multiply(decimal).toString(), individualEntry1.asObject().get(testingAccount1.getAddress().toString()).asString());
		assertEquals(BigInteger.valueOf(100).multiply(decimal).toString(), individualEntry2.asObject().get(testingAccount2.getAddress().toString()).asString());
	}

	@Test
	void accumulateWagersRemainingTokenIsZero(){
		setScoresMethod();
		contextMock.when(() -> Context.getCaller()).thenReturn(gameScore);
		doReturn(BigInteger.valueOf(0).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), any());
		doReturn(BigInteger.valueOf(10)).when(scoreSpy).callScore(eq(BigInteger.class), eq(gameScore), eq("get_batch_size"), any());
		doReturn(Boolean.FALSE).when(scoreSpy).callScore(eq(Boolean.class), eq(dividendScore), eq("distribute"));
		/*
		Accumulating wagers of different users
		 */
		rewardDistribution.invoke(owner, "accumulate_wagers", testingAccount.getAddress().toString(), BigInteger.valueOf(100).multiply(decimal), BigInteger.ONE);
		rewardDistribution.invoke(owner, "accumulate_wagers", testingAccount1.getAddress().toString(), BigInteger.valueOf(100).multiply(decimal), BigInteger.ONE);
		rewardDistribution.invoke(owner, "accumulate_wagers", testingAccount2.getAddress().toString(), BigInteger.valueOf(100).multiply(decimal), BigInteger.ONE);

		assertEquals(BigInteger.valueOf(100).multiply(decimal), rewardDistribution.call("get_daily_wagers", testingAccount.getAddress().toString()));
		assertEquals(BigInteger.valueOf(100).multiply(decimal), rewardDistribution.call("get_daily_wagers", testingAccount1.getAddress().toString()));
		assertEquals(BigInteger.valueOf(100).multiply(decimal), rewardDistribution.call("get_daily_wagers", testingAccount2.getAddress().toString()));
		String jsonString = (String) rewardDistribution.call("get_daily_wager_totals");
		JsonValue jsonValue = Json.parse(jsonString);
		System.out.println(jsonValue.asObject().get("today").asString().charAt(0));
		List<String> todayEntries = splitString(jsonValue.asObject().get("today").asString());
		JsonValue individualEntry0 = Json.parse(todayEntries.get(0));
		JsonValue individualEntry1 = Json.parse(todayEntries.get(1));
		JsonValue individualEntry2 = Json.parse(todayEntries.get(2));
		assertEquals(BigInteger.valueOf(100).multiply(decimal).toString(), individualEntry0.asObject().get(testingAccount.getAddress().toString()).asString());
		assertEquals(BigInteger.valueOf(100).multiply(decimal).toString(), individualEntry1.asObject().get(testingAccount1.getAddress().toString()).asString());
		assertEquals(BigInteger.valueOf(100).multiply(decimal).toString(), individualEntry2.asObject().get(testingAccount2.getAddress().toString()).asString());
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
		System.out.println(splitString);
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

	@Test
	void sortMap(){
		Map<String, Object>[] listOfMaps = new Map[17];
		String stringToBeMapped1 = "{\"hx0000000000000000000000000000000000000004\":\"100000000000000000000\"}";
		String stringToBeMapped2 = "{\"hx0000000000000000000000000000000000000005\":\"200000000000000000000\"}";
		String stringToBeMapped3 = "{\"hx0000000000000000000000000000000000000006\":\"300000000000000000000\"}";
		String stringToBeMapped4 = "{\"hx0000000000000000000000000000000000000007\":\"400000000000000000000\"}";
		String stringToBeMapped5 = "{\"hx0000000000000000000000000000000000000008\":\"500000000000000000000\"}";
		String stringToBeMapped6 = "{\"hx0000000000000000000000000000000000000009\":\"600000000000000000000\"}";
		String stringToBeMapped7 = "{\"hx0000000000000000000000000000000000000001\":\"700000000000000000000\"}";
		String stringToBeMapped8 = "{\"hx0000000000000000000000000000000000000002\":\"800000000000000000000\"}";
		String stringToBeMapped9 = "{\"hx0000000000000000000000000000000000000003\":\"900000000000000000000\"}";
		String stringToBeMapped10 = "{\"hx0000000000000000000000000000000000000015\":\"1300000000000000000000\"}";
		String stringToBeMapped11 = "{\"hx0000000000000000000000000000000000000025\":\"2300000000000000000000\"}";
		String stringToBeMapped12 = "{\"hx0000000000000000000000000000000000000035\":\"3300000000000000000000\"}";
		String stringToBeMapped13 = "{\"hx0000000000000000000000000000000000000045\":\"3400000000000000000000\"}";
		String stringToBeMapped14 = "{\"hx0000000000000000000000000000000000000055\":\"350000000000000000000\"}";
		String stringToBeMapped15 = "{\"hx0000000000000000000000000000000000000065\":\"360000000000000000000\"}";
		String stringToBeMapped16 = "{\"hx0000000000000000000000000000000000000075\":\"370000000000000000000\"}";
		String stringToBeMapped17 = "{\"hx0000000000000000000000000000000000000085\":\"380000000000000000000\"}";

		listOfMaps[0] = makeMap(stringToBeMapped1);
		listOfMaps[1] = makeMap(stringToBeMapped2);
		listOfMaps[2] = makeMap(stringToBeMapped17);
		listOfMaps[3] = makeMap(stringToBeMapped3);
		listOfMaps[4] = makeMap(stringToBeMapped4);
		listOfMaps[5] = makeMap(stringToBeMapped5);
		listOfMaps[6] = makeMap(stringToBeMapped6);
		listOfMaps[7] = makeMap(stringToBeMapped7);
		listOfMaps[8] = makeMap(stringToBeMapped8);
		listOfMaps[9] = makeMap(stringToBeMapped9);
		listOfMaps[10] = makeMap(stringToBeMapped10);
		listOfMaps[11] = makeMap(stringToBeMapped11);
		listOfMaps[12] = makeMap(stringToBeMapped12);
		listOfMaps[13] = makeMap(stringToBeMapped13);
		listOfMaps[14] = makeMap(stringToBeMapped14);
		listOfMaps[15] = makeMap(stringToBeMapped15);
		listOfMaps[16] = makeMap(stringToBeMapped16);


		System.out.println(Arrays.toString(listOfMaps));
		mergeSort(listOfMaps, 0, 16);
		System.out.println(Arrays.toString(listOfMaps));

		System.out.println(Arrays.toString(top(listOfMaps, 10, false)));
	}

	void mergeSort(Map<String, Object> array[], int left, int right) {
		if (left < right) {

			// m is the point where the array is divided into two sub arrays
			int mid = (left + right) / 2;

			// recursive call to each sub arrays
			mergeSort(array, left, mid);
			mergeSort(array, mid + 1, right);

			// Merge the sorted sub arrays
			merge(array, left, mid, right);
		}
	}

	void merge(Map<String, Object> array[], int p, int q, int r) {

		int n1 = q - p + 1;
		int n2 = r - q;

		Map<String, Object> L[] = new Map[n1];
		Map<String, Object> M[] = new Map[n2];

		// fill the left and right array
		for (int i = 0; i < n1; i++)
			L[i] = array[p + i];
		for (int j = 0; j < n2; j++)
			M[j] = array[q + 1 + j];

		// Maintain current index of sub-arrays and main array
		int i, j, k;
		i = 0;
		j = 0;
		k = p;

		// Until we reach either end of either L or M, pick larger among
		// elements L and M and place them in the correct position at A[p..r]
		// for sorting in descending
		// use if(L[i] >= <[j])
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

		// When we run out of elements in either L or M,
		// pick up the remaining elements and put in A[p..r]
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

	public static Map<String, Object>[] top(Map<String, Object>[] arr, int max, boolean startFromEnd) {

		if(arr == null) {
			return arr;
		}

		if (arr.length < max){
			max = arr.length;
		}

		Map<String, Object>[] dest = new Map[max];
		if(startFromEnd) {
			System.arraycopy(arr, arr.length-max, dest, 0, max);
		}else {
			System.arraycopy(arr, 0, dest, 0, max);
		}
		return dest;
	}

	@Test
	void accumulateWagersDistributionIsTrue(){
		setScoresMethod();
		rewardDistribution.invoke(owner, "toggleTapDistributionEnabled");
		contextMock.when(() -> Context.getCaller()).thenReturn(gameScore);
		doReturn(BigInteger.valueOf(264000000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), any());
		doReturn(BigInteger.valueOf(10)).when(scoreSpy).callScore(eq(BigInteger.class), eq(gameScore), eq("get_batch_size"), any());
		doReturn(Boolean.TRUE).when(scoreSpy).callScore(eq(Boolean.class), eq(dividendScore), eq("distribute"));
		doNothing().when(scoreSpy).callScore(eq(tapToken), eq("transfer"), any(), any());
		/*
		Accumulating wagers of different users
		 */
		rewardDistribution.invoke(owner, "accumulate_wagers", testingAccount.getAddress().toString(), BigInteger.valueOf(100).multiply(decimal), BigInteger.ONE);
		rewardDistribution.invoke(owner, "accumulate_wagers", testingAccount1.getAddress().toString(), BigInteger.valueOf(100).multiply(decimal), BigInteger.ONE);
		rewardDistribution.invoke(owner, "accumulate_wagers", testingAccount2.getAddress().toString(), BigInteger.valueOf(100).multiply(decimal), BigInteger.ONE);
		rewardDistribution.invoke(owner, "accumulate_wagers", testingAccount2.getAddress().toString(), BigInteger.valueOf(100).multiply(decimal), BigInteger.ZERO);

		assertEquals(Boolean.TRUE, rewardDistribution.call("rewards_dist_complete"));
	}

	@Test
	void accumulateWagersLinearMigrationStart(){
		setScoresMethod();
		rewardDistribution.invoke(owner, "toggleTapDistributionEnabled");
		rewardDistribution.invoke(owner, "setLinearityComplexityMigrationStart", Boolean.TRUE);
		contextMock.when(() -> Context.getCaller()).thenReturn(gameScore);
		doReturn(BigInteger.valueOf(250000000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), any());
		doReturn(BigInteger.valueOf(10)).when(scoreSpy).callScore(eq(BigInteger.class), eq(gameScore), eq("get_batch_size"), any());
		doReturn(Boolean.TRUE).when(scoreSpy).callScore(eq(Boolean.class), eq(dividendScore), eq("distribute"));
		doNothing().when(scoreSpy).callScore(eq(tapToken), eq("transfer"), any(), any());
		/*
		Accumulating wagers of different users
		 */
		rewardDistribution.invoke(owner, "accumulate_wagers", testingAccount.getAddress().toString(), BigInteger.valueOf(100).multiply(decimal), BigInteger.ONE);
		rewardDistribution.invoke(owner, "accumulate_wagers", testingAccount1.getAddress().toString(), BigInteger.valueOf(100).multiply(decimal), BigInteger.ONE);
		rewardDistribution.invoke(owner, "accumulate_wagers", testingAccount2.getAddress().toString(), BigInteger.valueOf(100).multiply(decimal), BigInteger.ONE);
		rewardDistribution.invoke(owner, "accumulate_wagers", testingAccount2.getAddress().toString(), BigInteger.valueOf(100).multiply(decimal), BigInteger.ZERO);

		assertEquals(Boolean.TRUE, rewardDistribution.call("rewards_dist_complete"));
		assertEquals(Boolean.TRUE, rewardDistribution.call("getLinearityComplexityMigrationComplete"));
	}

	public void expectErrorMessage(Executable contractCall, String errorMessage) {
		AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
		assertEquals(errorMessage, e.getMessage());
	}
	
}

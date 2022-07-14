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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.math.BigInteger;
import java.security.SecureRandom;
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

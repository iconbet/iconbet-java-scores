package com.iconbet.score.tap;

import static java.math.BigInteger.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;

import score.Address;
import static com.iconbet.score.tap.TapToken.StakedTAPTokenSnapshots;
import static com.iconbet.score.tap.TapToken.TotalStakedTAPTokenSnapshots;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;


import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import score.Context;

class TapTokenTest extends TestBase {

	private static final ServiceManager sm = getServiceManager();
	private static final Account owner = sm.createAccount();
	private static final BigInteger initialSupply = BigInteger.valueOf(625000000);
	private static final BigInteger decimals = BigInteger.valueOf(18);
	private static final BigInteger MULTIPLIER = new BigInteger("1000000000000000000");

	private static final BigInteger totalSupply = new BigInteger("625000000000000000000000000");

	private static final Account testingAccount = sm.createAccount();
	private static final Account testingAccount1 = sm.createAccount();
	private static final Account testingAccount2 = sm.createAccount();
	private static final Account testingAccount3 = sm.createAccount();
	private static final Address daoFund = Address.fromString("cx0000000000000000000000000000000000000001");
	private static final Address gameScore = Address.fromString("cx0000000000000000000000000000000000000002");
	private static final Address gameAuth = Address.fromString("cx0000000000000000000000000000000000000004");
	private static final Address dividends = Address.fromString("cx0000000000000000000000000000000000000005");
	private static final Address utap = Address.fromString("cx0000000000000000000000000000000000000006");
	private static final Address rewards = Address.fromString("cx0000000000000000000000000000000000000006");

	private static final Address dice = Address.fromString("cx0000000000000000000000000000000000000007");
	private static final Address roulette = Address.fromString("cx0000000000000000000000000000000000000008");
	private static final Address blackjack = Address.fromString("cx0000000000000000000000000000000000000009");
	private static final String symbol = "TAP";
	private static Score tapToken;
	private final SecureRandom secureRandom = new SecureRandom();
	TapToken scoreSpy;
	private static MockedStatic<Context> contextMock;

	@BeforeAll
	public static void init() {
		owner.addBalance(symbol, totalSupply);
		contextMock = Mockito.mockStatic(Context.class, Mockito.CALLS_REAL_METHODS);
	}

	@BeforeEach
	public void setup() throws Exception {
		tapToken = sm.deploy(owner, TapToken.class, initialSupply, decimals, false);
		TapToken instance = (TapToken) tapToken.getInstance();
		scoreSpy = spy(instance);
		tapToken.setInstance(scoreSpy);
		long currentTime = System.currentTimeMillis() / 1000L;
		sm.getBlock().increase(currentTime / 2);
	}

	@Test
	void name() {
		assertEquals(TapToken.TAG, tapToken.call("name"));
	}

	@Test
	void symbol() {
		assertEquals(symbol, tapToken.call("symbol"));
	}

	@Test
	void decimals() {
		assertEquals(decimals, tapToken.call("decimals"));
	}

	@Test
	void totalSupply() {
		assertEquals(totalSupply, tapToken.call("totalSupply"));
	}

	@Test
	void balanceOf() {
		assertEquals(owner.getBalance(symbol),
				tapToken.call("balanceOf", tapToken.getOwner().getAddress()));
	}

	@Test
	void setScores(){
		contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
		tapToken.invoke(owner, "set_dividends_score", dividends);
		tapToken.invoke(owner, "set_authorization_score", gameAuth);

		assertEquals(dividends, tapToken.call("get_dividends_score"));
		assertEquals(gameAuth, tapToken.call("get_authorization_score"));
	}

	private void setScoresMethod(){
		contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
		tapToken.invoke(owner, "set_dividends_score", dividends);
		tapToken.invoke(owner, "set_authorization_score", gameAuth);
	}


	@Test
	void togglePaused() {
		Boolean paused = (Boolean)tapToken.call("getPaused");
		contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
		tapToken.invoke(owner, "togglePaused");
		Boolean pausedAfter = (Boolean)tapToken.call("getPaused");
		assertEquals(pausedAfter, !paused);
	}


	@Test
	void testWhitelist() {
		setScoresMethod();
		contextMock.when(() -> Context.getCaller()).thenReturn(gameAuth);
		Account alice = sm.createAccount();
		tapToken.invoke(owner, "set_whitelist_address", alice.getAddress());

		@SuppressWarnings("unchecked")
		List<Address> addresses = (List<Address>)tapToken.call("get_whitelist_addresses");

		assertNotNull(addresses);
		assertEquals(1, addresses.size());
		assertEquals(alice.getAddress(), addresses.get(0));
	}

	@Test
	void testToggleStakingToEnable() {
		contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
		tapToken.invoke(owner, "toggle_staking_enabled");
		Boolean enabled = (Boolean)tapToken.call("staking_enabled");
		assertTrue(enabled);
	}

	@Test
	void testLockList() {
		setScoresMethod();
		contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
		Account alice = sm.createAccount();
		tapToken.invoke(owner, "toggle_staking_enabled");
		contextMock.when(() -> Context.getCaller()).thenReturn(gameAuth);
		tapToken.invoke(owner, "set_locklist_address", alice.getAddress());

		@SuppressWarnings("unchecked")
		List<Address> addresses = (List<Address>)tapToken.call("get_locklist_addresses");

		assertNotNull(addresses);
		assertEquals(1, addresses.size());
		assertEquals(alice.getAddress(), addresses.get(0));
	}
	
	@Test
	void testTransferNegative() {
		Account alice = sm.createAccount();
		Address a = alice.getAddress();

		Boolean paused = (Boolean)tapToken.call("getPaused");
		if(paused) {
			tapToken.invoke(owner, "togglePaused");
		}

		BigInteger value = BigInteger.valueOf(-1);
		byte[] data = "to alice".getBytes();
		AssertionError e = Assertions.assertThrows(AssertionError.class, () -> {
			tapToken.invoke(alice, "transfer", a, value, data);
		});

		assertEquals("Reverted(0): Transferring value cannot be less than zero", e.getMessage());
	}


	@Test
	void testStakedBalance() {
		Account alice = sm.createAccount();
		BigInteger balance = (BigInteger)tapToken.call("staked_balanceOf", alice.getAddress());

		assertNotNull(balance);
		assertEquals(BigInteger.ZERO, balance);
	}

	@Test
	void testUnStakedBalance() {
		Account alice = sm.createAccount();
		BigInteger balance = (BigInteger)tapToken.call("unstaked_balanceOf", alice.getAddress());

		assertNotNull(balance);
		assertEquals(BigInteger.ZERO, balance);
	}

	@Test
	void testTotalStakedBalance() {
		BigInteger balance = (BigInteger)tapToken.call("total_staked_balance");

		assertNotNull(balance);
		assertEquals(BigInteger.ZERO, balance);
	}

	@Test
	void testSwitchDivsToStakedTapEnabled() {
		Boolean active = (Boolean)tapToken.call("switch_divs_to_staked_tap_enabled");

		assertNotNull(active);
		assertFalse(active);
	}

	@Test
	void testDetailsBalance() {
		Account alice = sm.createAccount();
		@SuppressWarnings("unchecked")
		Map<String, BigInteger> details = (Map<String, BigInteger>)tapToken.call("details_balanceOf", alice.getAddress());

		assertNotNull(details);
		assertEquals(BigInteger.ZERO, details.get("Total balance"));
		assertEquals(BigInteger.ZERO, details.get("Available balance"));
		assertEquals(BigInteger.ZERO, details.get("Staked balance"));
		assertEquals(BigInteger.ZERO, details.get("Unstaking balance"));
		assertEquals(BigInteger.ZERO, details.get("Unstaking time (in microseconds)"));
	}

	@Test
	void loadTAPStakeSnapshot(){
		StakedTAPTokenSnapshots[] stakedTAPTokenSnapshotsArray = new StakedTAPTokenSnapshots[3];
		StakedTAPTokenSnapshots stakedTAPTokenSnapshots1 = new StakedTAPTokenSnapshots();
		stakedTAPTokenSnapshots1.address = testingAccount.getAddress();
		stakedTAPTokenSnapshots1.amount = BigInteger.valueOf(100).multiply(MULTIPLIER);
		stakedTAPTokenSnapshots1.day = BigInteger.ZERO;

		StakedTAPTokenSnapshots stakedTAPTokenSnapshots2 = new StakedTAPTokenSnapshots();
		stakedTAPTokenSnapshots2.address = testingAccount1.getAddress();
		stakedTAPTokenSnapshots2.amount = BigInteger.valueOf(100).multiply(MULTIPLIER);
		stakedTAPTokenSnapshots2.day = BigInteger.ZERO;

		StakedTAPTokenSnapshots stakedTAPTokenSnapshots3 = new StakedTAPTokenSnapshots();
		stakedTAPTokenSnapshots3.address = testingAccount2.getAddress();
		stakedTAPTokenSnapshots3.amount = BigInteger.valueOf(100).multiply(MULTIPLIER);
		stakedTAPTokenSnapshots3.day = BigInteger.ZERO;

		stakedTAPTokenSnapshotsArray[0] = stakedTAPTokenSnapshots1;
		stakedTAPTokenSnapshotsArray[1] = stakedTAPTokenSnapshots2;
		stakedTAPTokenSnapshotsArray[2] = stakedTAPTokenSnapshots3;
		contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
		tapToken.invoke(owner, "toggleEnableSnapshot");
		doReturn(BigInteger.ONE).when(scoreSpy).getDay();
		tapToken.invoke(owner, "loadTAPStakeSnapshot", (Object) stakedTAPTokenSnapshotsArray);

		assertEquals(BigInteger.valueOf(100).multiply(MULTIPLIER), tapToken.call("stakedBalanceOfAt", testingAccount.getAddress(), BigInteger.ZERO));
		assertEquals(BigInteger.valueOf(100).multiply(MULTIPLIER), tapToken.call("stakedBalanceOfAt", testingAccount1.getAddress(), BigInteger.ZERO));
		assertEquals(BigInteger.valueOf(100).multiply(MULTIPLIER), tapToken.call("stakedBalanceOfAt", testingAccount2.getAddress(), BigInteger.ZERO));

		System.out.println(tapToken.call("totalStakedBalanceOFAt", BigInteger.ZERO));
	}

	@Test
	void loadTotalStakeSnapshot(){
		TotalStakedTAPTokenSnapshots[] totalStakedTAPTokenSnapshots = new TotalStakedTAPTokenSnapshots[1];
		TotalStakedTAPTokenSnapshots totalStakedTAPTokenSnapshots1 = new TotalStakedTAPTokenSnapshots();
		totalStakedTAPTokenSnapshots1.amount = BigInteger.valueOf(100).multiply(MULTIPLIER);
		totalStakedTAPTokenSnapshots1.day = BigInteger.ZERO;

		totalStakedTAPTokenSnapshots[0] = totalStakedTAPTokenSnapshots1;
		contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
		tapToken.invoke(owner, "toggleEnableSnapshot");
		doReturn(BigInteger.ONE).when(scoreSpy).getDay();
		tapToken.invoke(owner, "loadTotalStakeSnapshot", (Object) totalStakedTAPTokenSnapshots);

		assertEquals(BigInteger.valueOf(100).multiply(MULTIPLIER), tapToken.call("totalStakedBalanceOFAt", BigInteger.ZERO));
	}

	@Test
	void stake(){
		contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
		tapToken.invoke(owner, "toggle_staking_enabled");
		tapToken.invoke(owner, "toggleEnableSnapshot");
		System.out.println(tapToken.call("balanceOf", owner.getAddress()));
		tapToken.invoke(owner, "stake", BigInteger.valueOf(100000).multiply(MULTIPLIER));
		assertEquals(BigInteger.valueOf(100000).multiply(MULTIPLIER), tapToken.call("staked_balanceOf", owner.getAddress()));
		assertEquals(BigInteger.valueOf(100000).multiply(MULTIPLIER), tapToken.call("total_staked_balance"));
		assertEquals(BigInteger.valueOf(100000).multiply(MULTIPLIER), tapToken.call("stakedBalanceOfAt", owner.getAddress(), BigInteger.ZERO));
		assertEquals(BigInteger.valueOf(100000).multiply(MULTIPLIER), tapToken.call("totalStakedBalanceOFAt", BigInteger.ZERO));
	}

	@Test
	void getStakeUpdates(){
		setScoresMethod();
		tapToken.invoke(owner, "toggle_staking_enabled");
		tapToken.invoke(owner, "toggleEnableSnapshot");
		tapToken.invoke(owner, "toggle_switch_divs_to_staked_tap_enabled");

		contextMock.when(() -> Context.getCaller()).thenReturn(gameAuth);
		tapToken.invoke(owner, "set_max_loop", 10);

		contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
		System.out.println(tapToken.call("balanceOf", owner.getAddress()));
		tapToken.invoke(owner, "stake", BigInteger.valueOf(100000).multiply(MULTIPLIER));
		contextMock.when(() -> Context.getCaller()).thenReturn(dividends);

		tapToken.invoke(owner, "get_stake_updates");
	}
}

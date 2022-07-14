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

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import score.Context;

class TapTokenTest extends TestBase {

	private static final ServiceManager sm = getServiceManager();
	private static final Account owner = sm.createAccount();
	private static final BigInteger initialSupply = BigInteger.valueOf(5);
	private static final BigInteger decimals = BigInteger.valueOf(10);
	private static final BigInteger MULTIPLIER = new BigInteger("1000000000000000000");

	private static final BigInteger totalSupply = BigInteger.valueOf(50000000000L);

	private static final Account testingAccount = sm.createAccount();
	private static final Account testingAccount1 = sm.createAccount();
	private static final Account testingAccount2 = sm.createAccount();
	private static final Account testingAccount3 = sm.createAccount();
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
		assertEquals(decimals.intValue(), tapToken.call("decimals"));
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
	void testTransfer() {

		Account alice = sm.createAccount();
		BigInteger value = TEN.pow(decimals.intValue());

		Boolean paused = (Boolean)tapToken.call("getPaused");
		if(paused) {
			tapToken.invoke(owner, "togglePaused");
		}

		tapToken.invoke(owner, "transfer", alice.getAddress(), value, "to alice".getBytes());
		owner.subtractBalance(symbol, value);

		assertEquals(owner.getBalance(symbol),
				tapToken.call("balanceOf", tapToken.getOwner().getAddress()));
		assertEquals(value,
				tapToken.call("balanceOf", alice.getAddress()));

		// transfer self
		tapToken.invoke(alice, "transfer", alice.getAddress(), value, "self transfer".getBytes());
		assertEquals(value, tapToken.call("balanceOf", alice.getAddress()));
	}

	@Test
	void togglePaused() {
		Boolean paused = (Boolean)tapToken.call("getPaused");
		tapToken.invoke(owner, "togglePaused");
		Boolean pausedAfter = (Boolean)tapToken.call("getPaused");
		assertEquals(pausedAfter, !paused);
	}

	@Test
	void transferPaused() {
		Account alice = sm.createAccount();
		BigInteger value = TEN.pow(decimals.intValue());

		Boolean paused = (Boolean)tapToken.call("getPaused");
		System.out.println(paused);
		if(!paused) {
			tapToken.invoke(owner, "togglePaused");
		}
		paused = (Boolean)tapToken.call("getPaused");
		System.out.println(paused);

		Address a = alice.getAddress();
		byte[] data = "to alice".getBytes();
		tapToken.invoke(owner, "set_whitelist_address", owner.getAddress());

		AssertionError e = Assertions.assertThrows(AssertionError.class, () -> {
			tapToken.invoke(owner, "transfer", a, value, data);
		});

		assertEquals("Reverted(0): TAP token transfers are paused", e.getMessage());

	}

	@Test
	void testWhitelist() {
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

		tapToken.invoke(owner, "toggle_staking_enabled");
		Boolean enabled = (Boolean)tapToken.call("staking_enabled");
		assertTrue(enabled);
	}

	@Test
	void testLockList() {
		Account alice = sm.createAccount();
		tapToken.invoke(owner, "toggle_staking_enabled");
		tapToken.invoke(owner, "set_locklist_address", alice.getAddress());

		@SuppressWarnings("unchecked")
		List<Address> addresses = (List<Address>)tapToken.call("get_locklist_addresses");

		assertNotNull(addresses);
		assertEquals(1, addresses.size());
		assertEquals(alice.getAddress(), addresses.get(0));
	}

	@Test
	void testTransferFailByLockList() {
		Account alice = sm.createAccount();
		Address a = alice.getAddress();
		Boolean paused = (Boolean)tapToken.call("getPaused");
		if(paused) {
			tapToken.invoke(owner, "togglePaused");
		}

		tapToken.invoke(owner, "toggle_staking_enabled");
		tapToken.invoke(owner, "set_locklist_address", a);

		BigInteger value = TEN.pow(decimals.intValue());
		byte[] data = "to alice".getBytes();
		AssertionError e = Assertions.assertThrows(AssertionError.class, () -> {
			tapToken.invoke(alice, "transfer", a, value, data);
		});

		assertEquals("Reverted(0): Transfer of TAP has been locked for this address.", e.getMessage());
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
	void testTransferOutOfBalance() {
		Account alice = sm.createAccount();
		Address a = alice.getAddress();

		Boolean paused = (Boolean)tapToken.call("getPaused");
		if(paused) {
			tapToken.invoke(owner, "togglePaused");
		}

		BigInteger value = TEN.pow(decimals.intValue());
		byte[] data = "to alice".getBytes();
		AssertionError e = Assertions.assertThrows(AssertionError.class, () -> {
			tapToken.invoke(alice, "transfer", a, value, data);
		});

		assertEquals("Reverted(0): Out of balance", e.getMessage());
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

		tapToken.invoke(owner, "toggleEnableSnapshot");
		doReturn(BigInteger.ONE).when(scoreSpy).getDay();
		tapToken.invoke(owner, "loadTotalStakeSnapshot", (Object) totalStakedTAPTokenSnapshots);

		assertEquals(BigInteger.valueOf(100).multiply(MULTIPLIER), tapToken.call("totalStakedBalanceOFAt", BigInteger.ZERO));
	}
}

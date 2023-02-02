package com.iconbet.score.tap;

import static java.math.BigInteger.TEN;
import static java.math.BigInteger.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.*;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;

import org.junit.jupiter.api.function.Executable;
import score.Address;

import static com.iconbet.score.tap.TapToken.StakedTAPTokenSnapshots;
import static com.iconbet.score.tap.TapToken.TotalStakedTAPTokenSnapshots;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


import org.mockito.MockedStatic;
import org.mockito.Mockito;
import score.ArrayDB;
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
    private static final Account testingAccount4 = sm.createAccount();
    private static final Account testingAccount5 = sm.createAccount();
    private static final Account testingAccount6 = sm.createAccount();
    private static final Account testingAccount7 = sm.createAccount();
    private static final Account testingAccount8 = sm.createAccount();
    private static final Account testingAccount9 = sm.createAccount();
    private static final Account testingAccount10 = sm.createAccount();

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
        tapToken = sm.deploy(owner, TapToken.class, initialSupply, decimals);
        TapToken instance = (TapToken) tapToken.getInstance();
        scoreSpy = spy(instance);
        tapToken.setInstance(scoreSpy);
        long currentTime = System.currentTimeMillis() / 1000L;
        sm.getBlock().increase(currentTime / 2);
        contextMock.reset();
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
    void setScores() {
        contextMock.when(ownerCall()).thenReturn(owner.getAddress());
        tapToken.invoke(owner, "set_dividends_score", dividends);
        tapToken.invoke(owner, "set_authorization_score", gameAuth);

        assertEquals(dividends, tapToken.call("get_dividends_score"));
        assertEquals(gameAuth, tapToken.call("get_authorization_score"));
    }

    private void setScoresMethod() {
        contextMock.when(ownerCall()).thenReturn(owner.getAddress());
        tapToken.invoke(owner, "set_dividends_score", dividends);
        tapToken.invoke(owner, "set_authorization_score", gameAuth);
    }


    @Test
    void togglePaused() {
        Boolean paused = (Boolean) tapToken.call("getPaused");
        contextMock.when(ownerCall()).thenReturn(owner.getAddress());
        tapToken.invoke(owner, "togglePaused");
        Boolean pausedAfter = (Boolean) tapToken.call("getPaused");
        assertEquals(pausedAfter, !paused);
    }


    @Test
    void testWhitelist() {
        setScoresMethod();
        contextMock.when(ownerCall()).thenReturn(gameAuth);
        Account alice = sm.createAccount();
        tapToken.invoke(owner, "set_whitelist_address", alice.getAddress());

        @SuppressWarnings("unchecked")
        List<Address> addresses = (List<Address>) tapToken.call("get_whitelist_addresses");

        assertNotNull(addresses);
        assertEquals(1, addresses.size());
        assertEquals(alice.getAddress(), addresses.get(0));
    }

    @Test
    void testToggleStakingToEnable() {
        contextMock.when(ownerCall()).thenReturn(owner.getAddress());
        tapToken.invoke(owner, "toggle_staking_enabled");
        Boolean enabled = (Boolean) tapToken.call("staking_enabled");
        assertTrue(enabled);
    }

    @Test
    void testLockList() {
        setScoresMethod();
        contextMock.when(ownerCall()).thenReturn(owner.getAddress());
        Account alice = sm.createAccount();
        tapToken.invoke(owner, "toggle_staking_enabled");
        contextMock.when(ownerCall()).thenReturn(gameAuth);
        tapToken.invoke(owner, "set_locklist_address", alice.getAddress());

        @SuppressWarnings("unchecked")
        List<Address> addresses = (List<Address>) tapToken.call("get_locklist_addresses");

        assertNotNull(addresses);
        assertEquals(1, addresses.size());
        assertEquals(alice.getAddress(), addresses.get(0));
    }

    @Test
    void testTransferNegative() {
        Account alice = sm.createAccount();
        Address a = alice.getAddress();

        Boolean paused = (Boolean) tapToken.call("getPaused");
        if (paused) {
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
        BigInteger balance = (BigInteger) tapToken.call("staked_balanceOf", alice.getAddress());

        assertNotNull(balance);
        assertEquals(BigInteger.ZERO, balance);
    }

    @Test
    void testUnStakedBalance() {
        Account alice = sm.createAccount();
        BigInteger balance = (BigInteger) tapToken.call("unstaked_balanceOf", alice.getAddress());

        assertNotNull(balance);
        assertEquals(BigInteger.ZERO, balance);
    }

    @Test
    void testTotalStakedBalance() {
        BigInteger balance = (BigInteger) tapToken.call("total_staked_balance");

        assertNotNull(balance);
        assertEquals(BigInteger.ZERO, balance);
    }

    @Test
    void testSwitchDivsToStakedTapEnabled() {
        Boolean active = (Boolean) tapToken.call("switch_divs_to_staked_tap_enabled");

        assertNotNull(active);
        assertFalse(active);
    }

    @Test
    void testDetailsBalance() {
        Account alice = sm.createAccount();
        @SuppressWarnings("unchecked")
        Map<String, BigInteger> details = (Map<String, BigInteger>) tapToken.call("details_balanceOf", alice.getAddress());

        assertNotNull(details);
        assertEquals(BigInteger.ZERO, details.get("Total balance"));
        assertEquals(BigInteger.ZERO, details.get("Available balance"));
        assertEquals(BigInteger.ZERO, details.get("Staked balance"));
        assertEquals(BigInteger.ZERO, details.get("Unstaking balance"));
        assertEquals(BigInteger.ZERO, details.get("Unstaking time (in microseconds)"));
    }

    @Test
    void loadTAPStakeSnapshot() {
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
        contextMock.when(ownerCall()).thenReturn(owner.getAddress());
        tapToken.invoke(owner, "toggleEnableSnapshot");
        doReturn(BigInteger.ONE).when(scoreSpy).getDay();
        tapToken.invoke(owner, "loadTAPStakeSnapshot", (Object) stakedTAPTokenSnapshotsArray);

        assertEquals(BigInteger.valueOf(100).multiply(MULTIPLIER), tapToken.call("stakedBalanceOfAt", testingAccount.getAddress(), BigInteger.ZERO));
        assertEquals(BigInteger.valueOf(100).multiply(MULTIPLIER), tapToken.call("stakedBalanceOfAt", testingAccount1.getAddress(), BigInteger.ZERO));
        assertEquals(BigInteger.valueOf(100).multiply(MULTIPLIER), tapToken.call("stakedBalanceOfAt", testingAccount2.getAddress(), BigInteger.ZERO));

        System.out.println(tapToken.call("totalStakedBalanceOFAt", BigInteger.ZERO));
    }

    @Test
    void loadTotalStakeSnapshot() {
        TotalStakedTAPTokenSnapshots[] totalStakedTAPTokenSnapshots = new TotalStakedTAPTokenSnapshots[1];
        TotalStakedTAPTokenSnapshots totalStakedTAPTokenSnapshots1 = new TotalStakedTAPTokenSnapshots();
        totalStakedTAPTokenSnapshots1.amount = BigInteger.valueOf(100).multiply(MULTIPLIER);
        totalStakedTAPTokenSnapshots1.day = BigInteger.ZERO;

        totalStakedTAPTokenSnapshots[0] = totalStakedTAPTokenSnapshots1;
        contextMock.when(ownerCall()).thenReturn(owner.getAddress());
        tapToken.invoke(owner, "toggleEnableSnapshot");
        doReturn(BigInteger.ONE).when(scoreSpy).getDay();
        tapToken.invoke(owner, "loadTotalStakeSnapshot", (Object) totalStakedTAPTokenSnapshots);

        assertEquals(BigInteger.valueOf(100).multiply(MULTIPLIER), tapToken.call("totalStakedBalanceOFAt", BigInteger.ZERO));
    }

    @Test
    void stake() {
        setScores();
        contextMock.when(ownerCall()).thenReturn(gameAuth);
        tapToken.invoke(owner, "set_unstaking_period", BigInteger.TEN);

        contextMock.when(ownerCall()).thenReturn(owner.getAddress());
        tapToken.invoke(owner, "toggle_staking_enabled");
        tapToken.invoke(owner, "toggleEnableSnapshot");
        System.out.println(tapToken.call("balanceOf", owner.getAddress()));

        assertEquals(tapToken.call("balanceOf", owner.getAddress()), tapToken.call("available_balance_of", owner.getAddress()));
        tapToken.invoke(owner, "stake", BigInteger.valueOf(100000).multiply(MULTIPLIER));
        assertEquals(BigInteger.valueOf(100000).multiply(MULTIPLIER), tapToken.call("staked_balanceOf", owner.getAddress()));
        assertEquals(BigInteger.valueOf(100000).multiply(MULTIPLIER), tapToken.call("total_staked_balance"));
        assertEquals(BigInteger.valueOf(100000).multiply(MULTIPLIER), tapToken.call("stakedBalanceOfAt", owner.getAddress(), BigInteger.ZERO));
        assertEquals(BigInteger.valueOf(100000).multiply(MULTIPLIER), tapToken.call("totalStakedBalanceOFAt", BigInteger.ZERO));

        System.out.println(tapToken.call("details_balanceOf", owner.getAddress()));

        assertEquals(((BigInteger) tapToken.call("balanceOf", owner.getAddress())).
                        subtract(BigInteger.valueOf(100000).multiply(MULTIPLIER)),
                tapToken.call("available_balance_of", owner.getAddress()));

        @SuppressWarnings("unchecked")
        Map<String, BigInteger> detailsBalanceOf = (Map<String, BigInteger>) tapToken.call("details_balanceOf", owner.getAddress());

        assertEquals(new BigInteger("624900000000000000000000000"), detailsBalanceOf.get("Available balance"));
        assertEquals(BigInteger.valueOf(100000).multiply(MULTIPLIER), detailsBalanceOf.get("Staked balance"));
        assertEquals(new BigInteger("625000000000000000000000000"), detailsBalanceOf.get("Total balance"));
        assertEquals(new BigInteger("0"), detailsBalanceOf.get("Unstaking balance"));

        tapToken.invoke(owner, "stake", BigInteger.valueOf(90000).multiply(MULTIPLIER));
        assertEquals(BigInteger.valueOf(90000).multiply(MULTIPLIER), tapToken.call("stakedBalanceOfAt", owner.getAddress(), BigInteger.ZERO));
        assertEquals(BigInteger.valueOf(90000).multiply(MULTIPLIER), tapToken.call("totalStakedBalanceOFAt", BigInteger.ZERO));


        System.out.println(tapToken.call("details_balanceOf", owner.getAddress()));

        //noinspection unchecked
        detailsBalanceOf = (Map<String, BigInteger>) tapToken.call("details_balanceOf", owner.getAddress());

        assertEquals(new BigInteger("624900000000000000000000000"), detailsBalanceOf.get("Available balance"));
        assertEquals(BigInteger.valueOf(90000).multiply(MULTIPLIER), detailsBalanceOf.get("Staked balance"));
        assertEquals(new BigInteger("625000000000000000000000000"), detailsBalanceOf.get("Total balance"));
        assertEquals(BigInteger.valueOf(10000).multiply(MULTIPLIER), detailsBalanceOf.get("Unstaking balance"));


        long currentTime = System.currentTimeMillis() / 1000L;
        sm.getBlock().increase(11 * currentTime / 2);

        assertEquals(BigInteger.valueOf(90000).multiply(MULTIPLIER), tapToken.call("stakedBalanceOfAt", owner.getAddress(), TEN));
        assertEquals(BigInteger.valueOf(90000).multiply(MULTIPLIER), tapToken.call("totalStakedBalanceOFAt", TEN));

        System.out.println(tapToken.call("details_balanceOf", owner.getAddress()));

        assertEquals(((BigInteger) tapToken.call("balanceOf", owner.getAddress())).
                        subtract(BigInteger.valueOf(90000).multiply(MULTIPLIER)),
                tapToken.call("available_balance_of", owner.getAddress()));
        //noinspection unchecked
        detailsBalanceOf = (Map<String, BigInteger>) tapToken.call("details_balanceOf", owner.getAddress());

        assertEquals(new BigInteger("624910000000000000000000000"), detailsBalanceOf.get("Available balance"));
        assertEquals(BigInteger.valueOf(90000).multiply(MULTIPLIER), detailsBalanceOf.get("Staked balance"));
        assertEquals(new BigInteger("625000000000000000000000000"), detailsBalanceOf.get("Total balance"));
        assertEquals(BigInteger.valueOf(0), detailsBalanceOf.get("Unstaking balance"));
    }

    @Test
    void stakeNotInTheSameDay() {
        setScores();
        contextMock.when(ownerCall()).thenReturn(gameAuth);
        tapToken.invoke(owner, "set_unstaking_period", BigInteger.TEN);

        contextMock.when(ownerCall()).thenReturn(owner.getAddress());
        tapToken.invoke(owner, "toggle_staking_enabled");
        tapToken.invoke(owner, "toggleEnableSnapshot");
        System.out.println(tapToken.call("balanceOf", owner.getAddress()));

        assertEquals(tapToken.call("balanceOf", owner.getAddress()), tapToken.call("available_balance_of", owner.getAddress()));
        tapToken.invoke(owner, "stake", BigInteger.valueOf(100000).multiply(MULTIPLIER));

        long currentTime = System.currentTimeMillis() / 1000L;
        sm.getBlock().increase(11 * currentTime / 2);

        tapToken.invoke(owner, "stake", BigInteger.valueOf(90000).multiply(MULTIPLIER));

        System.out.println(tapToken.call("details_balanceOf", owner.getAddress()));

        //noinspection unchecked
        Map<String, BigInteger> detailsBalanceOf = (Map<String, BigInteger>) tapToken.call("details_balanceOf", owner.getAddress());

        assertEquals(BigInteger.valueOf(100000).multiply(MULTIPLIER), tapToken.call("stakedBalanceOfAt", owner.getAddress(), BigInteger.valueOf(1000)));
        assertEquals(BigInteger.valueOf(100000).multiply(MULTIPLIER), tapToken.call("totalStakedBalanceOFAt", BigInteger.valueOf(1000)));
    }

    @Test
    void getStakeUpdates() {
        setScoresMethod();
        tapToken.invoke(owner, "toggle_staking_enabled");
        tapToken.invoke(owner, "toggleEnableSnapshot");
        tapToken.invoke(owner, "toggle_switch_divs_to_staked_tap_enabled");

        contextMock.when(ownerCall()).thenReturn(gameAuth);
        tapToken.invoke(owner, "set_max_loop", 10);

        contextMock.when(ownerCall()).thenReturn(owner.getAddress());
        assertEquals(new BigInteger("625000000000000000000000000"), tapToken.call("balanceOf", owner.getAddress()));
        tapToken.invoke(owner, "stake", BigInteger.valueOf(100000).multiply(MULTIPLIER));
        contextMock.when(ownerCall()).thenReturn(dividends);

        tapToken.invoke(owner, "get_stake_updates");
        tapToken.invoke(owner, "get_stake_updates");
    }

    @Test
    void clearYesterdaysStakeChanges() {
        setScoresMethod();
        tapToken.invoke(owner, "toggle_staking_enabled");
        tapToken.invoke(owner, "toggleEnableSnapshot");
        tapToken.invoke(owner, "toggle_switch_divs_to_staked_tap_enabled");

        contextMock.when(ownerCall()).thenReturn(gameAuth);
        tapToken.invoke(owner, "set_max_loop", 10);

        contextMock.when(ownerCall()).thenReturn(owner.getAddress());
        System.out.println(tapToken.call("balanceOf", owner.getAddress()));
        tapToken.invoke(owner, "stake", BigInteger.valueOf(100000).multiply(MULTIPLIER));
        contextMock.when(ownerCall()).thenReturn(dividends);

        tapToken.invoke(owner, "get_stake_updates");

        tapToken.invoke(owner, "clear_yesterdays_stake_changes");
    }

    @Test
    void transfer() {
        stake();
        tapToken.invoke(owner, "transfer", testingAccount.getAddress(), BigInteger.valueOf(10000).multiply(MULTIPLIER), new byte[0]);

        System.out.println(tapToken.call("details_balanceOf", owner.getAddress()));

        @SuppressWarnings("unchecked")
        Map<String, BigInteger> detailsBalanceOf = (Map<String, BigInteger>) tapToken.call("details_balanceOf", owner.getAddress());

        assertEquals(new BigInteger("624900000000000000000000000"), detailsBalanceOf.get("Available balance"));
        assertEquals(BigInteger.valueOf(90000).multiply(MULTIPLIER), detailsBalanceOf.get("Staked balance"));
        assertEquals(new BigInteger("625000000000000000000000000").subtract(BigInteger.valueOf(10000).multiply(MULTIPLIER)), detailsBalanceOf.get("Total balance"));
        assertEquals(BigInteger.valueOf(0), detailsBalanceOf.get("Unstaking balance"));

        System.out.println(tapToken.call("details_balanceOf", testingAccount.getAddress()));

        //noinspection unchecked
        detailsBalanceOf = (Map<String, BigInteger>) tapToken.call("details_balanceOf", testingAccount.getAddress());
        assertEquals(BigInteger.valueOf(10000).multiply(MULTIPLIER), detailsBalanceOf.get("Available balance"));
        assertEquals(ZERO, detailsBalanceOf.get("Staked balance"));
        assertEquals(BigInteger.valueOf(10000).multiply(MULTIPLIER), detailsBalanceOf.get("Total balance"));
        assertEquals(BigInteger.valueOf(0), detailsBalanceOf.get("Unstaking balance"));
    }

    @Test
    void transferFromLockedAccount() {
        transfer();

        contextMock.when(ownerCall()).thenReturn(gameAuth);

        tapToken.invoke(owner, "set_locklist_address", testingAccount.getAddress());

        contextMock.when(ownerCall()).thenReturn(testingAccount.getAddress());

        Executable transferfromLockedAccount = () -> tapToken.invoke(testingAccount, "transfer", testingAccount1.getAddress(), BigInteger.valueOf(10000).multiply(MULTIPLIER), new byte[0]);
        expectErrorMessage(transferfromLockedAccount, "Reverted(0): Transfer of TAP has been locked for this address.");
    }

    @DisplayName("transfer from locked account after removing it from locked account list")
    @Test
    void transferFromLockedAccount_2() {
        transferFromLockedAccount();

        contextMock.when(ownerCall()).thenReturn(gameAuth);

        tapToken.invoke(owner, "remove_from_locklist", testingAccount.getAddress());

        contextMock.when(ownerCall()).thenReturn(testingAccount.getAddress());
        tapToken.invoke(testingAccount, "transfer", testingAccount1.getAddress(), BigInteger.valueOf(10000).multiply(MULTIPLIER), new byte[0]);

        assertEquals(ZERO, tapToken.call("balanceOf", testingAccount.getAddress()));
        assertEquals(BigInteger.valueOf(10000).multiply(MULTIPLIER), tapToken.call("balanceOf", testingAccount1.getAddress()));
    }

    @Test
    void transferFromBlacklistedAccount() {
        transfer();

        contextMock.when(ownerCall()).thenReturn(gameAuth);

        tapToken.invoke(owner, "set_blacklist_address", testingAccount.getAddress());

        contextMock.when(ownerCall()).thenReturn(testingAccount.getAddress());

        Executable transferfromLockedAccount = () -> tapToken.invoke(testingAccount, "transfer", testingAccount1.getAddress(), BigInteger.valueOf(10000).multiply(MULTIPLIER), new byte[0]);
        expectErrorMessage(transferfromLockedAccount, "Reverted(0): Transfer of TAP has been locked for this address.");
    }

    @DisplayName("transfer from blacklist address after it is removed from blacklist address list")
    @Test
    void transferFromBlacklistedAccount_2() {
        transferFromBlacklistedAccount();

        contextMock.when(ownerCall()).thenReturn(gameAuth);

        tapToken.invoke(owner, "remove_from_blacklist", testingAccount.getAddress());

        contextMock.when(ownerCall()).thenReturn(testingAccount.getAddress());
        tapToken.invoke(testingAccount, "transfer", testingAccount1.getAddress(), BigInteger.valueOf(10000).multiply(MULTIPLIER), new byte[0]);

        assertEquals(ZERO, tapToken.call("balanceOf", testingAccount.getAddress()));
        assertEquals(BigInteger.valueOf(10000).multiply(MULTIPLIER), tapToken.call("balanceOf", testingAccount1.getAddress()));


    }

    @Test
    void transferfromPausedWhitelistAccountWhilePaused() {
        transfer();
        contextMock.when(ownerCall()).thenReturn(owner.getAddress());
        tapToken.invoke(owner, "togglePaused");

        contextMock.when(ownerCall()).thenReturn(gameAuth);

        tapToken.invoke(owner, "set_whitelist_address", testingAccount.getAddress());

        contextMock.when(ownerCall()).thenReturn(testingAccount.getAddress());

        tapToken.invoke(testingAccount, "transfer", testingAccount1.getAddress(), BigInteger.valueOf(10000).multiply(MULTIPLIER), new byte[0]);

        assertEquals(ZERO, tapToken.call("balanceOf", testingAccount.getAddress()));
        assertEquals(BigInteger.valueOf(10000).multiply(MULTIPLIER), tapToken.call("balanceOf", testingAccount1.getAddress()));
    }

    @DisplayName("Transfer from whitelist address after it is removed from whitelist")
    @Test
    void transfer_2() {
        transfer();
        contextMock.when(ownerCall()).thenReturn(owner.getAddress());
        tapToken.invoke(owner, "togglePaused");

        contextMock.when(ownerCall()).thenReturn(gameAuth);

        tapToken.invoke(owner, "set_whitelist_address", testingAccount.getAddress());

        contextMock.when(ownerCall()).thenReturn(testingAccount.getAddress());

        tapToken.invoke(testingAccount, "transfer", testingAccount1.getAddress(), BigInteger.valueOf(5000).multiply(MULTIPLIER), new byte[0]);

        assertEquals(BigInteger.valueOf(5000).multiply(MULTIPLIER), tapToken.call("balanceOf", testingAccount.getAddress()));
        assertEquals(BigInteger.valueOf(5000).multiply(MULTIPLIER), tapToken.call("balanceOf", testingAccount1.getAddress()));

        contextMock.when(ownerCall()).thenReturn(gameAuth);

        tapToken.invoke(owner, "remove_from_whitelist", testingAccount.getAddress());

        contextMock.when(ownerCall()).thenReturn(testingAccount.getAddress());

        Executable transfer = () -> tapToken.invoke(testingAccount, "transfer", testingAccount1.getAddress(), BigInteger.valueOf(5000).multiply(MULTIPLIER), new byte[0]);
        expectErrorMessage(transfer, "Reverted(0): TAP token transfers are paused");

    }

    @Test
    void transferfromNotWhitelistAccountWhilePaused() {
        transfer();
        contextMock.when(ownerCall()).thenReturn(owner.getAddress());
        tapToken.invoke(owner, "togglePaused");

        contextMock.when(ownerCall()).thenReturn(testingAccount.getAddress());

        Executable transferWhenPausedFromNonWhitelistAddress = () -> tapToken.invoke(testingAccount, "transfer", testingAccount1.getAddress(), BigInteger.valueOf(10000).multiply(MULTIPLIER), new byte[0]);
        expectErrorMessage(transferWhenPausedFromNonWhitelistAddress, "Reverted(0): TAP token transfers are paused");
    }


    public void expectErrorMessage(Executable contractCall, String errorMessage) {
        AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
        assertEquals(errorMessage, e.getMessage());
    }

    /***
     * Both owner and governance contract can set the locklist addresses
     ***/
    @Test
    void setLocklistAddress() {
        setScores();
        tapToken.invoke(owner, "toggle_staking_enabled");

        contextMock.when(ownerCall()).thenReturn(gameAuth);
        setInListedAddresses("set_locklist_address");
        verifyAddressInListedAddresses("get_locklist_addresses");

    }

    /***
     * Both owner and governance contract can set the blacklist addresses
     ***/
    @Test
    void setBlacklistAddress(){
        setScores();

        contextMock.when(ownerCall()).thenReturn(gameAuth);
        setInListedAddresses("set_blacklist_address");
        verifyAddressInListedAddresses("get_blacklist_addresses");
    }

    /***
     * Both owner and governance contract can set the blacklist addresses
     ***/
    @Test
    void setWhiteListAddress(){
        setScores();

        contextMock.when(ownerCall()).thenReturn(gameAuth);
        setInListedAddresses("set_whitelist_address");
        verifyAddressInListedAddresses("get_whitelist_addresses");
    }

    private void setInListedAddresses(String listedAddressType){
        tapToken.invoke(owner, listedAddressType, testingAccount.getAddress());
        tapToken.invoke(owner, listedAddressType, testingAccount1.getAddress());
        tapToken.invoke(owner, listedAddressType, testingAccount2.getAddress());
        tapToken.invoke(owner, listedAddressType, testingAccount3.getAddress());
        tapToken.invoke(owner, listedAddressType, testingAccount4.getAddress());
        tapToken.invoke(owner, listedAddressType, testingAccount5.getAddress());
        tapToken.invoke(owner, listedAddressType, testingAccount6.getAddress());
        tapToken.invoke(owner, listedAddressType, testingAccount7.getAddress());
        tapToken.invoke(owner, listedAddressType, testingAccount8.getAddress());
        tapToken.invoke(owner, listedAddressType, testingAccount9.getAddress());
        tapToken.invoke(owner, listedAddressType, testingAccount10.getAddress());
    }

    private void verifyAddressInListedAddresses(String listedAddressType){
        assertEquals(List.of(testingAccount.getAddress(),
                        testingAccount1.getAddress(),
                        testingAccount2.getAddress(),
                        testingAccount3.getAddress(),
                        testingAccount4.getAddress(),
                        testingAccount5.getAddress(),
                        testingAccount6.getAddress(),
                        testingAccount7.getAddress(),
                        testingAccount8.getAddress(),
                        testingAccount9.getAddress(),
                        testingAccount10.getAddress()),
                tapToken.call(listedAddressType));
    }

    @Test
    void removeFromBlackListAddresses(){
        setBlacklistAddress();

        tapToken.invoke(owner, "remove_from_blacklist", testingAccount.getAddress());

        assertFalse(containsInList(testingAccount.getAddress(), (List<Address>) tapToken.call("get_blacklist_addresses")));

    }

    @Test
    void removeFromWitelistAddresses(){
        setWhiteListAddress();

        tapToken.invoke(owner, "remove_from_whitelist", testingAccount.getAddress());

        assertFalse(containsInList(testingAccount.getAddress(), (List<Address>) tapToken.call("get_whitelist_addresses")));
    }

    @Test
    void removeFromLocklistAddresses(){
        setLocklistAddress();
        tapToken.invoke(owner, "remove_from_locklist", testingAccount.getAddress());

        assertFalse(containsInList(testingAccount.getAddress(), (List<Address>) tapToken.call("get_locklist_addresses")));
    }

    @Test
    void removeFromWhiteListNotPresentInList(){
        setScores();
        contextMock.when(ownerCall()).thenReturn(gameAuth);

        Executable remove = () -> tapToken.invoke(owner, "remove_from_whitelist", testingAccount.getAddress());
        expectErrorMessage(remove, "Reverted(0): hx0000000000000000000000000000000000000002 not in whitelist address");
    }

    @Test
    void removeFromLocklistListNotPresentInList(){
        setScores();
        contextMock.when(ownerCall()).thenReturn(gameAuth);

        Executable remove = () -> tapToken.invoke(owner, "remove_from_locklist", testingAccount.getAddress());
        expectErrorMessage(remove, "Reverted(0): hx0000000000000000000000000000000000000002 not in locklist address");
    }

    @Test
    void removeFromBlacklistListNotPresentInList(){
        setScores();
        contextMock.when(ownerCall()).thenReturn(gameAuth);

        Executable remove = () -> tapToken.invoke(owner, "remove_from_blacklist", testingAccount.getAddress());
        expectErrorMessage(remove, "Reverted(0): hx0000000000000000000000000000000000000002 not in blacklist address");
    }

    /***
     * Adding an address to locklist address which has staked.. the staked amount should be unstaked
     */
    @Test
    void addToLockListAddress(){
        setScores();
        contextMock.when(ownerCall()).thenReturn(gameAuth);
        tapToken.invoke(owner, "set_unstaking_period", BigInteger.TEN);

        contextMock.when(ownerCall()).thenReturn(owner.getAddress());
        tapToken.invoke(owner, "toggle_staking_enabled");
        tapToken.invoke(owner, "toggleEnableSnapshot");
        System.out.println(tapToken.call("balanceOf", owner.getAddress()));

        tapToken.invoke(owner, "transfer", testingAccount.getAddress(), BigInteger.valueOf(100000).multiply(MULTIPLIER), new byte[0]);

        contextMock.when(ownerCall()).thenReturn(testingAccount.getAddress());
        tapToken.invoke(testingAccount, "stake", BigInteger.valueOf(100000).multiply(MULTIPLIER));

        //noinspection unchecked
        Map<String, BigInteger> detailsBalanceOf = (Map<String, BigInteger>) tapToken.call("details_balanceOf", testingAccount.getAddress());
        assertEquals(ZERO, detailsBalanceOf.get("Unstaking balance"));

        contextMock.when(ownerCall()).thenReturn(gameAuth);

        tapToken.invoke(owner, "set_locklist_address", testingAccount.getAddress());

        //noinspection unchecked
        detailsBalanceOf = (Map<String, BigInteger>) tapToken.call("details_balanceOf", testingAccount.getAddress());
        assertEquals(BigInteger.valueOf(100000).multiply(MULTIPLIER), detailsBalanceOf.get("Unstaking balance"));
    }

    @Test
    void checkMigration(){
        setScores();
        tapToken.invoke(owner, "toggle_staking_enabled");
        contextMock.when(ownerCall()).thenReturn(gameAuth);
        setInListedAddresses("set_locklist_address");
        setInListedAddresses("set_whitelist_address");
        setInListedAddresses("set_blacklist_address");

        tapToken.invoke(owner, "set_max_loop", 4);

        contextMock.when(ownerCall()).thenReturn(owner.getAddress());
        tapToken.invoke(owner, "startLinearComplexityMigration");
        tapToken.invoke(owner, "checkMigrationOwnerCall");

        System.out.println(tapToken.call("migrationStatus"));

        tapToken.invoke(owner, "checkMigrationOwnerCall");

        System.out.println(tapToken.call("migrationStatus"));

        tapToken.invoke(owner, "checkMigrationOwnerCall");

        System.out.println(tapToken.call("migrationStatus"));
    }

    private static MockedStatic.Verification ownerCall() {
        return () -> Context.getCaller();
    }

    private <T> boolean containsInList(T value, List<T> arraydb) {
        boolean found = false;
        if (arraydb == null || value == null) {
            return found;
        }

        for (int i = 0; i < arraydb.size(); i++) {
            if (arraydb.get(i) != null
                    && arraydb.get(i).equals(value)) {
                found = true;
                break;
            }
        }
        return found;
    }
}

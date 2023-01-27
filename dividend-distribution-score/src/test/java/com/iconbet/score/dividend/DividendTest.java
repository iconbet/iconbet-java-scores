package com.iconbet.score.dividend;

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


import static org.mockito.Mockito.*;

import score.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;

public class DividendTest extends TestBase{
    private static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    private static final Address daoFund = Address.fromString("cx0000000000000000000000000000000000000001");
    private static final Address gameScore = Address.fromString("cx0000000000000000000000000000000000000002");
    private static final Address tapToken = Address.fromString("cx0000000000000000000000000000000000000003");
    private static final Address gameAuth = Address.fromString("cx0000000000000000000000000000000000000004");
    private static final Address ibpnpScore = Address.fromString("cx0000000000000000000000000000000000000005");
    private static final Address promo = Address.fromString("cx0000000000000000000000000000000000000006");

    private static final Address dice = Address.fromString("cx0000000000000000000000000000000000000007");
    private static final Address roulette = Address.fromString("cx0000000000000000000000000000000000000008");
    private static final Address blackjack = Address.fromString("cx0000000000000000000000000000000000000009");
    public static final String TAG = "ICONbet Dividends";

    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account testingAccount = sm.createAccount();
    private static final Account testingAccount1 = sm.createAccount();
    private static final Account tapStakeUser1 = sm.createAccount();
    private static final Account tapStakeUser2 = sm.createAccount();
    private static final Account tapStakeUser3 = sm.createAccount();
    private static final Account tapStakeUser4 = sm.createAccount();
    private static final Account revshareWallet = sm.createAccount();
    private static final Account notRevshareWallet = sm.createAccount();
    public static final BigInteger decimal = new BigInteger("1000000000000000000");
    private Score DividendScore;
    private final SecureRandom secureRandom = new SecureRandom();
    private static MockedStatic<Context> contextMock;


    Dividend scoreSpy;

    @BeforeEach
    public void setup() throws Exception {
        DividendScore = sm.deploy(owner, Dividend.class, false);
        Dividend instance = (Dividend) DividendScore.getInstance();
        scoreSpy = spy(instance);
        DividendScore.setInstance(scoreSpy);
        long currentTime = System.currentTimeMillis() / 1000L;
        sm.getBlock().increase(currentTime / 2);
    }

    @BeforeAll
    public static void init(){
        contextMock = Mockito.mockStatic(Context.class, CALLS_REAL_METHODS);
    }

    @Test
    void setDividendPercentage(){
        setScoresMethod();
        contextMock.when(() -> Context.getCaller()).thenReturn(gameAuth);
        DividendScore.invoke(owner, "set_dividend_percentage", BigInteger.TEN, BigInteger.valueOf(20), BigInteger.valueOf(40), BigInteger.valueOf(30));
        assertEquals(Map.of("_tap", BigInteger.TEN, "_platform", BigInteger.valueOf(30), "_promo", BigInteger.valueOf(40), "_gamedev", BigInteger.valueOf(20)), DividendScore.call("get_dividend_percentage"));
    }

    private void setDividendPercentageMethod(){
        setScoresMethod();
        contextMock.when(() -> Context.getCaller()).thenReturn(gameAuth);
        DividendScore.invoke(owner, "set_dividend_percentage", BigInteger.TEN, BigInteger.valueOf(20), BigInteger.valueOf(40), BigInteger.valueOf(30));
    }

    @Test
    void setDividendPercentageNotGovernance(){
        contextMock.when(() -> Context.getCaller()).thenReturn(testingAccount.getAddress());
        Executable setDividendPercentageNotOwner = () -> DividendScore.invoke(testingAccount, "set_dividend_percentage", BigInteger.TEN, BigInteger.valueOf(20), BigInteger.valueOf(40), BigInteger.valueOf(30));
        expectErrorMessage(setDividendPercentageNotOwner, "Reverted(0): " + TAG + ": Only Governance score can call this method.");
    }

    @Test
    void setDividendPercentageInvalidPercentage(){
        setScoresMethod();
        contextMock.when(() -> Context.getCaller()).thenReturn(gameAuth);
        Executable setDividendPercentageNotOwner = () -> DividendScore.invoke(owner, "set_dividend_percentage", BigInteger.TEN, BigInteger.valueOf(120), BigInteger.valueOf(40), BigInteger.valueOf(30));
        expectErrorMessage(setDividendPercentageNotOwner, "Reverted(0): " + TAG + ": The parameters must be between 0 to 100");
    }

    @Test
    void setDividendPercentagePercentageSumNotEqualTo100(){
        setScoresMethod();
        contextMock.when(() -> Context.getCaller()).thenReturn(gameAuth);
        Executable setDividendPercentageNotOwner = () -> DividendScore.invoke(owner, "set_dividend_percentage", BigInteger.TEN, BigInteger.valueOf(30), BigInteger.valueOf(40), BigInteger.valueOf(30));
        expectErrorMessage(setDividendPercentageNotOwner, "Reverted(0): " + TAG + ": Sum of all percentage is not equal to 100");
    }

    @Test
    void setScores(){
        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
        DividendScore.invoke(owner, "set_token_score", tapToken);
        DividendScore.invoke(owner, "setIBPNPScore", ibpnpScore);
        DividendScore.invoke(owner, "set_game_auth_score", gameAuth);
        DividendScore.invoke(owner, "set_daofund_score", daoFund);
        DividendScore.invoke(owner, "set_promo_score", promo);
        DividendScore.invoke(owner, "set_game_score", gameScore);

        assertEquals(DividendScore.call("get_token_score"), tapToken);
        assertEquals(DividendScore.call("get_game_score"), gameScore);
        assertEquals(DividendScore.call("get_promo_score"), promo);
        assertEquals(DividendScore.call("getIBPNPScore"), ibpnpScore);
        assertEquals(DividendScore.call("get_daofund_score"), daoFund);
        assertEquals(DividendScore.call("get_game_auth_score"), gameAuth);
    }

    private void setScoresMethod(){
        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
        DividendScore.invoke(owner, "set_token_score", tapToken);
        DividendScore.invoke(owner, "setIBPNPScore", ibpnpScore);
        DividendScore.invoke(owner, "set_game_auth_score", gameAuth);
        DividendScore.invoke(owner, "set_daofund_score", daoFund);
        DividendScore.invoke(owner, "set_promo_score", promo);
        DividendScore.invoke(owner, "set_game_score", gameScore);
    }

    @Test
    void setScoresNotContractAddresses(){
        Address _score = testingAccount.getAddress();

        Executable setScoresNotContractAddresses = () -> DividendScore.invoke(owner, "set_token_score", _score);
        expectErrorMessage(setScoresNotContractAddresses, "Reverted(0): " + TAG + ": " + _score + " is not a valid contract address");

        setScoresNotContractAddresses = () -> DividendScore.invoke(owner, "setIBPNPScore", _score);
        expectErrorMessage(setScoresNotContractAddresses, "Reverted(0): " + TAG + ": " + _score + " is not a valid contract address");

        setScoresNotContractAddresses = () -> DividendScore.invoke(owner, "set_game_auth_score", _score);
        expectErrorMessage(setScoresNotContractAddresses, "Reverted(0): " + TAG + ": " + _score + " is not a valid contract address");

        setScoresNotContractAddresses = () -> DividendScore.invoke(owner, "set_daofund_score", _score);
        expectErrorMessage(setScoresNotContractAddresses, "Reverted(0): " + TAG + ": " + _score + " is not a valid contract address");

        setScoresNotContractAddresses = () -> DividendScore.invoke(owner, "set_promo_score", _score);
        expectErrorMessage(setScoresNotContractAddresses, "Reverted(0): " + TAG + ": " + _score + " is not a valid contract address");

        setScoresNotContractAddresses = () -> DividendScore.invoke(owner, "set_game_score", _score);
        expectErrorMessage(setScoresNotContractAddresses, "Reverted(0): " + TAG + ": " + _score + " is not a valid contract address");
    }

    @Test
    void setContractAddressesNotOwner(){
        Address _score = tapToken;
        contextMock.when(() -> Context.getCaller()).thenReturn(testingAccount.getAddress());
        Executable setScoresNotContractAddresses = () -> DividendScore.invoke(testingAccount, "set_token_score", _score);
        expectErrorMessage(setScoresNotContractAddresses, "Reverted(0): " + TAG + ": Only owner can call this method.");

        setScoresNotContractAddresses = () -> DividendScore.invoke(testingAccount, "setIBPNPScore", _score);
        expectErrorMessage(setScoresNotContractAddresses, "Reverted(0): " + TAG + ": Only owner can call this method.");

        setScoresNotContractAddresses = () -> DividendScore.invoke(testingAccount, "set_game_auth_score", _score);
        expectErrorMessage(setScoresNotContractAddresses, "Reverted(0): " + TAG + ": Only owner can call this method.");

        setScoresNotContractAddresses = () -> DividendScore.invoke(testingAccount, "set_daofund_score", _score);
        expectErrorMessage(setScoresNotContractAddresses, "Reverted(0): " + TAG + ": Only owner can call this method.");

        setScoresNotContractAddresses = () -> DividendScore.invoke(testingAccount, "set_promo_score", _score);
        expectErrorMessage(setScoresNotContractAddresses, "Reverted(0): " + TAG + ": Only owner can call this method.");

        setScoresNotContractAddresses = () -> DividendScore.invoke(testingAccount, "set_game_score", _score);
        expectErrorMessage(setScoresNotContractAddresses, "Reverted(0): " + TAG + ": Only owner can call this method.");
    }

    @Test
    void toggleSwitchDividendsToStakedTapEnabled(){
        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
        DividendScore.invoke(owner, "toggle_switch_dividends_to_staked_tap_enabled");
        assertEquals(true, DividendScore.call("get_switch_dividends_to_staked_tap"));
        DividendScore.invoke(owner, "toggle_switch_dividends_to_staked_tap_enabled");
        assertEquals(false, DividendScore.call("get_switch_dividends_to_staked_tap"));
    }

    @Test
    void toggleSwitchDividendsToStakedTapEnabledNotOwner(){
        contextMock.when(() -> Context.getCaller()).thenReturn(testingAccount.getAddress());
        Executable toggleSwitchDividendsToStakedTapEnabledNotOwner = () -> DividendScore.invoke(testingAccount, "toggle_switch_dividends_to_staked_tap_enabled");
        expectErrorMessage(toggleSwitchDividendsToStakedTapEnabledNotOwner, "Reverted(0): " + TAG + ": Only owner can enable or disable switch dividends to staked tap holders.");
    }

    @Test
    void distributeDividendsReceivedIsOne(){
        setScoresMethod();
        contextMock.when(() -> Context.getCaller()).thenReturn(gameAuth);
        DividendScore.invoke(owner, "set_blacklist_address", testingAccount.getAddress());
        DividendScore.invoke(owner, "set_blacklist_address", testingAccount1.getAddress());
        contextMock.when(() -> Context.getCaller()).thenReturn(gameScore);
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_address_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        doReturn(BigInteger.valueOf(100).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), any());
        doReturn(BigInteger.valueOf(1000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalSupply"));

        DividendScore.invoke(owner, "fallback");

        DividendScore.invoke(owner, "distribute");
    }

    @Test
    void distributeDividendsReceivedIsTwo(){
        setScoresMethod();
        contextMock.when(() -> Context.getCaller()).thenReturn(gameAuth);
        DividendScore.invoke(owner, "add_exception_address", testingAccount.getAddress());
        DividendScore.invoke(owner, "add_exception_address", testingAccount1.getAddress());

        contextMock.when(() -> Context.getCaller()).thenReturn(gameScore);
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_address_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        doReturn(BigInteger.valueOf(1000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), any());
        doReturn(BigInteger.valueOf(100).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("staked_balanceOf"), any());

        doReturn(BigInteger.valueOf(1000000000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalSupply"));
        Map<String, BigInteger> stakedBalanceOf = Map.of(
                testingAccount.toString(), BigInteger.valueOf(1000).multiply(decimal),
                testingAccount1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser2.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser3.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser4.toString(), BigInteger.valueOf(1000).multiply(decimal)
        );
        Map<String, BigInteger> stakeBalanceOfFromUpdates = Map.of();
        doReturn(stakeBalanceOfFromUpdates).when(scoreSpy).callScore(eq(Map.class), eq(tapToken), eq("get_stake_updates"));
        doReturn(BigInteger.valueOf(100000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("total_staked_balance"));
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_stake_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        DividendScore.invoke(owner, "fallback");

        DividendScore.invoke(owner, "distribute");

        DividendScore.invoke(owner, "distribute");
        System.out.println(DividendScore.call("getEligibleStakedTapToken"));
    }

    @Test
    void distributeDividendsReceivedIsThreeTreasuryStatusTrue(){
        setScoresMethod();
        contextMock.when(() -> Context.getCaller()).thenReturn(gameAuth);
        DividendScore.invoke(owner, "add_exception_address", testingAccount.getAddress());
        DividendScore.invoke(owner, "add_exception_address", testingAccount1.getAddress());

        contextMock.when(() -> Context.getCaller()).thenReturn(gameScore);
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_address_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        doReturn(BigInteger.valueOf(1000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), any());
        doReturn(BigInteger.valueOf(100).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("staked_balanceOf"), any());

        doReturn(BigInteger.valueOf(1000000000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalSupply"));
        Map<String, BigInteger> stakedBalanceOf = Map.of(
                testingAccount.toString(), BigInteger.valueOf(1000).multiply(decimal),
                testingAccount1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser2.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser3.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser4.toString(), BigInteger.valueOf(1000).multiply(decimal)
        );
        Map<String, BigInteger> stakeBalanceOfFromUpdates = Map.of();
        doReturn(stakeBalanceOfFromUpdates).when(scoreSpy).callScore(eq(Map.class), eq(tapToken), eq("get_stake_updates"));
        doReturn(BigInteger.valueOf(100000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("total_staked_balance"));
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_stake_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        DividendScore.invoke(owner, "fallback");

        DividendScore.invoke(owner, "distribute");

        DividendScore.invoke(owner, "distribute");

        doReturn(Boolean.TRUE).when(scoreSpy).callScore(eq(Boolean.class), eq(gameScore), eq("get_treasury_status"));
        doReturn(BigInteger.valueOf(10)).when(scoreSpy).callScore(eq(BigInteger.class), eq(gameScore), eq("get_batch_size"), any());
        DividendScore.invoke(owner, "distribute");
        System.out.println(DividendScore.call("batchSize"));
        System.out.println(DividendScore.call("dividendsReceived"));
    }

    @Test
    void distributeDividendsReceivedIsThreeSwitchDividendsToStakedTapIsTrue(){
        setScoresMethod();
        contextMock.when(() -> Context.getCaller()).thenReturn(gameAuth);
        DividendScore.invoke(owner, "add_exception_address", testingAccount.getAddress());
        DividendScore.invoke(owner, "add_exception_address", testingAccount1.getAddress());
        contextMock.when(() -> Context.getCaller()).thenReturn(gameScore);
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_address_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        doReturn(BigInteger.valueOf(1000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), any());
        doReturn(BigInteger.valueOf(100).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("staked_balanceOf"), any());

        doReturn(BigInteger.valueOf(1000000000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalSupply"));
        Map<String, BigInteger> stakedBalanceOf = Map.of(
                testingAccount.toString(), BigInteger.valueOf(1000).multiply(decimal),
                testingAccount1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser2.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser3.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser4.toString(), BigInteger.valueOf(1000).multiply(decimal)
        );
        Map<String, BigInteger> stakeBalanceOfFromUpdates = Map.of();
        doReturn(stakeBalanceOfFromUpdates).when(scoreSpy).callScore(eq(Map.class), eq(tapToken), eq("get_stake_updates"));
        doReturn(BigInteger.valueOf(100000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("total_staked_balance"));
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_stake_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        DividendScore.invoke(owner, "fallback");

        DividendScore.invoke(owner, "distribute");

        DividendScore.invoke(owner, "distribute");

        doReturn(Boolean.FALSE).when(scoreSpy).callScore(eq(Boolean.class), eq(gameScore), eq("get_treasury_status"));
        doReturn(BigInteger.valueOf(10)).when(scoreSpy).callScore(eq(BigInteger.class), eq(gameScore), eq("get_batch_size"), any());

        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
        DividendScore.invoke(owner, "toggle_switch_dividends_to_staked_tap_enabled");

        Map<String, String> gamesExcess = Map.of(
                dice.toString(), BigInteger.valueOf(100).multiply(decimal).toString(),
                roulette.toString(), BigInteger.valueOf(100).multiply(decimal).toString(),
                blackjack.toString(), BigInteger.valueOf(100).multiply(decimal).toString()
        );
        doReturn(gamesExcess).when(scoreSpy).callScore(eq(Map.class), eq(gameAuth), eq("get_yesterdays_games_excess"));
        doReturn(revshareWallet.getAddress()).when(scoreSpy).callScore(eq(Address.class), eq(gameAuth), eq("get_revshare_wallet_address"), any());
        DividendScore.invoke(owner, "distribute");
        System.out.println(DividendScore.call("batchSize"));
        System.out.println(DividendScore.call("dividendsReceived"));
    }

    @Test
    void distributeDividendReceivedIsThreeLastElse(){
        setScoresMethod();
        contextMock.when(() -> Context.getCaller()).thenReturn(gameAuth);
        DividendScore.invoke(owner, "add_exception_address", testingAccount.getAddress());
        DividendScore.invoke(owner, "add_exception_address", testingAccount1.getAddress());
        setDividendPercentageMethod();
        contextMock.when(() -> Context.getCaller()).thenReturn(gameScore);
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_address_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        doReturn(BigInteger.valueOf(1000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), any());
        doReturn(BigInteger.valueOf(100).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("staked_balanceOf"), any());

        doReturn(BigInteger.valueOf(1000000000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalSupply"));
        Map<String, BigInteger> stakedBalanceOf = Map.of(
                testingAccount.toString(), BigInteger.valueOf(1000).multiply(decimal),
                testingAccount1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser2.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser3.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser4.toString(), BigInteger.valueOf(1000).multiply(decimal)
        );
        Map<String, BigInteger> stakeBalanceOfFromUpdates = Map.of();
        doReturn(stakeBalanceOfFromUpdates).when(scoreSpy).callScore(eq(Map.class), eq(tapToken), eq("get_stake_updates"));
        doReturn(BigInteger.valueOf(100000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("total_staked_balance"));
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_stake_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        DividendScore.invoke(owner, "fallback");

        DividendScore.invoke(owner, "distribute");

        DividendScore.invoke(owner, "distribute");

        doReturn(Boolean.FALSE).when(scoreSpy).callScore(eq(Boolean.class), eq(gameScore), eq("get_treasury_status"));
        doReturn(BigInteger.valueOf(10)).when(scoreSpy).callScore(eq(BigInteger.class), eq(gameScore), eq("get_batch_size"), any());

        Map<String, String> gamesExcess = Map.of(
                dice.toString(), BigInteger.valueOf(100).multiply(decimal).toString(),
                roulette.toString(), BigInteger.valueOf(100).multiply(decimal).toString(),
                blackjack.toString(), BigInteger.valueOf(100).multiply(decimal).toString()
        );
        doReturn(gamesExcess).when(scoreSpy).callScore(eq(Map.class), eq(gameAuth), eq("get_yesterdays_games_excess"));
        doReturn(revshareWallet.getAddress()).when(scoreSpy).callScore(eq(Address.class), eq(gameAuth), eq("get_revshare_wallet_address"), any());
        contextMock.when(() -> Context.getBalance(any())).thenReturn(BigInteger.valueOf(100000).multiply(decimal));
        DividendScore.invoke(owner, "distribute");
        System.out.println(DividendScore.call("divs_share"));
        System.out.println(DividendScore.call("dividends_dist_complete"));
        System.out.println(DividendScore.call("dividendsReceived"));
        System.out.println(DividendScore.call("batchSize"));
    }

    @Test
    void distributeDivsDistCompleteIsTrueNoNeedToUpdateStakeBalance(){
        setScoresMethod();
        contextMock.when(() -> Context.getCaller()).thenReturn(gameAuth);
        DividendScore.invoke(owner, "add_exception_address", testingAccount.getAddress());
        DividendScore.invoke(owner, "add_exception_address", testingAccount1.getAddress());

        setDividendPercentageMethod();
        contextMock.when(() -> Context.getCaller()).thenReturn(gameScore);
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_address_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        doReturn(BigInteger.valueOf(1000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), any());
        doReturn(BigInteger.valueOf(100).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("staked_balanceOf"), any());

        doReturn(BigInteger.valueOf(1000000000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalSupply"));
        Map<String, BigInteger> stakedBalanceOf = Map.of(
                testingAccount.toString(), BigInteger.valueOf(1000).multiply(decimal),
                testingAccount1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser2.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser3.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser4.toString(), BigInteger.valueOf(1000).multiply(decimal)
        );
        Map<String, BigInteger> stakeBalanceOfFromUpdates = Map.of();
        doReturn(stakeBalanceOfFromUpdates).when(scoreSpy).callScore(eq(Map.class), eq(tapToken), eq("get_stake_updates"));
        doReturn(BigInteger.valueOf(100000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("total_staked_balance"));
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_stake_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        DividendScore.invoke(owner, "fallback");

        DividendScore.invoke(owner, "distribute");

        DividendScore.invoke(owner, "distribute");

        doReturn(Boolean.FALSE).when(scoreSpy).callScore(eq(Boolean.class), eq(gameScore), eq("get_treasury_status"));
        doReturn(BigInteger.valueOf(10)).when(scoreSpy).callScore(eq(BigInteger.class), eq(gameScore), eq("get_batch_size"), any());

        Map<String, String> gamesExcess = Map.of(
                dice.toString(), BigInteger.valueOf(100).multiply(decimal).toString(),
                roulette.toString(), BigInteger.valueOf(100).multiply(decimal).toString(),
                blackjack.toString(), BigInteger.valueOf(100).multiply(decimal).toString()
        );
        doReturn(gamesExcess).when(scoreSpy).callScore(eq(Map.class), eq(gameAuth), eq("get_yesterdays_games_excess"));
        doReturn(revshareWallet.getAddress()).when(scoreSpy).callScore(eq(Address.class), eq(gameAuth), eq("get_revshare_wallet_address"), any());
        contextMock.when(() -> Context.getBalance(any())).thenReturn(BigInteger.valueOf(100000).multiply(decimal));

        doNothing().when(scoreSpy).callScore(eq(tapToken), eq("clear_yesterdays_stake_changes"));
        DividendScore.invoke(owner, "distribute");

        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());

        DividendScore.invoke(owner, "set_divs_dist_complete", true);

        DividendScore.invoke(owner, "distribute");
    }

    @Test
    void distributeDivsDistCompleteTrueNeedToUpdateStakeBalances(){
        setScoresMethod();
        setDividendPercentageMethod();
        DividendScore.invoke(owner, "add_exception_address", testingAccount.getAddress());
        DividendScore.invoke(owner, "add_exception_address", testingAccount1.getAddress());
        contextMock.when(() -> Context.getCaller()).thenReturn(gameScore);
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_address_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        doReturn(BigInteger.valueOf(1000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), any());
        doReturn(BigInteger.valueOf(100).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("staked_balanceOf"), any());

        doReturn(BigInteger.valueOf(1000000000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalSupply"));
        Map<String, BigInteger> stakedBalanceOf = Map.of(
                testingAccount.toString(), BigInteger.valueOf(1000).multiply(decimal),
                testingAccount1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser2.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser3.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser4.toString(), BigInteger.valueOf(1000).multiply(decimal)
        );
        Map<String, BigInteger> stakeBalanceOfFromUpdates = Map.of();
        doReturn(stakeBalanceOfFromUpdates).when(scoreSpy).callScore(eq(Map.class), eq(tapToken), eq("get_stake_updates"));
        doReturn(BigInteger.valueOf(100000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("total_staked_balance"));
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_stake_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        DividendScore.invoke(owner, "fallback");

        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());

        DividendScore.invoke(owner, "distribute");

        DividendScore.invoke(owner, "distribute");

        doReturn(Boolean.FALSE).when(scoreSpy).callScore(eq(Boolean.class), eq(gameScore), eq("get_treasury_status"));
        doReturn(BigInteger.valueOf(10)).when(scoreSpy).callScore(eq(BigInteger.class), eq(gameScore), eq("get_batch_size"), any());

        Map<String, String> gamesExcess = Map.of(
                dice.toString(), BigInteger.valueOf(100).multiply(decimal).toString(),
                roulette.toString(), BigInteger.valueOf(100).multiply(decimal).toString(),
                blackjack.toString(), BigInteger.valueOf(100).multiply(decimal).toString()
        );
        doReturn(gamesExcess).when(scoreSpy).callScore(eq(Map.class), eq(gameAuth), eq("get_yesterdays_games_excess"));
        doReturn(revshareWallet.getAddress()).when(scoreSpy).callScore(eq(Address.class), eq(gameAuth), eq("get_revshare_wallet_address"), any());
        contextMock.when(() -> Context.getBalance(any())).thenReturn(BigInteger.valueOf(100000).multiply(decimal));

        doNothing().when(scoreSpy).callScore(eq(tapToken), eq("clear_yesterdays_stake_changes"));
        DividendScore.invoke(owner, "distribute");

        DividendScore.invoke(owner, "set_divs_dist_complete", true);
        doReturn(stakedBalanceOf).when(scoreSpy).callScore(eq(Map.class), eq(tapToken), eq("get_stake_updates"));
        DividendScore.invoke(owner, "distribute");
    }

    @Test
    void distributeRemainingTapDivsGreaterThanZero(){
        setScoresMethod();
        setDividendPercentageMethod();
        DividendScore.invoke(owner, "add_exception_address", testingAccount.getAddress());
        DividendScore.invoke(owner, "add_exception_address", testingAccount1.getAddress());
        contextMock.when(() -> Context.getCaller()).thenReturn(gameScore);
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_address_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        doReturn(BigInteger.valueOf(1000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), any());
        doReturn(BigInteger.valueOf(100).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("staked_balanceOf"), any());

        doReturn(BigInteger.valueOf(1000000000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalSupply"));
        Map<String, BigInteger> stakedBalanceOf = Map.of(
                testingAccount.toString(), BigInteger.valueOf(1000).multiply(decimal),
                testingAccount1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser2.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser3.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser4.toString(), BigInteger.valueOf(1000).multiply(decimal)
        );
        Map<String, BigInteger> stakeBalanceOfFromUpdates = Map.of();
        doReturn(stakeBalanceOfFromUpdates).when(scoreSpy).callScore(eq(Map.class), eq(tapToken), eq("get_stake_updates"));
        doReturn(BigInteger.valueOf(100000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("total_staked_balance"));
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_stake_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        DividendScore.invoke(owner, "fallback");

        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());

        DividendScore.invoke(owner, "distribute");

        DividendScore.invoke(owner, "distribute");

        doReturn(Boolean.FALSE).when(scoreSpy).callScore(eq(Boolean.class), eq(gameScore), eq("get_treasury_status"));
        doReturn(BigInteger.valueOf(10)).when(scoreSpy).callScore(eq(BigInteger.class), eq(gameScore), eq("get_batch_size"), any());

        Map<String, String> gamesExcess = Map.of(
                dice.toString(), BigInteger.valueOf(100).multiply(decimal).toString(),
                roulette.toString(), BigInteger.valueOf(100).multiply(decimal).toString(),
                blackjack.toString(), BigInteger.valueOf(100).multiply(decimal).toString()
        );
        doReturn(gamesExcess).when(scoreSpy).callScore(eq(Map.class), eq(gameAuth), eq("get_yesterdays_games_excess"));
        doReturn(revshareWallet.getAddress()).when(scoreSpy).callScore(eq(Address.class), eq(gameAuth), eq("get_revshare_wallet_address"), any());
        contextMock.when(() -> Context.getBalance(any())).thenReturn(BigInteger.valueOf(100000).multiply(decimal));

        doNothing().when(scoreSpy).callScore(eq(tapToken), eq("clear_yesterdays_stake_changes"));
        DividendScore.invoke(owner, "distribute");

        doReturn(stakedBalanceOf).when(scoreSpy).callScore(eq(Map.class), eq(tapToken), eq("get_stake_updates"));
        contextMock.when(() -> Context.getCaller()).thenReturn(gameAuth);
        DividendScore.invoke(owner, "setTaxPercentage", 10);
        DividendScore.invoke(owner, "setNonTaxPeriod", 10);
        DividendScore.invoke(owner, "distribute");
        System.out.println(DividendScore.call("divs_share"));
    }

    @Test
    void distributePromoDivsGreaterThanZero(){
        setScoresMethod();
        setDividendPercentageMethod();
        DividendScore.invoke(owner, "add_exception_address", testingAccount.getAddress());
        DividendScore.invoke(owner, "add_exception_address", testingAccount1.getAddress());
        contextMock.when(() -> Context.getCaller()).thenReturn(gameScore);
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_address_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        doReturn(BigInteger.valueOf(1000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), any());
        doReturn(BigInteger.valueOf(100).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("staked_balanceOf"), any());

        doReturn(BigInteger.valueOf(1000000000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalSupply"));
        Map<String, BigInteger> stakedBalanceOf = Map.of(
                testingAccount.toString(), BigInteger.valueOf(1000).multiply(decimal),
                testingAccount1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser2.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser3.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser4.toString(), BigInteger.valueOf(1000).multiply(decimal)
        );
        Map<String, BigInteger> stakeBalanceOfFromUpdates = Map.of();
        doReturn(stakeBalanceOfFromUpdates).when(scoreSpy).callScore(eq(Map.class), eq(tapToken), eq("get_stake_updates"));
        doReturn(BigInteger.valueOf(100000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("total_staked_balance"));
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_stake_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        DividendScore.invoke(owner, "fallback");

        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());

        DividendScore.invoke(owner, "distribute");

        DividendScore.invoke(owner, "distribute");

        doReturn(Boolean.FALSE).when(scoreSpy).callScore(eq(Boolean.class), eq(gameScore), eq("get_treasury_status"));
        doReturn(BigInteger.valueOf(10)).when(scoreSpy).callScore(eq(BigInteger.class), eq(gameScore), eq("get_batch_size"), any());

        Map<String, String> gamesExcess = Map.of(
                dice.toString(), BigInteger.valueOf(100).multiply(decimal).toString(),
                roulette.toString(), BigInteger.valueOf(100).multiply(decimal).toString(),
                blackjack.toString(), BigInteger.valueOf(100).multiply(decimal).toString()
        );
        doReturn(gamesExcess).when(scoreSpy).callScore(eq(Map.class), eq(gameAuth), eq("get_yesterdays_games_excess"));
        doReturn(revshareWallet.getAddress()).when(scoreSpy).callScore(eq(Address.class), eq(gameAuth), eq("get_revshare_wallet_address"), any());
        contextMock.when(() -> Context.getBalance(any())).thenReturn(BigInteger.valueOf(100000).multiply(decimal));

        doNothing().when(scoreSpy).callScore(eq(tapToken), eq("clear_yesterdays_stake_changes"));
        DividendScore.invoke(owner, "distribute");

        doReturn(stakedBalanceOf).when(scoreSpy).callScore(eq(Map.class), eq(tapToken), eq("get_stake_updates"));
        contextMock.when(() -> Context.getCaller()).thenReturn(gameAuth);
        DividendScore.invoke(owner, "setTaxPercentage", 10);
        DividendScore.invoke(owner, "setNonTaxPeriod", 10);
        DividendScore.invoke(owner, "distribute");

        contextMock.when(() -> Context.transfer(any(), any())).thenAnswer((Answer<Void>) invocation -> null);
        DividendScore.invoke(owner, "distribute");
        System.out.println(DividendScore.call("divs_share"));
    }

    @Test
    void distributeDaofundDivsGreaterThanZero(){
        setScoresMethod();
        contextMock.when(() -> Context.getCaller()).thenReturn(gameAuth);
        DividendScore.invoke(owner, "add_exception_address", testingAccount.getAddress());
        DividendScore.invoke(owner, "add_exception_address", testingAccount1.getAddress());
        DividendScore.invoke(owner, "set_inhouse_games", dice);
        DividendScore.invoke(owner, "set_inhouse_games", roulette);
        DividendScore.invoke(owner, "set_inhouse_games", blackjack);
        contextMock.when(() -> Context.getCaller()).thenReturn(gameScore);
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_address_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        doReturn(BigInteger.valueOf(1000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), any());
        doReturn(BigInteger.valueOf(100).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("staked_balanceOf"), any());

        doReturn(BigInteger.valueOf(1000000000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalSupply"));
        Map<String, BigInteger> stakedBalanceOf = Map.of(
                testingAccount.toString(), BigInteger.valueOf(1000).multiply(decimal),
                testingAccount1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser2.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser3.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser4.toString(), BigInteger.valueOf(1000).multiply(decimal)
        );
        Map<String, BigInteger> stakeBalanceOfFromUpdates = Map.of();
        doReturn(stakeBalanceOfFromUpdates).when(scoreSpy).callScore(eq(Map.class), eq(tapToken), eq("get_stake_updates"));
        doReturn(BigInteger.valueOf(100000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("total_staked_balance"));
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_stake_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        DividendScore.invoke(owner, "fallback");

        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());

        DividendScore.invoke(owner, "distribute");

        DividendScore.invoke(owner, "distribute");

        doReturn(Boolean.FALSE).when(scoreSpy).callScore(eq(Boolean.class), eq(gameScore), eq("get_treasury_status"));
        doReturn(BigInteger.valueOf(10)).when(scoreSpy).callScore(eq(BigInteger.class), eq(gameScore), eq("get_batch_size"), any());
        DividendScore.invoke(owner, "toggle_switch_dividends_to_staked_tap_enabled");

        Map<String, String> gamesExcess = Map.of(
                dice.toString(), BigInteger.valueOf(100).multiply(decimal).toString(),
                roulette.toString(), BigInteger.valueOf(100).multiply(decimal).toString(),
                blackjack.toString(), BigInteger.valueOf(100).multiply(decimal).toString()
        );
        doReturn(gamesExcess).when(scoreSpy).callScore(eq(Map.class), eq(gameAuth), eq("get_yesterdays_games_excess"));
        doReturn(notRevshareWallet.getAddress()).when(scoreSpy).callScore(eq(Address.class), eq(gameAuth), eq("get_revshare_wallet_address"), any());
        DividendScore.invoke(owner, "distribute");

        doReturn(stakedBalanceOf).when(scoreSpy).callScore(eq(Map.class), eq(tapToken), eq("get_stake_updates"));
        contextMock.when(() -> Context.getCaller()).thenReturn(gameAuth);
        DividendScore.invoke(owner, "setTaxPercentage", 10);
        DividendScore.invoke(owner, "setNonTaxPeriod", 10);

        contextMock.when(() -> Context.transfer(any(), any())).thenAnswer((Answer<Void>) invocation -> null);
        System.out.println(DividendScore.call("get_daofund_divs"));

        DividendScore.invoke(owner, "distribute");
        System.out.println(DividendScore.call("divs_share"));
        System.out.println(DividendScore.call("get_daofund_divs"));
    }

    @Test
    void distributeGameDevelopersDivsGreaterThanZero(){
        setScoresMethod();
        setDividendPercentageMethod();
        DividendScore.invoke(owner, "add_exception_address", testingAccount.getAddress());
        DividendScore.invoke(owner, "add_exception_address", testingAccount1.getAddress());
        contextMock.when(() -> Context.getCaller()).thenReturn(gameScore);
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_address_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        doReturn(BigInteger.valueOf(1000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), any());
        doReturn(BigInteger.valueOf(100).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("staked_balanceOf"), any());

        doReturn(BigInteger.valueOf(1000000000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalSupply"));
        Map<String, BigInteger> stakedBalanceOf = Map.of(
                testingAccount.toString(), BigInteger.valueOf(1000).multiply(decimal),
                testingAccount1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser2.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser3.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser4.toString(), BigInteger.valueOf(1000).multiply(decimal)
        );
        Map<String, BigInteger> stakeBalanceOfFromUpdates = Map.of();
        doReturn(stakeBalanceOfFromUpdates).when(scoreSpy).callScore(eq(Map.class), eq(tapToken), eq("get_stake_updates"));
        doReturn(BigInteger.valueOf(100000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("total_staked_balance"));
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_stake_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        DividendScore.invoke(owner, "fallback");

        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());

        DividendScore.invoke(owner, "distribute");

        DividendScore.invoke(owner, "distribute");

        doReturn(Boolean.FALSE).when(scoreSpy).callScore(eq(Boolean.class), eq(gameScore), eq("get_treasury_status"));
        doReturn(BigInteger.valueOf(10)).when(scoreSpy).callScore(eq(BigInteger.class), eq(gameScore), eq("get_batch_size"), any());

        Map<String, String> gamesExcess = Map.of(
                dice.toString(), BigInteger.valueOf(100).multiply(decimal).toString(),
                roulette.toString(), BigInteger.valueOf(100).multiply(decimal).toString(),
                blackjack.toString(), BigInteger.valueOf(100).multiply(decimal).toString()
        );
        doReturn(gamesExcess).when(scoreSpy).callScore(eq(Map.class), eq(gameAuth), eq("get_yesterdays_games_excess"));
        doReturn(revshareWallet.getAddress()).when(scoreSpy).callScore(eq(Address.class), eq(gameAuth), eq("get_revshare_wallet_address"), any());
        contextMock.when(() -> Context.getBalance(any())).thenReturn(BigInteger.valueOf(100000).multiply(decimal));

        doNothing().when(scoreSpy).callScore(eq(tapToken), eq("clear_yesterdays_stake_changes"));
        DividendScore.invoke(owner, "distribute");

        doReturn(stakedBalanceOf).when(scoreSpy).callScore(eq(Map.class), eq(tapToken), eq("get_stake_updates"));
        contextMock.when(() -> Context.getCaller()).thenReturn(gameAuth);
        DividendScore.invoke(owner, "setTaxPercentage", 10);
        DividendScore.invoke(owner, "setNonTaxPeriod", 10);
        DividendScore.invoke(owner, "distribute");

        contextMock.when(() -> Context.transfer(any(), any())).thenAnswer((Answer<Void>) invocation -> null);
        DividendScore.invoke(owner, "distribute");

        DividendScore.invoke(owner, "distribute");
        System.out.println(DividendScore.call("divs_share"));
        System.out.println(DividendScore.call("get_daofund_divs"));
    }

    @Test
    void distributePlatformDivsGreaterThanZero(){
        setScoresMethod();
        setDividendPercentageMethod();
        DividendScore.invoke(owner, "add_exception_address", testingAccount.getAddress());
        DividendScore.invoke(owner, "add_exception_address", testingAccount1.getAddress());
        contextMock.when(() -> Context.getCaller()).thenReturn(gameScore);
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_address_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        doReturn(BigInteger.valueOf(1000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), any());
        doReturn(BigInteger.valueOf(100).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("staked_balanceOf"), any());

        doReturn(BigInteger.valueOf(1000000000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalSupply"));
        Map<String, BigInteger> stakedBalanceOf = Map.of(
                testingAccount.toString(), BigInteger.valueOf(1000).multiply(decimal),
                testingAccount1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser2.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser3.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser4.toString(), BigInteger.valueOf(1000).multiply(decimal)
        );
        Map<String, BigInteger> stakeBalanceOfFromUpdates = Map.of();
        doReturn(stakeBalanceOfFromUpdates).when(scoreSpy).callScore(eq(Map.class), eq(tapToken), eq("get_stake_updates"));
        doReturn(BigInteger.valueOf(100000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("total_staked_balance"));
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_stake_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        DividendScore.invoke(owner, "fallback");

        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());

        DividendScore.invoke(owner, "distribute");

        DividendScore.invoke(owner, "distribute");

        doReturn(Boolean.FALSE).when(scoreSpy).callScore(eq(Boolean.class), eq(gameScore), eq("get_treasury_status"));
        doReturn(BigInteger.valueOf(10)).when(scoreSpy).callScore(eq(BigInteger.class), eq(gameScore), eq("get_batch_size"), any());

        Map<String, String> gamesExcess = Map.of(
                dice.toString(), BigInteger.valueOf(100).multiply(decimal).toString(),
                roulette.toString(), BigInteger.valueOf(100).multiply(decimal).toString(),
                blackjack.toString(), BigInteger.valueOf(100).multiply(decimal).toString()
        );
        doReturn(gamesExcess).when(scoreSpy).callScore(eq(Map.class), eq(gameAuth), eq("get_yesterdays_games_excess"));
        doReturn(revshareWallet.getAddress()).when(scoreSpy).callScore(eq(Address.class), eq(gameAuth), eq("get_revshare_wallet_address"), any());
        contextMock.when(() -> Context.getBalance(any())).thenReturn(BigInteger.valueOf(100000).multiply(decimal));

        doNothing().when(scoreSpy).callScore(eq(tapToken), eq("clear_yesterdays_stake_changes"));
        DividendScore.invoke(owner, "distribute");

        doReturn(stakedBalanceOf).when(scoreSpy).callScore(eq(Map.class), eq(tapToken), eq("get_stake_updates"));
        contextMock.when(() -> Context.getCaller()).thenReturn(gameAuth);
        DividendScore.invoke(owner, "setTaxPercentage", 10);
        DividendScore.invoke(owner, "setNonTaxPeriod", 10);
        DividendScore.invoke(owner, "distribute");

        contextMock.when(() -> Context.transfer(any(), any())).thenAnswer((Answer<Void>) invocation -> null);
        DividendScore.invoke(owner, "distribute");

        DividendScore.invoke(owner, "distribute");

        DividendScore.invoke(owner, "set_blacklist_address", testingAccount.getAddress());
        DividendScore.invoke(owner, "set_blacklist_address", testingAccount1.getAddress());

        doReturn(BigInteger.valueOf(100).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), any());
        DividendScore.invoke(owner, "distribute");

        System.out.println(DividendScore.call("divs_share"));
    }

    @Test
    void claimRewards(){
        setScoresMethod();
        setDividendPercentageMethod();
        DividendScore.invoke(owner, "add_exception_address", testingAccount.getAddress());
        DividendScore.invoke(owner, "add_exception_address", testingAccount1.getAddress());
        contextMock.when(() -> Context.getCaller()).thenReturn(gameScore);
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_address_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        doReturn(BigInteger.valueOf(1000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), any());
        doReturn(BigInteger.valueOf(100).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("staked_balanceOf"), any());

        doReturn(BigInteger.valueOf(1000000000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalSupply"));
        Map<String, BigInteger> stakedBalanceOf = Map.of(
                testingAccount.toString(), BigInteger.valueOf(1000).multiply(decimal),
                testingAccount1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser1.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser2.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser3.toString(), BigInteger.valueOf(1000).multiply(decimal),
                tapStakeUser4.toString(), BigInteger.valueOf(1000).multiply(decimal)
        );
        Map<String, BigInteger> stakeBalanceOfFromUpdates = Map.of();
        doReturn(stakeBalanceOfFromUpdates).when(scoreSpy).callScore(eq(Map.class), eq(tapToken), eq("get_stake_updates"));
        doReturn(BigInteger.valueOf(100000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("total_staked_balance"));
        contextMock.when(() -> Context.call(eq(tapToken), eq("switch_stake_update_db"))).thenAnswer((Answer<Void>) invocation -> null);
        DividendScore.invoke(owner, "fallback");

        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());

        DividendScore.invoke(owner, "distribute");

        DividendScore.invoke(owner, "distribute");

        doReturn(Boolean.FALSE).when(scoreSpy).callScore(eq(Boolean.class), eq(gameScore), eq("get_treasury_status"));
        doReturn(BigInteger.valueOf(10)).when(scoreSpy).callScore(eq(BigInteger.class), eq(gameScore), eq("get_batch_size"), any());

        Map<String, String> gamesExcess = Map.of(
                dice.toString(), BigInteger.valueOf(100).multiply(decimal).toString(),
                roulette.toString(), BigInteger.valueOf(100).multiply(decimal).toString(),
                blackjack.toString(), BigInteger.valueOf(100).multiply(decimal).toString()
        );
        doReturn(gamesExcess).when(scoreSpy).callScore(eq(Map.class), eq(gameAuth), eq("get_yesterdays_games_excess"));
        doReturn(revshareWallet.getAddress()).when(scoreSpy).callScore(eq(Address.class), eq(gameAuth), eq("get_revshare_wallet_address"), any());
        contextMock.when(() -> Context.getBalance(any())).thenReturn(BigInteger.valueOf(100000).multiply(decimal));

        doNothing().when(scoreSpy).callScore(eq(tapToken), eq("clear_yesterdays_stake_changes"));
        DividendScore.invoke(owner, "distribute");

        doReturn(stakedBalanceOf).when(scoreSpy).callScore(eq(Map.class), eq(tapToken), eq("get_stake_updates"));
        contextMock.when(() -> Context.getCaller()).thenReturn(gameAuth);
        DividendScore.invoke(owner, "setTaxPercentage", 10);
        DividendScore.invoke(owner, "setNonTaxPeriod", 1);
        DividendScore.invoke(owner, "distribute");

        contextMock.when(() -> Context.transfer(any(), any())).thenAnswer((Answer<Void>) invocation -> null);
        DividendScore.invoke(owner, "distribute");

        DividendScore.invoke(owner, "distribute");

        DividendScore.invoke(owner, "set_blacklist_address", testingAccount.getAddress());
        DividendScore.invoke(owner, "set_blacklist_address", testingAccount1.getAddress());

        doReturn(BigInteger.valueOf(100).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), any());
        DividendScore.invoke(owner, "distribute");

        System.out.println(DividendScore.call("divs_share"));
        System.out.println(DividendScore.call("getClaimableAmount", testingAccount.getAddress()));
        System.out.println(DividendScore.call("getClaimableAmount", testingAccount1.getAddress()));

        contextMock.when(() -> Context.getCaller()).thenReturn(testingAccount.getAddress());
        doReturn(Boolean.TRUE).when(scoreSpy).callScore(eq(Boolean.class), eq(ibpnpScore), eq("has_alternate_wallet"), eq(testingAccount.getAddress()));
        doReturn(testingAccount1.getAddress()).when(scoreSpy).callScore(eq(Address.class), eq(ibpnpScore), eq("get_alternate_wallet_address"), eq(testingAccount.getAddress()));

        DividendScore.invoke(testingAccount, "claimRewards");

        contextMock.verify(() -> Context.transfer(daoFund, new BigInteger("39233791748526522592")), times(1));
        contextMock.verify(() -> Context.transfer(testingAccount.getAddress(), new BigInteger("353104125736738703340")), times(1));


        System.out.println(DividendScore.call("getClaimableAmount", testingAccount.getAddress()));
        System.out.println(DividendScore.call("getClaimableAmount", testingAccount1.getAddress()));
    }

    public void expectErrorMessage(Executable contractCall, String errorMessage) {
        AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
        assertEquals(errorMessage, e.getMessage());
    }
}


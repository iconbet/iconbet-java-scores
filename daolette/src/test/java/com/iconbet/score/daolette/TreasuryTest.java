package com.iconbet.score.daolette;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import com.sun.management.DiagnosticCommandMBean;
import org.junit.jupiter.api.*;
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
import static com.iconbet.score.daolette.Daolette.*;

import java.math.BigInteger;
import java.nio.channels.MulticastChannel;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
public class TreasuryTest extends TestBase{
    private static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    private static final Address daoFund = Address.fromString("cx0000000000000000000000000000000000000001");
    private static final Address gameScore = Address.fromString("cx0000000000000000000000000000000000000002");
    private static final Address tapToken = Address.fromString("cx0000000000000000000000000000000000000003");
    private static final Address gameAuth = Address.fromString("cx0000000000000000000000000000000000000004");
    private static final Address dividends = Address.fromString("cx0000000000000000000000000000000000000005");
    private static final Address ibpnp = Address.fromString("cx0000000000000000000000000000000000000006");
    private static final Address rewards = Address.fromString("cx0000000000000000000000000000000000000006");

    private static final Address dice = Address.fromString("cx0000000000000000000000000000000000000007");
    private static final Address roulette = Address.fromString("cx0000000000000000000000000000000000000008");
    private static final Address blackjack = Address.fromString("cx0000000000000000000000000000000000000009");
    public static final String TAG = "AUTHORIZATION";

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
    private Score treasury;
    private final SecureRandom secureRandom = new SecureRandom();
    private static MockedStatic<Context> contextMock;


    Daolette scoreSpy;

    @BeforeEach
    public void setup() throws Exception {
        treasury = sm.deploy(owner, Daolette.class);
        Daolette instance = (Daolette) treasury.getInstance();
        scoreSpy = spy(instance);
        treasury.setInstance(scoreSpy);
        long currentTime = System.currentTimeMillis() / 1000L;
        sm.getBlock().increase(currentTime / 2);
        contextMock.reset();
    }

    @BeforeAll
    public static void init(){
        contextMock = Mockito.mockStatic(Context.class, CALLS_REAL_METHODS);
    }

    @Test
    void toggleExcessSmoothing(){
        treasury.invoke(owner, "toggleExcessSmoothing");

        assertEquals(true, treasury.call("get_excess_smoothing_status"));
    }

    @Test
    void setTokenScore(){
        treasury.invoke(owner, "setTokenScore", tapToken);
        assertEquals(tapToken, treasury.call("get_token_score"));
    }

    @Test
    void setIbpnpScore(){
        treasury.invoke(owner, "setIbpnpScore", ibpnp);
        assertEquals(ibpnp, treasury.call("get_ibpnp_score"));
    }

    @Test
    void setRewardsScore(){
        treasury.invoke(owner, "setRewardsScore", rewards);
        assertEquals(rewards, treasury.call("get_rewards_score"));
    }

    @Test
    void setDividendScore(){
        treasury.invoke(owner, "setDividendsScore", dividends);
        assertEquals(dividends, treasury.call("get_dividends_score"));
    }

    @Test
    void setGameAuthScore(){
        treasury.invoke(owner, "setGameAuthScore", gameAuth);
        assertEquals(gameAuth, treasury.call("get_game_auth_score"));
    }

    @Test
    void setDaofundScore(){
        treasury.invoke(owner, "setDaofundScore", daoFund);
        assertEquals(daoFund, treasury.call("get_daofund_score"));
    }

    @Test
    void setTreasury(){
        contextMock.when(() -> Context.getValue()).thenReturn(BigInteger.valueOf(10000).multiply(decimal));
        treasury.invoke(owner, "set_treasury");
        BigInteger defaultTreasuryMin = new BigInteger("250000000000000000000000");
        assertEquals(defaultTreasuryMin.add(BigInteger.valueOf(10000).multiply(decimal)), treasury.call("get_treasury_min"));
        assertEquals(false, treasury.call("get_treasury_status"));
    }

    @Test
    void getTreasuryBalance(){
        contextMock.when(() -> Context.getBalance(treasury.getAddress())).thenReturn(BigInteger.valueOf(10000).multiply(decimal));
        assertEquals(BigInteger.valueOf(10000).multiply(decimal), treasury.call("getTreasuryBalance"));
    }

    @DisplayName("called from the approved games")
    @Test
    void take_wager(){
        setGameAuthScore();
        setIbpnpScore();
        setRewardsScore();
        setDividendScore();
        toggleExcessSmoothing();
        contextMock.when(() -> Context.getCaller()).thenReturn(dice);
        contextMock.when(() -> Context.getOrigin()).thenReturn(owner.getAddress());
        contextMock.when(() -> Context.getBlockTimestamp()).thenReturn(86400000000L);
        contextMock.when(() -> Context.getTransactionTimestamp()).thenReturn(0L);
        contextMock.when(() -> Context.transfer(eq(dividends), any())).thenAnswer((Answer<Void>) invocation -> null);

        doReturn("gameApproved").when(scoreSpy).callScore(eq(String.class), eq(gameAuth), eq("get_game_status"), eq(dice));
        doReturn(true).when(scoreSpy).callScore(eq(Boolean.class), eq(ibpnp), eq("hasIBPNPProfile"), eq(owner.getAddress()));
        doReturn(BigInteger.valueOf(10).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(gameAuth), eq("record_excess"));

        Map<String, String> yesterdaysGameExcess = Map.of(dice.toString(), BigInteger.valueOf(100).multiply(decimal).toString());
        doReturn(yesterdaysGameExcess).when(scoreSpy).callScore(eq(Map.class), eq(gameAuth), eq("get_yesterdays_games_excess"));

        doReturn(true).when(scoreSpy).callScore(eq(Boolean.class), eq(rewards), eq("rewards_dist_complete"));
        doReturn(true).when(scoreSpy).callScore(eq(Boolean.class), eq(dividends), eq("dividends_dist_complete"));

        doNothing().when(scoreSpy).callScore(eq(gameAuth), eq("accumulate_daily_wagers"), eq(dice), eq(BigInteger.valueOf(10).multiply(decimal)));
        doNothing().when(scoreSpy).callScore(eq(rewards), eq("accumulate_wagers"), eq(owner.getAddress().toString()), eq(BigInteger.valueOf(10).multiply(decimal)), any());

        doNothing().when(scoreSpy).callScore(eq(ibpnp), eq("addGameData"), any());

        treasury.invoke(owner, "take_wager", BigInteger.valueOf(10).multiply(decimal));

        /*
        Remarks:
        Tested with the values in the score itself
         */
    }

    @Test
    void wagerPayout(){
        setGameAuthScore();
        setIbpnpScore();
        contextMock.when(() -> Context.getCaller()).thenReturn(dice);
        contextMock.when(() -> Context.getOrigin()).thenReturn(owner.getAddress());
        contextMock.when(() -> Context.transfer(eq(owner.getAddress()), any())).thenAnswer((Answer<Void>) invocation -> null);

        doReturn("gameApproved").when(scoreSpy).callScore(eq(String.class), eq(gameAuth), eq("get_game_status"), eq(dice));
        doReturn(true).when(scoreSpy).callScore(eq(Boolean.class), eq(ibpnp), eq("hasIBPNPProfile"), eq(owner.getAddress()));
        doReturn(true).when(scoreSpy).callScore(eq(Boolean.class), eq(gameAuth), eq("accumulate_daily_payouts"), eq(dice), any());

        doNothing().when(scoreSpy).callScore(eq(ibpnp), eq("addGameData"), any());

        treasury.invoke(owner, "wager_payout", BigInteger.valueOf(10).multiply(decimal));
    }

}

package com.iconbet.score.ibpnp;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import score.Address;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import score.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import static com.iconbet.score.ibpnp.IBPNP.GameData;

public class IBPNPTest extends TestBase {


    private static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    private static final Address treasuryScore = Address.fromString("cx0000000000000000000000000000000000000001");
    private static final Address rewardsScore = Address.fromString("cx0000000000000000000000000000000000000002");
    private static final Address tapTokenScore = Address.fromString("cx0000000000000000000000000000000000000003");


    private static final String TAG = "IconBet Player NFT Profile";

    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account testingAccount = sm.createAccount();
    private static final Account testingAccount1 = sm.createAccount();

    public static final BigInteger decimal = new BigInteger("1000000000000000000");

    private Score IBPNPScore;
    private final SecureRandom secureRandom = new SecureRandom();

    IBPNP scoreSpy;

    @BeforeEach
    public void setup() throws Exception {
        IBPNPScore = sm.deploy(owner, IBPNP.class, TAG, "IBPNP");
        IBPNP instance = (IBPNP) IBPNPScore.getInstance();
        scoreSpy = spy(instance);
        IBPNPScore.setInstance(scoreSpy);
    }

    @Test
    void name() {
        assertEquals(TAG, IBPNPScore.call("name"));
    }

    @Test
    void symbol() {
        assertEquals("IBPNP", IBPNPScore.call("symbol"));
    }

    @Test
    void totalSupply() {
        assertEquals(0, IBPNPScore.call("totalSupply"));
    }

    @Test
    void setTreasuryScore() {
        IBPNPScore.invoke(owner, "setTreasuryScore", treasuryScore);
        assertEquals(treasuryScore, IBPNPScore.call("getTreasuryScore"));
    }

    private void setTreasuryScoreMethod(){
        IBPNPScore.invoke(owner, "setTreasuryScore", treasuryScore);
    }

    @Test
    void setTapTokenScore() {
        IBPNPScore.invoke(owner, "setTapTokenScore", tapTokenScore);
        assertEquals(tapTokenScore, IBPNPScore.call("getTapTokenScore"));
    }

    @Test
    void setRewardsScore() {
        setRewardsScoreMethod();
        assertEquals(rewardsScore, IBPNPScore.call("getRewardsScore"));
    }

    private void setRewardsScoreMethod() {
        IBPNPScore.invoke(owner, "setRewardsScore", rewardsScore);
    }

    @Test
    void setTreasuryScoreNotOwner() {
        Executable setTreasuryScoreNotOwner = () -> setTreasuryScoreExceptions();
        expectErrorMessage(setTreasuryScoreNotOwner, TAG + ": Only owner can call this method");
    }

    private void setTreasuryScoreExceptions() {
        try {
            IBPNPScore.invoke(testingAccount, "setTreasuryScore", treasuryScore);
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    void setTapTokenScoreNotOwner() {
        Executable setTapTokenScoreNotOwner = () -> setTapTokenScoreExceptions();
        expectErrorMessage(setTapTokenScoreNotOwner, TAG + ": Only owner can call this method");
    }

    private void setTapTokenScoreExceptions() {
        try {
            IBPNPScore.invoke(testingAccount, "setTapTokenScore", tapTokenScore);
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    void setRewardsScoreNotOwner() {
        Executable setRewardsScoreNotOwner = () -> setRewardsScoreExceptions();
        expectErrorMessage(setRewardsScoreNotOwner, TAG + ": Only owner can call this method");
    }


    private void setRewardsScoreExceptions() {
        try {
            IBPNPScore.invoke(testingAccount, "setRewardsScore", rewardsScore);
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    void transfer() {
        Executable transfer = () -> transferException();
        expectErrorMessage(transfer, "Reverted(0): " + TAG + ": Transfer is not allowed.");
    }

    private void transferException() {
        try {
            IBPNPScore.invoke(owner, "transfer", testingAccount.getAddress(), BigInteger.ONE);
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    void transferFrom() {
        Executable transfer = () -> transferFromException();
        expectErrorMessage(transfer, "Reverted(0): " + TAG + ": Transfer is not allowed.");
    }

    private void transferFromException() {
        try {
            IBPNPScore.invoke(owner, "transferFrom", owner.getAddress(), testingAccount.getAddress(), BigInteger.ONE);
        } catch (Exception e) {
            throw e;
        }
    }

    public void expectErrorMessage(Executable contractCall, String errorMessage) {
        AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
        assertEquals(errorMessage, e.getMessage());
    }

    @Test
    void createIBPNP() {
        setRewardsScoreMethod();
        IBPNPScore.invoke(owner, "createIBPNP", "testingAccount1");
        assertEquals(1, IBPNPScore.call("totalSupply"));
        assertEquals(owner.getAddress().toString(), IBPNPScore.call("getWalletByUsername", "testingAccount1"));
        @SuppressWarnings("unchecked")
        List<String> usernames = (List<String>) IBPNPScore.call("getUsernames");
        assertEquals("testingaccount1", usernames.get(0));
        BigInteger value = BigInteger.valueOf(1000);
        doReturn(value).when(scoreSpy).callScore(eq(BigInteger.class), any(), eq("get_expected_rewards"), eq(owner.getAddress().toString()));
        Map<String, Object> userData = Map.ofEntries(
                Map.entry("amount_won", BigInteger.ZERO),
                Map.entry("username", "testingAccount1"),
                Map.entry("tokenId", BigInteger.ONE),
                Map.entry("largest_bet", BigInteger.ZERO),
                Map.entry("bets_won", 0),
                Map.entry("wager_level", 0),
                Map.entry("bets_lost", 0),
                Map.entry("wallet_address", "hx0000000000000000000000000000000000000001"),
                Map.entry("linked_wallet", ""),
                Map.entry("amount_lost", BigInteger.ZERO),
                Map.entry("amount_wagered", BigInteger.ZERO),
                Map.entry("daily_earning", BigInteger.valueOf(1000)));

        //noinspection unchecked
        assertEquals(userData, (Map<String, Object>) IBPNPScore.call("getUserData", owner.getAddress()));
        assertEquals(Boolean.TRUE, IBPNPScore.call("hasIBPNPProfile", owner.getAddress()));
        assertEquals(Boolean.TRUE, IBPNPScore.call("can_request_to_another_wallet", owner.getAddress()));
        assertEquals(Boolean.FALSE, IBPNPScore.call("has_alternate_wallet", owner.getAddress()));
        assertEquals(owner.getAddress().toString(), IBPNPScore.call("getWalletByUsername", "testingAccount1"));
    }

    @Test
    void requestLinkingWallet() {
        IBPNPScore.invoke(owner, "createIBPNP", "testingAccount1");
        IBPNPScore.invoke(testingAccount, "createIBPNP", "testingAccount2");
        IBPNPScore.invoke(owner, "requestLinkingWallet", testingAccount.getAddress(), "ICONex");
        Map<String, Object> senderWalletLinkData = Map.of(
                "wallet_type", "",
                "requested_block", BigInteger.valueOf(Context.getBlockTimestamp()),
                "request_status", "_pending",
                "requested_wallet", "hx0000000000000000000000000000000000000002");

        Map<String, Object> receiverWalletLinkData = Map.of(
                "wallet_type", "ICONex",
                "requested_block", BigInteger.valueOf(Context.getBlockTimestamp()),
                "request_status", "_pending",
                "requested_wallet", "hx0000000000000000000000000000000000000001");

        assertEquals(senderWalletLinkData, IBPNPScore.call("getLinkWalletStatus", owner.getAddress()));
        assertEquals(receiverWalletLinkData, IBPNPScore.call("getLinkWalletStatus", testingAccount.getAddress()));
        assertEquals(Boolean.FALSE, IBPNPScore.call("can_request_to_another_wallet", owner.getAddress()));
        assertEquals(Boolean.FALSE, IBPNPScore.call("has_alternate_wallet", owner.getAddress()));
        assertEquals(Boolean.FALSE, IBPNPScore.call("has_alternate_wallet", testingAccount.getAddress()));
        assertEquals("hx0000000000000000000000000000000000000001", IBPNPScore.call("get_requesting_wallet_address", testingAccount.getAddress()));
    }

    private void requestLinkWalletMethod(){
        IBPNPScore.invoke(owner, "createIBPNP", "testingAccount1");
        IBPNPScore.invoke(testingAccount, "createIBPNP", "testingAccount2");
        IBPNPScore.invoke(owner, "requestLinkingWallet", testingAccount.getAddress(), "ICONex");
    }

    @Test
    void respondToLinkRequest(){
        requestLinkWalletMethod();
        IBPNPScore.invoke(testingAccount, "respondToLinkRequest", owner.getAddress(), "_approve");

        @SuppressWarnings("unchecked")
        Map<String, Object> senderWalletLinkData = (Map<String, Object>) IBPNPScore.call("getLinkWalletStatus", owner.getAddress());
        @SuppressWarnings("unchecked")
        Map<String, Object> receiverWalletLinkData = (Map<String, Object>) IBPNPScore.call("getLinkWalletStatus", testingAccount.getAddress());

        assertEquals(senderWalletLinkData.get("request_status"), "_approve");
        assertEquals(receiverWalletLinkData.get("request_status"), "_approve");
        assertEquals(Boolean.FALSE, IBPNPScore.call("can_request_to_another_wallet", owner.getAddress()));
        assertEquals(Boolean.FALSE, IBPNPScore.call("can_request_to_another_wallet", testingAccount.getAddress()));
        assertEquals("", IBPNPScore.call("get_requesting_wallet_address", testingAccount.getAddress()));
        assertEquals(testingAccount.getAddress().toString(), IBPNPScore.call("get_alternate_wallet_address", owner.getAddress()));
        assertEquals(owner.getAddress().toString(), IBPNPScore.call("get_alternate_wallet_address", testingAccount.getAddress()));
        assertEquals(Boolean.TRUE, IBPNPScore.call("has_alternate_wallet", owner.getAddress()));
        assertEquals(Boolean.TRUE, IBPNPScore.call("has_alternate_wallet", testingAccount.getAddress()));

        doReturn(BigInteger.valueOf(1000)).when(scoreSpy).callScore(eq(BigInteger.class), any(), eq("get_expected_rewards"), any());
        @SuppressWarnings("unchecked")
        Map<String, Object> senderUserData = (Map<String, Object>) IBPNPScore.call("getUserData", owner.getAddress());
        @SuppressWarnings("unchecked")
        Map<String, Object> receiverUserData = (Map<String, Object>) IBPNPScore.call("getUserData", testingAccount.getAddress());

        assertEquals(testingAccount.getAddress().toString(), senderUserData.get("linked_wallet"));
        assertEquals(owner.getAddress().toString(), receiverUserData.get("linked_wallet"));
    }

    private void respondToLinkRequestMethod(){
        requestLinkWalletMethod();
        IBPNPScore.invoke(testingAccount, "respondToLinkRequest", owner.getAddress(), "_approve");
    }

    @Test
    void unLinkWallets(){
        respondToLinkRequestMethod();
        IBPNPScore.invoke(testingAccount, "unlinkWallets");
        @SuppressWarnings("unchecked")
        Map<String, Object> senderWalletData = (Map<String, Object>) IBPNPScore.call("getLinkWalletStatus", owner.getAddress());
        @SuppressWarnings("unchecked")
        Map<String, Object> receiverWalletData = (Map<String, Object>) IBPNPScore.call("getLinkWalletStatus", testingAccount.getAddress());

        assertEquals("", senderWalletData.get("requested_wallet"));
        assertEquals("", receiverWalletData.get("requested_wallet"));

        assertEquals("_unlinked", senderWalletData.get("request_status"));
        assertEquals("_unlinked", receiverWalletData.get("request_status"));

        doReturn(BigInteger.valueOf(1000)).when(scoreSpy).callScore(eq(BigInteger.class), any(), eq("get_expected_rewards"), any());
        @SuppressWarnings("unchecked")
        Map<String, Object> senderUserData = (Map<String, Object>) IBPNPScore.call("getUserData", owner.getAddress());
        @SuppressWarnings("unchecked")
        Map<String, Object> receiverUserData = (Map<String, Object>) IBPNPScore.call("getUserData", testingAccount.getAddress());
        assertEquals("", senderUserData.get("linked_wallet"));
        assertEquals("", receiverUserData.get("linked_wallet"));
    }

    @Test
    void changeUsername(){
        IBPNPScore.invoke(owner, "createIBPNP", "testingAccount1");
        IBPNPScore.invoke(testingAccount, "createIBPNP", "testingAccount2");

        IBPNPScore.invoke(owner, "changeUsername", "testingAccount3");

        assertEquals(owner.getAddress().toString(), IBPNPScore.call("getWalletByUsername", "testingAccount3"));
        assertEquals("testingaccount3", IBPNPScore.call("getUsernameByWallet", owner.getAddress()));

        doReturn(BigInteger.valueOf(1000)).when(scoreSpy).callScore(eq(BigInteger.class), any(), eq("get_expected_rewards"), any());
        @SuppressWarnings("unchecked")
        Map<String, Object> senderUserData = (Map<String, Object>) IBPNPScore.call("getUserData", owner.getAddress());
        assertEquals("testingaccount3", senderUserData.get("username"));

        IBPNPScore.invoke(testingAccount, "changeUsername", "testingAccount1");
        @SuppressWarnings("unchecked")
        List<String> usernames = (List<String>) IBPNPScore.call("getUsernames");
        assertEquals(List.of("testingaccount3", "testingaccount1"), usernames);
    }

    @Test
    void addGameData(){
        GameData gameData = new GameData();
        gameData.game_amount_lost = BigInteger.TEN;
        gameData.game_bets_lost = 1;
        gameData.game_amount_wagered = BigInteger.TEN;
        gameData.game_bets_won = 1;
        gameData.wallet_address = owner.getAddress();
        gameData.remarks = "take_wager";
        gameData.game_largest_bet = BigInteger.TEN;
        gameData.game_amount_won = BigInteger.TEN;
        gameData.game_wager_level = BigInteger.ZERO;
        setTreasuryScoreMethod();

        IBPNPScore.invoke(owner, "createIBPNP", "testingAccount1");

        IBPNPScore.invoke(owner,"addGameData", gameData);

        doReturn(BigInteger.valueOf(1000)).when(scoreSpy).callScore(eq(BigInteger.class), any(), eq("get_expected_rewards"), any());
        @SuppressWarnings("unchecked")
        Map<String, Object> senderUserData = (Map<String, Object>) IBPNPScore.call("getUserData", owner.getAddress());

        Map<String, Object> expectedSenderUserData = Map.ofEntries(
                Map.entry("amount_won", BigInteger.ZERO),
                Map.entry("username", "testingAccount1"),
                Map.entry("tokenId", BigInteger.ONE),
                Map.entry("largest_bet", BigInteger.TEN),
                Map.entry("bets_won", 0),
                Map.entry("wager_level", 0),
                Map.entry("bets_lost", 1),
                Map.entry("wallet_address", "hx0000000000000000000000000000000000000001"),
                Map.entry("linked_wallet", ""),
                Map.entry("amount_lost", BigInteger.TEN),
                Map.entry("amount_wagered", BigInteger.TEN),
                Map.entry("daily_earning", BigInteger.valueOf(1000))
        );

        assertEquals(expectedSenderUserData, senderUserData);

        gameData.game_amount_lost = BigInteger.TEN;
        gameData.game_bets_lost = 1;
        gameData.game_amount_wagered = BigInteger.TEN;
        gameData.game_bets_won = 1;
        gameData.wallet_address = owner.getAddress();
        gameData.remarks = "wager_payout";
        gameData.game_largest_bet = BigInteger.TEN;
        gameData.game_amount_won = BigInteger.TEN;
        gameData.game_wager_level = BigInteger.ZERO;

        IBPNPScore.invoke(owner,"addGameData", gameData);
        doReturn(BigInteger.valueOf(1000)).when(scoreSpy).callScore(eq(BigInteger.class), any(), eq("get_expected_rewards"), any());
        senderUserData = (Map<String, Object>) IBPNPScore.call("getUserData", owner.getAddress());
        expectedSenderUserData = Map.ofEntries(
                Map.entry("amount_won", BigInteger.TEN),
                Map.entry("username", "testingAccount1"),
                Map.entry("tokenId", BigInteger.ONE),
                Map.entry("largest_bet", BigInteger.TEN),
                Map.entry("bets_won", 1),
                Map.entry("wager_level", 0),
                Map.entry("bets_lost", 0),
                Map.entry("wallet_address", "hx0000000000000000000000000000000000000001"),
                Map.entry("linked_wallet", ""),
                Map.entry("amount_lost", BigInteger.ZERO),
                Map.entry("amount_wagered", BigInteger.TEN),
                Map.entry("daily_earning", BigInteger.valueOf(1000))
        );
        assertEquals(expectedSenderUserData, senderUserData);
    }

}

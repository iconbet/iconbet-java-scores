package com.iconbet.score.authorization;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
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


import static com.iconbet.score.authorization.utils.Consts.MULTIPLIER;
import static com.iconbet.score.authorization.utils.Consts.TAG;
import static org.mockito.Mockito.*;

import score.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.math.BigInteger;
import java.nio.channels.MulticastChannel;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

class AuthorizationTest extends TestBase {

	private static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
	private static final Address daoFund = Address.fromString("cx0000000000000000000000000000000000000001");
	private static final Address gameScore = Address.fromString("cx0000000000000000000000000000000000000002");
	private static final Address tapToken = Address.fromString("cx0000000000000000000000000000000000000003");
	private static final Address gameAuth = Address.fromString("cx0000000000000000000000000000000000000004");
	private static final Address dividends = Address.fromString("cx0000000000000000000000000000000000000005");
	private static final Address utap = Address.fromString("cx0000000000000000000000000000000000000006");
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
	private Score authorization;
	private final SecureRandom secureRandom = new SecureRandom();
	private static MockedStatic<Context> contextMock;


	Authorization scoreSpy;

	@BeforeEach
	public void setup() throws Exception {
		authorization = sm.deploy(owner, Authorization.class);
		Authorization instance = (Authorization) authorization.getInstance();
		scoreSpy = spy(instance);
		authorization.setInstance(scoreSpy);
		long currentTime = System.currentTimeMillis() / 1000L;
		sm.getBlock().increase(currentTime / 2);
	}

	@BeforeAll
	public static void init(){
		contextMock = Mockito.mockStatic(Context.class, CALLS_REAL_METHODS);
	}

	public void expectErrorMessage(Executable contractCall, String errorMessage) {
		AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
		assertEquals(errorMessage, e.getMessage());
	}

	@Test
	void setScores(){
		authorization.invoke(owner, "set_roulette_score", gameScore);
		authorization.invoke(owner, "set_tap_token_score", tapToken);
		authorization.invoke(owner, "set_dividend_distribution_score", dividends);
		authorization.invoke(owner, "set_rewards_score", rewards);
		authorization.invoke(owner, "set_utap_token_score", utap);

		assertEquals(authorization.call("get_utap_token_score"), utap);
		assertEquals(authorization.call("get_rewards_score"), rewards);
		assertEquals(authorization.call("get_dividend_distribution"), dividends);
		assertEquals(authorization.call("get_tap_token_score"), tapToken);
		assertEquals(authorization.call("get_roulette_score"), gameScore);
	}

	private void setScoresMethod(){
		authorization.invoke(owner, "set_roulette_score", gameScore);
		authorization.invoke(owner, "set_tap_token_score", tapToken);
		authorization.invoke(owner, "set_dividend_distribution_score", dividends);
		authorization.invoke(owner, "set_rewards_score", rewards);
		authorization.invoke(owner, "set_utap_token_score", utap);
	}

	@Test
	void setScoresNotOwner(){
		Executable setScoresNotOwner = () -> authorization.invoke(testingAccount, "set_roulette_score", gameScore);
		expectErrorMessage(setScoresNotOwner, "Reverted(0): " + TAG + ": Only owner can call this method");
		setScoresNotOwner = () -> authorization.invoke(testingAccount, "set_tap_token_score", tapToken);
		expectErrorMessage(setScoresNotOwner, "Reverted(0): " + TAG + ": Only owner can call this method");
		setScoresNotOwner = () -> authorization.invoke(testingAccount, "set_dividend_distribution_score", dividends);
		expectErrorMessage(setScoresNotOwner, "Reverted(0): " + TAG + ": Only owner can call this method");
		setScoresNotOwner = () -> authorization.invoke(testingAccount, "set_rewards_score", rewards);
		expectErrorMessage(setScoresNotOwner, "Reverted(0): " + TAG + ": Only owner can call this method");
		setScoresNotOwner = () -> authorization.invoke(testingAccount, "set_utap_token_score", utap);
		expectErrorMessage(setScoresNotOwner, "Reverted(0): " + TAG + ": Only owner can call this method");
	}

	@Test
	void setSuperAdmin(){
		authorization.invoke(owner, "set_super_admin", testingAccount.getAddress());
		assertEquals(authorization.call("get_super_admin"), testingAccount.getAddress());
		assertEquals(List.of(testingAccount.getAddress()), authorization.call("get_admin"));
	}

	@Test
	void setSetSuperAdminNotOwner(){
		Executable setSetSuperAdminNotOwner = () -> authorization.invoke(testingAccount, "set_super_admin", testingAccount.getAddress());
		expectErrorMessage(setSetSuperAdminNotOwner, "Reverted(0): " + TAG + ": Only owner can call this method");
	}

	@Test
	void setAdmin(){
		authorization.invoke(owner, "set_super_admin", testingAccount.getAddress());
		authorization.invoke(testingAccount, "set_admin", owner.getAddress());
		assertEquals(authorization.call("get_super_admin"), testingAccount.getAddress());
		assertEquals(List.of(testingAccount.getAddress(), owner.getAddress()), authorization.call("get_admin"));
	}

	@Test
	void setAdminNotSuperAdmin(){
		Executable setAdminNotSuperAdmin = () -> authorization.invoke(testingAccount, "set_admin", owner.getAddress());
		expectErrorMessage(setAdminNotSuperAdmin, "Reverted(0): " + TAG + ": Only super admin can call this method.");
	}

	@Test
	void removeAdmin(){
		authorization.invoke(owner, "set_super_admin", testingAccount.getAddress());
		authorization.invoke(testingAccount, "set_admin", owner.getAddress());

		authorization.invoke(testingAccount, "remove_admin", owner.getAddress());
		assertEquals(List.of(testingAccount.getAddress()), authorization.call("get_admin"));
	}

	@Test
	void removeAdminNotSuperAdmin(){
		authorization.invoke(owner, "set_super_admin", testingAccount.getAddress());
		authorization.invoke(testingAccount, "set_admin", owner.getAddress());

		Executable removeAdminNotSuperAdmin = () -> authorization.invoke(owner, "remove_admin", owner.getAddress());
		expectErrorMessage(removeAdminNotSuperAdmin, "Reverted(0): " + TAG + ": Only super admin can call this method.");
	}

	@Test
	void setNewDivChangingTime(){
		List<Address> approvedGames = List.of(dice, roulette, blackjack);
		doReturn(approvedGames).when(scoreSpy).get_approved_games();
		authorization.invoke(owner, "set_new_div_changing_time", BigInteger.ONE);
		assertEquals(authorization.call("get_new_div_changing_time"), BigInteger.ONE);
		Map<String, String> todaysExcess = Map.of(dice.toString(), "0",
				roulette.toString(), "0",
				blackjack.toString(), "0");
		assertEquals(todaysExcess, authorization.call("get_todays_games_excess"));
	}

	@Test
	void setGameDevelopersShare(){
		authorization.invoke(owner, "set_game_developers_share", BigInteger.TEN);
		assertEquals(authorization.call("get_game_developers_share"), BigInteger.TEN);
	}

	@Test
	void defineGameConceptVote(){
		defineGameConceptVoteMethod();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> proposalDetails = (List<Map<String, Object>>) authorization.call("getGameConceptProposals", 20, 0);
		System.out.println(proposalDetails);
		assertEquals(proposalDetails.get(0).get("quorum"), MULTIPLIER.divide(BigInteger.valueOf(100)));
		assertEquals(proposalDetails.get(0).get("proposer"), testingAccount.getAddress());
		assertEquals(proposalDetails.get(0).get("name"), "dice");
		assertEquals(proposalDetails.get(0).get("description"), "diceGame");
		assertEquals(proposalDetails.get(0).get("status"), "active");
	}

	private void defineGameConceptVoteMethod(){
		setScoresMethod();
		doReturn(BigInteger.valueOf(100000000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalSupply"));
		doReturn(BigInteger.valueOf(1000000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), eq(rewards));
		doReturn(BigInteger.valueOf(20000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("staked_balanceOf"), eq(testingAccount.getAddress()));
		doReturn(BigInteger.valueOf(400000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("total_staked_balance"));
		doReturn(BigInteger.valueOf(10000000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(utap), eq("tradingTokenContractbalanceOf"));
		doReturn(BigInteger.valueOf(10000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(utap), eq("balanceOf"), eq(testingAccount.getAddress()));
		doReturn(BigInteger.valueOf(100000000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(utap), eq("totalSupply"));
		doReturn(BigInteger.valueOf(1000000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalStakedBalanceOfAt"), any());
		doReturn(Boolean.TRUE).when(scoreSpy).callScore(eq(Boolean.class), eq(tapToken), eq("getSnapshotEnabled"));
		doReturn(BigInteger.TEN).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("getTimeOffset"));

		authorization.invoke(owner, "set_super_admin", owner.getAddress());
		authorization.invoke(owner, "startGovernance", BigInteger.valueOf(10000), 1, 1, 1);
		doReturn(BigInteger.valueOf(10)).when(scoreSpy).getDay();
		BigInteger day = (BigInteger) authorization.call("getDay");
		authorization.invoke(testingAccount, "defineGameConceptVote", "dice", "diceGame", "diceipfs", day.add(BigInteger.TEN), day.add(BigInteger.ONE));
	}
	@Test
	void castGameConceptVote(){
		defineGameConceptVoteMethod();
		doReturn(BigInteger.valueOf(10000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("stakedBalanceOfAt"), eq(testingAccount.getAddress()), any());
		doReturn(BigInteger.valueOf(1000000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalStakedBalanceOfAt"), any());
		authorization.invoke(testingAccount,"castGameConceptVote", "dice", Boolean.TRUE);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> proposalDetails = (List<Map<String, Object>>) authorization.call("getGameConceptProposals", 20, 0);
		System.out.println(proposalDetails);
		assertEquals(proposalDetails.get(0).get("for"), MULTIPLIER.divide(BigInteger.valueOf(100)));

		@SuppressWarnings("unchecked")
		Map<String, Object> checkGameConceptVote = (Map<String, Object>) authorization.call("checkGameConceptVote", "dice");
		assertEquals(checkGameConceptVote.get("status"), "active");

		doReturn(MULTIPLIER).when(scoreSpy).getDay();
		//noinspection unchecked
		checkGameConceptVote = (Map<String, Object>) authorization.call("checkGameConceptVote", "dice");
		System.out.println(checkGameConceptVote);
		assertEquals(checkGameConceptVote.get("status"), "succeeded");
	}

	private void castGameConceptVoteApproveMethod(){
		defineGameConceptVoteMethod();
		doReturn(BigInteger.valueOf(10000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("stakedBalanceOfAt"), eq(testingAccount.getAddress()), any());
		doReturn(BigInteger.valueOf(1000000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalStakedBalanceOfAt"), any());
		authorization.invoke(testingAccount,"castGameConceptVote", "dice", Boolean.TRUE);
		doReturn(MULTIPLIER).when(scoreSpy).getDay();
	}

	@Test
	void submitGameProposal(){
		castGameConceptVoteApproveMethod();
		authorization.invoke(owner, "set_super_admin", owner.getAddress());
		authorization.invoke(owner, "set_maximum_loss", MULTIPLIER);
		authorization.invoke(owner, "toggle_apply_watch_dog_method");
		JsonObject gameSubmitData = new JsonObject();
		gameSubmitData.add("name", "dice");
		gameSubmitData.add("scoreAddress", dice.toString());
		gameSubmitData.add("minBet", MULTIPLIER.toString());
		gameSubmitData.add("maxBet", BigInteger.TEN.multiply(MULTIPLIER).toString());
		gameSubmitData.add("houseEdge", "10");
		gameSubmitData.add("gameType", "Per wager settlement");
		gameSubmitData.add("revShareMetadata", "Reward Distribution");
		gameSubmitData.add("revShareWalletAddress", owner.getAddress().toString());
		gameSubmitData.add("linkProofPage", "link");
		gameSubmitData.add("gameUrlMainnet", dice.toString());
		gameSubmitData.add("gameUrlTestnet", dice.toString());
		gameSubmitData.add("maxPayout", BigInteger.valueOf(100).multiply(MULTIPLIER).toString());

		System.out.println("hello: " + gameSubmitData.toString());

		doReturn(testingAccount.getAddress()).when(scoreSpy).callScore(eq(Address.class), eq(dice), eq("get_score_owner"));
		authorization.invoke(testingAccount, "submit_game_proposal", gameSubmitData.toString());

		assertEquals(authorization.call("get_game_status", dice), "waiting");

		authorization.invoke(owner, "approveGameProposal", "dice");
		assertEquals(authorization.call("get_game_status", dice), "proposalApproved");
	}

	private void submitGameProposalsProposalApproved(){
		castGameConceptVoteApproveMethod();
		authorization.invoke(owner, "set_super_admin", owner.getAddress());
		authorization.invoke(owner, "set_maximum_loss", MULTIPLIER);
		authorization.invoke(owner, "toggle_apply_watch_dog_method");
		JsonObject gameSubmitData = new JsonObject();
		gameSubmitData.add("name", "dice");
		gameSubmitData.add("scoreAddress", dice.toString());
		gameSubmitData.add("minBet", MULTIPLIER.toString());
		gameSubmitData.add("maxBet", BigInteger.TEN.multiply(MULTIPLIER).toString());
		gameSubmitData.add("houseEdge", "10");
		gameSubmitData.add("gameType", "Per wager settlement");
		gameSubmitData.add("revShareMetadata", "Reward Distribution");
		gameSubmitData.add("revShareWalletAddress", owner.getAddress().toString());
		gameSubmitData.add("linkProofPage", "link");
		gameSubmitData.add("gameUrlMainnet", dice.toString());
		gameSubmitData.add("gameUrlTestnet", dice.toString());
		gameSubmitData.add("maxPayout", BigInteger.valueOf(100).multiply(MULTIPLIER).toString());

		doReturn(testingAccount.getAddress()).when(scoreSpy).callScore(eq(Address.class), eq(dice), eq("get_score_owner"));
		authorization.invoke(testingAccount, "submit_game_proposal", gameSubmitData.toString());

		authorization.invoke(owner, "approveGameProposal", "dice");
	}

	@Test
	void defineGameApprovalsVote(){
		submitGameProposalsProposalApproved();

		doReturn(BigInteger.valueOf(100000000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalSupply"));
		doReturn(BigInteger.valueOf(1000000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), eq(rewards));
		doReturn(BigInteger.valueOf(20000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("staked_balanceOf"), eq(testingAccount.getAddress()));
		doReturn(BigInteger.valueOf(10000000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(utap), eq("tradingTokenContractbalanceOf"));
		doReturn(BigInteger.valueOf(10000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(utap), eq("balanceOf"), eq(testingAccount.getAddress()));
		doReturn(BigInteger.valueOf(100000000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(utap), eq("totalSupply"));
		doReturn(Boolean.TRUE).when(scoreSpy).callScore(eq(Boolean.class), eq(tapToken), eq("get_snapshot_enabled"));
		doReturn(BigInteger.TEN).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("get_time_offset"));

		authorization.invoke(owner, "set_super_admin", owner.getAddress());
		authorization.invoke(owner, "startGovernance", BigInteger.valueOf(10000), 1, 1, 1);
//		doReturn(BigInteger.valueOf(Context.getBlockTimestamp()).subtract(BigInteger.ONE).divide(new BigInteger("86400000000")).add(BigInteger.valueOf(500))).when(scoreSpy).getDay();

		BigInteger day = (BigInteger) authorization.call("getDay");
		authorization.invoke(testingAccount, "defineGameApprovalVote", "dice", "diceGame", "diceipfs", day.add(BigInteger.TEN), day.add(BigInteger.ONE));

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> proposalDetails = (List<Map<String, Object>>) authorization.call("getGameApprovalProposals", 20, 0);
		System.out.println(proposalDetails);
		assertEquals(proposalDetails.get(0).get("status"), "active");
	}

	private void defineGameApprovalsVoteMethod(){
		submitGameProposalsProposalApproved();

		doReturn(BigInteger.valueOf(100000000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalSupply"));
		doReturn(BigInteger.valueOf(1000000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("balanceOf"), eq(rewards));
		doReturn(BigInteger.valueOf(20000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("staked_balanceOf"), eq(testingAccount.getAddress()));
		doReturn(BigInteger.valueOf(10000000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(utap), eq("tradingTokenContractbalanceOf"));
		doReturn(BigInteger.valueOf(10000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(utap), eq("balanceOf"), eq(testingAccount.getAddress()));
		doReturn(BigInteger.valueOf(100000000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(utap), eq("totalSupply"));
		doReturn(Boolean.TRUE).when(scoreSpy).callScore(eq(Boolean.class), eq(tapToken), eq("get_snapshot_enabled"));
		doReturn(BigInteger.TEN).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("get_time_offset"));

		authorization.invoke(owner, "set_super_admin", owner.getAddress());
		authorization.invoke(owner, "startGovernance", BigInteger.valueOf(10000), 1, 1, 1);
//		doReturn(BigInteger.valueOf(Context.getBlockTimestamp()).subtract(BigInteger.ONE).divide(new BigInteger("86400000000")).add(BigInteger.valueOf(500))).when(scoreSpy).getDay();

		BigInteger day = (BigInteger) authorization.call("getDay");
		authorization.invoke(testingAccount, "defineGameApprovalVote", "dice", "diceGame", "diceipfs", day.add(BigInteger.TEN), day.add(BigInteger.ONE));
	}

	@Test
	void castGameApprovalsVote(){
		defineGameApprovalsVoteMethod();
		doReturn(BigInteger.valueOf(10000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("stakedBalanceOfAt"), eq(testingAccount.getAddress()), any());
		doReturn(BigInteger.valueOf(1000000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalStakedBalanceOfAt"), any());
		authorization.invoke(testingAccount,"castGameApprovalVote", "dice", Boolean.TRUE);
		doReturn(MULTIPLIER.add(MULTIPLIER)).when(scoreSpy).getDay();

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> proposalDetails = (List<Map<String, Object>>) authorization.call("getGameApprovalProposals", 20, 0);
		System.out.println(proposalDetails);
		assertEquals(proposalDetails.get(0).get("for"), MULTIPLIER.divide(BigInteger.valueOf(100)));
		assertEquals(proposalDetails.get(0).get("status"), "succeeded");
	}

	private void castGameApprovalVoteMethod(){
		defineGameApprovalsVoteMethod();
		doReturn(BigInteger.valueOf(10000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("stakedBalanceOfAt"), eq(testingAccount.getAddress()), any());
		doReturn(BigInteger.valueOf(1000000).multiply(MULTIPLIER)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("totalStakedBalanceOfAt"), any());
		authorization.invoke(testingAccount,"castGameApprovalVote", "dice", Boolean.TRUE);
		doReturn(MULTIPLIER.add(MULTIPLIER)).when(scoreSpy).getDay();
	}

	@Test
	void startOfficialReview(){
		castGameApprovalVoteMethod();
		authorization.invoke(owner, "setOfficialReviewCost", BigInteger.valueOf(10));
		JsonObject gameReviewData = new JsonObject();
		gameReviewData.add("method", "_startOfficialReview");
		JsonObject params = new JsonObject();
		params.add("game", "dice");
		gameReviewData.add("params", params);
		contextMock.when(() -> Context.getCaller()).thenReturn(tapToken);
		authorization.invoke(owner, "tokenFallback", testingAccount.getAddress(), BigInteger.valueOf(10).multiply(MULTIPLIER), gameReviewData.toString().getBytes());
		assertEquals(authorization.call("getGameStatus", "dice"), "gameReady");
	}
}

class GameMetadata{
	private String name;
	private String scoreAddress;
	private String minBet;
	private String maxBet;
	private String houseEdge;
	private String gameType;
	private String revShareMetadata;
	private String revShareWalletAddress;
	private String linkProofPage;
	private String gameUrlMainnet;
	private String gameUrlTestnet;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getScoreAddress() {
		return scoreAddress;
	}
	public void setScoreAddress(String scoreAddress) {
		this.scoreAddress = scoreAddress;
	}
	public String getMinBet() {
		return minBet;
	}
	public void setMinBet(String minBet) {
		this.minBet = minBet;
	}
	public String getMaxBet() {
		return maxBet;
	}
	public void setMaxBet(String maxBet) {
		this.maxBet = maxBet;
	}
	public String getHouseEdge() {
		return houseEdge;
	}
	public void setHouseEdge(String houseEdge) {
		this.houseEdge = houseEdge;
	}
	public String getGameType() {
		return gameType;
	}
	public void setGameType(String gameType) {
		this.gameType = gameType;
	}
	public String getRevShareMetadata() {
		return revShareMetadata;
	}
	public void setRevShareMetadata(String revShareMetadata) {
		this.revShareMetadata = revShareMetadata;
	}
	public String getRevShareWalletAddress() {
		return revShareWalletAddress;
	}
	public void setRevShareWalletAddress(String revShareWalletAddress) {
		this.revShareWalletAddress = revShareWalletAddress;
	}
	public String getLinkProofPage() {
		return linkProofPage;
	}
	public void setLinkProofPage(String linkProofPage) {
		this.linkProofPage = linkProofPage;
	}
	public String getGameUrlMainnet() {
		return gameUrlMainnet;
	}
	public void setGameUrlMainnet(String gameUrlMainnet) {
		this.gameUrlMainnet = gameUrlMainnet;
	}
	public String getGameUrlTestnet() {
		return gameUrlTestnet;
	}
	public void setGameUrlTestnet(String gameUrlTestnet) {
		this.gameUrlTestnet = gameUrlTestnet;
	}
}
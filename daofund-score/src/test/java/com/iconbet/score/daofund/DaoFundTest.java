package com.iconbet.score.daofund;

import com.eclipsesource.json.JsonObject;
import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import score.Address;

import static org.mockito.Mockito.*;
import org.mockito.stubbing.Answer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import score.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.math.BigInteger;
import java.security.SecureRandom;
import scorex.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DaoFundTest extends TestBase {
    private static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    public static final Address SYSTEM_ADDRESS = Address.fromString("cx0000000000000000000000000000000000000000");
    private static final Address proposalsSubmission = Address.fromString("cx0000000000000000000000000000000000000001");
    private static final Address proposalsFund = Address.fromString("cx0000000000000000000000000000000000000002");

    public static final String TAG = "ICONbet DAOfund";

    private static final BigInteger X_6 = new BigInteger("1000000"); // 10 ** 6

    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account testingAccount = sm.createAccount();
    private static final Account testingAccount1 = sm.createAccount();
    private static final Account faucet = sm.createAccount(1000);

    public static final BigInteger decimal = new BigInteger("1000000000000000000");

    private Score DAOFundScore;
    private final SecureRandom secureRandom = new SecureRandom();

    DaoFund scoreSpy;
    private static MockedStatic<Context> contextMock;

    @BeforeEach
    public void setup() throws Exception {
        DAOFundScore = sm.deploy(owner, DaoFund.class, false);
        DaoFund instance = (DaoFund) DAOFundScore.getInstance();
        scoreSpy = spy(instance);
        DAOFundScore.setInstance(scoreSpy);
    }

    @BeforeAll
    public static void init(){
        contextMock = Mockito.mockStatic(Context.class, Mockito.CALLS_REAL_METHODS);
    }

    @Test
    void name() {
        assertEquals(TAG, DAOFundScore.call("name"));
    }

    @Test
    void addAdmin(){
        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
        DAOFundScore.invoke(owner, "add_admin", testingAccount.getAddress());
        @SuppressWarnings("unchecked")
        List<Address> admins = (List<Address>) DAOFundScore.call("get_admins");
        assertEquals(testingAccount.getAddress(), admins.get(0));
    }

    @Test
    void addAdminNotOwner(){
        contextMock.when(() -> Context.getCaller()).thenReturn(testingAccount.getAddress());
        Executable addAdminNotOwner = () -> DAOFundScore.invoke(testingAccount, "add_admin", testingAccount.getAddress());
        expectErrorMessage(addAdminNotOwner, "Reverted(0): " + TAG + ": Only owners can set new admins.");
    }

    @Test
    void addAdminDuplicateAdmin(){
        DAOFundScore.invoke(owner, "add_admin", testingAccount.getAddress());
        Executable addAdminDuplicateAdmin = () -> DAOFundScore.invoke(owner, "add_admin", testingAccount.getAddress());
        expectErrorMessage(addAdminDuplicateAdmin, "Reverted(0): " + TAG + ":  " + testingAccount.getAddress() + " is already on admin list.");
    }

    @Test
    void removeAdmin(){
        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
        DAOFundScore.invoke(owner, "add_admin", testingAccount.getAddress());
        @SuppressWarnings("unchecked")
        List<Address> admins = (List<Address>) DAOFundScore.call("get_admins");
        assertEquals(testingAccount.getAddress(), admins.get(0));
        DAOFundScore.invoke(owner, "remove_admin", testingAccount.getAddress());
        //noinspection unchecked
        admins = (List<Address>) DAOFundScore.call("get_admins");
        assertEquals(0, admins.size());
    }


    @Test
    void removeAdminNotOwner(){
        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
        DAOFundScore.invoke(owner, "add_admin", testingAccount.getAddress());
        List<Address> admins = (List<Address>) DAOFundScore.call("get_admins");
        assertEquals(testingAccount.getAddress(), admins.get(0));
        contextMock.when(() -> Context.getCaller()).thenReturn(testingAccount.getAddress());
        Executable removeAdminNotOwner = () -> DAOFundScore.invoke(testingAccount, "remove_admin", testingAccount.getAddress());
        expectErrorMessage(removeAdminNotOwner, "Reverted(0): " + TAG + ": Only admins can remove admins.");
    }

    @Test
    void removeOwnerAsAdmin(){
        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
        DAOFundScore.invoke(owner, "add_admin", owner.getAddress());
        List<Address> admins = (List<Address>) DAOFundScore.call("get_admins");
        assertEquals(owner.getAddress(), admins.get(0));
        Executable removeAdminNotOwner = () -> DAOFundScore.invoke(owner, "remove_admin", owner.getAddress());
        expectErrorMessage(removeAdminNotOwner, "Reverted(0): " + TAG + ": Owner address cannot be removed from the admins list.");
    }

    @Test
    void removeAdminNotInAdminList(){
        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
        DAOFundScore.invoke(owner, "add_admin", owner.getAddress());
        List<Address> admins = (List<Address>) DAOFundScore.call("get_admins");
        assertEquals(owner.getAddress(), admins.get(0));
        Executable removeAdminNotOwner = () -> DAOFundScore.invoke(owner, "remove_admin", testingAccount.getAddress());
        expectErrorMessage(removeAdminNotOwner, "Reverted(0): " + TAG + ": "+ testingAccount.getAddress() + " not in Admins List");
    }

    @Test
    void withdrawFund(){
        DAOFundScore.invoke(owner, "add_admin", owner.getAddress());
        contextMock.when(() -> Context.getBalance(any())).thenReturn(BigInteger.valueOf(1000));
        contextMock.when(() -> Context.transfer(any(), eq(BigInteger.TEN))).thenAnswer((Answer<Void>) invocation -> null);
        DAOFundScore.invoke(owner, "withdraw_fund", testingAccount.getAddress(), BigInteger.TEN, "withdrawn by owner to testingAccount");
        assertEquals(1, DAOFundScore.call("get_withdraw_count"));
        Map<String, String> withdrawRecord = Map.of(
                "withdraw_address", "hx0000000000000000000000000000000000000002",
                "withdraw_timestamp", String.valueOf(BigInteger.valueOf(Context.getBlockTimestamp()).divide(X_6)),
                "withdraw_reason", "withdrawn by owner to testingAccount",
                "withdraw_amount", BigInteger.TEN.toString());

        assertEquals(withdrawRecord, DAOFundScore.call("get_withdraw_record_by_index", 1));
    }

    private void withdrawFundMethod(){
        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
        DAOFundScore.invoke(owner, "add_admin", owner.getAddress());
        contextMock.when(() -> Context.getBalance(any())).thenReturn(BigInteger.valueOf(1000));
        contextMock.when(() -> Context.transfer(any(), any())).thenAnswer((Answer<Void>) invocation -> null);
    }

    @Test
    void withdrawFundCallerNotAdmin(){
        Executable withdrawFundCallerNotAdmin = () -> DAOFundScore.invoke(owner, "withdraw_fund", testingAccount.getAddress(), BigInteger.TEN, "withdrawn by owner to testingAccount");
        expectErrorMessage(withdrawFundCallerNotAdmin, "Reverted(0): " + TAG + ": Only admins can run this method.");
    }

    @Test
    void withdrawFundNotEnoughBalance(){
        DAOFundScore.invoke(owner, "add_admin", owner.getAddress());
        Executable withdrawFundNotEnoughBalance = () -> DAOFundScore.invoke(owner, "withdraw_fund", testingAccount.getAddress(), BigInteger.TEN, "withdrawn by owner to testingAccount");
        expectErrorMessage(withdrawFundNotEnoughBalance, "Reverted(0): " + TAG + ": Not Enough balance. Available Balance =" + BigInteger.ZERO.toString());
    }

    @Test
    void getWithDrawRecords(){
        withdrawFundMethod();
        DAOFundScore.invoke(owner, "withdraw_fund", testingAccount.getAddress(), BigInteger.TEN, "withdrawn by owner to " + testingAccount.getAddress().toString());
        DAOFundScore.invoke(owner, "withdraw_fund", testingAccount1.getAddress(), BigInteger.valueOf(100), "withdrawn by owner to " + testingAccount1.getAddress().toString());
        @SuppressWarnings("unchecked")
        List<Map<String, ?>> withdrawRecords = (List<Map<String, ?>>) DAOFundScore.call("get_withdraw_records", 1, 5);
        Map<String, ?> withdrawRecord_0 = Map.of(
                "withdraw_address", "hx0000000000000000000000000000000000000002",
                "withdraw_timestamp", String.valueOf(BigInteger.valueOf(Context.getBlockTimestamp()).divide(X_6)),
                "withdraw_memo", "withdrawn by owner to hx0000000000000000000000000000000000000002",
                "withdraw_amount", BigInteger.TEN.toString());

        Map<String, ?> withdrawRecord_1 = Map.of(
                "withdraw_address", "hx0000000000000000000000000000000000000003",
                "withdraw_timestamp", String.valueOf(BigInteger.valueOf(Context.getBlockTimestamp()).divide(X_6)),
                "withdraw_memo", "withdrawn by owner to hx0000000000000000000000000000000000000003",
                "withdraw_amount", BigInteger.valueOf(100).toString());

        assertEquals(withdrawRecords.get(0).get("withdraw_address"), withdrawRecord_0.get("withdraw_address"));
        assertEquals(withdrawRecords.get(0).get("withdraw_memo"), withdrawRecord_0.get("withdraw_memo"));
        assertEquals(withdrawRecords.get(0).get("withdraw_amount"), withdrawRecord_0.get("withdraw_amount"));

        assertEquals(withdrawRecords.get(1).get("withdraw_address"), withdrawRecord_1.get("withdraw_address"));
        assertEquals(withdrawRecords.get(1).get("withdraw_memo"), withdrawRecord_1.get("withdraw_memo"));
        assertEquals(withdrawRecords.get(1).get("withdraw_amount"), withdrawRecord_1.get("withdraw_amount"));
    }

    @Test
    void setScores(){
        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
        DAOFundScore.invoke(owner, "set_proposals_submission_score", proposalsSubmission);
        DAOFundScore.invoke(owner, "set_proposals_fund_score", proposalsFund);
        assertEquals(proposalsSubmission, DAOFundScore.call("get_proposals_submission_score"));
        assertEquals(proposalsFund, DAOFundScore.call("get_proposals_fund_score"));
    }

    private void setScoresMethod(){
        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
        DAOFundScore.invoke(owner, "set_proposals_submission_score", proposalsSubmission);
        DAOFundScore.invoke(owner, "set_proposals_fund_score", proposalsFund);
    }

    @Test
    void setScoresNotOwner(){
        contextMock.when(() -> Context.getCaller()).thenReturn(testingAccount.getAddress());
        Executable proposalsSubmissionScore = () -> DAOFundScore.invoke(testingAccount, "set_proposals_submission_score", proposalsSubmission);
        expectErrorMessage(proposalsSubmissionScore, "Reverted(0): " + TAG + ": Only owner can call this method.");

        Executable proposalsFundScore = () -> DAOFundScore.invoke(testingAccount, "set_proposals_fund_score", proposalsFund);
        expectErrorMessage(proposalsFundScore, "Reverted(0): " + TAG + ": Only owner can call this method.");
    }

    @Test
    void setScoresNotScoreAddress(){
        contextMock.when(() -> Context.getCaller()).thenReturn(owner.getAddress());
        Executable proposalsSubmissionScore = () -> DAOFundScore.invoke(owner, "set_proposals_submission_score", testingAccount.getAddress());
        expectErrorMessage(proposalsSubmissionScore, "Reverted(0): " + TAG + ": Target " + testingAccount.getAddress() + " is not a SCORE");

        Executable proposalsFundScore = () -> DAOFundScore.invoke(owner, "set_proposals_fund_score", testingAccount.getAddress());
        expectErrorMessage(proposalsFundScore, "Reverted(0): " + TAG + ": Target " + testingAccount.getAddress() + " is not a SCORE");
    }

    @Test
    void transfer_proposal_fund_to_proposals_fund(){
        transferProposalFundMethod();
        @SuppressWarnings("unchecked")
        List<Map<String, ?>> withdrawRecords = (List<Map<String, ?>>) DAOFundScore.call("get_withdraw_records", 1, 5);
        System.out.println(withdrawRecords);
        assertEquals(withdrawRecords.get(0).get("withdraw_memo"), "Proposal: Proposal 1");
        assertEquals(withdrawRecords.get(0).get("withdraw_address"), proposalsFund.toString());
        assertEquals(withdrawRecords.get(0).get("withdraw_amount"), BigInteger.TEN.toString());
    }

    private void transferProposalFundMethod(){
        setScoresMethod();
        JsonObject depositParameters = new JsonObject();
        depositParameters.add("ipfs_hash", "Proposal 1");
        depositParameters.add("project_duration", 3);
        depositParameters.add("proposer_address", testingAccount.getAddress().toString());
        depositParameters.add("total_budget", BigInteger.TEN.toString());

        contextMock.when(() -> Context.getCaller()).thenReturn(proposalsSubmission);
        contextMock.when(() -> Context.getBalance(any())).thenReturn(BigInteger.valueOf(1000));
        contextMock.when(() -> Context.call(eq(BigInteger.TEN), eq(proposalsFund), eq("depositProposalFund"), eq(depositParameters.toString().getBytes()))).thenAnswer((Answer<Void>) invocation -> null);
        DAOFundScore.invoke(owner, "transfer_proposal_fund_to_proposals_fund", "Proposal 1", 3, testingAccount.getAddress(), BigInteger.TEN);

    }

    @Test
    void transfer_proposal_fund_to_proposals_fund_already_added_proposal(){
        transferProposalFundMethod();
        Executable proposalExists = () -> DAOFundScore.invoke(owner, "transfer_proposal_fund_to_proposals_fund", "Proposal 1", 3, testingAccount.getAddress(), BigInteger.TEN);
        expectErrorMessage(proposalExists, "Reverted(0): ICONbet DAOfund: IPFS key already Exists");
    }

    @Test
    public void update_proposal_fund(){
        setScoresMethod();
        JsonObject depositParameters = new JsonObject();
        depositParameters.add("ipfs_hash", "Proposal 1");
        depositParameters.add("project_duration", 3);
        depositParameters.add("proposer_address", testingAccount.getAddress().toString());
        depositParameters.add("total_budget", BigInteger.TEN.toString());

        contextMock.when(() -> Context.getCaller()).thenReturn(proposalsSubmission);
        contextMock.when(() -> Context.getBalance(any())).thenReturn(BigInteger.valueOf(1000));
        contextMock.when(() -> Context.call(eq(BigInteger.TEN), eq(proposalsFund), eq("depositProposalFund"), eq(depositParameters.toString().getBytes()))).thenAnswer((Answer<Void>) invocation -> null);
        DAOFundScore.invoke(owner, "transfer_proposal_fund_to_proposals_fund", "Proposal 1", 3, testingAccount.getAddress(), BigInteger.TEN);

        contextMock.when(() -> Context.call(eq(BigInteger.TEN), eq(proposalsFund), eq("update_proposal_fund"), eq("Proposal 1"), eq(BigInteger.TEN), eq(2))).thenAnswer((Answer<Void>) invocation -> null);

        DAOFundScore.invoke(owner, "update_proposal_fund", "Proposal 1", BigInteger.TEN, 2);
    }

    @Test
    public void disqualify_proposal_fund(){
        setScoresMethod();
        JsonObject depositParameters = new JsonObject();
        depositParameters.add("ipfs_hash", "Proposal 1");
        depositParameters.add("project_duration", 3);
        depositParameters.add("proposer_address", testingAccount.getAddress().toString());
        depositParameters.add("total_budget", BigInteger.TEN.toString());
        contextMock.when(() -> Context.getCaller()).thenReturn(proposalsSubmission);
        contextMock.when(() -> Context.getBalance(any())).thenReturn(BigInteger.valueOf(1000));
        contextMock.when(() -> Context.call(eq(BigInteger.TEN), eq(proposalsFund), eq("depositProposalFund"), eq(depositParameters.toString().getBytes()))).thenAnswer((Answer<Void>) invocation -> null);
        DAOFundScore.invoke(owner, "transfer_proposal_fund_to_proposals_fund", "Proposal 1", 3, testingAccount.getAddress(), BigInteger.TEN);

        contextMock.when(() -> Context.getCaller()).thenReturn(proposalsFund);
        contextMock.when(() -> Context.getValue()).thenReturn(BigInteger.TEN);
        contextMock.when(() -> Context.call(eq(BigInteger.TEN), eq(SYSTEM_ADDRESS), eq("burn"))).thenAnswer((Answer<Void>) invocation -> null);
        DAOFundScore.invoke(owner, "disqualify_proposal_fund", "Proposal 1");
        Map<String, Object> proposalDetailsMap = (Map<String, Object>) DAOFundScore.call("get_proposal_details", 0, 5);
        List<Map<String, Object>> proposalDataList = (List<Map<String, Object>>) proposalDetailsMap.get("data");
        assertEquals(BigInteger.ZERO, proposalDataList.get(0).get("_budget_transfer"));
        assertEquals("Proposal 1", proposalDataList.get(0).get("_ipfs_hash"));
    }

    public void expectErrorMessage(Executable contractCall, String errorMessage) {
        AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
        assertEquals(errorMessage, e.getMessage());
    }
}


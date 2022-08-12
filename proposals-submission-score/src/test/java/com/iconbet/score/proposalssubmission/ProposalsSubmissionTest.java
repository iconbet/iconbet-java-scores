package com.iconbet.score.proposalssubmission;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import score.Address;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.iconbet.score.proposalssubmission.db.ProposalData.ProposalAttributes;
import com.iconbet.score.proposalssubmission.db.ProgressReportData.ProgressReportAttributes;


import static com.iconbet.score.proposalssubmission.utils.consts.*;
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

public class ProposalsSubmissionTest extends TestBase{
    private static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    private static final Address daoFund = Address.fromString("cx0000000000000000000000000000000000000001");
    private static final Address proposalsFund = Address.fromString("cx0000000000000000000000000000000000000002");
    private static final Address tapToken = Address.fromString("cx0000000000000000000000000000000000000003");
    public static final String TAG = "ICONbet Proposal Submission";

    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account testingAccount = sm.createAccount();
    private static final Account testingAccount1 = sm.createAccount();

    public static final BigInteger decimal = new BigInteger("1000000000000000000");

    private Score ProposalsSubmissionScore;
    private final SecureRandom secureRandom = new SecureRandom();
    private static MockedStatic<Context> contextMock;


    ProposalsSubmission scoreSpy;

    @BeforeEach
    public void setup() throws Exception {
        ProposalsSubmissionScore = sm.deploy(owner, ProposalsSubmission.class);
        ProposalsSubmission instance = (ProposalsSubmission) ProposalsSubmissionScore.getInstance();
        scoreSpy = spy(instance);
        ProposalsSubmissionScore.setInstance(scoreSpy);
        long currentTime = System.currentTimeMillis() / 1000L;
        sm.getBlock().increase(currentTime / 2);
    }

    @BeforeAll
    public static void init(){
        contextMock = Mockito.mockStatic(Context.class, Mockito.CALLS_REAL_METHODS);
    }

    @Test
    void name(){
        assertEquals(ProposalsSubmissionScore.call("name"), TAG);
    }

    @Test
    void addAdmin(){
        ProposalsSubmissionScore.invoke(owner, "addAdmin", testingAccount.getAddress());
        //noinspection unchecked
        assertEquals(testingAccount.getAddress(), ((List<Address>)ProposalsSubmissionScore.call("getAdmins")).get(0));
    }

    @Test
    void addAdminNotOwner(){
        Executable addAdminNotOwner = () -> ProposalsSubmissionScore.invoke(testingAccount, "addAdmin", testingAccount.getAddress());
        expectErrorMessage(addAdminNotOwner, "Reverted(0): " + "Only owner can call this method");
    }

    @Test
    void addAdminAlreadyInAdminList(){
        ProposalsSubmissionScore.invoke(owner, "addAdmin", testingAccount.getAddress());
        Executable addAdminNotOwner = () -> ProposalsSubmissionScore.invoke(owner, "addAdmin", testingAccount.getAddress());
        expectErrorMessage(addAdminNotOwner, "Reverted(0): " + "Address already an admin");
    }

    @Test
    void removeAdmin(){
        ProposalsSubmissionScore.invoke(owner, "addAdmin", testingAccount.getAddress());
        //noinspection unchecked
        assertEquals(testingAccount.getAddress(), ((List<Address>)ProposalsSubmissionScore.call("getAdmins")).get(0));
        ProposalsSubmissionScore.invoke(owner, "removeAdmin", testingAccount.getAddress());
        //noinspection unchecked
        assertEquals(0, ((List<Address>)ProposalsSubmissionScore.call("getAdmins")).size());
    }

    @Test
    void removeAdminNotOwner(){
        Executable removeAdminNotOwner = () -> ProposalsSubmissionScore.invoke(testingAccount, "removeAdmin", testingAccount.getAddress());
        expectErrorMessage(removeAdminNotOwner, "Reverted(0): " + "Only owner can call this method");
    }

    @Test
    void removeOwnerFromAdmin(){
        Executable removeAdminNotOwner = () -> ProposalsSubmissionScore.invoke(owner, "removeAdmin", owner.getAddress());
        expectErrorMessage(removeAdminNotOwner, "Reverted(0): " + "Owner cannot be removed from admin list");
    }

    @Test
    void removeAdminAddressNotAdmin(){
        Executable removeAdminNotOwner = () -> ProposalsSubmissionScore.invoke(owner, "removeAdmin", testingAccount.getAddress());
        expectErrorMessage(removeAdminNotOwner, "Reverted(0): " + "The provided address could not be removed from admin list. Check if the address is in admin list.");
    }

    public void expectErrorMessage(Executable contractCall, String errorMessage) {
        AssertionError e = Assertions.assertThrows(AssertionError.class, contractCall);
        assertEquals(errorMessage, e.getMessage());
    }

    @Test
    void setScores(){
        ProposalsSubmissionScore.invoke(owner, "addAdmin", testingAccount.getAddress());
        ProposalsSubmissionScore.invoke(testingAccount, "setDaoFundScore", daoFund);
        ProposalsSubmissionScore.invoke(testingAccount, "setTapTokenScore", tapToken);
        ProposalsSubmissionScore.invoke(testingAccount, "setProposalsFundScore", proposalsFund);
        assertEquals(daoFund, ProposalsSubmissionScore.call("getDaoFundScore"));
        assertEquals(tapToken, ProposalsSubmissionScore.call("getTapTokenScore"));
        assertEquals(proposalsFund, ProposalsSubmissionScore.call("getProposalsFundScore"));
    }

    private void setScoresMethod(){
        ProposalsSubmissionScore.invoke(owner, "addAdmin", testingAccount.getAddress());
        ProposalsSubmissionScore.invoke(testingAccount, "setDaoFundScore", daoFund);
        ProposalsSubmissionScore.invoke(testingAccount, "setTapTokenScore", tapToken);
        ProposalsSubmissionScore.invoke(testingAccount, "setProposalsFundScore", proposalsFund);
    }

    @Test
    void setScoresNotScoreAddresses(){
        ProposalsSubmissionScore.invoke(owner, "addAdmin", testingAccount.getAddress());

        Executable setScoresNotAdmin = () -> ProposalsSubmissionScore.invoke(testingAccount, "setDaoFundScore", testingAccount1.getAddress());
        expectErrorMessage(setScoresNotAdmin, "Reverted(0): " + "The given address is not a contract address");
        setScoresNotAdmin = () -> ProposalsSubmissionScore.invoke(testingAccount, "setTapTokenScore", testingAccount1.getAddress());
        expectErrorMessage(setScoresNotAdmin, "Reverted(0): " + "The given address is not a contract address");
        setScoresNotAdmin = () -> ProposalsSubmissionScore.invoke(testingAccount, "setProposalsFundScore", testingAccount1.getAddress());
        expectErrorMessage(setScoresNotAdmin, "Reverted(0): " + "The given address is not a contract address");
    }

    @Test
    void setScoresNotAdmin(){
        Executable setScoresNotAdmin = () -> ProposalsSubmissionScore.invoke(testingAccount, "setDaoFundScore", daoFund);
        expectErrorMessage(setScoresNotAdmin, "Reverted(0): " + "Only admin can call this method.");
        setScoresNotAdmin = () -> ProposalsSubmissionScore.invoke(testingAccount, "setTapTokenScore", tapToken);
        expectErrorMessage(setScoresNotAdmin, "Reverted(0): " + "Only admin can call this method.");
        setScoresNotAdmin = () -> ProposalsSubmissionScore.invoke(testingAccount, "setProposalsFundScore", proposalsFund);
        expectErrorMessage(setScoresNotAdmin, "Reverted(0): " + "Only admin can call this method.");
    }

    @Test
    void submitProposal(){
        submitProposalMethod();
        ProposalAttributes proposalAttributes = new ProposalAttributes();
        proposalAttributes.ipfsHash = "Proposal 1";
        proposalAttributes.projectDuration = 3;
        proposalAttributes.totalBudget = BigInteger.valueOf(100).multiply(decimal);
        proposalAttributes.projectTitle = "Proposal 1 Title";
        proposalAttributes.ipfsLink = "ipfs_link";
        proposalAttributes.projectType = "type";
        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDetails = (Map<String, ?>) ProposalsSubmissionScore.call("getProposalDetailsByHash", proposalAttributes.ipfsHash);

        assertEquals(proposalDetails.get("project_title"), proposalAttributes.projectTitle);
        assertEquals(proposalDetails.get("project_type"), proposalAttributes.projectType);
        assertEquals(proposalDetails.get("budget_adjustment"), Boolean.FALSE);
        assertEquals(proposalDetails.get("proposer_address"), owner.getAddress());
        assertEquals(proposalDetails.get("ipfs_hash"), proposalAttributes.ipfsHash);
        assertEquals(proposalDetails.get("total_budget"), proposalAttributes.totalBudget);
        assertEquals(proposalDetails.get("submit_progress_report"), Boolean.FALSE);
        assertEquals(proposalDetails.get("status"), "_pending");
        assertEquals(proposalDetails.get("project_duration"), proposalAttributes.projectDuration);
        assertEquals(List.of(proposalAttributes.ipfsHash), ProposalsSubmissionScore.call("getProposalsKeysByStatus", "_pending"));

        Map<String, ?> proposalDetailsMap = (Map<String, ?>) ProposalsSubmissionScore.call("getProposalDetails", "_pending", owner.getAddress(), 0, 5);
        List<Map<String, ?>> detailList = (List<Map<String, ?>>) proposalDetailsMap.get("DATA");
        assertEquals(proposalDetails, detailList.get(0));

        assertEquals(List.of(proposalDetails), ProposalsSubmissionScore.call("getProposalDetailByWallet", owner.getAddress()));

        Map<String, Map<String, ?>> projectAmounts = (Map<String, Map<String, ?>>) ProposalsSubmissionScore.call("getProjectAmounts");

        assertEquals(projectAmounts.get("_pending").get("_count"), 1);
        assertEquals(projectAmounts.get("_pending").get("_total_amount"), proposalAttributes.totalBudget);

        assertEquals(List.of(owner.getAddress()), ProposalsSubmissionScore.call("getProposers", 0, 5));
    }

    private void submitProposalMethod(){
        setScoresMethod();
        ProposalAttributes proposalAttributes = new ProposalAttributes();
        proposalAttributes.ipfsHash = "Proposal 1";
        proposalAttributes.projectDuration = 3;
        proposalAttributes.totalBudget = BigInteger.valueOf(100).multiply(decimal);
        proposalAttributes.projectTitle = "Proposal 1 Title";
        proposalAttributes.ipfsLink = "ipfs_link";
        proposalAttributes.projectType = "type";

        ProposalsSubmissionScore.invoke(owner, "setInitialBlock");
        doReturn(BigInteger.valueOf(10000).multiply(decimal)).when(scoreSpy).getRemainingFund();
        doReturn(BigInteger.valueOf(100000).multiply(decimal)).when(scoreSpy).callScore(eq(BigInteger.class), eq(tapToken), eq("staked_balanceOf"), any());
        contextMock.when(() -> Context.getTransactionHash()).thenReturn("Proposal 1".getBytes());
        ProposalsSubmissionScore.invoke(owner, "submitProposal", proposalAttributes);
    }

    @Test
    void voteProposalReject(){
        submitProposalMethod();
        ProposalsSubmissionScore.invoke(owner, "change_period");

        ProposalsSubmissionScore.invoke(testingAccount, "voteProposal", "Proposal 1", "_reject", "Reason");
        ProposalsSubmissionScore.invoke(owner, "voteProposal", "Proposal 1", "_reject", "Reason");

        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDetails = (Map<String, ?>) ProposalsSubmissionScore.call("getProposalDetailsByHash", "Proposal 1");
        assertEquals(proposalDetails.get("total_votes"), BigInteger.valueOf(200000).multiply(decimal));
        assertEquals(proposalDetails.get("approve_voters"), 0);
        assertEquals(proposalDetails.get("total_voters"), 2);
        assertEquals(proposalDetails.get("rejected_votes"), BigInteger.valueOf(200000).multiply(decimal));
        assertEquals(proposalDetails.get("reject_voters"), 2);
        assertEquals(proposalDetails.get("approved_votes"), BigInteger.ZERO);
    }

    @Test
    void voteProposalApprove(){
        submitProposalMethod();
        ProposalsSubmissionScore.invoke(owner, "change_period");

        ProposalsSubmissionScore.invoke(testingAccount, "voteProposal", "Proposal 1", "_approve", "Reason");
        ProposalsSubmissionScore.invoke(owner, "voteProposal", "Proposal 1", "_approve", "Reason");

        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDetails = (Map<String, ?>) ProposalsSubmissionScore.call("getProposalDetailsByHash", "Proposal 1");
        assertEquals(proposalDetails.get("total_votes"), BigInteger.valueOf(200000).multiply(decimal));
        assertEquals(proposalDetails.get("approve_voters"), 2);
        assertEquals(proposalDetails.get("total_voters"), 2);
        assertEquals(proposalDetails.get("approved_votes"), BigInteger.valueOf(200000).multiply(decimal));
        assertEquals(proposalDetails.get("reject_voters"), 0);
        assertEquals(proposalDetails.get("rejected_votes"), BigInteger.ZERO);
    }

    @Test
    void voteProposalApplicationPeriod(){
        submitProposalMethod();

        Executable voteProposalApplicationPeriod = () -> ProposalsSubmissionScore.invoke(testingAccount, "voteProposal", "Proposal 1", "_reject", "Reason");
        expectErrorMessage(voteProposalApplicationPeriod, "Reverted(0): " + TAG + " Proposals can be voted on Voting period only.");
    }

    @Test
    void submitProgressReport(){
        submitProposalMethod();
        ProgressReportAttributes progressReportAttributes = new ProgressReportAttributes();
        progressReportAttributes.ipfsHash = "Proposal 1";
        progressReportAttributes.reportHash = "Report 1";
        progressReportAttributes.progressReportTitle = "Report 1 Title";
        progressReportAttributes.ipfsLink = "link";
        progressReportAttributes.budgetAdjustment = true;
        progressReportAttributes.additionalBudget = BigInteger.TEN.multiply(decimal);
        progressReportAttributes.percentageCompleted = 10;
        progressReportAttributes.additionalMonth = 2;

        ProposalsSubmissionScore.invoke(owner, "submitProgressReport", progressReportAttributes);
        Map<String, ?> progressReportsDetails = (Map<String, ?>) ProposalsSubmissionScore.call("getProgressReportDetailsByHash", "Report 1");
        assertEquals(progressReportsDetails.get("ipfs_hash"), progressReportAttributes.ipfsHash);
        assertEquals(progressReportsDetails.get("progress_report_title"), progressReportAttributes.progressReportTitle);
        assertEquals(progressReportsDetails.get("report_hash"), progressReportAttributes.reportHash);
        assertEquals(progressReportsDetails.get("status"), "_waiting");
        assertEquals(progressReportsDetails.get("budget_adjustment"), progressReportAttributes.budgetAdjustment);
        assertEquals(progressReportsDetails.get("additional_month"), progressReportAttributes.additionalMonth);
        assertEquals(progressReportsDetails.get("additional_budget"), progressReportAttributes.additionalBudget);

        Map<String, ?> reportDetailsMap = (Map<String, ?>) ProposalsSubmissionScore.call("getProgressReportDetails", "_waiting", 0, 5);
        List<Map<String, ?>> reportDetailsList = (List<Map<String, ?>>) reportDetailsMap.get("DATA");
        assertEquals(progressReportsDetails, reportDetailsList.get(0));
        assertEquals(reportDetailsMap.get("COUNT"), 1);

        assertEquals(List.of(progressReportAttributes.reportHash), ProposalsSubmissionScore.call("getProgressReportKeysByStatus", "_waiting"));
    }

    private void submitProgressReportMethod(){
        submitProposalMethod();
        ProgressReportAttributes progressReportAttributes = new ProgressReportAttributes();
        progressReportAttributes.ipfsHash = "Proposal 1";
        progressReportAttributes.reportHash = "Report 1";
        progressReportAttributes.progressReportTitle = "Report 1 Title";
        progressReportAttributes.ipfsLink = "link";
        progressReportAttributes.budgetAdjustment = true;
        progressReportAttributes.additionalBudget = BigInteger.TEN.multiply(decimal);
        progressReportAttributes.percentageCompleted = 10;
        progressReportAttributes.additionalMonth = 2;

        ProposalsSubmissionScore.invoke(owner, "submitProgressReport", progressReportAttributes);
    }

    private void submitProgressReportWithoutSubmittingProposalMethod(){
        ProgressReportAttributes progressReportAttributes = new ProgressReportAttributes();
        progressReportAttributes.ipfsHash = "Proposal 1";
        progressReportAttributes.reportHash = "Report 1";
        progressReportAttributes.progressReportTitle = "Report 1 Title";
        progressReportAttributes.ipfsLink = "link";
        progressReportAttributes.budgetAdjustment = true;
        progressReportAttributes.additionalBudget = BigInteger.TEN.multiply(decimal);
        progressReportAttributes.percentageCompleted = 10;
        progressReportAttributes.additionalMonth = 2;

        ProposalsSubmissionScore.invoke(owner, "submitProgressReport", progressReportAttributes);
    }

    private void voteProgressReportWithoutSubmittingProgressReport(){
        ProposalsSubmissionScore.invoke(owner, "change_period");

        ProposalsSubmissionScore.invoke(testingAccount, "voteProgressReport", "Proposal 1", "Report 1", "_approve", "Reason", "_approve");
        ProposalsSubmissionScore.invoke(owner, "voteProgressReport", "Proposal 1", "Report 1", "_approve", "Reason", "_approve");
    }

    @Test
    void voteProgressReportApprove(){
        submitProgressReportMethod();
        ProposalsSubmissionScore.invoke(owner, "change_period");

        ProposalsSubmissionScore.invoke(testingAccount, "voteProgressReport", "Proposal 1", "Report 1", "_approve", "Reason", "_approve");
        ProposalsSubmissionScore.invoke(owner, "voteProgressReport", "Proposal 1", "Report 1", "_approve", "Reason", "_approve");

        Map<String, ?> progressReportsDetails = (Map<String, ?>) ProposalsSubmissionScore.call("getProgressReportDetailsByHash", "Report 1");
        System.out.println(progressReportsDetails);
        assertEquals(progressReportsDetails.get("total_votes"), BigInteger.valueOf(200000).multiply(decimal));
        assertEquals(progressReportsDetails.get("approve_voters"), 2);
        assertEquals(progressReportsDetails.get("total_voters"), 2);
        assertEquals(progressReportsDetails.get("approved_votes"), BigInteger.valueOf(200000).multiply(decimal));
        assertEquals(progressReportsDetails.get("reject_voters"), 0);
        assertEquals(progressReportsDetails.get("rejected_votes"), BigInteger.ZERO);
        assertEquals(progressReportsDetails.get("budget_approved_votes"), BigInteger.valueOf(200000).multiply(decimal));
        assertEquals(progressReportsDetails.get("budget_approve_voters"), 2);
    }

    @Test
    void voteProgressReportReject(){
        submitProgressReportMethod();
        ProposalsSubmissionScore.invoke(owner, "change_period");

        ProposalsSubmissionScore.invoke(testingAccount, "voteProgressReport", "Proposal 1", "Report 1", "_reject", "Reason", "_reject");
        ProposalsSubmissionScore.invoke(owner, "voteProgressReport", "Proposal 1", "Report 1", "_reject", "Reason", "_reject");

        Map<String, ?> progressReportsDetails = (Map<String, ?>) ProposalsSubmissionScore.call("getProgressReportDetailsByHash", "Report 1");
        assertEquals(progressReportsDetails.get("total_votes"), BigInteger.valueOf(200000).multiply(decimal));
        assertEquals(progressReportsDetails.get("approve_voters"), 0);
        assertEquals(progressReportsDetails.get("total_voters"), 2);
        assertEquals(progressReportsDetails.get("approved_votes"), BigInteger.ZERO);
        assertEquals(progressReportsDetails.get("reject_voters"), 2);
        assertEquals(progressReportsDetails.get("rejected_votes"), BigInteger.valueOf(200000).multiply(decimal));
        assertEquals(progressReportsDetails.get("budget_rejected_votes"), BigInteger.valueOf(200000).multiply(decimal));
        assertEquals(progressReportsDetails.get("budget_reject_voters"), 2);
    }

    @Test
    void updatePeriodApplicationToVoting(){
        submitProposalMethod();
        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        Map<String, ?> periodStatus = (Map<String, ?>) ProposalsSubmissionScore.call("getPeriodStatus");

        assertEquals(periodStatus.get("period_name"), VOTING_PERIOD);
        assertEquals(periodStatus.get("previous_period_name"), APPLICATION_PERIOD);
    }

    @Test
    void updatePeriodUpdatePeriodIndexZero(){
        voteProposalApprove();
        contextMock.when(() -> Context.call(any(), eq("transfer_proposal_fund_to_proposals_fund"), eq("Proposal 1"), eq(3), eq(owner.getAddress()), eq(BigInteger.valueOf(100).multiply(decimal)))).thenAnswer((Answer<Void>) invocation -> null);
        ProposalsSubmissionScore.invoke(owner, "change_period");

        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        Map<String, ?> proposalDetails = (Map<String, ?>) ProposalsSubmissionScore.call("getProposalDetailsByHash", "Proposal 1");
        assertEquals(proposalDetails.get("status"), "_active");

        System.out.println(ProposalsSubmissionScore.call("periodIndex"));
    }

    @Test
    void updatePeriodProgressReportAccepted(){
        voteProposalApprove();

        contextMock.when(() -> Context.call(any(), eq("transfer_proposal_fund_to_proposals_fund"), eq("Proposal 1"), eq(3), eq(owner.getAddress()), eq(BigInteger.valueOf(100).multiply(decimal)))).thenAnswer((Answer<Void>) invocation -> null);

        ProposalsSubmissionScore.invoke(owner, "change_period");

        submitProgressReportWithoutSubmittingProposalMethod();

        voteProgressReportWithoutSubmittingProgressReport();

        ProposalsSubmissionScore.invoke(owner, "change_period");

        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        contextMock.when(() -> Context.call(any(), eq("update_proposal_fund"), eq("Proposal 1"), eq(BigInteger.TEN.multiply(decimal)), eq(2))).thenAnswer((Answer<Void>) invocation -> null);
        contextMock.when(() -> Context.call(any(), eq("send_installment_to_proposer"), eq("Proposal 1"))).thenAnswer((Answer<Void>) invocation -> null);

        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        Map<String, ?> progressReportsDetails = (Map<String, ?>) ProposalsSubmissionScore.call("getProgressReportDetailsByHash", "Report 1");
        assertEquals(progressReportsDetails.get("status"), "_approved");
    }

    @Test
    void updatePeriodWhenProposalIsRejected(){
        submitProposalMethod();

        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        ProposalsSubmissionScore.invoke(testingAccount, "voteProposal", "Proposal 1", "_reject", "Reason");
        ProposalsSubmissionScore.invoke(owner, "voteProposal", "Proposal 1", "_reject", "Reason");

        Map<String, ?> proposalDetails = (Map<String, ?>) ProposalsSubmissionScore.call("getProposalDetailsByHash", "Proposal 1");
        System.out.println(proposalDetails);

        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        proposalDetails = (Map<String, ?>) ProposalsSubmissionScore.call("getProposalDetailsByHash", "Proposal 1");
        assertEquals(proposalDetails.get("status"), "_rejected");
        assertEquals(List.of("Proposal 1"), ProposalsSubmissionScore.call("getProposalsKeysByStatus", "_rejected"));
    }

    @Test
    void updatePeriodNotSubmitProgressReport(){
        submitProposalMethod();
        contextMock.when(() -> Context.call(any(), eq("transfer_proposal_fund_to_proposals_fund"), eq("Proposal 1"), eq(3), eq(owner.getAddress()), eq(BigInteger.valueOf(100).multiply(decimal)))).thenAnswer((Answer<Void>) invocation -> null);
        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        ProposalsSubmissionScore.invoke(testingAccount, "voteProposal", "Proposal 1", "_approve", "Reason");
        ProposalsSubmissionScore.invoke(owner, "voteProposal", "Proposal 1", "_approve", "Reason");

        Map<String, ?> proposalDetails = (Map<String, ?>) ProposalsSubmissionScore.call("getProposalDetailsByHash", "Proposal 1");
        System.out.println(proposalDetails);

        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        Map<String, ?> periodStatus = (Map<String, ?>) ProposalsSubmissionScore.call("getPeriodStatus");
        System.out.println(periodStatus);
        proposalDetails = (Map<String, ?>) ProposalsSubmissionScore.call("getProposalDetailsByHash", "Proposal 1");
        assertEquals(proposalDetails.get("status"), "_paused");

        contextMock.when(() -> Context.call(any(), eq("disqualify_project"), eq("Proposal 1"))).thenAnswer((Answer<Void>) invocation -> null);
        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");
        proposalDetails = (Map<String, ?>) ProposalsSubmissionScore.call("getProposalDetailsByHash", "Proposal 1");
        assertEquals(proposalDetails.get("status"), "_disqualified");

    }

    @Test
    void updatePeriodProgressReportRejected(){
        submitProposalMethod();
        contextMock.when(() -> Context.call(any(), eq("transfer_proposal_fund_to_proposals_fund"), eq("Proposal 1"), eq(3), eq(owner.getAddress()), eq(BigInteger.valueOf(100).multiply(decimal)))).thenAnswer((Answer<Void>) invocation -> null);
        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        ProposalsSubmissionScore.invoke(testingAccount, "voteProposal", "Proposal 1", "_approve", "Reason");
        ProposalsSubmissionScore.invoke(owner, "voteProposal", "Proposal 1", "_approve", "Reason");

        @SuppressWarnings("unchecked")
        Map<String, ?> proposalDetails = (Map<String, ?>) ProposalsSubmissionScore.call("getProposalDetailsByHash", "Proposal 1");

        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        Map<String, ?> periodStatus = (Map<String, ?>) ProposalsSubmissionScore.call("getPeriodStatus");

        submitProgressReportWithoutSubmittingProposalMethod();

        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        ProposalsSubmissionScore.invoke(testingAccount, "voteProgressReport", "Proposal 1", "Report 1", "_reject", "Reason", "_reject");
        ProposalsSubmissionScore.invoke(owner, "voteProgressReport", "Proposal 1", "Report 1", "_reject", "Reason", "_reject");

        Map<String, ?> progressReportsDetails = (Map<String, ?>) ProposalsSubmissionScore.call("getProgressReportDetailsByHash", "Report 1");

        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        ProposalsSubmissionScore.invoke(owner, "updatePeriod");


        ProposalsSubmissionScore.invoke(owner, "updatePeriod");
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        progressReportsDetails = (Map<String, ?>) ProposalsSubmissionScore.call("getProgressReportDetailsByHash", "Report 1");
        assertEquals(progressReportsDetails.get("status"), "_progress_report_rejected");

        proposalDetails = (Map<String, ?>) ProposalsSubmissionScore.call("getProposalDetailsByHash", "Proposal 1");
        assertEquals(proposalDetails.get("status"), "_paused");

        periodStatus = (Map<String, ?>) ProposalsSubmissionScore.call("getPeriodStatus");
        System.out.println(periodStatus);
        contextMock.when(() -> Context.call(any(), eq("disqualify_project"), eq("Proposal 1"))).thenAnswer((Answer<Void>) invocation -> null);
        sm.getBlock().increase((BLOCKS_DAY_COUNT * DAY_COUNT) + 1);
        ProposalsSubmissionScore.invoke(owner, "updatePeriod");

        proposalDetails = (Map<String, ?>) ProposalsSubmissionScore.call("getProposalDetailsByHash", "Proposal 1");
        assertEquals(proposalDetails.get("status"), "_disqualified");
    }

    @Test
    void block(){
        System.out.println(ProposalsSubmissionScore.call("getCurrnetBlock"));
    }
}

package com.iconbet.score.proposalsfund;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import score.Address;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.iconbet.score.proposalsfund.db.ProposalData.ProposalAttributes;

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

public class ProposalsFundTest extends TestBase{
    private static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    private static final Address daoFund = Address.fromString("cx0000000000000000000000000000000000000001");
    private static final Address proposalsSubmission = Address.fromString("cx0000000000000000000000000000000000000002");


    private static final String TAG = "ICONBet ProposalsFund";

    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account testingAccount = sm.createAccount();
    private static final Account testingAccount1 = sm.createAccount();

    public static final BigInteger decimal = new BigInteger("1000000000000000000");

    private Score ProposalsFundScore;
    private final SecureRandom secureRandom = new SecureRandom();

    private static MockedStatic<Context> contextMock;

    ProposalsFund scoreSpy;

    @BeforeEach
    public void setup() throws Exception {
        ProposalsFundScore = sm.deploy(owner, ProposalsFund.class);
        ProposalsFund instance = (ProposalsFund) ProposalsFundScore.getInstance();
        scoreSpy = spy(instance);
        ProposalsFundScore.setInstance(scoreSpy);

    }

    @BeforeAll
    public static void init(){
        contextMock = Mockito.mockStatic(Context.class, Mockito.CALLS_REAL_METHODS);
    }

    @Test
    void name(){
        assertEquals(TAG, ProposalsFundScore.call("name"));
    }

    @Test
    void setScores(){
        ProposalsFundScore.invoke(owner, "setProposalSubmissionScore", proposalsSubmission);
        ProposalsFundScore.invoke(owner, "setDaofundScore", daoFund);
        assertEquals(proposalsSubmission, ProposalsFundScore.call("getProposalSubmissionScore"));
        assertEquals(daoFund, ProposalsFundScore.call("getDaofundScore"));
    }

    private void setScoreMethod(){
        ProposalsFundScore.invoke(owner, "setProposalSubmissionScore", proposalsSubmission);
        ProposalsFundScore.invoke(owner, "setDaofundScore", daoFund);
    }

    @Test
    void depositProposalFund(){
        setScoreMethod();
        ProposalAttributes proposalAttributes = new ProposalAttributes();
        proposalAttributes.ipfsHash = "Proposal 1";
        proposalAttributes.proposerAddress = testingAccount.getAddress();
        proposalAttributes.projectDuration = 3;
        proposalAttributes.totalBudget = BigInteger.valueOf(30);
        contextMock.when(() -> Context.getCaller()).thenReturn(daoFund);
        ProposalsFundScore.invoke(owner, "depositProposalFund", proposalAttributes);

        Map<String, ?> proposerProjectedFundDetail = (Map<String, ?>) ProposalsFundScore.call("get_proposer_projected_fund", testingAccount.getAddress());
        List<Map<String, ?>> proposerProjectedFundData = (List<Map<String, ?>>) proposerProjectedFundDetail.get("data");

        assertEquals(proposerProjectedFundData.get(0).get("total_installment_count"), 3);
        assertEquals(proposerProjectedFundData.get(0).get("ipfs_hash"), "Proposal 1");
        assertEquals(proposerProjectedFundData.get(0).get("installment_amount"), BigInteger.TEN);
        assertEquals(proposerProjectedFundData.get(0).get("total_budget"), BigInteger.valueOf(30));

        assertEquals(proposerProjectedFundDetail.get("project_count"), 1);
        assertEquals(proposerProjectedFundDetail.get("total_amount"), BigInteger.TEN);
    }

    private void depositProposalFundMethod(){
        setScoreMethod();
        ProposalAttributes proposalAttributes = new ProposalAttributes();
        proposalAttributes.ipfsHash = "Proposal 1";
        proposalAttributes.proposerAddress = testingAccount.getAddress();
        proposalAttributes.projectDuration = 3;
        proposalAttributes.totalBudget = BigInteger.valueOf(30);
        contextMock.when(() -> Context.getCaller()).thenReturn(daoFund);
        ProposalsFundScore.invoke(owner, "depositProposalFund", proposalAttributes);
    }

    @Test
    void update_proposal_fund(){
        depositProposalFundMethod();
        ProposalsFundScore.invoke(owner, "update_proposal_fund", "Proposal 1", BigInteger.TEN, 2);
        Map<String, ?> proposerProjectedFundDetail = (Map<String, ?>) ProposalsFundScore.call("get_proposer_projected_fund", testingAccount.getAddress());
        List<Map<String, ?>> proposerProjectedFundData = (List<Map<String, ?>>) proposerProjectedFundDetail.get("data");

        assertEquals(proposerProjectedFundData.get(0).get("total_installment_count"), 5);
        assertEquals(proposerProjectedFundData.get(0).get("ipfs_hash"), "Proposal 1");
        assertEquals(proposerProjectedFundData.get(0).get("installment_amount"), BigInteger.valueOf(8));
        assertEquals(proposerProjectedFundData.get(0).get("total_budget"), BigInteger.valueOf(40));

        assertEquals(proposerProjectedFundDetail.get("project_count"), 1);
        assertEquals(proposerProjectedFundDetail.get("total_amount"), BigInteger.valueOf(8));
    }

    @Test
    void sendInstallmentToProposser(){
        depositProposalFundMethod();

        contextMock.when(() -> Context.getCaller()).thenReturn(proposalsSubmission);
        ProposalsFundScore.invoke(owner, "send_installment_to_proposer", "Proposal 1");

        Map<String, ?> proposerProjectedFundDetail = (Map<String, ?>) ProposalsFundScore.call("get_proposer_projected_fund", testingAccount.getAddress());
        List<Map<String, ?>> proposerProjectedFundData = (List<Map<String, ?>>) proposerProjectedFundDetail.get("data");

        assertEquals(proposerProjectedFundData.get(0).get("total_installment_count"), 3);
        assertEquals(proposerProjectedFundData.get(0).get("ipfs_hash"), "Proposal 1");
        assertEquals(proposerProjectedFundData.get(0).get("installment_amount"), BigInteger.valueOf(10));
        assertEquals(proposerProjectedFundData.get(0).get("total_budget"), BigInteger.valueOf(30));
        assertEquals(proposerProjectedFundData.get(0).get("remaining_amount"), BigInteger.valueOf(20));
        assertEquals(proposerProjectedFundData.get(0).get("installment_count"), 2);

        assertEquals(proposerProjectedFundDetail.get("project_count"), 1);
        assertEquals(proposerProjectedFundDetail.get("total_amount"), BigInteger.valueOf(10));
        System.out.println(proposerProjectedFundDetail);

        ProposalsFundScore.invoke(owner, "send_installment_to_proposer", "Proposal 1");
        proposerProjectedFundDetail = (Map<String, ?>) ProposalsFundScore.call("get_proposer_projected_fund", testingAccount.getAddress());
        proposerProjectedFundData = (List<Map<String, ?>>) proposerProjectedFundDetail.get("data");

        assertEquals(proposerProjectedFundData.get(0).get("total_installment_count"), 3);
        assertEquals(proposerProjectedFundData.get(0).get("ipfs_hash"), "Proposal 1");
        assertEquals(proposerProjectedFundData.get(0).get("installment_amount"), BigInteger.valueOf(10));
        assertEquals(proposerProjectedFundData.get(0).get("total_budget"), BigInteger.valueOf(30));
        assertEquals(proposerProjectedFundData.get(0).get("remaining_amount"), BigInteger.valueOf(10));
        assertEquals(proposerProjectedFundData.get(0).get("installment_count"), 1);

        assertEquals(proposerProjectedFundDetail.get("project_count"), 1);
        assertEquals(proposerProjectedFundDetail.get("total_amount"), BigInteger.valueOf(10));
        System.out.println(proposerProjectedFundDetail);

        ProposalsFundScore.invoke(owner, "send_installment_to_proposer", "Proposal 1");
        proposerProjectedFundDetail = (Map<String, ?>) ProposalsFundScore.call("get_proposer_projected_fund", testingAccount.getAddress());

//        empty because the status changes to completed
        System.out.println(proposerProjectedFundDetail);
    }

    @Test
    void disqualifyProject(){
        depositProposalFundMethod();

        contextMock.when(() -> Context.getCaller()).thenReturn(proposalsSubmission);
        contextMock.when(() -> Context.call(eq(BigInteger.valueOf(30)), eq(daoFund), eq("disqualify_proposal_fund"), eq("Proposal 1"))).thenAnswer((Answer<Void>) invocation -> null);
        ProposalsFundScore.invoke(owner, "disqualify_project", "Proposal 1");
        Map<String, ?> proposalDetails = (Map<String, ?>) ProposalsFundScore.call("getProposalDetails", "Proposal 1");
        assertEquals(proposalDetails.get("status"), "disqualified");
        System.out.println(ProposalsFundScore.call("getProposalDetails", "Proposal 1"));
    }
}

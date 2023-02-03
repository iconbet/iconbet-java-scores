package com.iconbet.score.proposalssubmission.db;

import com.iconbet.score.proposalssubmission.utils.consts;
import score.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import scorex.util.ArrayList;

public class ProposalData{
    private static final String proposalDBPrefix = "proposal";

    public static class ProposalAttributes{
        public String ipfsHash;
        public String projectType;
        public String projectTitle;
        public int projectDuration;
        public BigInteger totalBudget;
        public String ipfsLink;
    }
    private static final BranchDB<String, VarDB<String>> ipfsHash = Context.newBranchDB("ipfs_hash", String.class);
    private static final BranchDB<String, VarDB<String>> projectType = Context.newBranchDB("projectType", String.class);
    private static final BranchDB<String, VarDB<String>> projectTitle = Context.newBranchDB("projectTitle", String.class);
    private static final BranchDB<String, VarDB<BigInteger>> timestamp = Context.newBranchDB("timestamp", BigInteger.class);
    private static final BranchDB<String, VarDB<BigInteger>> totalBudget = Context.newBranchDB("totalBudget", BigInteger.class);
    private static final BranchDB<String, VarDB<Integer>> projectDuration = Context.newBranchDB("projectDuration", Integer.class);
    private static final BranchDB<String, VarDB<Integer>> approvedReports = Context.newBranchDB("approvedReports", Integer.class);
    private static final BranchDB<String, VarDB<Address>> proposerAddress = Context.newBranchDB("proposerAddress", Address.class);
    private static final BranchDB<String, VarDB<String>> status = Context.newBranchDB("status", String.class);
    private static final BranchDB<String, VarDB<String>> txHash = Context.newBranchDB("txHash", String.class);
    private static final BranchDB<String, VarDB<Integer>> percentageCompleted = Context.newBranchDB("percentageCompleted", Integer.class);

    public static final BranchDB<String, ArrayDB<String>> votersReasons = Context.newBranchDB("votersReasons", String.class);
    private static final BranchDB<String, VarDB<BigInteger>> totalVotes = Context.newBranchDB("totalVotes", BigInteger.class);
    private static final BranchDB<String, VarDB<Integer>> totalVoters = Context.newBranchDB("totalVoters", Integer.class);
    private static final BranchDB<String, VarDB<BigInteger>> approvedVotes = Context.newBranchDB("approvedVotes", BigInteger.class);
    private static final BranchDB<String, VarDB<BigInteger>> rejectedVotes = Context.newBranchDB("rejectedVotes", BigInteger.class);

    public static final BranchDB<String, ArrayDB<Address>> votersList = Context.newBranchDB("votersList", Address.class);
    public static final BranchDB<String, ArrayDB<Address>> approveVoters = Context.newBranchDB("approveVoters", Address.class);
    public static final BranchDB<String, ArrayDB<Address>> rejectVoters = Context.newBranchDB("rejectVoters", Address.class);

    private static final BranchDB<String, ArrayDB<String>> progressReports = Context.newBranchDB("progressReports", String.class);
    private static final BranchDB<String, VarDB<Boolean>> budgetAdjustment = Context.newBranchDB("budgetAdjustment", Boolean.class);
    private static final BranchDB<String, VarDB<Boolean>> submitProgressReport = Context.newBranchDB("submitProgressReport", Boolean.class);


    public static String proposalPrefix(String proposalKey) {
        return proposalDBPrefix + "|" + proposalKey;
    }

    public void addDataToProposalDB(ProposalAttributes proposals, String prefix){
        ipfsHash.at(prefix).set(proposals.ipfsHash);
        projectType.at(prefix).set(proposals.projectType);
        projectTitle.at(prefix).set(proposals.projectTitle);
        totalBudget.at(prefix).set(proposals.totalBudget);
        projectDuration.at(prefix).set(proposals.projectDuration);

        totalVotes.at(prefix).set(BigInteger.ZERO);
        totalVoters.at(prefix).set(0);
        approvedVotes.at(prefix).set(BigInteger.ZERO);
        rejectedVotes.at(prefix).set(BigInteger.ZERO);
        approvedReports.at(prefix).set(0);
        budgetAdjustment.at(prefix).set(Boolean.FALSE);
        submitProgressReport.at(prefix).set(Boolean.FALSE);
    }

    public Map<String, ?> getDataFromProposalDB(String prefix){
        return Map.ofEntries(
                Map.entry(consts.IPFS_HASH, ipfsHash.at(prefix).getOrDefault("")),
                Map.entry(consts.PROJECT_TYPE, projectType.at(prefix).getOrDefault("")),
                Map.entry(consts.PROJECT_TITLE, projectTitle.at(prefix).getOrDefault("")),
                Map.entry(consts.TIMESTAMP, timestamp.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(consts.TOTAL_BUDGET, totalBudget.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(consts.PROJECT_DURATION, projectDuration.at(prefix).getOrDefault(0)),
                Map.entry(consts.APPROVED_REPORTS, approvedReports.at(prefix).getOrDefault(0)),
                Map.entry(consts.PROPOSER_ADDRESS, proposerAddress.at(prefix).get()),
                Map.entry(consts.STATUS, status.at(prefix).getOrDefault("")),
                Map.entry(consts.TX_HASH, txHash.at(prefix).getOrDefault("")),
                Map.entry(consts.PERCENTAGE_COMPLETED, percentageCompleted.at(prefix).getOrDefault(0)),

                Map.entry(consts.TOTAL_VOTES, totalVotes.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(consts.TOTAL_VOTERS, totalVoters.at(prefix).getOrDefault(0)),
                Map.entry(consts.APPROVED_VOTES, approvedVotes.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(consts.REJECTED_VOTES, rejectedVotes.at(prefix).getOrDefault(BigInteger.ZERO)),

                Map.entry(consts.APPROVE_VOTERS, approveVoters.at(prefix).size()),
                Map.entry(consts.REJECT_VOTERS, rejectVoters.at(prefix).size()),
                Map.entry(consts.BUDGET_ADJUSTMENT, budgetAdjustment.at(prefix).getOrDefault(Boolean.FALSE)),
                Map.entry(consts.SUBMIT_PROGRESS_REPORT, submitProgressReport.at(prefix).getOrDefault(Boolean.FALSE))
        );
    }

    public String getProjectType(String proposalPrefix){
        return projectType.at(proposalPrefix).get();
    }

    public String getProjectTitle(String proposalPrefix){
        return projectTitle.at(proposalPrefix).get();
    }

    public BigInteger getTotalBudget(String proposalPrefix){
        return totalBudget.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
    }

    public void setTotalBudget(String proposalPrefix, BigInteger totalBudget){
        ProposalData.totalBudget.at(proposalPrefix).set(totalBudget);
    }

    public void setTimestamp(String proposalPrefix, BigInteger timestamp){
        ProposalData.timestamp.at(proposalPrefix).set(timestamp);
    }


    public int getProjectDuration(String proposalPrefix){
        return projectDuration.at(proposalPrefix).getOrDefault(0);
    }

    public void setProjectDuration(String proposalPrefix, int projectDuration){
        ProposalData.projectDuration.at(proposalPrefix).set(projectDuration);
    }

    public int getApprovedReports(String proposalPrefix){
        return approvedReports.at(proposalPrefix).getOrDefault(0);
    }

    public void setApprovedReports(String proposalPrefix, int approvedReports){
        ProposalData.approvedReports.at(proposalPrefix).set(approvedReports);
    }

    public Address getProposerAddress(String proposalPrefix){
        return proposerAddress.at(proposalPrefix).get();
    }

    public void setProposerAddress(String proposalPrefix, Address proposerAddress){
        ProposalData.proposerAddress.at(proposalPrefix).set(proposerAddress);
    }

    public String getStatus(String proposalPrefix){
        return status.at(proposalPrefix).getOrDefault("");
    }

    public void setStatus(String proposalPrefix, String status){
        ProposalData.status.at(proposalPrefix).set(status);
    }

    public String getTxHash(String proposalPrefix){
        return txHash.at(proposalPrefix).getOrDefault("");
    }

    public void setTxHash(String proposalPrefix, String txHash){
        ProposalData.txHash.at(proposalPrefix).set(txHash);
    }

    public int getPercentageCompleted(String proposalPrefix){
        return percentageCompleted.at(proposalPrefix).getOrDefault(0);
    }

    public void setPercentageCompleted(String proposalPrefix, int percentageCompleted){
        ProposalData.percentageCompleted.at(proposalPrefix).set(percentageCompleted);
    }

    public void setVotersReasons(String proposalPrefix, String votersReasons){
        ProposalData.votersReasons.at(proposalPrefix).add(votersReasons);
    }

    public BigInteger getTotalVotes(String proposalPrefix){
        return totalVotes.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
    }

    public void setTotalVotes(String proposalPrefix, BigInteger votes){
        ProposalData.totalVotes.at(proposalPrefix).set(votes);
    }

    public int getTotalVoters(String proposalPrefix){
        return totalVoters.at(proposalPrefix).getOrDefault(0);
    }

    public void setTotalVoters(String proposalPrefix, int voters){
        ProposalData.totalVoters.at(proposalPrefix).set(voters);
    }

    public BigInteger getApprovedVotes(String proposalPrefix){
        return approvedVotes.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
    }

    public void setApprovedVotes(String proposalPrefix, BigInteger approvedVotes){
        ProposalData.approvedVotes.at(proposalPrefix).set(approvedVotes);
    }

    public BigInteger getRejectedVotes(String proposalPrefix){
        return rejectedVotes.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
    }

    public void setRejectedVotes(String proposalPrefix, BigInteger rejectedVotes){
        ProposalData.rejectedVotes.at(proposalPrefix).set(rejectedVotes);
    }

    public List<Address> getVotersList(String proposalPrefix){
        List<Address> votersList = new ArrayList<>();
        for(int i = 0; i < ProposalData.votersList.at(proposalPrefix).size(); i++){
            votersList.add(ProposalData.votersList.at(proposalPrefix).get(i));
        }
        return votersList;
    }

    public void setVotersList(String proposalPrefix, Address voters){
        ProposalData.votersList.at(proposalPrefix).add(voters);
    }

    public List<Address> getApproveVotersList(String proposalPrefix){
        List<Address> votersList = new ArrayList<>();
        for(int i = 0; i < ProposalData.approveVoters.at(proposalPrefix).size(); i++){
            votersList.add(ProposalData.approveVoters.at(proposalPrefix).get(i));
        }
        return votersList;
    }

    public void setApproveVoters(String proposalPrefix, Address voters){
        ProposalData.approveVoters.at(proposalPrefix).add(voters);
    }

    public List<Address> getRejectVotersList(String proposalPrefix){
        List<Address> votersList = new ArrayList<>();
        for(int i = 0; i < ProposalData.rejectVoters.at(proposalPrefix).size(); i++){
            votersList.add(ProposalData.rejectVoters.at(proposalPrefix).get(i));
        }
        return votersList;
    }

    public void setRejectVoters(String proposalPrefix, Address voters){
        ProposalData.rejectVoters.at(proposalPrefix).add(voters);
    }

    public List<String> getProgressReportsList(String proposalPrefix){
        List<String> progressReports = new ArrayList<>();
        for(int i = 0; i < ProposalData.progressReports.at(proposalPrefix).size(); i++){
            progressReports.add(ProposalData.progressReports.at(proposalPrefix).get(i));
        }
        return progressReports;
    }

    public void setProgressReports(String proposalPrefix, String reportHash){
        ProposalData.progressReports.at(proposalPrefix).add(reportHash);
    }

    public Boolean getBudgetAdjustment(String proposalPrefix){
        return budgetAdjustment.at(proposalPrefix).getOrDefault(Boolean.FALSE);
    }

    public void setBudgetAdjustment(String proposalPrefix){
        ProposalData.budgetAdjustment.at(proposalPrefix).set(Boolean.TRUE);
    }

    public Boolean getSubmitProgressReport(String proposalPrefix){
        return ProposalData.submitProgressReport.at(proposalPrefix).get();
    }

    public void setSubmitProgressReport(String proposalPrefix, Boolean status){
        ProposalData.submitProgressReport.at(proposalPrefix).set(status);
    }

    public String getIpfsHash(String proposalDBPrefix){
        return ProposalData.ipfsHash.at(proposalDBPrefix).getOrDefault("");
    }

}
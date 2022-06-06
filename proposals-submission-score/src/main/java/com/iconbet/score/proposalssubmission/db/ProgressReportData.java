package com.iconbet.score.proposalssubmission.db;

import com.iconbet.score.proposalssubmission.utils.consts;
import score.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import scorex.util.ArrayList;

public class ProgressReportData {
    private static final String progressDBPrefix = "progressReport";

    public static class ProgressReportAttributes{
        public String ipfsHash;
        public String reportHash;
        public String ipfsLink;
        public String progressReportTitle;
        public Boolean budgetAdjustment;
        public BigInteger additionalBudget;
        public int additionalMonth;
        public int percentageCompleted;
    }

    private final BranchDB<String, VarDB<String>> ipfsHash = Context.newBranchDB("ipfs_hash",String.class);
    private final BranchDB<String, VarDB<String>> reportHash = Context.newBranchDB("report_hash", String.class);
    private final BranchDB<String, VarDB<String>> progressReportTitle = Context.newBranchDB("progress_report_title", String.class);
    private final BranchDB<String, VarDB<BigInteger>> timestamp = Context.newBranchDB("timestamp", BigInteger.class);
    private final BranchDB<String, VarDB<String>> status = Context.newBranchDB("status", String.class);
    private final BranchDB<String, VarDB<String>> txHash = Context.newBranchDB("tx_hash", String.class);

    private final BranchDB<String, VarDB<Boolean>> budgetAdjustment = Context.newBranchDB("budget_adjustment", Boolean.class);
    private final BranchDB<String, VarDB<BigInteger>> additionalBudget = Context.newBranchDB("additional_budget", BigInteger.class);
    private final BranchDB<String, VarDB<Integer>> additionalMonth = Context.newBranchDB("additional_month", Integer.class);
    private final BranchDB<String, ArrayDB<String>> votersReasons = Context.newBranchDB("voters_reasons", String.class);

    private final BranchDB<String, VarDB<BigInteger>> totalVotes = Context.newBranchDB("total_votes", BigInteger.class);
    private final BranchDB<String, VarDB<BigInteger>> approvedVotes = Context.newBranchDB("approved_votes", BigInteger.class);
    private final BranchDB<String, VarDB<BigInteger>> rejectedVotes = Context.newBranchDB("rejected_votes", BigInteger.class);

    private final BranchDB<String, ArrayDB<Address>> votersList = Context.newBranchDB("voters_list", Address.class);
    private final BranchDB<String, ArrayDB<Address>> approveVoters = Context.newBranchDB("approve_voters", Address.class);
    private final BranchDB<String, ArrayDB<Address>> rejectVoters = Context.newBranchDB("reject_voters", Address.class);
    private final BranchDB<String, VarDB<Integer>> totalVoters = Context.newBranchDB("total_voters", Integer.class);

    private final BranchDB<String, VarDB<BigInteger>> budgetApprovedVotes = Context.newBranchDB("budget_approved_votes", BigInteger.class);
    private final BranchDB<String, VarDB<BigInteger>> budgetRejectedVotes = Context.newBranchDB("budget_rejected_votes", BigInteger.class);
    private final BranchDB<String, ArrayDB<Address>> budgetApproveVoters = Context.newBranchDB("budget_approve_voters", Address.class);
    private final BranchDB<String, ArrayDB<Address>> budgetRejectVoters = Context.newBranchDB("budget_reject_voters", Address.class);
    private final BranchDB<String, VarDB<String>> budgetAdjustmentStatus = Context.newBranchDB("budget_adjustment_status", String.class);

    public static String progressReportPrefix(String progressKey) {
        return progressDBPrefix + "|" + progressKey;
    }
    public void addDataToProgressReportDB(ProgressReportAttributes progressReports, String prefix){
        ipfsHash.at(prefix).set(progressReports.ipfsHash);
        reportHash.at(prefix).set(progressReports.reportHash);
        progressReportTitle.at(prefix).set(progressReports.progressReportTitle);
        additionalBudget.at(prefix).set(progressReports.additionalBudget);
        additionalMonth.at(prefix).set(progressReports.additionalMonth);
        budgetAdjustment.at(prefix).set(progressReports.budgetAdjustment);

        totalVotes.at(prefix).set(BigInteger.ZERO);
        totalVoters.at(prefix).set(0);
        approvedVotes.at(prefix).set(BigInteger.ZERO);
        rejectedVotes.at(prefix).set(BigInteger.ZERO);
        budgetApprovedVotes.at(prefix).set(BigInteger.ZERO);
        budgetRejectedVotes.at(prefix).set(BigInteger.ZERO);
    }

    public Map<String, ?> getDataFromProposalDB(String prefix){
        return Map.ofEntries(
                Map.entry(consts.IPFS_HASH, ipfsHash.at(prefix).getOrDefault("")),
                Map.entry(consts.REPORT_HASH, reportHash.at(prefix).getOrDefault("")),

                Map.entry(consts.PROGRESS_REPORT_TITLE, progressReportTitle.at(prefix).getOrDefault("")),
                Map.entry(consts.TIMESTAMP, timestamp.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(consts.ADDITIONAL_BUDGET, additionalBudget.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(consts.ADDITIONAL_DURATION, additionalMonth.at(prefix).getOrDefault(0)),
                Map.entry(consts.STATUS, status.at(prefix).getOrDefault("")),
                Map.entry(consts.TX_HASH, txHash.at(prefix).getOrDefault("")),
                Map.entry(consts.BUDGET_ADJUSTMENT, budgetAdjustment.at(prefix).getOrDefault(Boolean.FALSE)),

                Map.entry(consts.TOTAL_VOTES, totalVotes.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(consts.TOTAL_VOTERS, totalVoters.at(prefix).getOrDefault(0)),
                Map.entry(consts.APPROVED_VOTES, approvedVotes.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(consts.REJECTED_VOTES, rejectedVotes.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(consts.APPROVE_VOTERS, approveVoters.at(prefix).size()),
                Map.entry(consts.REJECT_VOTERS, rejectVoters.at(prefix).size()),

                Map.entry(consts.BUDGET_APPROVED_VOTES, budgetApprovedVotes.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(consts.BUDGET_REJECTED_VOTES, budgetRejectedVotes.at(prefix).getOrDefault(BigInteger.ZERO)),
                Map.entry(consts.BUDGET_APPROVE_VOTERS, budgetApproveVoters.at(prefix).size()),
                Map.entry(consts.BUDGET_REJECT_VOTERS, budgetRejectVoters.at(prefix).size())
        );
    }

    public String getProgressReportTitle(String progressPrefix){
        return this.progressReportTitle.at(progressPrefix).get();
    }

    public BigInteger getAdditionalBudget(String progressPrefix){
        return additionalBudget.at(progressPrefix).getOrDefault(BigInteger.ZERO);
    }

    public void setTimestamp(String progressPrefix, BigInteger timestamp){
        this.timestamp.at(progressPrefix).set(timestamp);
    }

    public void setAdditionalBudget(String progressPrefix, BigInteger additionalBudget){
        this.additionalBudget.at(progressPrefix).set(additionalBudget);
    }

    public int getAdditionalMonth(String progressPrefix){
        return additionalMonth.at(progressPrefix).getOrDefault(0);
    }

    public void setAdditionalMonth(String progressPrefix, int additionalMonth){
        this.additionalMonth.at(progressPrefix).set(additionalMonth);
    }

    public void setVotersReasons(String progressPrefix, String votersReasons){
        this.votersReasons.at(progressPrefix).add(votersReasons);
    }

    public String getStatus(String progressPrefix){
        return status.at(progressPrefix).getOrDefault("");
    }

    public void setStatus(String progressPrefix, String status){
        this.status.at(progressPrefix).set(status);
    }

    public String getTxHash(String progressPrefix){
        return txHash.at(progressPrefix).getOrDefault("");
    }

    public void setTxHash(String progressPrefix, String txHash){
        this.txHash.at(progressPrefix).set(txHash);
    }

    public BigInteger getTotalVotes(String progressPrefix){
        return totalVotes.at(progressPrefix).getOrDefault(BigInteger.ZERO);
    }

    public void setTotalVotes(String progressPrefix, BigInteger votes){
        this.totalVotes.at(progressPrefix).set(votes);
    }

    public int getTotalVoters(String progressPrefix){
        return totalVoters.at(progressPrefix).getOrDefault(0);
    }

    public void setTotalVoters(String progressPrefix, int voters){
        this.totalVoters.at(progressPrefix).set(voters);
    }

    public BigInteger getApprovedVotes(String progressPrefix){
        return approvedVotes.at(progressPrefix).getOrDefault(BigInteger.ZERO);
    }

    public void setApprovedVotes(String progressPrefix, BigInteger approvedVotes){
        this.approvedVotes.at(progressPrefix).set(approvedVotes);
    }

    public BigInteger getRejectedVotes(String progressPrefix){
        return rejectedVotes.at(progressPrefix).getOrDefault(BigInteger.ZERO);
    }

    public void setRejectedVotes(String progressPrefix, BigInteger rejectedVotes){
        this.rejectedVotes.at(progressPrefix).set(rejectedVotes);
    }

    public List<Address> getVotersList(String progressPrefix){
        List<Address> votersList = new ArrayList<>();
        for(int i = 0; i < this.votersList.at(progressPrefix).size(); i++){
            votersList.add(this.votersList.at(progressPrefix).get(i));
        }
        return votersList;
    }

    public void setVotersList(String progressPrefix, Address voters){
        this.votersList.at(progressPrefix).add(voters);
    }

    public List<Address> getApproveVotersList(String progressPrefix){
        List<Address> votersList = new ArrayList<>();
        for(int i = 0; i < this.approveVoters.at(progressPrefix).size(); i++){
            votersList.add(this.approveVoters.at(progressPrefix).get(i));
        }
        return votersList;
    }

    public void setApproveVoters(String progressPrefix, Address voters){
        this.approveVoters.at(progressPrefix).add(voters);
    }

    public List<Address> getRejectVotersList(String progressPrefix){
        List<Address> votersList = new ArrayList<>();
        for(int i = 0; i < this.rejectVoters.at(progressPrefix).size(); i++){
            votersList.add(this.rejectVoters.at(progressPrefix).get(i));
        }
        return votersList;
    }

    public void setRejectVoters(String progressPrefix, Address voters){
        this.rejectVoters.at(progressPrefix).add(voters);
    }

    public Boolean getBudgetAdjustment(String progressPrefix){
        return budgetAdjustment.at(progressPrefix).getOrDefault(Boolean.FALSE);
    }

    public void setBudgetAdjustment(String progressPrefix){
        this.budgetAdjustment.at(progressPrefix).set(Boolean.TRUE);
    }

    public BigInteger getBudgetApprovedVotes(String progressPrefix){
        return budgetApprovedVotes.at(progressPrefix).getOrDefault(BigInteger.ZERO);
    }

    public void setBudgetApprovedVotes(String progressPrefix, BigInteger budgetApprovedVotes){
        this.budgetApprovedVotes.at(progressPrefix).set(budgetApprovedVotes);
    }

    public BigInteger getBudgetRejectedVotes(String progressPrefix){
        return budgetRejectedVotes.at(progressPrefix).getOrDefault(BigInteger.ZERO);
    }

    public void setBudgetRejectedVotes(String progressPrefix, BigInteger budgetRejectedVotes){
        this.budgetRejectedVotes.at(progressPrefix).set(budgetRejectedVotes);
    }

    public List<Address> getBudgetApproveVotersList(String progressPrefix){
        List<Address> votersList = new ArrayList<>();
        for(int i = 0; i < this.budgetApproveVoters.at(progressPrefix).size(); i++){
            votersList.add(this.budgetApproveVoters.at(progressPrefix).get(i));
        }
        return votersList;
    }

    public void setBudgetApproveVoters(String progressPrefix, Address voters){
        this.budgetApproveVoters.at(progressPrefix).add(voters);
    }

    public List<Address> getBudgetRejectVotersList(String progressPrefix){
        List<Address> votersList = new ArrayList<>();
        for(int i = 0; i < this.budgetRejectVoters.at(progressPrefix).size(); i++){
            votersList.add(this.budgetRejectVoters.at(progressPrefix).get(i));
        }
        return votersList;
    }

    public void setBudgetRejectVoters(String progressPrefix, Address voters){
        this.budgetRejectVoters.at(progressPrefix).add(voters);
    }

    public String getBudgetAdjustmentStatus(String progressPrefix){
        return budgetAdjustmentStatus.at(progressPrefix).getOrDefault("");
    }

    public void setBudgetAdjustmentStatus(String progressPrefix, String status){
        this.budgetAdjustmentStatus.at(progressPrefix).set(status);
    }

}

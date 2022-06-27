package com.iconbet.score.proposalsfund.db;

import score.Address;
import score.BranchDB;
import score.Context;
import score.VarDB;

import java.math.BigInteger;
import java.util.Map;
import static com.iconbet.score.proposalsfund.utils.Constants.*;

public class ProposalData {
    public static class ProposalAttributes{
        public String ipfsHash;
        public int projectDuration;
        public BigInteger totalBudget;
        public Address proposerAddress;
    }
    private final BranchDB<String, VarDB<String>> ipfs_hash = Context.newBranchDB("ipfs_hash",String.class);
    private final BranchDB<String, VarDB<BigInteger>> total_budget = Context.newBranchDB("total_budget", BigInteger.class);
    private final BranchDB<String, VarDB<Integer>> project_duration = Context.newBranchDB("project_duration", Integer.class);
    private final BranchDB<String, VarDB<Address>> proposer_address = Context.newBranchDB("proposer_address", Address.class);

    private final BranchDB<String, VarDB<BigInteger>> withdraw_amount = Context.newBranchDB("withdraw_amount", BigInteger.class);
    private final BranchDB<String, VarDB<BigInteger>> remaining_amount = Context.newBranchDB("remaining_amount", BigInteger.class);

    private final BranchDB<String, VarDB<Integer>> installment_count = Context.newBranchDB("installment_count", Integer.class);

    private final BranchDB<String, VarDB<String>> status = Context.newBranchDB("status", String.class);

    public void addDataToProposalDB(ProposalAttributes proposalAttributes, String proposalPrefix){
        this.ipfs_hash.at(proposalPrefix).set(proposalAttributes.ipfsHash);
        this.total_budget.at(proposalPrefix).set(proposalAttributes.totalBudget);
        this.project_duration.at(proposalPrefix).set(proposalAttributes.projectDuration);
        this.proposer_address.at(proposalPrefix).set(proposalAttributes.proposerAddress);

        this.withdraw_amount.at(proposalPrefix).set(BigInteger.ZERO);
        this.remaining_amount.at(proposalPrefix).set(proposalAttributes.totalBudget);
        this.installment_count.at(proposalPrefix).set(proposalAttributes.projectDuration);
        this.status.at(proposalPrefix).set(_ACTIVE);
    }

    public Map<String, ?> getDataFromProposalDB(String proposalPrefix){
        return Map.of("ipfs_hash", ipfs_hash,
                "total_budget", total_budget.at(proposalPrefix).getOrDefault(BigInteger.ZERO),
                "project_duration", project_duration.at(proposalPrefix).getOrDefault(0),
                "proposer_address", proposer_address.at(proposalPrefix).get(),
                "withdraw_amount",  withdraw_amount.at(proposalPrefix).getOrDefault(BigInteger.ZERO),
                "installment_count", installment_count.at(proposalPrefix).getOrDefault(0),
                "remaining_amount", remaining_amount.at(proposalPrefix).getOrDefault(BigInteger.ZERO),
                "status", status.at(proposalPrefix).getOrDefault(""));
    }

    public void setTotalBudget(String proposalPrefix, BigInteger totalBudget){
        this.total_budget.at(proposalPrefix).set(totalBudget);
    }

    public BigInteger getTotalBudget(String proposalPrefix){
        return this.total_budget.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
    }

    public void setProjectDuration(String proposalPrefix, int projectDuration){
        this.project_duration.at(proposalPrefix).set(projectDuration);
    }

    public int getProjectDuration(String proposalPrefix){
        return this.project_duration.at(proposalPrefix).getOrDefault(0);
    }

    public void setWithdrawAmount(String proposalPrefix, BigInteger withdrawAmount){
        this.withdraw_amount.at(proposalPrefix).set(withdrawAmount);
    }

    public BigInteger getWithdrawAmount(String proposalPrefix){
        return this.withdraw_amount.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
    }

    public void setRemainingAmount(String proposalPrefix, BigInteger remainingAmount){
        this.remaining_amount.at(proposalPrefix).set(remainingAmount);
    }

    public BigInteger getRemainingAmount(String proposalPrefix){
        return this.remaining_amount.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
    }

    public void setInstallmentCount(String proposalPrefix, int installmentCount){
        this.installment_count.at(proposalPrefix).set(installmentCount);
    }

    public int getInstallmentCount(String proposalPrefix){
        return this.installment_count.at(proposalPrefix).getOrDefault(0);
    }

    public Address getProposerAddress(String proposalPrefix){
        return this.proposer_address.at(proposalPrefix).get();
    }

    public void setStatus(String proposalPrefix, String status){
        this.status.at(proposalPrefix).set(status);
    }

    public String getStatus(String proposalPrefix){
        return this.status.at(proposalPrefix).get();
    }
}

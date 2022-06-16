package com.iconbet.score.authorization.db;

import score.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static com.iconbet.score.authorization.utils.Consts.*;
import static com.iconbet.score.authorization.utils.Consts.PROPOSAL_COUNT;
import static com.iconbet.score.authorization.utils.ArrayDBUtils.*;

public class ProposalData {
    public static class ProposalAttributes {
        public String name;
        public String description;
        public String ipfsHash;
        public Address proposerAddress;
        public BigInteger quorum;
        public BigInteger majority;
        public BigInteger snapshot;
        public BigInteger start;
        public BigInteger end;
        public String actions;
    }
    private static final String Prefix = "GameApproval";
    private final BranchDB<String, DictDB<String, Integer>> id = Context.newBranchDB(Prefix + "_id", Integer.class);
    private final BranchDB<String, VarDB<Integer>> proposals_count = Context.newBranchDB(Prefix + "_proposals_count", Integer.class);
    private final BranchDB<String, VarDB<Address>> proposer = Context.newBranchDB(Prefix + "_proposer",Address.class);
    private final BranchDB<String, VarDB<BigInteger>> quorum = Context.newBranchDB(Prefix + "_quorum", BigInteger.class);
    private final BranchDB<String, VarDB<BigInteger>> majority = Context.newBranchDB(Prefix + "_majority", BigInteger.class);
    private final BranchDB<String, VarDB<BigInteger>> vote_snapshot = Context.newBranchDB(Prefix + "_vote_snapshot", BigInteger.class);
    private final BranchDB<String, VarDB<BigInteger>> start_snapshot = Context.newBranchDB(Prefix + "_start_snapshot", BigInteger.class);
    private final BranchDB<String, VarDB<BigInteger>> end_snapshot = Context.newBranchDB(Prefix + "_end_snapshot", BigInteger.class);
    private final BranchDB<String, VarDB<String> >actions = Context.newBranchDB(Prefix + "_actions", String.class);
    private final BranchDB<String, VarDB<String> >name = Context.newBranchDB(Prefix + "_name", String.class);
    private final BranchDB<String, VarDB<String> >description = Context.newBranchDB(Prefix + "_description", String.class);
    private final BranchDB<String, VarDB<String> >ipfs_hash = Context.newBranchDB(Prefix + "_ipfs_hash", String.class);
    private final BranchDB<String, VarDB<Boolean>> active = Context.newBranchDB(Prefix + "_active", Boolean.class);
    private final BranchDB<String, DictDB<Address, BigInteger>> for_votes_of_user = Context.newBranchDB(Prefix + "_for_votes_of_user", BigInteger.class);
    private final BranchDB<String, DictDB<Address, BigInteger>> against_votes_of_user = Context.newBranchDB(Prefix + "_against_votes_of_user", BigInteger.class);
    private final BranchDB<String, VarDB<BigInteger>> total_for_votes = Context.newBranchDB(Prefix + "_total_for_votes", BigInteger.class);
    private final BranchDB<String, VarDB<Integer>> for_voters_count = Context.newBranchDB(Prefix + "_for_voters_count", Integer.class);
    private final BranchDB<String, VarDB<Integer>> against_voters_count = Context.newBranchDB(Prefix + "_against_voters_count", Integer.class);
    private final BranchDB<String, VarDB<BigInteger>> total_against_votes = Context.newBranchDB(Prefix + "_total_against_votes", BigInteger.class);
    private final BranchDB<String, VarDB<String> >status = Context.newBranchDB(Prefix + "_status", String.class);

    public static final String PENDING = "pending";
    public static final String ACTIVE = "active";
    public static final String CANCELLED = "cancelled";
    public static final String DEFEATED = "defeated";
    public static final String SUCCEEDED = "succeeded";
    public static final String NO_QUORUM = "no_quorum";
    public static final String EXECUTED = "executed";
    public static final String FAILED_EXECUTION = "failed_execution";


    public void createProposal(ProposalAttributes proposalAttributes, String proposalPrefix){
        this.proposer.at(proposalPrefix).set(proposalAttributes.proposerAddress);
        this.quorum.at(proposalPrefix).set(proposalAttributes.quorum);
        this.majority.at(proposalPrefix).set(proposalAttributes.majority);
        this.vote_snapshot.at(proposalPrefix).set(proposalAttributes.snapshot);
        this.end_snapshot.at(proposalPrefix).set(proposalAttributes.end);
        this.actions.at(proposalPrefix).set(proposalAttributes.actions);
        this.name.at(proposalPrefix).set(proposalAttributes.name);
        this.description.at(proposalPrefix).set(proposalAttributes.description);
        this.ipfs_hash.at(proposalPrefix).set(proposalAttributes.ipfsHash);
        this.status.at(proposalPrefix).set(ACTIVE);
        this.active.at(proposalPrefix).set(true);
    }

    public void setProposer(String proposalPrefix, Address address) {
        proposer.at(proposalPrefix).set(address);
    }

    public void setQuorum(String proposalPrefix, BigInteger quorum) {
        this.quorum.at(proposalPrefix).set(quorum);
    }

    public void setMajority(String proposalPrefix, BigInteger majority) {
        this.majority.at(proposalPrefix).set(majority);
    }

    public void setVoteSnapshot(String proposalPrefix, BigInteger voteSnapshot) {
        this.vote_snapshot.at(proposalPrefix).set(voteSnapshot);
    }

    public void setStartSnapshot(String proposalPrefix, BigInteger startSnapshot) {
        this.start_snapshot.at(proposalPrefix).set(startSnapshot);
    }

    public void setEndSnapshot(String proposalPrefix, BigInteger endSnapshot) {
        this.end_snapshot.at(proposalPrefix).set(endSnapshot);
    }

    public void setActions(String proposalPrefix, String actions) {
        this.actions.at(proposalPrefix).set(actions);
    }

    public void setName(String proposalPrefix, String name) {
        this.name.at(proposalPrefix).set(name);
    }

    public void setDescription(String proposalPrefix, String description) {
        this.description.at(proposalPrefix).set(description);
    }

    public void setIpfs_hash(String proposalPrefix, String ipfsHash) {
        this.ipfs_hash.at(proposalPrefix).set(ipfsHash);
    }

    public void setActive(String proposalPrefix, boolean active) {
        this.active.at(proposalPrefix).set(active);
    }

    public void setForVotesOfUser(String proposalPrefix, BigInteger votes, Address user) {
        this.for_votes_of_user.at(proposalPrefix).set(user, votes);
    }

    public void setAgainstVotesOfUser(String proposalPrefix, BigInteger votes, Address user) {
        this.against_votes_of_user.at(proposalPrefix).set(user, votes);
    }

    public void setTotalForVotes(String proposalPrefix, BigInteger votes) {
        this.total_for_votes.at(proposalPrefix).set(votes);
    }

    public void setForVotersCount(String proposalPrefix, int voters) {
        this.for_voters_count.at(proposalPrefix).set(voters);
    }

    public void setAgainstVotersCount(String proposalPrefix, int voters) {
        this.against_voters_count.at(proposalPrefix).set(voters);
    }

    public void setTotalAgainstVotes(String proposalPrefix, BigInteger votes) {
        this.total_against_votes.at(proposalPrefix).set(votes);
    }

    public void setStatus(String proposalPrefix, String status) {
        this.status.at(proposalPrefix).set(status);
    }

    public Address getProposer(String proposalPrefix) {
        return proposer.at(proposalPrefix).get();
    }

    public BigInteger getQuorum(String proposalPrefix) {
        return quorum.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
    }

    public BigInteger getMajority(String proposalPrefix) {
        return majority.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
    }

    public BigInteger getVote_snapshot(String proposalPrefix) {
        return vote_snapshot.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
    }

    public BigInteger getStart_snapshot(String proposalPrefix) {
        return start_snapshot.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
    }

    public BigInteger getEnd_snapshot(String proposalPrefix) {
        return end_snapshot.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
    }

    public String getActions(String proposalPrefix) {
        return actions.at(proposalPrefix).getOrDefault("");
    }

    public String getName(String proposalPrefix) {
        return name.at(proposalPrefix).getOrDefault("");
    }

    public String getDescription(String proposalPrefix) {
        return description.at(proposalPrefix).getOrDefault("");
    }

    public String getIpfs_hash(String proposalPrefix) {
        return ipfs_hash.at(proposalPrefix).getOrDefault("");
    }

    public boolean getActive(String proposalPrefix) {
        return active.at(proposalPrefix).getOrDefault(false);
    }

    public BigInteger getForVotesOfUser(String proposalPrefix, Address user) {
        return for_votes_of_user.at(proposalPrefix).getOrDefault(user, BigInteger.ZERO);
    }

    public BigInteger getAgainstVotesOfUser(String proposalPrefix, Address user) {
        return against_votes_of_user.at(proposalPrefix).getOrDefault(user, BigInteger.ZERO);
    }

    public BigInteger getTotalForVotes(String proposalPrefix) {
        return total_for_votes.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
    }

    public int getForVotersCount(String proposalPrefix) {
        return for_voters_count.at(proposalPrefix).getOrDefault(0);
    }

    public int getAgainstVotersCount(String proposalPrefix) {
        return against_voters_count.at(proposalPrefix).getOrDefault(0);
    }

    public BigInteger getTotalAgainstVotes(String proposalPrefix) {
        return total_against_votes.at(proposalPrefix).getOrDefault(BigInteger.ZERO);
    }

    public String getStatus(String proposalPrefix) {
        return status.at(proposalPrefix).getOrDefault("");
    }
}



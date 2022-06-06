package com.iconbet.score.proposalssubmission;

import com.iconbet.score.proposalssubmission.db.ProgressReportData;
import com.iconbet.score.proposalssubmission.db.ProposalData;
import com.iconbet.score.proposalssubmission.utils.ArrayDBUtils;
import static com.iconbet.score.proposalssubmission.utils.consts.*;
import score.Address;
import score.VarDB;
import score.ArrayDB;
import score.DictDB;
import score.Context;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import scorex.util.ArrayList;

import com.iconbet.score.proposalssubmission.db.ProposalData.*;
import com.iconbet.score.proposalssubmission.db.ProgressReportData.*;
import static com.iconbet.score.proposalssubmission.db.ProposalData.proposalPrefix;
import static com.iconbet.score.proposalssubmission.db.ProgressReportData.progressReportPrefix;

public class ProposalsSubmission {
    public static final String TAG = "ICONbet: Proposal Submission";
    private static final String PENDING = "_pending";
    private static final String ACTIVE = "_active";
    private static final String PAUSED = "_paused";
    private static final String COMPLETED = "_completed";
    private static final String REJECTED = "_rejected";
    private static final String DISQUALIFIED = "_disqualified";

    private static final String WAITING = "_waiting";
    private static final String APPROVED = "_approved";
    private static final String PROGRESS_REPORT_REJECTED = "_progress_report_rejected";

    private final VarDB<Address> tapTokenScore = Context.newVarDB(TAP_TOKEN_SCORE, Address.class);
    private final VarDB<Address> daoFundScore = Context.newVarDB(DAOFUND_SCORE, Address.class);
    private final VarDB<BigInteger> initialBlock = Context.newVarDB(INITIAL_BLOCK, BigInteger.class);
    private final VarDB<String> periodName = Context.newVarDB(PERIOD_NAME, String.class);
    private final VarDB<String> previousPeriodName = Context.newVarDB(PREVIOUS_PERIOD_NAME, String.class);
    private final VarDB<BigInteger> nextBlock = Context.newVarDB(NEXTBLOCK, BigInteger.class);
    private final VarDB<Integer> updatePeriodIndex = Context.newVarDB(UPDATE_PERIOD_INDEX, Integer.class);

    private final ArrayDB<String> proposalsKeyList = Context.newArrayDB(PROPOSALS_KEY_LIST, String.class);
    private final DictDB<String, Integer> proposalsKeyListIndex = Context.newDictDB(PROPOSALS_KEY_LIST_INDEX, Integer.class);

    private final ArrayDB<String> progressKeyList = Context.newArrayDB(PROGRESS_KEY_LIST, String.class);
    private final DictDB<String, Integer> progressKeyListIndex = Context.newDictDB(PROGRESS_KEY_LIST_INDEX, Integer.class);

    private final ArrayDB<String> budgetApprovalsList = Context.newArrayDB(BUDGET_APPROVALS_LIST, String.class);

    private final ArrayDB<String> activeProposals = Context.newArrayDB(ACTIVE_PROPOSALS, String.class);

    private final ArrayDB<Address> proposers = Context.newArrayDB(PROPOSERS, Address.class);
    private final ArrayDB<Address> admins = Context.newArrayDB(ADMINS, Address.class);


    private final ArrayDB<String> pending = Context.newArrayDB(PENDING, String.class);
    private final ArrayDB<String> active = Context.newArrayDB(ACTIVE, String.class);
    private final ArrayDB<String> paused = Context.newArrayDB(PAUSED, String.class);
    private final ArrayDB<String> completed = Context.newArrayDB(COMPLETED, String.class);
    private final ArrayDB<String> rejected = Context.newArrayDB(REJECTED, String.class);
    private final ArrayDB<String> disqualified = Context.newArrayDB(DISQUALIFIED, String.class);

    private final ArrayDB<String> waitingProgressReport = Context.newArrayDB(WAITING, String.class);
    private final ArrayDB<String> approvedProgressReport = Context.newArrayDB(APPROVED_REPORTS, String.class);
    private final ArrayDB<String> reportRejected = Context.newArrayDB(REJECTED_REPORTS, String.class);
    private final Map<String, ArrayDB<String>> proposalsStatus = Map.of(
            PENDING, this.pending,
            ACTIVE, this.active,
            PAUSED, this.paused,
            COMPLETED, this.completed,
            REJECTED, this.rejected,
            DISQUALIFIED, this.disqualified
    );

    private final Map<String, ArrayDB<String>> progressReportStatus = Map.of(
            WAITING, this.waitingProgressReport,
            APPROVED, this.approvedProgressReport,
            PROGRESS_REPORT_REJECTED, this.reportRejected
    );

    public ProposalsSubmission() {
    }

    @External(readonly = true)
    public String name() {
        return TAG;
    }

    private void validateOwner() {
        Context.require(Context.getCaller().equals(Context.getOwner()), "Only owner can call this method");
    }

    @External
    public void addAdmin(Address _address) {
        validateOwner();
        Context.require(!ArrayDBUtils.containsInArrayDb(_address, admins), "Address already an admin");
        this.admins.add(_address);
    }

    private void validateAdmins(Address _address) {
        Context.require(ArrayDBUtils.containsInArrayDb(_address, admins), "Only admin can call this method.");
    }

    @External(readonly = true)
    public List<Address> getAdmins() {
        List<Address> adminList = new ArrayList<>();
        for (int i = 0; i < this.admins.size(); i++) {
            adminList.add(this.admins.get(i));
        }
        return adminList;
    }

    @External
    public void removeAdmin(Address _address) {
        validateOwner();
        if (_address.equals(Context.getOwner())) {
            Context.revert("Owner cannot be removed from admin list");
        }
        if (!ArrayDBUtils.removeArrayItem(this.admins, _address)) {
            Context.revert("The provided address could not be removed from admin list. Check if the address is in admin list.");
        }
    }

    @External
    public void setDaoFundScore(Address _score) {
        validateAdmins(Context.getCaller());
        Context.require(_score.isContract(), "The given address is not a contract address");
        this.daoFundScore.set(_score);
    }

    @External(readonly = true)
    public Address getDaoFundScore() {
        return this.daoFundScore.get();
    }

    @External
    public void setTapTokenScore(Address _score) {
        validateAdmins(Context.getCaller());
        Context.require(_score.isContract(), "The given address is not a contract address");
        this.tapTokenScore.set(_score);
    }

    @External(readonly = true)
    public Address getTapTokenScore() {
        return this.tapTokenScore.get();
    }

    private void removeProposer(Address _address) {
        Context.require(ArrayDBUtils.containsInArrayDb(_address, this.proposers),
                "The provided address could not be removed from proposer list. " +
                        "Check if the address is in proposer list.");
        ArrayDBUtils.removeArrayItem(this.proposers, _address);
    }


    @External(readonly = true)
    public BigInteger getRemainingFund() {
        return Context.getBalance(this.daoFundScore.get());
    }

    @External
    public void submitProposal(ProposalAttributes _proposals) {
        updatePeriod();
        if (this.periodName.get().equals(VOTING_PERIOD)) {
            Context.revert("Proposals cannot be submitted in voting period. " + TAG);
        }

        Context.require(!Context.getCaller().isContract(), "Contracts cannot send proposal. " + TAG);
        Context.require(!(_proposals.projectDuration > MAX_PROJECT_PERIOD), "Maximum Project Duration is " + MAX_PROJECT_PERIOD + " " + TAG);
        BigInteger remaining_fund = getRemainingFund();
        if (_proposals.totalBudget.compareTo(remaining_fund) > 0) {
            Context.revert(TAG + "Budget Exceeds than Daofund Treasury Amount. remaining_fund: " + remaining_fund.toString() + ", total_budget: " + _proposals.totalBudget.toString());
        }
        BigInteger staked_balance = (BigInteger) Context.call(this.tapTokenScore.get(), "staked_balanceOf", Context.getCaller());
        if (staked_balance.compareTo(MINIMUM_TAP_TO_SUBMIT_PROPOSAL) < 0) {
            Context.revert(TAG + "Must stake atleast " + MINIMUM_TAP_TO_SUBMIT_PROPOSAL + " to submit proposal.");
        }

        addProposals(_proposals);
        this.pending.add(_proposals.ipfsHash);
        this.proposers.add(Context.getCaller());
        ProposalSubmitted(Context.getCaller(), "Proposal for " + _proposals.projectTitle + " is submitted successfully.");
    }


    private void addProposals(ProposalAttributes _proposals) {
        if (this.proposalsKeyListIndex.getOrDefault(_proposals.ipfsHash, 0).equals(0)) {
            this.proposalsKeyList.add(_proposals.ipfsHash);
            this.proposalsKeyListIndex.set(_proposals.ipfsHash, this.proposalsKeyList.size());
            String proposalPrefix = proposalPrefix(_proposals.ipfsHash);
            ProposalData proposalData = new ProposalData();
            proposalData.addDataToProposalDB(_proposals, proposalPrefix);

            proposalData.setTimestamp(proposalPrefix, BigInteger.valueOf(Context.getBlockTimestamp()));
            proposalData.setProposerAddress(proposalPrefix, Context.getCaller());
            proposalData.setTxHash(proposalPrefix, recordTxHash(Context.getTransactionHash()));
            proposalData.setPercentageCompleted(proposalPrefix, 0);
            proposalData.setStatus(proposalPrefix, PENDING);


        } else {
            Context.revert(TAG + " The ipfs_hash already exists " + _proposals.ipfsHash);
        }
    }


    @External(readonly = true)
    public Map<String, ?> getProposalDetailsByHash(String ipfs_hash) {
        return _getProposalDetails(proposalPrefix(ipfs_hash));
    }

    @External(readonly = true)
    public Map<String, ?> getProposalDetails(String _status, Address _wallet_address, @Optional  int _start_index, @Optional  int _end_index) {
        if (_end_index == 0){
            _end_index = 20;
        }
        List<Map<String, ?>> proposals_list = new ArrayList<>();
        List<String> proposals_keys = _getProposalsKeysByStatus(_status);
        if ((_end_index - _start_index) > 50) {
            Context.revert("Page Length cannot be greater than 50");
        }
        int count = this.proposalsStatus.get(_status).size();

        if (_start_index < 0 || _start_index > count) {
            _start_index = 0;
        }

        if (_end_index > count) {
            _end_index = count;
        }

        for (int i = _start_index; i < _end_index; i++) {
            Map<String, ?> proposal_details = _getProposalDetails(proposalPrefix(proposals_keys.get(i)));
            if (proposal_details.get(STATUS).equals(_status)) {
                proposals_list.add(proposal_details);
            }
        }
        return Map.of(
                "DATA", proposals_list,
                "COUNT", proposals_list.size()
        );
    }

    @External(readonly = true)
    public List<Map<String, ?>> getActiveProposals(Address _wallet_address) {
        List<Map<String, ?>> proposalTitles = new ArrayList<>();
        for (int i = 0; i < this.proposalsKeyList.size(); i++) {
            String proposalKey = this.proposalsKeyList.get(i);
            String proposalPrefix = proposalPrefix(proposalKey);
            Map<String, ?> proposalDetails = _getProposalDetails(proposalKey);
            if (proposalDetails.get(STATUS).equals(ACTIVE) || proposalDetails.get(STATUS).equals(PAUSED)) {
                if (proposalDetails.get(PROPOSER_ADDRESS).equals(_wallet_address)) {
                    ProposalData proposalData = new ProposalData();
                    int projectDuration = (int) proposalDetails.get(PROJECT_DURATION);
                    int approvedReportsCount = (int) proposalDetails.get(APPROVED_REPORTS);
                    Boolean lastProgressReport = Boolean.FALSE;

                    if (projectDuration - approvedReportsCount == 1) {
                        lastProgressReport = Boolean.TRUE;
                    }
                    Map<String, ?> proposalsDetails = Map.of(
                            PROJECT_TITLE, proposalDetails.get(PROJECT_TITLE),
                            IPFS_HASH, proposalDetails.get(proposalKey),
                            NEW_PROGRESS_REPORT, proposalData.getSubmitProgressReport(proposalPrefix),
                            LAST_PROGRESS_REPORT, lastProgressReport
                    );
                    proposalTitles.add(proposalsDetails);
                }
            }
        }
        return proposalTitles;
    }

    @External(readonly = true)
    public List<Map<String, ?>> getProposalDetailByWallet(Address _wallet_address) {
        List<Map<String, ?>> proposalTitles = new ArrayList<>();
        for (int i = 0; i < this.proposalsKeyList.size(); i++) {
            String proposalHash = this.proposalsKeyList.get(i);
            Map<String, ?> proposalDetails = _getProposalDetails(proposalHash);
            if (proposalDetails.get(PROPOSER_ADDRESS).equals(_wallet_address)) {
                proposalTitles.add(proposalDetails);
            }
        }
        return proposalTitles;
    }

    private Map<String, ?> _getProposalDetails(String ipfsHash) {
        String proposalPrefix = proposalPrefix(ipfsHash);
        ProposalData proposalData = new ProposalData();
        return proposalData.getDataFromProposalDB(ipfsHash);
    }

    private BigInteger getStake() {
        return (BigInteger) Context.call(this.tapTokenScore.get(), "staked_balanceOf", Context.getCaller());
    }

    @External(readonly = true)
    public Map<String, Map<String, ?>> getProjectAmounts() {
        String[] status_list = new String[5];
        status_list[0] = PENDING;
        status_list[1] = ACTIVE;
        status_list[2] = PAUSED;
        status_list[3] = COMPLETED;
        status_list[4] = DISQUALIFIED;

        BigInteger _pending_amount = BigInteger.ZERO;
        BigInteger _active_amount = BigInteger.ZERO;
        BigInteger _paused_amount = BigInteger.ZERO;
        BigInteger _completed_amount = BigInteger.ZERO;
        BigInteger _disqualified_amount = BigInteger.ZERO;

        for (String status : status_list) {
            BigInteger _amount = BigInteger.ZERO;
            for (String keys : _getProposalsKeysByStatus(status)) {
                _amount = _amount.add((BigInteger) _getProposalDetails(keys).get(TOTAL_BUDGET));
            }

            switch (status) {
                case PENDING:
                    _pending_amount = _amount;
                    break;
                case ACTIVE:
                    _active_amount = _amount;
                    break;
                case PAUSED:
                    _paused_amount = _amount;
                    break;
                case COMPLETED:
                    _completed_amount = _amount;
                    break;
                case DISQUALIFIED:
                    _disqualified_amount = _amount;
                    break;
            }
        }
        return Map.of(PENDING, Map.of(AMOUNT, _pending_amount, "_count", _getProposalsKeysByStatus(PENDING).size()),
                ACTIVE, Map.of(AMOUNT, _active_amount, "_count", _getProposalsKeysByStatus(ACTIVE).size()),
                PAUSED, Map.of(AMOUNT, _paused_amount, "_count", _getProposalsKeysByStatus(PAUSED).size()),
                COMPLETED, Map.of(AMOUNT, _completed_amount, "_count", _getProposalsKeysByStatus(COMPLETED).size()),
                DISQUALIFIED, Map.of(AMOUNT, _disqualified_amount, "_count", _getProposalsKeysByStatus(DISQUALIFIED).size()));
    }

    @External(readonly = true)
    public List<Address> getProposers(int start_index, int end_index) {
        List<Address> proposers = new ArrayList<>();

        for (int i = 0; i < this.proposers.size(); i++) {
            proposers.add(this.proposers.get(i));
        }
        return proposers;
    }

    @External
    public void voteProposal(String _ipfs_key, String _vote, String _vote_reason) {
        updatePeriod();
        Context.require(this.periodName.get().equals(VOTING_PERIOD), TAG + " Proposals can be voted on Voting period only.");
        BigInteger staked_balances = (BigInteger) Context.call(this.tapTokenScore.get(), "staked_balanceOf", Context.getCaller());
        Context.require(staked_balances.compareTo(MINIMUM_TAP_TO_VOTE) >= 0, "Must stake at least " + MINIMUM_TAP_TO_VOTE + " tap to vote.");
        Context.require(List.of(ABSTAIN,APPROVE,REJECT).contains(_vote), TAG + " Vote should be on _approve, _reject, _abstain");

        Context.require(!isInProposalVotersList(Context.getCaller(), _ipfs_key), TAG + " Already voted on this proposal");
        String proposalPrefix = proposalPrefix(_ipfs_key);
        ProposalData proposalData = new ProposalData();
        if (proposalData.getStatus(proposalPrefix).equals(PENDING)) {
            BigInteger _voter_stake = getStake();
            BigInteger _total_votes = proposalData.getTotalVotes(proposalPrefix);
            BigInteger _approved_votes = proposalData.getApprovedVotes(proposalPrefix);
            BigInteger _rejected_votes = proposalData.getRejectedVotes(proposalPrefix);

            proposalData.setVotersList(proposalPrefix, Context.getCaller());
            proposalData.setTotalVoters(proposalPrefix, proposalData.getVotersList(proposalPrefix).size());
            proposalData.setTotalVotes(proposalPrefix, _total_votes.add(_voter_stake));
            proposalData.setVotersReasons(proposalPrefix, _vote_reason);

            if (_vote.equals(APPROVE)) {
                proposalData.setApproveVoters(proposalPrefix, Context.getCaller());
                proposalData.setApprovedVotes(proposalPrefix, _approved_votes.add(_voter_stake));

            } else if (_vote.equals(REJECT)) {
                proposalData.setRejectVoters(proposalPrefix, Context.getCaller());
                proposalData.setRejectedVotes(proposalPrefix, _rejected_votes.add(_voter_stake));
            }
        }
        VotedSuccessfully(Context.getCaller(), proposalData.getProjectType(proposalPrefix)
                + " Proposal vote for "
                + proposalData.getProjectTitle(proposalPrefix)
                + " is successful.");
    }

    @External
    public void submitProgressReport(ProgressReportAttributes _progress) {
        updatePeriod();
        Context.require(this.periodName.getOrDefault("None").equals(APPLICATION_PERIOD),
                TAG + " Proposals can only be submitted on application period.");
        Context.require(!Context.getCaller().isContract(), TAG + " Contract addresses are not supported.");

        ProposalData proposalData = new ProposalData();
        ProgressReportData progressReportData = new ProgressReportData();
        String proposalPrefix = proposalPrefix(_progress.ipfsHash);
        String progress = progressReportPrefix(_progress.reportHash);

        Context.require(Context.getCaller().equals(proposalData.getProposerAddress(proposalPrefix)), TAG + " Sorry, you are not the proposer for the project");

        if (proposalData.getSubmitProgressReport(proposalPrefix)) {
            Context.revert(TAG + " Progress report is already submitted in this cycle.");
        }

        if (_progress.budgetAdjustment) {

            if (!proposalData.getBudgetAdjustment(proposalPrefix)) {
                BigInteger remaining_fund = getRemainingFund();
                if (_progress.additionalBudget.compareTo(remaining_fund) > 0) {
                    Context.revert(TAG + " Additional budget exceeds the DAODund Treasury Amount. " + remaining_fund);
                }
                this.budgetApprovalsList.add(_progress.reportHash);
                progressReportData.setBudgetAdjustmentStatus(progress, PENDING);
                proposalData.setBudgetAdjustment(proposalPrefix);
            } else {
                Context.revert(TAG + " Budget adjustment is already submitted for this proposal");
            }
        }

        if (0 <= _progress.percentageCompleted && _progress.percentageCompleted <= 100) {
            proposalData.setPercentageCompleted(proposalPrefix, _progress.percentageCompleted);
        } else {
            Context.revert(TAG + " Not a valid percentage value");
        }
        proposalData.setSubmitProgressReport(proposalPrefix, Boolean.TRUE);

        addProgressReport(_progress);
        this.waitingProgressReport.add(_progress.reportHash);

        ProgressReportSubmitted(Context.getCaller(), _progress.progressReportTitle +
                " --> Progress report submitted successfully.");

    }

    private void addProgressReport(ProgressReportAttributes progressReportAttributes) {
        String reportHash = progressReportAttributes.reportHash;
        if (progressKeyListIndex.getOrDefault(reportHash, 0) == 0) {
            addNewProgressReportKey(progressReportAttributes.ipfsHash, reportHash);
            ProgressReportData progressReportData = new ProgressReportData();
            String progressPrefix = progressReportPrefix(reportHash);
            progressReportData.addDataToProgressReportDB(progressReportAttributes, progressPrefix);
            progressReportData.setTimestamp(progressPrefix, BigInteger.valueOf(Context.getBlockTimestamp()));
            progressReportData.setTxHash(progressPrefix, recordTxHash(Context.getTransactionHash()));
            progressReportData.setBudgetAdjustmentStatus(progressPrefix, "N/A");
            progressReportData.setStatus(progressPrefix, WAITING);
        }
    }

    @External(readonly = true)
    public Map<String, ?> getProgressReportDetailsByHash(String report_hash) {
        return _getProgressReportDetails(report_hash);
    }

    @External(readonly = true)
    public Map<String, ?> getProgressReportDetails(String _status, int _start_index, int _end_index) {
        List<Map<String, ?>> progressList = new ArrayList<>();
        List<String> progressKeys = getProgressReportKeysByStatus(_status);
        if ((_end_index - _start_index) > 50) {
            Context.revert("Page Length cannot be greater than 50");
        }
        int count = this.progressReportStatus.get(_status).size();

        if (_start_index < 0 || _start_index > count) {
            _start_index = 0;
        }

        if (_end_index > count) {
            _end_index = count;
        }

        for (int i = _start_index; i < _end_index; i++) {
            Map<String, ?> proposal_details = _getProgressReportDetails(progressReportPrefix(progressKeys.get(i)));
            if (proposal_details.get(STATUS).equals(_status)) {
                progressList.add(proposal_details);
            }
        }
        return Map.of(
                "DATA", progressList,
                "COUNT", progressList.size()
        );
    }


    private Map<String, ?> _getProgressReportDetails(String report_hash) {
        ProgressReportData progressReportData = new ProgressReportData();
        String progressPrefix = progressReportPrefix(report_hash);
        return progressReportData.getDataFromProposalDB(progressPrefix);
    }

    @External
    public void voteProgressReport(String ipfs_hash, String report_hash, String _vote, String _vote_reason, String budget_adjustment_vote) {
        updatePeriod();
        Context.require(this.periodName.getOrDefault("None").equals(VOTING_PERIOD),
                TAG + ": Progress reports can be voted only on voting period");
        BigInteger staked_balance = (BigInteger) Context.call(this.tapTokenScore.get(), "staked_balanceOf", Context.getCaller());
        Context.require(staked_balance.compareTo(MINIMUM_TAP_TO_VOTE) > 0, TAG + ": Must stake at least " + MINIMUM_TAP_TO_VOTE + " tap to vote progress report.");
        Context.require(!isInProgressReportVotersList(Context.getCaller(), report_hash), "Already voted for this progress_report");
        ProgressReportData progressReportData = new ProgressReportData();
        String progressPrefix = progressReportPrefix(report_hash);
        if (progressReportData.getStatus(progressPrefix).equals(WAITING)) {
            BigInteger _voter_stake = getStake();
            BigInteger _total_votes = progressReportData.getTotalVotes(progressPrefix);
            BigInteger _approved_votes = progressReportData.getApprovedVotes(progressPrefix);
            BigInteger _rejected_votes = progressReportData.getRejectedVotes(progressPrefix);

            progressReportData.setVotersList(progressPrefix, Context.getCaller());
            progressReportData.setTotalVoters(progressPrefix, progressReportData.getVotersList(progressPrefix).size());
            progressReportData.setTotalVotes(progressPrefix, _total_votes.add(_voter_stake));
            progressReportData.setVotersReasons(progressPrefix, _vote_reason);

            if (_vote.equals(APPROVE)) {
                progressReportData.setApproveVoters(progressPrefix, Context.getCaller());
                progressReportData.setApprovedVotes(progressPrefix, _approved_votes.add(_voter_stake));
            } else if (_vote.equals(REJECT)) {
                progressReportData.setRejectVoters(progressPrefix, Context.getCaller());
                progressReportData.setRejectedVotes(progressPrefix, _rejected_votes.add(_voter_stake));
            }

            if (isInBudgetApprovalList(report_hash)) {
                BigInteger _budget_approved_votes = progressReportData.getBudgetApprovedVotes(progressPrefix);
                BigInteger _budget_rejected_votes = progressReportData.getBudgetRejectedVotes(progressPrefix);

                if (budget_adjustment_vote.equals(APPROVE)) {
                    progressReportData.setBudgetApproveVoters(progressPrefix, Context.getCaller());
                    progressReportData.setBudgetApprovedVotes(progressPrefix, _budget_approved_votes.add(_voter_stake));
                } else if (budget_adjustment_vote.equals(REJECT)) {
                    progressReportData.setBudgetRejectVoters(progressPrefix, Context.getCaller());
                    progressReportData.setBudgetRejectedVotes(progressPrefix, _budget_rejected_votes.add(_voter_stake));
                }

            }
            VotedSuccessfully(Context.getCaller(), "Progress report vote for " +
                    progressReportData.getProgressReportTitle(progressPrefix)
                    + "is successful.");
        }
    }

    @External
    public void setInitialBlock() {
        validateOwner();
        this.initialBlock.set(BigInteger.valueOf(Context.getBlockHeight()));
        this.nextBlock.set(BigInteger.valueOf(Context.getBlockHeight() + BLOCKS_DAY_COUNT * DAY_COUNT));
        this.periodName.set(APPLICATION_PERIOD);
        this.previousPeriodName.set("None");
    }

    @External
    public void updatePeriod() {
        BigInteger currentBlockHeight = BigInteger.valueOf(Context.getBlockHeight());
        if (currentBlockHeight.compareTo(this.nextBlock.get()) > 0) {
            if (this.periodName.get().equals(APPLICATION_PERIOD)) {
                this.periodName.set(VOTING_PERIOD);
                this.previousPeriodName.set(APPLICATION_PERIOD);
                this.nextBlock.set(this.nextBlock.get().add(BigInteger.valueOf(BLOCKS_DAY_COUNT * DAY_COUNT)));
                updateApplicationResult();
                this.updatePeriodIndex.set(0);
            } else {
                int update_period_index = this.updatePeriodIndex.get();
                if (update_period_index == 0) {
                    this.periodName.set(TRANSITION_PERIOD);
                    this.previousPeriodName.set(APPLICATION_PERIOD);
                    this.updatePeriodIndex.set(1);
                    updateProposalsResult();
                    PeriodUpdate(
                            "1/4. Period Updated to Transition Period. After all the calculations are completed, " +
                                    "Period will change to " + APPLICATION_PERIOD);
                } else if (update_period_index == 1) {
                    checkProgressReportSubmission();
                    this.updatePeriodIndex.set(2);
                    PeriodUpdate("2/4. Progress reports checks completed.");
                } else if (update_period_index == 2) {
                    updateProgressReportResult();
                    this.updatePeriodIndex.set(3);
                    PeriodUpdate("3/4. Progress Reports Calculations Completed.");
                } else {
                    this.nextBlock.set(this.nextBlock.get().add(BigInteger.valueOf(BLOCKS_DAY_COUNT * DAY_COUNT)));
                    this.periodName.set(APPLICATION_PERIOD);
                    this.previousPeriodName.set(VOTING_PERIOD);
                    PeriodUpdate("4/4. Period Successfully updated to Application Period.");
                }
            }
        }
    }

    private void updateApplicationResult() {
        if (_getProposalsKeysByStatus(PENDING).size() == 0 && this.progressReportStatus.get(WAITING).size() == 0) {
            this.periodName.set(APPLICATION_PERIOD);
            PeriodUpdate("Period Updated back to Application Period due not enough Voting Proposals or Progress Reports.");
        } else {
            for (int i = 0; i < this.active.size(); i++) {
                if (isInActiveProposals(this.active.get(i))) {
                    this.activeProposals.add(this.active.get(i));
                }
            }

            for (int i = 0; i < this.paused.size(); i++) {
                if (isInActiveProposals(this.paused.get(i))) {
                    this.activeProposals.add(this.paused.get(i));
                }
            }
            PeriodUpdate("Period updated to Voting Period");
        }
    }

    private void updateProposalsResult() {
        List<String> pendingProposals = new ArrayList<>();
        for (int i = 0; i < this.pending.size(); i++) {
            pendingProposals.add(this.pending.get(i));
        }
        for (String pendingProposal : pendingProposals) {
            String proposalPrefix = proposalPrefix(pendingProposal);
            Map<String, ?> proposal_details = _getProposalDetails(proposalPrefix);

            BigInteger approved_votes = (BigInteger) proposal_details.get(APPROVED_VOTES);
            BigInteger total_votes = (BigInteger) proposal_details.get(TOTAL_VOTES);
            BigInteger total_budget = (BigInteger) proposal_details.get(TOTAL_BUDGET);
            Address proposer_address = (Address) proposal_details.get(PROPOSER_ADDRESS);
            int period_count = (int) proposal_details.get(PROJECT_DURATION);


            if (proposal_details.get(TOTAL_VOTES).equals("0")) {
                updateProposalStatus(pendingProposal, REJECTED);
            } else if (approved_votes.divide(total_votes).doubleValue() > MAJORITY) {
                updateProposalStatus(pendingProposal, ACTIVE);
                Context.call(this.daoFundScore.get(), "allocateFundsToProposals", pendingProposal, period_count, proposer_address, total_budget);
            } else {
                updateProposalStatus(pendingProposal, REJECTED);
                removeProposer(proposer_address);
            }
        }
    }

    private void updateProposalStatus(String proposal_hash, String _status) {
        String proposalPrefix = proposalPrefix(proposal_hash);
        ProposalData proposalData = new ProposalData();
        String current_status = proposalData.getStatus(proposalPrefix);
        proposalData.setTimestamp(proposalPrefix, BigInteger.valueOf(Context.getBlockTimestamp()));
        proposalData.setStatus(proposalPrefix, _status);
        ArrayDBUtils.removeArrayItem(this.proposalsStatus.get(current_status), proposal_hash);
        this.proposalsStatus.get(_status).add(proposal_hash);
    }

    private void checkProgressReportSubmission() {
        ProposalData proposalData = new ProposalData();
        for (int i = 0; i < this.activeProposals.size(); i++) {
            String ipfs_hash = this.activeProposals.get(i);
            String proposalPrefix = proposalPrefix(ipfs_hash);
            Map<String, ?> proposalDetails = _getProposalDetails(ipfs_hash);
            String proposalStatus = (String) proposalDetails.get(STATUS);
            Address proposerAddress = (Address) proposalDetails.get(PROPOSER_ADDRESS);

            if (proposalData.getSubmitProgressReport(proposalPrefix)) {
                if (proposalStatus.equals(ACTIVE)) {
                    updateProposalStatus(ipfs_hash, PAUSED);
                } else if (proposalStatus.equals(PAUSED)) {
                    updateProposalStatus(ipfs_hash, DISQUALIFIED);
                    Context.call(this.daoFundScore.get(), "disqualify_project", ipfs_hash);
                    removeProposer(proposerAddress);
                }
            }
        }
    }

    private void updateProgressReportResult() {
        List<String> waitingProgressReports = new ArrayList<>();
        for (int i = 0; i < this.waitingProgressReport.size(); i++) {
            waitingProgressReports.add(this.waitingProgressReport.get(i));
        }
        ProposalData proposalData = new ProposalData();

        for (String waiting_reports : waitingProgressReports) {
            Map<String, ?> reportResult = _getProgressReportDetails(waiting_reports);
            String ipfs_hash = (String) reportResult.get(IPFS_HASH);
            String proposalPrefix = proposalPrefix(ipfs_hash);
            Map<String, ?> proposalDetails = _getProposalDetails(ipfs_hash);
            proposalData.setSubmitProgressReport(proposalPrefix, Boolean.FALSE);

            String proposalStatus = (String) proposalDetails.get(STATUS);
            int approvedReportsCount = (int) proposalDetails.get(APPROVED_REPORTS);
            Address proposerAddress = (Address) proposalDetails.get(PROPOSER_ADDRESS);
            Boolean budgetAdjustment = (Boolean) reportResult.get(BUDGET_ADJUSTMENT);

            BigInteger approvedVotes = (BigInteger) reportResult.get(APPROVED_VOTES);
            BigInteger totalVotes = (BigInteger) reportResult.get(TOTAL_VOTES);

            if (budgetAdjustment) {
                updateBudgetAdjustments(waiting_reports);
            }

            int projectDuration = proposalData.getProjectDuration(proposalPrefix);

            if (approvedVotes.divide(totalVotes).doubleValue() >= MAJORITY) {
                updateProgressReportStatus(waiting_reports, APPROVED);
                approvedReportsCount = approvedReportsCount + 1;

                if (approvedReportsCount == projectDuration) {
                    updateProposalStatus(ipfs_hash, COMPLETED);
                } else if (proposalStatus.equals(PAUSED)) {
                    updateProposalStatus(ipfs_hash, ACTIVE);
                }
                proposalData.setApprovedReports(proposalPrefix, approvedReportsCount);
                Context.call(this.daoFundScore.get(), "send_installment_to_proposer", ipfs_hash);
            } else {
                updateProgressReportStatus(waiting_reports, PROGRESS_REPORT_REJECTED);
                if (proposalStatus.equals(ACTIVE)) {
                    updateProposalStatus(ipfs_hash, PAUSED);
                } else if (proposalStatus.equals(PAUSED)) {
                    updateProposalStatus(ipfs_hash, DISQUALIFIED);
                    Context.call(this.daoFundScore.get(), "disqualify_project", ipfs_hash);
                    removeProposer(proposerAddress);
                }
            }
        }
    }

    private void updateBudgetAdjustments(String report_hash) {
        String progressReportPrefix = progressReportPrefix(report_hash);
        ProgressReportData progressReportData = new ProgressReportData();

        Map<String, ?> reportResult = _getProgressReportDetails(report_hash);
        BigInteger approvedVotes = progressReportData.getBudgetApprovedVotes(progressReportPrefix);
        BigInteger totalVotes = progressReportData.getTotalVotes(progressReportPrefix);

        if (approvedVotes.divide(totalVotes).doubleValue() >= MAJORITY) {
            String ipfsHash = (String) reportResult.get(IPFS_HASH);
            String proposalPrefix = proposalPrefix(ipfsHash);
            ProposalData proposalData = new ProposalData();
            int periodCount = proposalData.getProjectDuration(proposalPrefix);
            int additionalDuration = (int) reportResult.get(ADDITIONAL_DURATION);
            BigInteger totalBudget = proposalData.getTotalBudget(proposalPrefix);
            BigInteger additionalBudget = (BigInteger) reportResult.get(ADDITIONAL_BUDGET);

            proposalData.setProjectDuration(proposalPrefix, periodCount + additionalDuration);
            proposalData.setTotalBudget(proposalPrefix, totalBudget.add(additionalBudget));
            progressReportData.setBudgetAdjustmentStatus(progressReportPrefix, APPROVED);
            Context.call(this.daoFundScore.get(), "update_proposal_fund", ipfsHash, additionalBudget, additionalDuration);
        } else {
            progressReportData.setBudgetAdjustmentStatus(progressReportPrefix, REJECTED);
        }
    }

    private void addNewProgressReportKey(String proposalKey, String progressKey) {
        this.progressKeyList.add(progressKey);
        this.progressKeyListIndex.set(progressKey, this.progressKeyList.size());
        ProposalData proposalData = new ProposalData();
        proposalData.setProgressReports(proposalPrefix(proposalKey), progressKey);
    }

    private void updateProgressReportStatus(String progress_report_key, String status) {
        ProgressReportData progressReportData = new ProgressReportData();
        String progressPrefix = progressReportPrefix(progress_report_key);
        String currentStatus = progressReportData.getStatus(progressPrefix);
        progressReportData.setTimestamp(progressPrefix, BigInteger.valueOf(Context.getBlockTimestamp()));
        progressReportData.setStatus(progressPrefix, status);
        ArrayDBUtils.removeArrayItem(this.progressReportStatus.get(currentStatus), progress_report_key);
        this.progressReportStatus.get(status).add(progress_report_key);
    }


    @External
    public void update_period() {
        this.nextBlock.set(BigInteger.valueOf(Context.getBlockHeight()));
    }

    @External(readonly = true)
    public Map<String, String> getPeriodStatus() {
        BigInteger remainingTime = (this.nextBlock.get().subtract(BigInteger.valueOf(Context.getBlockHeight()))).multiply(BigInteger.TWO);
        return Map.of(
                PERIOD_NAME, this.periodName.getOrDefault("None"),
                NEXTBLOCK, this.nextBlock.get().toString(),
                REMAINING_TIME, remainingTime.toString(),
                PREVIOUS_PERIOD_NAME, this.previousPeriodName.get(),
                PERIOD_SPAN, BigInteger.valueOf(BLOCKS_DAY_COUNT * DAY_COUNT * 2).toString());
    }

    private List<String> _getProposalsKeysByStatus(String status) {
        List<String> proposalKeys = new ArrayList<>();
        for (int i = 0; i < this.proposalsStatus.get(status).size(); i++) {
            proposalKeys.add(this.proposalsStatus.get(status).get(i));
        }
        return proposalKeys;
    }

    private List<String> _getProgressReportKeysByStatus(String status) {
        List<String> progressKeys = new ArrayList<>();
        for (int i = 0; i < this.progressReportStatus.get(status).size(); i++) {
            progressKeys.add(this.progressReportStatus.get(status).get(i));
        }
        return progressKeys;
    }

    @External(readonly = true)
    public List<String> getProposalsKeysByStatus(String status) {
        return _getProposalsKeysByStatus(status);
    }

    @External(readonly = true)
    public List<String> getProgressReportKeysByStatus(String status) {
        return _getProgressReportKeysByStatus(status);
    }


    @External
    public void setNextBlock(BigInteger block) {
        validateOwner();
        this.nextBlock.set(BigInteger.valueOf(Context.getBlockHeight()).add(block));
    }

    @External
    public void change_period() {
        if (this.periodName.get().equals(APPLICATION_PERIOD)) {
            this.periodName.set(VOTING_PERIOD);
        } else {
            this.periodName.set(APPLICATION_PERIOD);
        }
    }

    private String recordTxHash(byte[] tx_hash) {
        String tx_hash_string = encodeHexString(tx_hash);
        return "0x" + tx_hash_string;
    }

    private String encodeHexString(byte[] byteArray) {
        StringBuffer hexStringBuffer = new StringBuffer();
        for (byte b : byteArray) {
            hexStringBuffer.append(byteToHex(b));
        }
        return hexStringBuffer.toString();
    }

    private String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    @Payable
    public void fallback() {
        // just receive incoming funds
    }

    private boolean isInProposalVotersList(Address address, String ipfs_hash) {
        ProposalData proposalData = new ProposalData();
        String proposalPrefix = proposalPrefix(ipfs_hash);
        List<Address> votesList = proposalData.getVotersList(proposalPrefix);
        for (Address value : votesList) {
            if (address.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInProgressReportVotersList(Address address, String report_hash) {
        ProgressReportData progressReportData = new ProgressReportData();
        String progressPrefix = progressReportPrefix(report_hash);
        List<Address> votesList = progressReportData.getVotersList(progressPrefix);
        for (Address value : votesList) {
            if (address.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInBudgetApprovalList(String report_hash) {
        for (int i = 0; i < this.budgetApprovalsList.size(); i++) {
            if (report_hash.equals(this.budgetApprovalsList.get(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean isInActiveProposals(String proposal_hash) {
        for (int i = 0; i < this.activeProposals.size(); i++) {
            if (proposal_hash.equals(this.activeProposals.get(i))) {
                return false;
            }
        }
        return true;
    }

    @EventLog(indexed = 1)
    public void ProposalSubmitted(Address _sender_address, String note) {
    }

    @EventLog(indexed = 1)
    public void VotedSuccessfully(Address _sender_address, String note) {
    }

    @EventLog(indexed = 1)
    public void ProgressReportSubmitted(Address _sender_address, String note) {
    }

    @EventLog(indexed = 1)
    public void PeriodUpdate(String note) {
    }

    @External(readonly = true)
    public Address getProposer(String prefix){
        ProposalData proposalData = new ProposalData();
        return proposalData.getProposerAddress(prefix);
    }

}


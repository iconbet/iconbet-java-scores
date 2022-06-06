package com.iconbet.score.proposalsfund;

import score.*;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import scorex.util.ArrayList;

import com.iconbet.score.proposalsfund.db.ProposalData;
import com.iconbet.score.proposalsfund.utils.Constants;

public class ProposalsFund extends ProposalData {
    private static final String TAG = "ICONBet ProposalsFund";
    private static final String PROPOSAL_DB_PREFIX = "iconbetProposal";
    private static final String ID = "id";
    private static final String _PROPOSALS_KEYS = "_proposals_keys";
    private static final String _FUND_RECORD = "fund_record";

    private static final String _TOTAL_INSTALLMENT_COUNT = "_total_installment_count";
    private static final String _TOTAL_TIMES_INSTALLMENT_PAID = "_total_times_installment_paid";
    private static final String _TOTAL_TIMES_REWARD_PAID = "_total_times_reward_paid";
    private static final String _TOTAL_INSTALLMENT_PAID = "_total_installment_paid";
    private static final String _TOTAL_REWARD_PAID = "_total_reward_paid";
    private static final String _INSTALLMENT_AMOUNT = "installment_amount";

    private static final String _PROPOSAL_SUBMISSION_SCORE = "_proposal_submission_score";
    private static final String _DAOFUND_SCORE = "_daofund_score";

    private static final String _PROPOSER_ADDRESS = "proposer_address";
    private static final String _STATUS = "status";
    private static final String _IPFS_HASH = "ipfs_hash";
    private static final String _TOTAL_BUDGET = "total_budget";

    private static final String _ACTIVE = "active";
    private static final String _DISQUALIFIED = "disqualified";
    private static final String _COMPLETED = "completed";

    private final ArrayDB<String> proposalsKeys = Context.newArrayDB(_PROPOSALS_KEYS, String.class);
    private final DictDB<String, Integer> proposalKeyIndex = Context.newDictDB(_PROPOSALS_KEYS + "_index", Integer.class);
    private final DictDB<String, BigInteger> fundRecord = Context.newDictDB(_FUND_RECORD, BigInteger.class);

    private final VarDB<Address> daoFundScore = Context.newVarDB(_DAOFUND_SCORE, Address.class);
    private final VarDB<Address> proposalSubmissionScore = Context.newVarDB(_PROPOSAL_SUBMISSION_SCORE, Address.class);

    public ProposalsFund() {

    }

    @External(readonly = true)
    public String name() {
        return TAG;
    }

    @Payable
    public void fallback() {
        Context.revert(TAG + ": ICX can only be sent by ICONBet DAOFund.");
    }

    private String proposalPrefix(String proposalKey) {
        return PROPOSAL_DB_PREFIX + "|" + proposalKey;
    }

    private void validateOwner() {
        Context.require(Context.getCaller().equals(Context.getOwner()), TAG + ": Only owner can call this method.");
    }

    private void validateOwnerScore(Address score) {
        validateOwner();
        Context.require(score.isContract(), TAG + ": Target (" + score + ") is not a score");
    }

    private void validateProposalSubmissionScore() {
        Context.require(Context.getCaller().equals(this.proposalSubmissionScore.get()), TAG + " :Only ProposalSubmission(" + this.proposalSubmissionScore.get() + ") SCORE can send fund using this method.");
    }

    private void validateDaoFundScore() {
        Context.require(Context.getCaller().equals(this.daoFundScore.get()), TAG + " :Only DaoFund (" + this.daoFundScore.get() + ") SCORE can send fund using this method.");
    }

    private void addRecord(ProposalAttributes proposalAttributes) {
        String ipfsHash = proposalAttributes.ipfsHash;
        if (this.proposalKeyIndex.getOrDefault(ipfsHash, 0) == 0) {
            this.proposalsKeys.add(ipfsHash);
            this.proposalKeyIndex.set(ipfsHash, this.proposalsKeys.size());
            String proposalPrefix = proposalPrefix(ipfsHash);
            addDataToProposalDB(proposalAttributes, proposalPrefix);
            setStatus(proposalPrefix, _ACTIVE);
        } else {
            Context.revert(TAG + ": Already have this project.");
        }
    }

    private Map<String, ?> getProjects(String proposalKey) {
        String proposalPrefix = proposalPrefix(proposalKey);
        return getDataFromProposalDB(proposalPrefix);
    }

    /***
     Sets the proposal submission score address. Only owner can set the address.
     :param score: Address of the proposal submission score address
     :type score: :class:`iconservice.base.address.Address`
     :return:
     ***/
    @External
    public void setProposalSubmissionScore(Address score) {
        validateOwnerScore(score);
        this.proposalSubmissionScore.set(score);
    }

    @External(readonly = true)
    public Address getProposalSubmissionScore() {
        return proposalSubmissionScore.get();
    }

    @External
    public void setDaofundScore(Address score) {
        validateOwnerScore(score);
        this.daoFundScore.set(score);
    }

    @External(readonly = true)
    public Address getDaofundScore() {
        return this.daoFundScore.get();
    }

    @External(readonly = true)
    public Map<String, ?> get_proposer_projected_fund(Address _wallet_address) {
        BigInteger totalAmountToBePaidICX = BigInteger.ZERO;
        List<Map<String, ?>> projectDetails = new ArrayList<>();
        for (int i = 0; i < proposalsKeys.size(); i++) {
            String _ipfs_key = proposalsKeys.get(i);
            String proposalPrefix = proposalPrefix(_ipfs_key);
            // todo: getting entire proposal details or getting individual values?
            Map<String, ?> proposal_details = getDataFromProposalDB(proposalPrefix);
            if (!proposal_details.get(Constants.STATUS).equals(_DISQUALIFIED)) {
                if (proposal_details.get(Constants.PROPOSER_ADDRESS).equals(_wallet_address)) {
                    int totalInstallment = (int) proposal_details.get(Constants.PROJECT_DURATION);
                    int totalPaidCount = totalInstallment - (int) proposal_details.get(Constants.INSTALLMENT_COUNT);

                    if (totalPaidCount < totalInstallment) {
                        BigInteger totalBudget = (BigInteger) proposal_details.get(Constants.TOTAL_BUDGET);
                        BigInteger totalPaidAmount = (BigInteger) proposal_details.get(Constants.WITHDRAW_AMOUNT);

                        Map<String, ?> project_details = Map.of(
                                Constants.IPFS_HASH, _ipfs_key,
                                Constants.TOTAL_BUDGET, totalBudget,
                                Constants.TOTAL_INSTALLMENT_PAID, totalPaidAmount,
                                Constants.TOTAL_INSTALLMENT_COUNT, totalInstallment,
                                Constants.TOTAL_TIMES_INSTALLMENT_PAID, totalPaidCount,
                                Constants.INSTALLMENT_AMOUNT, totalBudget.divide(BigInteger.valueOf(totalInstallment)));

                        projectDetails.add(project_details);
                        totalAmountToBePaidICX = totalBudget.divide(BigInteger.valueOf(totalInstallment));
                    }
                }
            }
        }
        return Map.of(
                "data", projectDetails,
                "project_count", projectDetails.size(),
                "total_amount", Map.of("ICX", totalAmountToBePaidICX),
                "withdraw_amount_icx", this.fundRecord.getOrDefault(_wallet_address.toString(), BigInteger.ZERO));
    }

    /***
     Treasury Score sending the amount to the proposals fund Score
     :ipfs_hash: Proposal IPFS HASH key
     :project_duration: Total Duration month count
     :proposer_address: Proposer Address
     :total_budget: Total Budget for the project (LOOP)
     ***/
    @External
    @Payable
    public void depositProposalFund(ProposalAttributes _proposals) {
        validateDaoFundScore();
        addRecord(_proposals);
        ProposalFundDeposited(_proposals.ipfsHash, _proposals.totalBudget, "Received " + Context.getValue() + " fund from DAOFund");
    }

    /***
     After the budget adjustment is successfully approved. The added budget will be transferred from DAOFund

     :param _ipfs_key: Proposal IPFS HASH key
     :param _added_budget: added budget
     :type _added_budget: int
     :param _added_installment_count: Added duration in month
     :type _added_installment_count: int
     :return:
     ***/
    @External
    @Payable
    public void update_proposal_fund(String _ipfs_key, @Optional BigInteger _added_budget, @Optional int _added_installment_count) {

        validateDaoFundScore();

        String prefix = proposalPrefix(_ipfs_key);
        BigInteger _total_budget = getTotalBudget(prefix);
        int _total_duration = getProjectDuration(prefix);
        BigInteger _remaining_amount = getRemainingAmount(prefix);
        int installment_count = getInstallmentCount(prefix);

        if (this.proposalKeyIndex.getOrDefault(_ipfs_key, 0) > 0) {
            setTotalBudget(prefix, _total_budget.add(_added_budget));
            setProjectDuration(prefix, _total_duration + _added_installment_count);
            setRemainingAmount(prefix, _remaining_amount.add(_added_budget));
            setInstallmentCount(prefix, installment_count + _added_installment_count);

            ProposalFundDeposited(_ipfs_key, _added_budget,
                    _ipfs_key + "Added Budget : " + _added_budget + "and Added Time:" +
                            _added_installment_count + " Successfully");
        } else {
            Context.revert(TAG + " : IPFS Hash doesn't exist");
        }
    }

    /***
     Installment of the Proposal is send to the proposer after the progress report is approved by majority
     TAP-Holder(s)
     :param _ipfs_key: Proposal IPFS HASH key
     :return:
     ***/
    @External
    public void send_installment_to_proposer(String _ipfs_key) {
        validateProposalSubmissionScore();

        if (this.proposalKeyIndex.getOrDefault(_ipfs_key, 0) > 0) {
            String prefix = proposalPrefix(_ipfs_key);
            BigInteger _installment_amount = BigInteger.ZERO;
            int _installment_count = getInstallmentCount(prefix);
            BigInteger withdraw_amount = getWithdrawAmount(prefix);
            BigInteger remaining_amount = getRemainingAmount(prefix);
            Address proposer_address = getProposerAddress(prefix);

            Context.require(_installment_count != 0, TAG + ": Installments for the proposal has been completed.");

//            Calculating Installment Amount and adding to Wallet Address
            if (_installment_count == 1) {
                _installment_amount = remaining_amount;
            } else {
                _installment_amount = remaining_amount.divide(BigInteger.valueOf(_installment_count));
            }
            setInstallmentCount(prefix, _installment_count - 1);
            setRemainingAmount(prefix, remaining_amount.subtract(_installment_amount));
            setWithdrawAmount(prefix, withdraw_amount.add(_installment_amount));
            this.fundRecord.set(proposer_address.toString(), this.fundRecord.getOrDefault(proposer_address.toString(), BigInteger.ZERO).add(_installment_amount));

            ProposalFundSent(proposer_address, _installment_amount,
                    "New Installment sent to proposer's address.");

            if (getInstallmentCount(prefix) == 0) {
                setStatus(prefix, _COMPLETED);
            }
        } else {
            Context.revert(TAG + "Proposal hash not found");
        }
    }

    /***
     In case, Proposer fails to pass the progress report twice in a row, the project get disqualified.
     The remaining amount of the project is sent back to the DAOFund.
     :param _ipfs_key: Proposal IPFS HASH key
     ***/
    @External
    public void disqualify_project(String _ipfs_key) {
        validateProposalSubmissionScore();

        if (this.proposalKeyIndex.getOrDefault(_ipfs_key, 0) > 0) {
            String prefix = proposalPrefix(_ipfs_key);
//            #Set Proposal status to disqualified
            setStatus(prefix, _DISQUALIFIED);

            BigInteger _total_budget = getTotalBudget(prefix);
            BigInteger _withdraw_amount = getWithdrawAmount(prefix);

            BigInteger _remaining_budget = _total_budget.subtract(_withdraw_amount);

//            return remaining fund amount to the DAOFund
            Context.call(_remaining_budget, daoFundScore.get(), "disqualify_proposal_fund", _ipfs_key);

            ProposalDisqualified(_ipfs_key, _ipfs_key + ", Proposal disqualified");
        } else {
            Context.revert(TAG + ": Provided IPFS key not found.");
        }
    }

    /***
     Claim the reward or the installment amount
     ***/
    @External
    public void claim_reward() {

        Address caller = Context.getCaller();
        BigInteger _available_amount = this.fundRecord.getOrDefault(caller.toString(), BigInteger.ZERO);
        if (_available_amount.compareTo(BigInteger.ZERO) > 0) {
//            set the remaining fund 0
            this.fundRecord.set(caller.toString(), BigInteger.ZERO);
            Context.transfer(caller, _available_amount);
            ProposalFundWithdrawn(caller, _available_amount, _available_amount + "withdrawn to " +
                    caller);
        }

        else {
            Context.revert(TAG+ ":Claim Reward Fails. Available Amount = " + _available_amount + ".");
        }
    }

    @EventLog(indexed = 1)
    public void ProposalFundDeposited(String _ipfs_key, BigInteger _total_budget, String note) {
    }

    @EventLog(indexed = 1)
    public void ProposalFundSent(Address _receiver_address, BigInteger _fund, String note) {
    }

    @EventLog(indexed = 1)
    public void ProposalFundWithdrawn(Address _receiver_address, BigInteger _fund, String note) {
    }

    @EventLog(indexed = 1)
    public void ProposalDisqualified(String _ipfs_key, String note) {
    }
}

package com.iconbet.score.daofund;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.sun.nio.sctp.PeerAddressChangeNotification;
import score.Address;
import score.ArrayDB;
import score.BranchDB;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.Json;
import scorex.util.ArrayList;

public class DaoFund {

    public static final String TAG = "ICONbet DAOfund";
    public static final Address SYSTEM_ADDRESS = Address.fromString("cx0000000000000000000000000000000000000000");
    private static final String ADMINS = "admins";
    private static final String WITHDRAW_COUNT = "withdraw_count";
    private static final String WITHDRAW_RECORD = "withdraw_record";
    private static final BigInteger X_6 = new BigInteger("1000000"); // 10 ** 6
    private static final int BATCH_SIZE = 100;

    private static final String _PROPOSAL_BUDGETS = "_proposals_budgets";
    private static final String _PROPOSALS_KEYS = "_proposals_keys";
    private static final String PROPOSALS_SUBMISSION_SCORE = "_proposals_submission_score";
    public static final String PROPOSALS_FUND_SCORE = "_proposals_fund_score";
    public static final String _IPFS_HASH = "_ipfs_hash";
    public static final String _TOTAL_BUDGET = "_budget_transfer";
    private final ArrayDB<Address> admins = Context.newArrayDB(ADMINS, Address.class);
    private final VarDB<Integer> withdraw_count = Context.newVarDB(WITHDRAW_COUNT, Integer.class);
    private final BranchDB<Integer, DictDB<String, String>> withdraw_record = Context.newBranchDB(WITHDRAW_RECORD, String.class);

    private final ArrayDB<String> _proposals_keys = Context.newArrayDB(_PROPOSALS_KEYS, String.class);
    private final DictDB<String, Integer> proposalKeyIndex = Context.newDictDB(_PROPOSALS_KEYS + "_index", Integer.class);
    private final DictDB<String, BigInteger> _proposal_budgets = Context.newDictDB(_PROPOSAL_BUDGETS, BigInteger.class);

    private final VarDB<Address> proposalsSubmissionScore = Context.newVarDB(PROPOSALS_SUBMISSION_SCORE, Address.class);
    private final VarDB<Address> proposalsFundScore = Context.newVarDB(PROPOSALS_FUND_SCORE, Address.class);

    public DaoFund(@Optional boolean _on_update_var) {
        if (_on_update_var) {
            Context.println("updating contract only");
            onUpdate();
            return;
        }

        Context.println("In __init__. " + TAG);

    }

    public void onUpdate() {
        Context.println("calling on update. " + TAG);
    }

    private <T> boolean remove_array_item(ArrayDB<T> arraydb, T target) {

        T _out = arraydb.get(arraydb.size() - 1);
        if (_out != null && _out.equals(target)) {
            arraydb.pop();
            return Boolean.TRUE;
        }

        for (int i = 0; i < arraydb.size() - 1; i++) {
            T value = arraydb.get(i);
            if (value.equals(target)) {
                arraydb.set(i, _out);
                arraydb.pop();
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    /***
     :return: name of the Score
     ***/
    @External(readonly = true)
    public String name() {
        return TAG;
    }

    @External
    public void add_admin(Address _admin) {
        Address sender = Context.getCaller();
        Address owner = Context.getOwner();
        Context.require(sender.equals(owner), TAG + ": Only owners can set new admins.");

        if (!containsInArrayDb(_admin, this.admins)) {
            this.admins.add(_admin);
            AdminAdded(_admin);

        } else {
            Context.revert(TAG + ":  " + _admin + " is already on admin list.");
        }
    }

    @External
    public void remove_admin(Address _admin) {
        Address sender = Context.getCaller();
        Address owner = Context.getOwner();
        if (!sender.equals(owner)) {
            Context.revert(TAG + ": Only admins can remove admins.");
        }

        if (_admin.equals(owner)) {
            Context.revert(TAG + ": Owner address cannot be removed from the admins list.");
        }

        if (containsInArrayDb(_admin, this.admins)) {
            remove_array_item(this.admins, _admin);
            AdminRemoved(_admin);
        } else {
            Context.revert(TAG + ": " + _admin + " not in Admins List");
        }
    }

    @External(readonly = true)
    public List<Address> get_admins() {

        Address[] addressList = new Address[this.admins.size()];

        for (int i = 0; i < this.admins.size(); i++) {
            addressList[i] = this.admins.get(i);
        }
        return List.of(addressList);
    }

    /***
     * Add fund to the daoFund wallet
     ***/
    @External
    @Payable
    public void add_fund() {
    }

    @External
    public void withdraw_fund(Address _address, BigInteger _amount, String _memo) {
        Address sender = Context.getCaller();

        if (!containsInArrayDb(sender, this.admins)) {
            Context.revert(TAG + ": Only admins can run this method.");
        }

        BigInteger _available_amount = Context.getBalance(Context.getAddress());
        if (_available_amount.compareTo(_amount) < 0) {
            Context.revert(TAG + ": Not Enough balance. Available Balance =" + _available_amount.toString());
        }

        int _count = this.withdraw_count.getOrDefault(0);
        int _withdraw_count = _count + 1;

        BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());
        BigInteger day = BigInteger.ZERO;
        day = day.add(now.divide(X_6));

        this.withdraw_count.set(_withdraw_count);
        this.withdraw_record.at(_withdraw_count).set("withdraw_amount", _amount.toString());
        this.withdraw_record.at(_withdraw_count).set("withdraw_address", _address.toString());
        this.withdraw_record.at(_withdraw_count).set("withdraw_memo", _memo);
        this.withdraw_record.at(_withdraw_count).set("withdraw_timestamp", day.toString());

        // self.icx.transfer(_address, _amount)
        Context.transfer(_address, _amount);
        FundTransferred(_address, _amount.toString() + " transferred to " + _address.toString() + " for " + _memo);

    }

    @External(readonly = true)
    public int get_withdraw_count() {
        return this.withdraw_count.get();
    }

    @SuppressWarnings("unchecked")
    @External(readonly = true)
    public List<Map<String, ?>> get_withdraw_records(int _start, int _end) {
        int wd_count = this.withdraw_count.get();

        Context.require((_start != 0 || _end != 0 || wd_count != 0), "No Records Found.");


        Context.require(wd_count != 0, "No Records Found.");

        if (_start == 0 && _end == 0) {
            _end = wd_count;
            _start = Math.max(1, _end - BATCH_SIZE);
        } else if (_end == 0) {
            _end = Math.min(wd_count, _start + BATCH_SIZE);
        } else if (_start == 0) {
            _start = Math.max(1, _end - BATCH_SIZE);
        }

        if (_end > wd_count) {
            _end = wd_count;
        }

        Context.require(_start <= _end, "Start must not be greater than or equal to end.");


        Context.require((_end - _start) < BATCH_SIZE, "Maximum allowed range is " + BATCH_SIZE);
        List<Map<String, ?>> withdrawRecordsList = new ArrayList<>();
        for (int _withdraw = _start; _withdraw <= _end; _withdraw++) {
            Map<String, ?> withdrawRecords = Map.ofEntries(Map.entry("withdraw_address", this.withdraw_record.at(_withdraw).get("withdraw_address")),
                    Map.entry("withdraw_timestamp", this.withdraw_record.at(_withdraw).get("withdraw_timestamp")),
                    Map.entry("withdraw_memo", this.withdraw_record.at(_withdraw).get("withdraw_memo")),
                    Map.entry("withdraw_amount", this.withdraw_record.at(_withdraw).get("withdraw_amount")));
            withdrawRecordsList.add(withdrawRecords);
        }
        return withdrawRecordsList;
    }


    @External(readonly = true)
    public Map<String, String> get_withdraw_record_by_index(int _idx) {
        int _count = this.withdraw_count.get();

        Context.require(_idx > 0 || _idx < _count, _idx + " must be in range [1," + _count + "]");

        return Map.of(
                "withdraw_address", this.withdraw_record.at(_idx).get("withdraw_address"),
                "withdraw_timestamp", this.withdraw_record.at(_idx).get("withdraw_timestamp"),
                "withdraw_reason", this.withdraw_record.at(_idx).get("withdraw_memo"),
                "withdraw_amount", this.withdraw_record.at(_idx).get("withdraw_amount"));
    }

    @Payable
    public void fallback() {
    }

    private void validateOwner() {
        Context.require(Context.getOwner().equals(Context.getCaller()), TAG + ": Only owner can call this method.");
    }

    private void validateAdmin() {
        Context.require(containsInArrayDb(Context.getCaller(), this.admins), TAG + ": Only admins can call this method.");
    }

    private void validateOwnerScore(Address _score) {
        validateOwner();
        Context.require(_score.isContract(), TAG + ": Target " + _score + " is not a SCORE");
    }

    private void validateProposalsSubmissionScore() {
        Context.require(Context.getCaller().equals(this.proposalsSubmissionScore.get()), TAG + ": Only ProposalSubmission (" + this.proposalsSubmissionScore.get() + ") SCORE can send fund using this method.");
    }

    private void validateProposalsFundScore() {
        Context.require(Context.getCaller().equals(this.proposalsFundScore.get()), TAG + ": Only ProposalSubmission (" + this.proposalsFundScore.get() + ") SCORE can send fund using this method.");
    }

    /***
     Sets the proposals_submission score address. Only owner can set the address.
     :param _score: Address of the proposals submission score address
     :type _score: :class:`iconservice.base.address.Address`
     :return:
     ***/
    @External
    public void set_proposals_submission_score(Address _score) {
        this.validateOwnerScore(_score);
        this.proposalsSubmissionScore.set(_score);
    }

    /***
     Returns the proposals_submission score address
     :return: proposal submissionscore address
     ***/
    @External(readonly = true)
    public Address get_proposals_submission_score() {
        return this.proposalsSubmissionScore.get();
    }

    /***
     Sets the proposals fund score address. Only owner can set the address.
     :param _score: Address of the proposals fund score address
     :type _score: :class:`iconservice.base.address.Address`
     :return:
     ***/
    @External
    public void set_proposals_fund_score(Address _score) {
        this.validateOwnerScore(_score);
        this.proposalsFundScore.set(_score);
    }

    /***
     Returns the proposals fund score address
     :return: proposals fund score address
     :rtype: :class:`iconservice.base.address.Address`
     ***/
    @External(readonly = true)
    public Address get_proposals_fund_score() {
        return this.proposalsFundScore.get();
    }

    /***
     Sends the Allocated budget of a proposal after being passed approved.
     :param _ipfs_key: IPFS Hash key for the proposal
     :param _total_installment_count: Total Month count of the project
     :param _proposer_address: proposer Address
     :param _total_budget: Total Budget for the Project.
     :return:
     ***/
    @External
    public void transfer_proposal_fund_to_proposals_fund(String _ipfs_key, int _total_installment_count, Address _proposer_address, BigInteger _total_budget) {
        this.validateProposalsSubmissionScore();
        Context.require(Context.getBalance(Context.getAddress()).compareTo(_total_budget) >= 0, TAG + ": Not enough fund in DAOfund");
        if (this.proposalKeyIndex.getOrDefault(_ipfs_key, 0) == 0) {
            this._proposals_keys.add(_ipfs_key);
            this.proposalKeyIndex.set(_ipfs_key, this._proposals_keys.size());
            this._proposal_budgets.set(_ipfs_key, _total_budget);

//            Required Params for the deposit_proposal_fund method for proposals_fund Score

            JsonObject depositParameters = new JsonObject();
            depositParameters.add("ipfs_hash", _ipfs_key);
            depositParameters.add("project_duration", _total_installment_count);
            depositParameters.add("proposer_address", _proposer_address.toString());
            depositParameters.add("total_budget", _total_budget.toString());

            int _count = this.withdraw_count.getOrDefault(0);
            int _withdraw_count = _count + 1;
            this.withdraw_count.set(_withdraw_count);
            this.withdraw_record.at(_withdraw_count).set("withdraw_amount", _total_budget.toString());
            this.withdraw_record.at(_withdraw_count).set("withdraw_address", this.proposalsFundScore.get().toString());
            this.withdraw_record.at(_withdraw_count).set("withdraw_memo", "Proposal: " + _ipfs_key);
            this.withdraw_record.at(_withdraw_count).set("withdraw_timestamp", String.valueOf(Context.getBlockTimestamp() / 1_000_000L));


            Context.call(_total_budget, proposalsFundScore.get(), "depositProposalFund", depositParameters.toString().getBytes());

            FundTransferred(proposalsFundScore.get(), _total_budget + "transferred to ProposalsFund for Proposal " + _ipfs_key);
            ProposalFundTransferred(_ipfs_key, _total_budget, "Successfully transferred " +
                    _total_budget + "to ProposalsFund.");
        } else {
            Context.revert(TAG + ": IPFS key already Exists");
        }
    }

    /***
     Update the proposal fund after the budget adjustment voting is passed by majority of TAP-Holder(s)
     :param _ipfs_key: Proposal IPFS Hash Key
     :param _added_budget: New added Budget
     :param _total_installment_count: Added Month Count
     :return:
     ***/
    @External
    public void update_proposal_fund(String _ipfs_key, @Optional BigInteger _added_budget, @Optional int _added_installment_count) {
        validateProposalsSubmissionScore();
        Context.require(Context.getBalance(Context.getAddress()).compareTo(_added_budget) >= 0, TAG + ": Not enough fund in treasury.");
        if (this.proposalKeyIndex.get(_ipfs_key) > 0) {
            BigInteger proposalBudget = this._proposal_budgets.getOrDefault(_ipfs_key, BigInteger.ZERO);
            this._proposal_budgets.set(_ipfs_key, proposalBudget.add(_added_budget));
            Context.call(_added_budget, proposalsFundScore.get(), "update_proposal_fund", _ipfs_key, _added_budget, _added_installment_count);
        }
    }

    /***
     After being approved by the majority, if the proposer failed to submit the progress
     report as their milestones, the project will be disqualified after being rejected the two progress reports.
     :param _ipfs_key: Proposal IPFS Hash
     :return:
     ***/
    @External
    @Payable
    public void disqualify_proposal_fund(String _ipfs_key) {
        validateProposalsFundScore();

        if (this.proposalKeyIndex.getOrDefault(_ipfs_key, 0) > 0) {
            BigInteger budget = this._proposal_budgets.getOrDefault(_ipfs_key, BigInteger.ZERO);
            BigInteger value = Context.getValue();
            this._proposal_budgets.set(_ipfs_key, budget.subtract(value));
            burnExtraFund(value);
            ProposalDisqualified(_ipfs_key, "Proposal disqualified. " + value + "returned back to Treasury.");
        }
    }

    @External(readonly = true)
    public Map<String, Object> get_proposal_details(@Optional int _start_index, @Optional int _end_index) {
        if (_end_index == 0) {
            _end_index = 20;
        }
        List<Map<String, Object>> proposalsList = new ArrayList<>();
        if ((_end_index - _start_index) > 50) {
            Context.revert(TAG + ": Page Length cannot be greater than 50");
        }
        int count = _proposals_keys.size();
        if (_start_index > count) {
            Context.revert(TAG + ": Start index can't be higher than total count.");
        }

        if (_start_index < 0) {
            _start_index = 0;
        }

        if (_end_index > count) {
            _end_index = count;

        }

        for (int i = _start_index; i < _end_index; i++) {
            String proposalHash = _proposals_keys.get(i);
            Map<String, Object> proposalDetails = Map.of(_TOTAL_BUDGET, _proposal_budgets.getOrDefault(proposalHash, BigInteger.ZERO), _IPFS_HASH, proposalHash);
            proposalsList.add(proposalDetails);
        }
        return Map.of("data", proposalsList, "count", count);
    }


    @EventLog(indexed = 1)
    public void AdminAdded(Address _address) {
    }

    @EventLog(indexed = 1)
    public void AdminRemoved(Address _address) {
    }

    @EventLog(indexed = 1)
    public void FundTransferred(Address _address, String note) {
    }

    @EventLog(indexed = 1)
    public void ProposalFundTransferred(String _ipfs_key, BigInteger total_budget, String note) {
    }

    @EventLog(indexed = 1)
    public void ProposalDisqualified(String _ipfs_key, String note) {
    }


    /***
     private LinkedList<String> getWithdrawRecord(String property){
     LinkedList<String> withdrawRecord = this.withdraw_record.get(property);
     if(withdrawRecord == null) {
     withdrawRecord = new LinkedList<String>();
     this.withdraw_record.set(property, withdrawRecord);
     }
     return this.withdraw_record.get(property);
     }


     private LinkedList<String> getWithdrawRecordReadOnly(String property){
     LinkedList<String> withdrawRecord = this.withdraw_record.get(property);
     if(withdrawRecord == null) {
     withdrawRecord = new LinkedList<String>();
     }
     return withdrawRecord;
     }
     ***/
    private <T> Boolean containsInArrayDb(T value, ArrayDB<T> arraydb) {
        boolean found = false;
        if (arraydb == null || value == null) {
            return found;
        }

        for (int i = 0; i < arraydb.size(); i++) {
            if (arraydb.get(i) != null
                    && arraydb.get(i).equals(value)) {
                found = true;
                break;
            }
        }
        return found;
    }

    private void burnExtraFund(BigInteger amount){
        Context.call(amount, SYSTEM_ADDRESS, "burn");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <K, V> String mapToJsonString(Map<K, V> map) {
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                sb.append("\"" + entry.getKey() + "\":\"" + mapToJsonString((Map) entry.getValue()) + "\",");
            } else {
                sb.append("\"" + entry.getKey() + "\":\"" + entry.getValue() + "\",");
            }
        }
        char c = sb.charAt(sb.length() - 1);
        if (c == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("}");
        String json = sb.toString();
        Context.println(json);
        return json;
    }
}

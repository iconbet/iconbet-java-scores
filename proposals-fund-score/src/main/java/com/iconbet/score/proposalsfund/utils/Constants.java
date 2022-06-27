package com.iconbet.score.proposalsfund.utils;

import java.math.BigInteger;

public class Constants {
    //# ICX Multiplier
    public static final BigInteger MULTIPLIER = BigInteger.valueOf(10 ^ 18);
    //    # MAXIMUM PROJECT PERIOD
    public static final int MAX_PROJECT_PERIOD = 6;

    //# 2/3 Vot
    public static final double MAJORITY = 0.67;

    //# Period Interval Time
    public static final int DAY_COUNT = 15;

    //# Total Blocks in 1 day
    public static final int BLOCKS_DAY_COUNT = 20;

    //# User Defined Db
    //    PROPOSAL_DB_PREFIX = b'iconbetProposal'
    //    PROGRESS_REPORT_DB_PREFIX = b'progressReport'

    //            # Period Names
    public static final String APPLICATION_PERIOD = "Application Period";
    public static final String VOTING_PERIOD = "Voting Period";
    public static final String TRANSITION_PERIOD = "Transition Period";

    //            # SCOREs Constants
    public static final String DAOFUND_SCORE = "_daofund_score";
    public static final String TAP_TOKEN_SCORE = "_tap_token_score";
    public static final String PROPOSALS_FUND_SCORE = "_proposals_fund_score";

    //            # PERIOD CONSTANTS
    public static final String INITIAL_BLOCK = "initial_block";
    public static final String PERIOD_DETAILS = "_period_details";
    public static final String PERIOD_NAME = "period_name";
    public static final String PREVIOUS_PERIOD_NAME = "previous_period_name";
    public static final String PERIOD_SPAN = "period_span";
    public static final String LASTBLOCK = "last_block";
    public static final String CURRENTBLOCK = "current_block";
    public static final String NEXTBLOCK = "next_block";
    public static final String REMAINING_TIME = "remaining_time";
    public static final String UPDATE_PERIOD_INDEX = "update_period_index";

    //            #Admins
    public static final String ADMINS = "admins";

    //# VarDB/ArrayDB Params
    public static final String PROPOSALS_KEY_LIST = "proposals_key_list";
    public static final String PROGRESS_KEY_LIST = "progress_key_list";
    public static final String PROPOSERS = "proposers";
    public static final String BUDGET_APPROVALS_LIST = "budget_approvals_list";
    public static final String TOTAL_BUDGET = "total_budget";
    public static final String ACTIVE_PROPOSALS = "active_proposals";
    public static final String VOTING_PROPOSALS = "voting_proposals";
    public static final String VOTING_PROGRESS_REPORTS = "voting_progress_reports";
    public static final String AMOUNT = "_total_amount";
    public static final String ADDRESS = "address";

    //            # Proposals and Progress reports keys
    public static final String PROPOSAL = "proposal";
    public static final String PROGRESS_REPORTS = "progress_report";
    public static final String NEW_PROGRESS_REPORT = "new_progress_report";
    public static final String PROJECT_TYPE = "project_type";
    public static final String PROJECT_TITLE = "project_title";
    public static final String PROGRESS_REPORT_TITLE = "progress_report_title";
    public static final String TOTAL_VOTES = "total_votes";
    public static final String TOTAL_VOTERS = "total_voters";
    public static final String REJECTED_VOTES = "rejected_votes";
    public static final String APPROVED_VOTES = "approved_votes";
    public static final String REJECT_VOTERS = "reject_voters";
    public static final String APPROVE_VOTERS = "approve_voters";

    public static final String TIMESTAMP = "timestamp";
    public static final String PROPOSER_ADDRESS = "proposer_address";
    public static final String TX_HASH = "tx_hash";
    public static final String IPFS_HASH = "ipfs_hash";
    public static final String REPORT_KEY = "report_key";
    public static final String REPORT_HASH = "report_hash";
    public static final String PROJECT_DURATION = "project_duration";
    public static final String APPROVED_REPORTS = "approved_reports";
    public static final String IPFS_LINK = "ipfs_link";
    public static final String PERCENTAGE_COMPLETED = "percentage_completed";
    public static final String SUBMIT_PROGRESS_REPORT = "submit_progress_report";
    public static final String ADDITIONAL_BUDGET = "additional_budget";
    public static final String ADDITIONAL_DURATION = "additional_month";
    public static final String BUDGET_ADJUSTMENT = "budget_adjustment";
    public static final String BUDGET_ADJUSTMENT_STATUS = "budget_adjustment_status";
    public static final String BUDGET_APPROVED_VOTES = "budget_approved_votes";
    public static final String BUDGET_REJECTED_VOTES = "budget_rejected_votes";
    public static final String BUDGET_APPROVE_VOTERS = "budget_approve_voters";
    public static final String BUDGET_REJECT_VOTERS = "budget_reject_voters";


    public static final String DENYLIST = "denylist";
    public static final String PENALTY_AMOUNT = "penalty_amount";
    public static final String STATUS = "status";
    public static final String DATA = "data";
    public static final String COUNT = "count";

    //            # VOTE KEYS
    public static final String VOTE = "vote";
    public static final String VOTE_REASON = "vote_reason";
    public static final String APPROVE = "_approve";
    public static final String REJECT = "_reject";
    public static final String ABSTAIN = "_abstain";
    public static final String ACCEPT = "_accept";

    //            # MINIMUM TAP
    public static final BigInteger MINIMUM_TAP_TO_SUBMIT_PROPOSAL = BigInteger.valueOf(10000).multiply(MULTIPLIER);
    public static final BigInteger MINIMUM_TAP_TO_VOTE = BigInteger.valueOf(10 ^ 18);

    public static final String ICX = "ICX";
    public static final String bnUSD = "bnUSD";

    public static final String SPONSOR_REWARD = "sponsor_reward";
    public static final String SPONSOR_ADDRESS = "sponsor_address";
    public static final String CONTRIBUTOR_ADDRESS = "contributor_address";
    public static final String WITHDRAW_AMOUNT = "withdraw_amount";
    public static final String SPONSOR_WITHDRAW_AMOUNT = "sponsor_withdraw_amount";
    public static final String REMAINING_AMOUNT = "remaining_amount";
    public static final String SPONSOR_REMAINING_AMOUNT = "sponsor_remaining_amount";
    public static final String INSTALLMENT_COUNT = "installment_count";
    public static final String SPONSOR_REWARD_COUNT = "sponsor_reward_count";
    public static final String TOKEN = "token";
    public static final String TOTAL_INSTALLMENT_PAID = "total_installment_paid";
    public static final String TOTAL_TIMES_INSTALLMENT_PAID = "total_times_installment_paid";
    public static final String INSTALLMENT_AMOUNT = "installment_amount";
    public static final String TOTAL_INSTALLMENT_COUNT = "total_installment_count";
    public static final String SPONSOR_BOND_AMOUNT = "sponsor_bond_amount";

    public static final String TAG = "ICONBet ProposalsFund";
    public static final String PROPOSAL_DB_PREFIX = "iconbetProposal";
    public static final String ID = "id";
    public static final String _PROPOSALS_KEYS = "_proposals_keys";
    public static final String _FUND_RECORD = "fund_record";

    public static final String _TOTAL_INSTALLMENT_COUNT = "_total_installment_count";
    public static final String _TOTAL_TIMES_INSTALLMENT_PAID = "_total_times_installment_paid";
    public static final String _TOTAL_TIMES_REWARD_PAID = "_total_times_reward_paid";
    public static final String _TOTAL_INSTALLMENT_PAID = "_total_installment_paid";
    public static final String _TOTAL_REWARD_PAID = "_total_reward_paid";
    public static final String _INSTALLMENT_AMOUNT = "installment_amount";

    public static final String _PROPOSAL_SUBMISSION_SCORE = "_proposal_submission_score";
    public static final String _DAOFUND_SCORE = "_daofund_score";

    public static final String _PROPOSER_ADDRESS = "proposer_address";
    public static final String _STATUS = "status";
    public static final String _IPFS_HASH = "ipfs_hash";
    public static final String _TOTAL_BUDGET = "total_budget";

    public static final String _ACTIVE = "active";
    public static final String _DISQUALIFIED = "disqualified";
    public static final String _COMPLETED = "completed";

}

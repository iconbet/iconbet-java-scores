package com.iconbet.score.authorization.utils;

import java.math.BigInteger;

public class Consts {
    public static final BigInteger EXA = new BigInteger("1000000000000000000");
    public static final BigInteger U_SECONDS_DAY = new BigInteger("86400000000"); // Microseconds in a day.
    public static final BigInteger DAY_ZERO = BigInteger.valueOf(18647);
    public static final BigInteger DAY_START = new BigInteger("61200000000"); // 17:00 UTC
    public static final BigInteger MAJORITY = new BigInteger("666666666666666667");
    public static final int BALNBNUSD_ID = 3;
    public static final int BALNSICX_ID = 4;
    public static final BigInteger POINTS = BigInteger.valueOf(10000);


//  Proposals and Progress reports keys
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

    public static final String GOVERNANCE = "governance";
    public static final String NEW_GAME = "newGame";
    public static final String GAME_APPROVAL = "gameApproval";
}

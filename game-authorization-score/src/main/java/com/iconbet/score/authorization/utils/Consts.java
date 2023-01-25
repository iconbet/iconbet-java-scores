package com.iconbet.score.authorization.utils;

import java.math.BigInteger;
import java.util.List;

public class Consts {
    public static final BigInteger EXA = new BigInteger("1000000000000000000");
    public static final BigInteger U_SECONDS_DAY = new BigInteger("86400000000"); // Microseconds in a day.
    public static final BigInteger MAJORITY = new BigInteger("666666666666666667");


//  Proposals and Progress reports keys
    public static final String STATUS = "status";

    public static final String GOVERNANCE = "governance";
    public static final String NEW_GAME = "newGame";
    public static final String GAME_APPROVAL = "gameApproval";


    public static final String ADMIN_LIST = "admin_list";
    public static final String SUPER_ADMIN = "super_admin";
    public static final String PROPOSAL_DATA = "proposal_data";
    public static final String PROPOSAL_LIST = "proposal_list";
    public static final String STATUS_DATA = "status_data";
    public static final String OWNER_DATA = "owner_data";
    public static final String ROULETTE_SCORE = "roulette_score";
    public static final String TAP_TOKEN_SCORE = "tap_token_score";
    public static final String DIVIDEND_DISTRIBUTION_SCORE = "dividend_distribution_score";
    public static final String REWARDS_SCORE = "rewards_score";
    public static final String UTAP_TOKEN_SCORE = "utap_token_score";
    public static final String DAY = "day";
    public static final String PAYOUTS = "payouts";
    public static final String WAGERS = "wagers";
    public static final String NEW_DIV_CHANGING_TIME = "new_div_changing_time";
    public static final String GAME_DEVELOPERS_SHARE = "game_developers_share";

    public static final String TODAYS_GAMES_EXCESS = "todays_games_excess";
//    dividends paid according to this excess
    public static final String GAMES_EXCESS_HISTORY = "games_excess_history";


    public static final String APPLY_WATCH_DOG_METHOD = "apply_watch_dog_method";
    public static final String MAXIMUM_PAYOUTS = "maximum_payouts";
    public static final String MAXIMUM_LOSS = "maximum_loss";

    public static final String GOVERNANCE_ENABLED = "governance_enabled";
    public static final String VOTE_DEFINITION_CRITERION = "vote_definition_criterion";
    public static final String QUORUM = "quorum";
    public static final String TIME_OFFSET = "time_offset";
    public static final String VOTE_DURATION = "vote_duration";
    public static final String MAX_ACTIONS = "max_actions";

    public static final String GAME_ADDRESSES = "game_addresses";
    public static final String PROPOSAL_KEYS = "proposal_keys";
    public static final String PROPOSAL_KEY_INDEX = PROPOSAL_KEYS + "_index";
    public static final String PROPOSAL_COUNT = "proposal_count";
    public static final String GAME_NAMES = "game_names";

    public static final String TAG = "AUTHORIZATION";
    public static final boolean DEBUG = false;
    public static final BigInteger MULTIPLIER = new BigInteger("1000000000000000000");
    public static final BigInteger POINT_1_ICX = new BigInteger("100000000000000000"); // 0.1 ICX = 10^18 * 0.1

    public static final List<String> METADATA_FIELDS = List.of("name", "scoreAddress", "minBet", "maxBet", "houseEdge",
     "gameType", "revShareMetadata", "revShareWalletAddress", "maxPayout",
    "linkProofPage", "gameUrlMainnet", "gameUrlTestnet");

    public static final List<String> GAME_TYPE = List.of("Per wager settlement", "Game defined interval settlement");

    public static final List<String> STATUS_TYPE = List.of("waiting", "proposalApproved", "proposalRejected", "gameReady",
            "gameApproved", "gameRejected", "gameSuspended", "gameDeleted");

    public static final List<String> dbName = List.of(GOVERNANCE, NEW_GAME, GAME_APPROVAL);

    public static final String OFFICIAL_REVIEW = "official_review";

    public static final BigInteger PER_ONE_HUNDRED = BigInteger.valueOf(100);
}

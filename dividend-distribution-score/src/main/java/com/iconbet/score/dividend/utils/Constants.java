package com.iconbet.score.dividend.utils;

import score.Address;

import java.math.BigInteger;
import java.util.List;

public class Constants {
    public static final List<String> DIVIDEND_CATEGORIES = List.of("_tap", "_gamedev", "_promo", "_platform");

    public static final BigInteger _3 = BigInteger.valueOf(3);
    public static final BigInteger _20 = BigInteger.valueOf(20);
    public static final BigInteger _80 = BigInteger.valueOf(80);
    public static final BigInteger _90 = BigInteger.valueOf(90);
    public static final BigInteger _100 = BigInteger.valueOf(100);
    public static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);

    public static final String _DIVS_DIST_COMPLETE = "dist_complete";

    //TODO: ref var not used, just declared
    public static final String _TAP_DIST_INDEX = "dist_index";
    public static final String _BATCH_SIZE = "batch_size";

    public static final String _TAP_HOLDERS = "holders";
    public static final String _TAP_BALANCES = "balances";

    public static final String _TOTAL_DIVS = "total_divs";
    public static final String _REMAINING_TAP_DIVS = "remaining_divs";
    public static final String _REMAINING_GAMEDEV_DIVS = "remaining_gamedev_divs";
    public static final String _PLATFORM_DIVS = "platform_divs";
    public static final String _PROMO_DIVS = "promo_divs";
    public static final String _DAOFUND_DIVS = "daofund_divs";

    public static final String _TOTAL_ELIGIBLE_TAP_TOKENS = "remaining_tokens";
    public static final String _BLACKLIST_ADDRESS = "blacklist_addresses";
    public static final String _INHOUSE_GAMES = "inhouse_games";

    public static final String _GAMES_LIST = "games_list";
    public static final String _GAMES_EXCESS = "games_excess";
    public static final String _REVSHARE_WALLET_ADDRESS = "revshare_wallet_address";

    public static final String _DIVIDEND_PERCENTAGE = "dividend_percentage";

    public static final String _TOKEN_SCORE = "token_score";
    public static final String _GAME_SCORE = "game_score";
    public static final String _PROMO_SCORE = "promo_score";
    public static final String _DAOFUND_SCORE = "daofund_score";
    public static final String _IBPNP_SCORE = "ibpnp_score";
    public static final String _GAME_AUTH_SCORE = "game_auth_score";
    public static final String _DIVIDENDS_RECEIVED = "dividends_received";

    public static final String _STAKE_HOLDERS = "stake_holders";
    public static final String _STAKE_BALANCES = "stake_balances";
    public static final String _TOTAL_ELIGIBLE_STAKED_TAP_TOKENS = "total_eligible_staked_tap_tokens";
    public static final String _STAKE_DIST_INDEX = "stake_dist_index";

    public static final String _SWITCH_DIVIDENDS_TO_STAKED_TAP = "switch_dividends_to_staked_tap";

    public static final String _EXCEPTION_ADDRESS = "exception_address";
    public static final String _TAP_DIVIDENDS = "tap_dividends";

}

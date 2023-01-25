package com.iconbet.score.authorization.db;
import com.eclipsesource.json.JsonObject;
import com.iconbet.score.authorization.Authorization;
import score.Address;
import score.Context;

import java.math.BigInteger;

public class VoteActions {
    public static void execute(Authorization auth, String method, JsonObject params){
//        for tap token score
        switch (method){
            case "set_minimum_stake":
                auth.setMinimumStake(new BigInteger(params.get("_amount").asString()));
                break;
            case "set_unstaking_period":
                auth.setUnstakingPeriod(new BigInteger(params.get("_time").asString()));
                break;
            case "set_max_loop":
                auth.setMaxLoop(params.getInt("_loops", 0));
                break;
            case "remove_from_blacklist_tap":
                auth.removeFromBlacklistTap(Address.fromString(params.get("_address").asString()));
                break;
            case "set_blacklist_address_tap":
                auth.setBlacklistAddressTap(Address.fromString(params.get("_address").asString()));
                break;
            case "remove_from_locklist":
                auth.remove_from_locklist(Address.fromString(params.get("_address").asString()));
                break;
            case "set_locklist_address":
                auth.setLocklistAddress(Address.fromString(params.get("_address").asString()));
                break;
            case "remove_from_whitelist":
                auth.removeFromWhitelist(Address.fromString(params.get("_address").asString()));
                break;
            case "set_whitelist_address":
                auth.setWhitelistAddress(Address.fromString(params.get("_address").asString()));
                break;
// For dividend scores
            case "set_dividend_percentage":
                auth.setDividendPercentage(params.get("tap").asInt(), params.get("gamedev").asInt(), params.get("promo").asInt(), params.get("platform").asInt());
                break;
            case "set_non_tax_period":
                auth.setNonTaxPeriod(new BigInteger(params.get("period").asString()));
                break;
            case "set_tax_percentage":
                auth.setTaxPercentage(params.get("percentage").asInt());
                break;
            case "remove_from_blacklist_dividend":
                auth.removeFromBlacklistDividend(Address.fromString(params.get("_address").asString()));
                break;
            case "set_blacklist_address_dividend":
                auth.setBlacklistAddressDividend(Address.fromString(params.get("_address").asString()));
                break;
            case "set_inhouse_games":
                auth.setInhouseGames(Address.fromString(params.get("_score").asString()));
                break;
            case "remove_from_inhouse_games":
                auth.removeFromInhouseGames(Address.fromString(params.get("_score").asString()));
                break;
            case "add_exception_address":
                auth.addExceptionAddress(Address.fromString(params.get("_address").asString()));
                break;
            case "remove_exception_address":
                auth.removeExceptionAddress(Address.fromString(params.get("_address").asString()));
                break;
            case "setQuorum":
                auth.setQuorum(params.get("quorum").asInt());
                break;
            case "setVoteDuration":
                auth.setVoteDuration(new BigInteger(params.get("duration").asString()));
                break;
            case "setMaxActions":
                auth.setMaxActions(params.get("max_actions").asInt());
                break;
            case "setTAPVoteDefinitionCriterion":
                auth.setTAPVoteDefinitionCriterion(new BigInteger(params.get("percentage").asString()));
                break;
            default:
                Context.revert("Method not found");
        }
    }
}

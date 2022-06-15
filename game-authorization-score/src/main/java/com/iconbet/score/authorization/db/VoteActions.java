package com.iconbet.score.authorization.db;
import com.eclipsesource.json.JsonObject;
import com.iconbet.score.authorization.Authorization;
import score.Address;

import java.math.BigInteger;

public class VoteActions {
    public static void execute(Authorization auth, String method, JsonObject params){
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
                auth.remove_from_blacklist_tap(Address.fromString(params.get("_address").asString()));
                break;
            case "set_blacklist_address":
                auth.set_blacklist_address_tap(Address.fromString(params.get("_address").asString()));
                break;
            case "remove_from_locklist":
                auth.remove_from_locklist(Address.fromString(params.get("_address").asString()));
                break;
            case "set_locklist_address":
                auth.set_locklist_address(Address.fromString(params.get("_address").asString()));
                break;
            case "remove_from_whitelist":
                auth.remove_from_whitelist(Address.fromString(params.get("_address").asString()));
                break;
            case "set_whitelist_address":
                auth.set_whitelist_address(Address.fromString(params.get("_address").asString()));
                break;

            case "set_dividend_percentage":
                auth.set_dividend_percentage(params.get("_tap").asInt(), params.get("_gamedev").asInt(), params.get("_promo").asInt(), params.get("platform").asInt());
                break;
            case "set_non_tax_period":
                auth.set_non_tax_period(new BigInteger(params.get("period").asString()));
                break;
            case "set_tax_percentage":
                auth.set_tax_percentage(params.get("percentage").asInt());
                break;
            case "remove_from_blacklist_dividend":
                auth.remove_from_blacklist_dividend(Address.fromString(params.get("_address").asString()));
                break;
            case "set_blacklist_address_dividend":
                auth.set_blacklist_address_dividend(Address.fromString(params.get("_address").asString()));
                break;
            case "set_inhouse_games":
                auth.set_inhouse_games(Address.fromString(params.get("_score").asString()));
                break;
            case "remove_from_inhouse_games":
                auth.remove_from_inhouse_games(Address.fromString(params.get("_score").asString()));
                break;
            case "add_exception_address":
                auth.add_exception_address(Address.fromString(params.get("_address").asString()));
                break;
            case "remove_exception_address":
                auth.remove_exception_address(Address.fromString(params.get("_address").asString()));
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
                auth.setTAPVoteDefinitionCriterion(params.get("percentage").asInt());
                break;
        }
    }
}

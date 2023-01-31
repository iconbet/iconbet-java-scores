package com.iconbet.score.reward;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;
import static java.math.BigInteger.ZERO;

import com.eclipsesource.json.JsonObject;

import scorex.util.ArrayList;

import java.math.BigInteger;
import java.util.List;

import score.Address;
import score.ArrayDB;
import score.BranchDB;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;

public class RewardDistribution {
    protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);

    public static final String TAG = "REWARDS";
    public static final BigInteger DAILY_TOKEN_DISTRIBUTION = new BigInteger("1000000000000000000000000");
    public static final BigInteger TAP = new BigInteger("1000000000000000000");

    private static final String _WAGERS = "wagers";
    private static final String _DAY = "day";
    private static final String _EVEN_DAY = "even_day";
    private static final String _ODD_DAY = "odd_day";
    private static final String _EVEN_DAY_TOTAL = "even_day_total";
    private static final String _ODD_DAY_TOTAL = "odd_day_total";
    private static final String _WAGER_TOTAL = "wager_total";

    private static final String _DAILY_DIST = "daily_dist";
    private static final String _DIST_INDEX = "dist_index";
    private static final String _DIST_COMPLETE = "dist_complete";

    private static final String _GAME_SCORE = "game_score";
    private static final String TAP_DISTRIBUTION_ENABLED = "tap_distribution_enabled";
    private static final String _TOKEN_SCORE = "token_score";
    private static final String _DIVIDENDS_SCORE = "dividends_score";
    private static final String _BATCH_SIZE = "batch_size";

    private static final String _REWARDS_GONE = "rewards_gone";
    private static final String _YESTERDAYS_TAP_DISTRIBUTION = "yesterdays_tap_distribution";
    private static final String LINEARITY_COMPLEXITY_MIGRATION = "linearity_complexity_migration";

    @EventLog(indexed = 2)
    public void TokenTransfer(Address recipient, BigInteger amount) {
    }

    //TODO: review this py dept = 2 data structure and possible null pointer ex.
    private final BranchDB<BigInteger, DictDB<String, BigInteger>> _wagers = Context.newBranchDB(_WAGERS, BigInteger.class);
    private final VarDB<BigInteger> _day_index = Context.newVarDB(_DAY, BigInteger.class);
    private final ArrayDB<String> _even_day_addresses = Context.newArrayDB(_EVEN_DAY, String.class);
    private final ArrayDB<String> _odd_day_addresses = Context.newArrayDB(_ODD_DAY, String.class);
    @SuppressWarnings("unchecked")
    private final
    ArrayDB<String>[] _addresses = new ArrayDB[]{this._even_day_addresses, this._odd_day_addresses};
    private final
    VarDB<BigInteger> _even_day_total = Context.newVarDB(_EVEN_DAY_TOTAL, BigInteger.class);
    private final
    VarDB<BigInteger> _odd_day_total = Context.newVarDB(_ODD_DAY_TOTAL, BigInteger.class);
    @SuppressWarnings("unchecked")
    private final VarDB<BigInteger>[] _daily_totals = new VarDB[]{this._even_day_total, this._odd_day_total};
    private final VarDB<BigInteger> _wager_total = Context.newVarDB(_WAGER_TOTAL, BigInteger.class);
    private final VarDB<BigInteger> _daily_dist = Context.newVarDB(_DAILY_DIST, BigInteger.class);
    private final VarDB<BigInteger> _dist_index = Context.newVarDB(_DIST_INDEX, BigInteger.class);
    private final VarDB<Boolean> _dist_complete = Context.newVarDB(_DIST_COMPLETE, Boolean.class);

    private final VarDB<Address> _game_score = Context.newVarDB(_GAME_SCORE, Address.class);
    private final VarDB<Boolean> tapDistributionEnabled = Context.newVarDB(TAP_DISTRIBUTION_ENABLED, Boolean.class);
    private final VarDB<Address> _token_score = Context.newVarDB(_TOKEN_SCORE, Address.class);
    private final VarDB<Address> _dividends_score = Context.newVarDB(_DIVIDENDS_SCORE, Address.class);
    private final VarDB<BigInteger> _batch_size = Context.newVarDB(_BATCH_SIZE, BigInteger.class);

    // rewards gone variable checks if the 500M tap token held for distribution is completed
    private final VarDB<Boolean> _rewards_gone = Context.newVarDB(_REWARDS_GONE, Boolean.class);
    private final VarDB<BigInteger> _yesterdays_tap_distribution = Context.newVarDB(_YESTERDAYS_TAP_DISTRIBUTION, BigInteger.class);

    private final DictDB<Address, BigInteger> evenDayAddressesIndex = Context.newDictDB(_EVEN_DAY + "_index", BigInteger.class);
    private final DictDB<Address, BigInteger> oddDayAddressesIndex = Context.newDictDB(_ODD_DAY + "_index", BigInteger.class);
    private final DictDB<Address, BigInteger>[] addressesIndex = new DictDB[]{this.evenDayAddressesIndex, this.oddDayAddressesIndex};

    private final VarDB<Boolean> linearityComplexityMigrationStart = Context.newVarDB(LINEARITY_COMPLEXITY_MIGRATION + "_start", Boolean.class);
    private final VarDB<Boolean> linearityComplexityMigrationComplete = Context.newVarDB(LINEARITY_COMPLEXITY_MIGRATION + "_complete", Boolean.class);
    private final VarDB<Integer> linearityComplexityMigrationIndex = Context.newVarDB(LINEARITY_COMPLEXITY_MIGRATION + "_index", Integer.class);

    public RewardDistribution() {
        if (_day_index.get() == null) {
            Context.println("In __init__. " + TAG);
            Context.println("owner is " + Context.getOwner() + ". " + TAG);
            this._day_index.set(ZERO);
            this._dist_index.set(ZERO);
            this._dist_complete.set(true);

            this._even_day_total.set(ZERO);
            this._odd_day_total.set(ZERO);
            this._rewards_gone.set(false);
        }
    }

    private boolean validateOwner() {
        return Context.getCaller().equals(Context.getOwner());
    }

    private void validateOwnerScore(Address score) {
        Context.require(validateOwner(), TAG + ": Only owner can call this score");
        Context.require(score.isContract(), TAG + ": The address is not a contract address");
    }

    /*
    Sets the tap token score address
    :param score: Address of the token score
    :type score: :class:`iconservice.base.address.Address`
    :return:
     */
    @External
    public void setTokenScore(Address score) {
        validateOwnerScore(score);
        this._token_score.set(score);

    }

    /*
    Returns the tap token score address
    :return: Address of the tap token score
    :rtype: :class:`iconservice.base.address.Address`
     */
    @External(readonly = true)
    public Address get_token_score() {
        return this._token_score.getOrDefault(null);
    }

    /*
    Sets the dividends distribution score address
    :param score: Address of the dividends distribution score
    :type score: :class:`iconservice.base.address.Address`
    :return:
     */
    @External
    public void setDividendsScore(Address score) {
        validateOwnerScore(score);
        this._dividends_score.set(score);
    }

    /*
    Returns the dividends distribution score address
    :return: Address of the dividends distribution score
    :rtype: :class:`iconservice.base.address.Address`
     */
    @External(readonly = true)
    public Address get_dividends_score() {
        return this._dividends_score.getOrDefault(null);
    }

    /*
    Sets the roulette score address
    :param _score: Address of the treasury score
    :type _score: :class:`iconservice.base.address.Address`
    :return:
     */
    @External
    public void setTreasuryScore(Address _score) {
        validateOwnerScore(_score);
        this._game_score.set(_score);
    }

    /*
    Returns the roulette score address
    :return: Address of the treasury score
    :rtype: :class:`iconservice.base.address.Address`
     */
    @External(readonly = true)
    public Address get_game_score() {
        return this._game_score.getOrDefault(null);
    }

    @External
    public void toggleTapDistributionEnabled() {
        Context.require(validateOwner(), TAG + ": Only owner can call this method.");
        this.tapDistributionEnabled.set(!this.tapDistributionEnabled.getOrDefault(Boolean.FALSE));
    }

    @External(readonly = true)
    public boolean getTapDistributionEnabled() {
        return this.tapDistributionEnabled.getOrDefault(Boolean.FALSE);
    }

    /*
    Checks the status for tap token distribution
    :return: True if tap token has been distributed for previous day
    :rtype: bool
     */
    @External(readonly = true)
    public boolean rewards_dist_complete() {
        return this._dist_complete.getOrDefault(false);
    }

    /*
    Provides total wagers made in current day.
    :return: Total wagers made in current day in loop
    :rtype: int
     */
    @External(readonly = true)
    public BigInteger get_todays_total_wagers() {
        return this._daily_totals[this._day_index.get().intValue()].getOrDefault(ZERO);
    }

    /*
    Returns total wagers made by the player in the current day
    :param _player: Player address for which the wagers has to be checked
    :type _player: str
    :return: Wagers made by the player in current day
    :rtype: int
     */
    @External(readonly = true)
    public BigInteger get_daily_wagers(String _player) {
        return this._wagers.at(this._day_index.get()).getOrDefault(_player, ZERO);
    }


    /*
    Returns the expected TAP tokens the player will receive according to the total wagers at that moment
    :param _player: Player address for which expected rewards is to be checked
    :type _player: str
    :return: Expected TAP tokens that the player can receive
    :rtype: int
     */
    @External(readonly = true)
    public BigInteger get_expected_rewards(String _player) {
        BigInteger total = this.get_todays_total_wagers();
        if (total.equals(ZERO)) {
            return ZERO;
        }
        return this.get_todays_tap_distribution().multiply(this.get_daily_wagers(_player)).divide(total);
    }

    /*
    Returns the amount of TAP to be distributed today
    :return:
     */
    @External(readonly = true)
    public BigInteger get_todays_tap_distribution() {
        BigInteger remainingTokens = Context.call(BigInteger.class, this._token_score.get(), "balanceOf", Context.getAddress());
        if (remainingTokens.equals(BigInteger.valueOf(264000000).multiply(TAP))) {
            return TWO.multiply(DAILY_TOKEN_DISTRIBUTION).add(remainingTokens).mod(DAILY_TOKEN_DISTRIBUTION);
        } else if (remainingTokens.compareTo(BigInteger.valueOf(251000000).multiply(TAP)) >= 0) {
            return DAILY_TOKEN_DISTRIBUTION.add(remainingTokens).mod(DAILY_TOKEN_DISTRIBUTION);
        } else {
            BigInteger yesterdaysDistribution = this._yesterdays_tap_distribution.getOrDefault(ZERO)
                    .multiply(BigInteger.valueOf(995))
                    .divide(BigInteger.valueOf(1000));
            return (BigInteger.valueOf(200_000).multiply(TAP)
                    .max(yesterdaysDistribution)).min(remainingTokens);
        }
    }

    /*
    Returns all the addresses which have played games today and yesterday with their wagered amount in the entire
    platform
    :return: JSON data of yesterday's and today's players and their wagers
    :rtype: str
     */
    @SuppressWarnings("unchecked")
    @External(readonly = true)
    public String get_daily_wager_totals() {
        Context.println(Context.getCaller() + " is getting daily wagers. " + TAG);
        BigInteger index = this._day_index.get();

        List<JsonObject> today = new ArrayList<>();
        int j = 0;

        ArrayDB<String> addresses = this._addresses[index.intValue()];
        int size = addresses.size();
        for (int i = 0; i < size; i++) {
            String address = addresses.get(i);
            BigInteger amount = this._wagers.at(index).get(address);
            JsonObject todayEntry = new JsonObject();
            todayEntry.add(address, amount.toString());
            today.add(todayEntry);
            j++;
            Context.println("Wager amount of " + amount + " being added. " + TAG);
        }

        index = this._day_index.get().add(ONE).mod(TWO);

        List<JsonObject> yesterday = new ArrayList<>();
        j = 0;
        for (int i = 0; i < size; i++) {
            String address = addresses.get(i);
            BigInteger amount = this._wagers.at(index).getOrDefault(address, ZERO);
            JsonObject yesterdayEntry = new JsonObject();
            yesterdayEntry.add(address, amount.toString());
            yesterday.add(yesterdayEntry);
            j++;
            Context.println("Wager amount of " + amount + " being added. " + TAG);
        }
        JsonObject dailyWagers = new JsonObject();
        dailyWagers.add("today", today.toString());
        dailyWagers.add("yesterday", yesterday.toString());

        String json = dailyWagers.toString();
        Context.println("Wager totals " + json + " " + TAG);
        return json;
    }

    /*
    Records data of wagers made by players in any games in the ICONbet platform. If the day has changed then
    data for the index of today is cleared. Index can be 0 or 1. The wagerers from previous day are made eligible to
    receive TAP tokens. Calls the distribute function of dividends distribution score and distribute function for
    TAP tokens distribution if they are not completed.
    :param player: Address of the player playing any games in ICONbet platform
    :type player: str
    :param wager: Wager amount of the player
    :type wager: int
    :param day_index: Day index for which player data is to be recorded(0 or 1)
    :type day_index: int
    :return:
     */
    @External
    public void accumulate_wagers(String player, BigInteger wager, BigInteger day_index) {
        if (!Context.getCaller().equals(this._game_score.get())) {
            Context.revert(TAG + ": This function can only be called from the game score.");
        }
        Context.println("In accumulate_wagers, day_index = " + day_index + ". " + TAG);
        BigInteger day = this._day_index.get();
        Context.println(this._day_index + " = " + day + ". " + TAG);
        if (day.compareTo(day_index) != 0) {
            Context.println("Setting self._day_index to " + day_index + ". " + TAG);
            this._day_index.set(day_index);

            int dayIndexInt = day_index.intValue();
            for (int i = 0; i < this._addresses[dayIndexInt].size(); i++) {
                Context.println(TAG + ": exception here???? " + this._addresses[dayIndexInt].size());

                String _address = this._addresses[dayIndexInt].pop();

                //TODO: review removal logic
                this._wagers.at(day_index).set(_address, null);

                if (linearityComplexityMigrationComplete.getOrDefault(Boolean.FALSE)) {
                    addressesIndex[dayIndexInt].set(Address.fromString(_address), ZERO);
                }
            }
            if (!this._rewards_gone.get()) {
                Address tapTokenScore = this._token_score.get();
                BigInteger remainingTokens = callScore(BigInteger.class, tapTokenScore, "balanceOf", Context.getAddress());
                Context.println("remaining tokens here: " + remainingTokens);
                if (remainingTokens.equals(ZERO)) {
                    this._rewards_gone.set(true);
                } else {
                    this._set_batch_size();
                    this._dist_index.set(ZERO);
                    this._dist_complete.set(false);
                    this._wager_total.set(this._daily_totals[day.intValue()].get());
                    this._set_daily_dist();
                }
            }
            this._daily_totals[dayIndexInt].set(ZERO);
        }

        Context.println("Lengths: " + this._addresses[0].size() + " , " + this._addresses[1].size() + " " + TAG);
        Context.println("Adding wager from " + player + ". " + TAG);
        this._daily_totals[day_index.intValue()].set(this._daily_totals[day_index.intValue()].get().add(wager));
        Context.println("Total wagers = " + this._daily_totals[day_index.intValue()].get() + ". " + TAG);

        if (linearityComplexityMigrationComplete.getOrDefault(Boolean.FALSE)) {
            if (this.addressesIndex[day_index.intValue()].getOrDefault(Address.fromString(player), ZERO).compareTo(ZERO) > 0) {
                Context.println("Adding wager to " + player + " in _addresses[" + day_index.intValue() + " ]. " + TAG);
                this._wagers.at(day_index).set(player, this._wagers.at(day_index).get(player).add(wager));
            } else {
                Context.println("Putting " + player + " in _addresses[" + day_index.intValue() + "]. " + TAG);
                this._addresses[day_index.intValue()].add(player);
                this.addressesIndex[day_index.intValue()].set(Address.fromString(player), BigInteger.valueOf(this._addresses[day_index.intValue()].size()));
                this._wagers.at(day_index).set(player, wager);
            }
        } else {
            if (containsInArrayDb(player, this._addresses[day_index.intValue()])) {
                this._wagers.at(day_index).set(player, this._wagers.at(day_index).getOrDefault(player, ZERO).add(wager));
            } else {
                this._addresses[day_index.intValue()].add(player);
                this._wagers.at(day_index).set(player, wager);
            }
        }
        Context.println("calling distribute method from rewards");
        Boolean distribute = callScore(Boolean.class, this._dividends_score.get(), "distribute");
        Context.println("distribution enabled?: " + this.tapDistributionEnabled.getOrDefault(Boolean.FALSE));
        if (distribute != null && distribute && this.tapDistributionEnabled.getOrDefault(Boolean.FALSE)) {
            this._distribute();
        }
        Context.println("Done in accumulate_wagers.  self._day_index = " + this._day_index.get() + ". " + TAG);

        if (this.linearityComplexityMigrationStart.getOrDefault(Boolean.FALSE) && !this.linearityComplexityMigrationComplete.getOrDefault(Boolean.FALSE)) {
            this.migrateFromLinearComplexity();
        }
    }

    /*
    Sets the batch size to be used for TAP distribution. Uses the function from roulette score
    :return:
     */
    public void _set_batch_size() {
        BigInteger size = callScore(BigInteger.class, this._game_score.get(), "get_batch_size",
                BigInteger.valueOf(
                        this._addresses[this._day_index.get().intValue()]
                                .size()));
        this._batch_size.set(size);
    }

    /*
    Main distribution function to distribute the TAP token to the wagerers. Distributes the TAP token only if this
    contract holds some TAP token.
    :return:
     */
    public void _distribute() {
        if (this._rewards_gone.get()) {
            Context.println("Rewards gone is true");
            this._dist_complete.set(true);
            return;
        }
        Context.println("Beginning rewards distribution. " + TAG);
        int index = (this._day_index.getOrDefault(ZERO).intValue() + 1) % 2;
        int count = this._batch_size.getOrDefault(ZERO).intValue();
        ArrayDB<String> addresses = this._addresses[index];
        int length = addresses.size();
        int start = this._dist_index.getOrDefault(ZERO).intValue();
        int remainingAddresses = length - start;
        if (count > remainingAddresses) {
            count = remainingAddresses;
        }
        int end = start + count;
        Context.println("Length of address list: " + length + ". Remaining = " + remainingAddresses + " " + TAG);

        BigInteger totalDist = this._daily_dist.getOrDefault(ZERO);
        BigInteger totalWagers = this._wager_total.getOrDefault(ZERO);
        if (totalWagers.equals(ZERO)) {
            this._dist_index.set(ZERO);
            this._dist_complete.set(true);
            return;
        }

        for (int i = start; i < end; i++) {
            BigInteger wagered = this._wagers.at(BigInteger.valueOf(index)).getOrDefault(addresses.get(i), ZERO);
            BigInteger rewardsDue = totalDist.multiply(wagered).divide(totalWagers);
            totalDist = totalDist.subtract(rewardsDue);
            totalWagers = totalWagers.subtract(wagered);
            Context.println("Rewards due to " + addresses.get(i) + " = " + rewardsDue + " " + TAG);
            Context.println("Trying to send to (" + addresses.get(i) + "): " + rewardsDue + ". " + TAG);
            Address fullAddress = Address.fromString(addresses.get(i));
            callScore(this._token_score.get(), "transfer", fullAddress, rewardsDue);
            this.TokenTransfer(fullAddress, rewardsDue);
            Context.println("Sent player (" + addresses.get(i) + ") " + rewardsDue + ". " + TAG);
        }
        this._daily_dist.set(totalDist);
        this._wager_total.set(totalWagers);
        if (end == length) {
            this._dist_index.set(ZERO);
            this._dist_complete.set(true);
        } else {
            this._dist_index.set(this._dist_index.getOrDefault(ZERO).add(BigInteger.valueOf(count)));
        }
    }

    /*
    Sets the amount of TAP to be distributed on each day
    :param remaining_tokens: Remaining TAP tokens on the rewards contract
    :return:
     */
    private void _set_daily_dist() {
        BigInteger dailyDist = get_todays_tap_distribution();
        Context.println("Daily dist in else: " + dailyDist);
        this._yesterdays_tap_distribution.set(dailyDist);
        this._daily_dist.set(dailyDist);
    }


    @Payable
    public void fallback() {
        Context.revert("This contract doesn't accept ICX");
    }

    /*This score will hold the 80% of TAP tokens for distribution.*/
    @External
    public void tokenFallback(Address _from, BigInteger _value, byte[] _data) {

        Context.println("calling balance of token score " + this._token_score.get() + " for addr " + Context.getAddress());
        BigInteger remainingTokens = Context.call(BigInteger.class, this._token_score.get(), "balanceOf", Context.getAddress());
        Context.println("remaining tokens of " + Context.getAddress() + ": " + remainingTokens);
        if (remainingTokens.equals(BigInteger.valueOf(264000000).multiply(TAP))) {
            Context.revert("Not able to receive further TAP when the balance is 264M tap tokens");
        }
        String symbol = Context.call(String.class, this._token_score.get(), "symbol");
        if (!symbol.equals("TAP")) {
            Context.revert("The Rewards Score can only receive TAP tokens.");
        }
        this._rewards_gone.set(false);
        Context.println(_value + " TAP tokens received from " + _from + ". " + TAG);
    }

    private <T> boolean containsInArrayDb(T value, ArrayDB<T> arraydb) {
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

    @External(readonly = true)
    public boolean getLinearityComplexityMigrationStart() {
        return this.linearityComplexityMigrationStart.get();
    }

    @External
    public void setLinearityComplexityMigrationStart(boolean start) {
        Context.require(Context.getCaller().equals(Context.getOwner()), TAG + ": Only owner of the score call this method.");
        this.linearityComplexityMigrationStart.set(start);
    }

    @External(readonly = true)
    public boolean getLinearityComplexityMigrationComplete() {
        return linearityComplexityMigrationComplete.get();
    }

    private void migrateFromLinearComplexity() {
        int index = this._day_index.get().add(ONE).intValue() % 2;
        int count = this._batch_size.get().intValue();
        int addressLength = this._addresses[index].size();
        int start = this.linearityComplexityMigrationIndex.getOrDefault(0);
        int remainingAddresses = addressLength - start;

        if (count > remainingAddresses) {
            count = remainingAddresses;
        }
        int end = start + count;
        Context.println("Migrating addresses in rewards :: start " + start + " end: " + end);
        for (int i = start; i < end; i++) {
            String address = this._addresses[index].get(i);
            if (this.addressesIndex[index].getOrDefault(Address.fromString(address), ZERO).equals(ZERO)) {
                this.addressesIndex[index].set(Address.fromString(address), BigInteger.valueOf(i + 1));
            }
        }
        if (end == addressLength) {
            this.linearityComplexityMigrationComplete.set(Boolean.TRUE);
        } else {
            this.linearityComplexityMigrationIndex.set(start + count);
        }
    }

    /*
    For Context.call methods
     */
    public <T> T callScore(Class<T> t, Address address, String method, Object... params) {
        return Context.call(t, address, method, params);
    }

    public void callScore(Address address, String method, Object... params) {
        Context.call(address, method, params);
    }
}

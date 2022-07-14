package com.iconbet.score.dividend;

import static java.math.BigInteger.ZERO;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import score.Address;
import score.ArrayDB;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;

import static com.iconbet.score.dividend.utils.Constants.*;


public class Dividend extends Utils {

    public static final String TAG = "ICONbet Dividends";

    //# Variables related to completion of distribution
    private final VarDB<Boolean> _divs_dist_complete = Context.newVarDB(_DIVS_DIST_COMPLETE, Boolean.class);

    // Variables related to batch of tap distribution
    //TODO:not used, commented out
    private final VarDB<BigInteger> _tap_dist_index = Context.newVarDB(_TAP_DIST_INDEX, BigInteger.class);
    private final VarDB<BigInteger> _batch_size = Context.newVarDB(_BATCH_SIZE, BigInteger.class);

    // Tap holders and their balances of TAP tokens
    private final ArrayDB<Address> tapHolders = Context.newArrayDB(_TAP_HOLDERS, Address.class);
    private final DictDB<String, BigInteger> _tap_balances = Context.newDictDB(_TAP_BALANCES, BigInteger.class);
    private final VarDB<BigInteger> _total_eligible_tap_tokens = Context.newVarDB(_TOTAL_ELIGIBLE_TAP_TOKENS, BigInteger.class);

    // Games which have made excess and their excess amount
    private final ArrayDB<Address> _games_list = Context.newArrayDB(_GAMES_LIST, Address.class);
    private final DictDB<String, BigInteger> _games_excess = Context.newDictDB(_GAMES_EXCESS, BigInteger.class);
    private final DictDB<String, Address> _revshare_wallet_address = Context.newDictDB(_REVSHARE_WALLET_ADDRESS, Address.class);

    // Founders/platform holders addresses
    private final ArrayDB<String> _blacklist_address = Context.newArrayDB(_BLACKLIST_ADDRESS, String.class);

    // Dividends of each category
    private final VarDB<BigInteger> _total_divs = Context.newVarDB(_TOTAL_DIVS, BigInteger.class);
    private final VarDB<BigInteger> _remaining_tap_divs = Context.newVarDB(_REMAINING_TAP_DIVS, BigInteger.class);
    private final VarDB<BigInteger> _remaining_gamedev_divs = Context.newVarDB(_REMAINING_GAMEDEV_DIVS, BigInteger.class);
    private final VarDB<BigInteger> _platform_divs = Context.newVarDB(_PLATFORM_DIVS, BigInteger.class);
    private final VarDB<BigInteger> _promo_divs = Context.newVarDB(_PROMO_DIVS, BigInteger.class);
    private final VarDB<BigInteger> _daofund_divs = Context.newVarDB(_DAOFUND_DIVS, BigInteger.class);

    // Games marked as inhouse games
    private final ArrayDB<Address> _inhouse_games = Context.newArrayDB(_INHOUSE_GAMES, Address.class);

    // Dividend percentage for each of the category
    private final ArrayDB<BigInteger> _dividend_percentage = Context.newArrayDB(_DIVIDEND_PERCENTAGE, BigInteger.class);

    // Addresses of external scores with which the dividends score communicates
    private final VarDB<BigInteger> _dividends_received = Context.newVarDB(_DIVIDENDS_RECEIVED, BigInteger.class);

    private final ArrayDB<String> _stake_holders = Context.newArrayDB(_STAKE_HOLDERS, String.class);
    private final DictDB<String, BigInteger> _stake_holders_index = Context.newDictDB(_STAKE_HOLDERS + "_indexes", BigInteger.class);

    private final DictDB<String, BigInteger> _stake_balances = Context.newDictDB(_STAKE_BALANCES, BigInteger.class);
    private final VarDB<BigInteger> _total_eligible_staked_tap_tokens = Context.newVarDB(_TOTAL_ELIGIBLE_STAKED_TAP_TOKENS, BigInteger.class);
    private final VarDB<BigInteger> _stake_dist_index = Context.newVarDB(_STAKE_DIST_INDEX, BigInteger.class);

    private final VarDB<Boolean> _switch_dividends_to_staked_tap = Context.newVarDB(_SWITCH_DIVIDENDS_TO_STAKED_TAP, Boolean.class);

    private final ArrayDB<String> _exception_address = Context.newArrayDB(_EXCEPTION_ADDRESS, String.class);

    public Dividend(@Optional boolean _on_update_var) {
        if (_on_update_var) {
            Context.println("updating contract only");
            onUpdate();
            return;
        }
        this._total_divs.set(ZERO);

    }

    public void onUpdate() {
        Context.println("calling on update. " + TAG);

    }

    @EventLog(indexed = 2)
    public void FundTransfer(String winner, BigInteger amount, String note) {
    }

    @EventLog(indexed = 2)
    public void DivsReceived(BigInteger total_divs, BigInteger batch_size) {
    }

    @EventLog(indexed = 1)
    public void BlacklistAddress(String address, String note) {
    }

    @EventLog(indexed = 1)
    public void InhouseGames(Address address, String note) {
    }

    /*
    Sets the percentage for distribution to tap holders, game developers, promotion and platform. The sum of the
    percentage must be 100.
    Can only be called by owner of the score
    :param _tap: Percentage for distribution to tap holders
    :type _tap: int
    :param _gamedev: Percentage for distribution to game developers
    :type _gamedev: int
    :param _promo: Percentage for distribution to promotion
    :type _promo: int
    :param _platform: Percentage for distribution to platform/founders
    :type _platform: int
     */
    @External
    public void set_dividend_percentage(BigInteger _tap, BigInteger _gamedev, BigInteger _promo, BigInteger _platform) {

        Context.require(_tap != null);
        Context.require(_gamedev != null);
        Context.require(_promo != null);
        Context.require(_platform != null);

        if (!Context.getCaller().equals(Context.getOwner())) {
            Context.revert(TAG + ": Only the owner of the score can call the method");
        }

        if (!(
                inBetween(ZERO, _tap, _100)
                        && inBetween(ZERO, _gamedev, _100)
                        && inBetween(ZERO, _promo, _100)
                        && inBetween(ZERO, _platform, _100)
        )) {
            Context.revert(TAG + ": The parameters must be between 0 to 100");
        }

        if (_tap.add(_gamedev).add(_platform).add(_promo).compareTo(_100) != 0) {
            Context.revert(TAG + ": Sum of all percentage is not equal to 100");
        }

        int size = this._dividend_percentage.size();
        for (int i = 0; i < size; i++) {
            this._dividend_percentage.pop();
        }

        this._dividend_percentage.add(_tap);
        this._dividend_percentage.add(_gamedev);
        this._dividend_percentage.add(_promo);
        this._dividend_percentage.add(_platform);
    }

    /*
    Returns all the categories of dividend and their percentage.
    :return: Category of dividends and their percentage
    :rtype: map
     */
    @SuppressWarnings("unchecked")
    @External(readonly = true)
    public Map<String, BigInteger> get_dividend_percentage() {

        Map.Entry<String, BigInteger>[] entries = new Map.Entry[DIVIDEND_CATEGORIES.size()];

        for (int i = 0; i < DIVIDEND_CATEGORIES.size(); i++) {
            entries[i] = Map.entry(DIVIDEND_CATEGORIES.get(i), this._dividend_percentage.get(i));
        }
        return Map.ofEntries(entries);
    }

    /*
    Sets the token score address. The function can only be invoked by score owner.
    :param _score: Score address of the token
    :type _score: :class:`iconservice.base.address.Address`
     */
    @External
    public void set_token_score(Address _score) {
        if (!_score.isContract()) {
            Context.revert(TAG + ": " + _score + " is not a valid contract address");
        }
        Context.require(Context.getCaller().equals(Context.getOwner()), TAG + ": Only owner can call this method.");
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        Context.println("setting token score address: " + _score);
        scoreAddresses._token_score.set(_score);
    }

    /*
    Returns the status of the switch to enable the dividends to staked tap holders
    :return: True if the switch for dividends to staked tap is enabled
     */
    @External(readonly = true)
    public boolean get_switch_dividends_to_staked_tap() {
        return this._switch_dividends_to_staked_tap.getOrDefault(false);
    }

    @External
    public void toggle_switch_dividends_to_staked_tap_enabled() {
        if (!Context.getCaller().equals(Context.getOwner())) {
            Context.revert(TAG + ": Only owner can enable or disable switch dividends to staked tap holders.");
        }
        this._switch_dividends_to_staked_tap.set(!this._switch_dividends_to_staked_tap.getOrDefault(false));
    }

    /*
    Sets the roulette score address. The function can only be invoked by score owner.
    :param _score: Score address of the roulette
    :type _score: :class:`iconservice.base.address.Address`
     */
    @External
    public void set_game_score(Address _score) {
        if (!_score.isContract()) {
            Context.revert(TAG + ": " + _score + " is not a valid contract address");
        }
        Context.require(Context.getCaller().equals(Context.getOwner()), TAG + ": Only owner can call this method.");
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        scoreAddresses._game_score.set(_score);

    }

    /*
    Sets the promo score address. The function can only be invoked by score owner.
    :param _score: Score address of the promo
    :type _score: :class:`iconservice.base.address.Address`
     */
    @External
    public void set_promo_score(Address _score) {

        if (!_score.isContract()) {
            Context.revert(TAG + ": " + _score + " is not a valid contract address");
        }

        Context.require(Context.getCaller().equals(Context.getOwner()), TAG + ": Only owner can call this method.");
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        scoreAddresses._promo_score.set(_score);

    }

    /*
    Sets the promo score address. The function can only be invoked by score owner.
    :param _score: Score address of the daofund
    :type _score: :class:`iconservice.base.address.Address`
     */
    @External
    public void set_daofund_score(Address _score) {
        if (!_score.isContract()) {
            Context.revert(TAG + ": " + _score + " is not a valid contract address");
        }
        Context.require(Context.getCaller().equals(Context.getOwner()), TAG + ": Only owner can call this method.");
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        scoreAddresses._daofund_score.set(_score);

    }

    /*
    Sets the game authorization score address. The method can only be invoked by score owner
    :param _score: Score address of the game authorization score
    :type _score: :class:`iconservice.base.address.Address`
    :return:
     */
    @External
    public void set_game_auth_score(Address _score) {

        if (!_score.isContract()) {
            Context.revert(TAG + ": " + _score + " is not a valid contract address");
        }
        Context.require(Context.getCaller().equals(Context.getOwner()), TAG + ": Only owner can call this method.");
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        scoreAddresses._game_auth_score.set(_score);

    }

    @External
    public void setIBPNPScore(Address _score) {

        if (!_score.isContract()) {
            Context.revert(TAG + ": " + _score + " is not a valid contract address");
        }
        Context.require(Context.getCaller().equals(Context.getOwner()), TAG + ": Only owner can call this method.");
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        scoreAddresses.ibpnpScore.set(_score);

    }

    /*
    Returns the token score address.
    :return: Address of the token score
    :rtype: :class:`iconservice.base.address.Address`
     */
    @External(readonly = true)
    public Address get_token_score() {
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        return scoreAddresses._token_score.getOrDefault(ZERO_ADDRESS);
    }

    /*
    Returns the roulette score address.
    :return: Address of the roulette score
    :rtype: :class:`iconservice.base.address.Address`
     */
    @External(readonly = true)
    public Address get_game_score() {
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        return scoreAddresses._game_score.getOrDefault(ZERO_ADDRESS);
    }

    /*
    Returns the promotion score address.
    :return: Address of the promotion score
    :rtype: :class:`iconservice.base.address.Address`
     */
    @External(readonly = true)
    public Address get_promo_score() {
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        return scoreAddresses._promo_score.getOrDefault(ZERO_ADDRESS);
    }

    @External(readonly = true)
    public Address getIBPNPScore() {
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        return scoreAddresses.ibpnpScore.getOrDefault(ZERO_ADDRESS);
    }

    /*
    Returns the promotion score address.
    :return: Address of the daofund score
    :rtype: :class:`iconservice.base.address.Address`
     */
    @External(readonly = true)
    public Address get_daofund_score() {
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        return scoreAddresses._daofund_score.getOrDefault(ZERO_ADDRESS);
    }

    /*
    Returns the game authorization score address
    :return: Address of the game authorization score address
    :rtype: :class:`iconservice.base.address.Address`
     */
    @External(readonly = true)
    public Address get_game_auth_score() {
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        return scoreAddresses._game_auth_score.getOrDefault(ZERO_ADDRESS);
    }

    /*
    Checks the status of dividends distribution
    :return: True if distribution is completed
    :rtype: bool
     */
    @External(readonly = true)
    public boolean dividends_dist_complete() {
        return this._divs_dist_complete.getOrDefault(false);
    }

    /*
    Returns total dividends of previous day. It is distributed on the current day.
    :return: Total dividends for distribution of previous day
    :rtype: int
     */
    @External(readonly = true)
    public BigInteger get_total_divs() {
        return this._total_divs.get();
    }


    /*
    A function to redefine the value of self.owner once it is possible.
    To be included through an update if it is added to IconService.

    Sets the value of self.owner to the score holding the game treasury.
     */
    @External
    public void untether() {
        if (!Context.getOrigin().equals(Context.getOwner())) {
            Context.revert(TAG + ": Only the owner can call the untether method.");
        }
    }

    /*
    Main distribute function invoked by rewards distribution contract. This function can also be called when the
    treasury needs to be dissolved. For dissolving the treasury, there must be a majority of votes from TAP holders.
    :return: True if distribution is completed
    :rtype: bool
     */
    @External
    public boolean distribute() {
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        if (this._dividends_received.getOrDefault(ZERO).equals(ONE)) {
            Context.println("dividend is one");
            this._divs_dist_complete.set(false);
            this._dividends_received.set(TWO);
            if (!this._switch_dividends_to_staked_tap.getOrDefault(false)) {
                Context.call(scoreAddresses._token_score.get(), "switch_address_update_db");
                // calculate total eligible tap
                this._set_total_tap();
            }

        } else if (this._dividends_received.getOrDefault(ZERO).equals(TWO)) {
            Context.println("dividend is two");
            if (this._update_stake_balances()) {
                Context.println("updated stake balances");
                callScore(scoreAddresses._token_score.get(), "switch_stake_update_db");
                //calculate total eligible staked tap tokens
                this._set_total_staked_tap();
                this._set_tap_of_exception_address();
                this._dividends_received.set(_3);
            }

        } else if (this._dividends_received.getOrDefault(ZERO).equals(_3)) {
            Context.println("dividend is three");
            // Set the dividends for each category
            BigInteger balance = Context.getBalance(Context.getAddress());
            this._total_divs.set(balance);
            boolean treasuryStatus = callScore(Boolean.class, scoreAddresses._game_score.get(), "get_treasury_status");
            if (treasuryStatus) {
                this._remaining_tap_divs.set(balance);
                this._remaining_gamedev_divs.set(ZERO);
                this._promo_divs.set(ZERO);
                this._daofund_divs.set(ZERO);
                this._platform_divs.set(ZERO);
            } else if (this._switch_dividends_to_staked_tap.getOrDefault(false)) {
                this._set_games_ip();
            } else {
                // Set the games making excess and their excess balance and the dividends of categories
                this._set_games();
            }

            BigInteger batchSize = callScore(BigInteger.class, scoreAddresses._game_score.get(), "get_batch_size", BigInteger.valueOf(this._stake_holders.size()));
            this._batch_size.set(batchSize);
            this._dividends_received.set(ZERO);

        } else if (this._divs_dist_complete.getOrDefault(false)) {
            Context.println("dividends distribuition complete");
            this._update_stake_balances();
            callScore(scoreAddresses._token_score.get(), "clear_yesterdays_stake_changes");
            return true;
        } else if (this._remaining_tap_divs.getOrDefault(ZERO).compareTo(ZERO) > 0) {
            this._distribute_to_stake_holders();
        } else if (this._promo_divs.getOrDefault(ZERO).compareTo(ZERO) > 0) {
            this._distribute_to_promo_address();
        } else if (this._daofund_divs.getOrDefault(ZERO).compareTo(ZERO) > 0) {
            this._distribute_to_daofund_address();
        } else if (this._remaining_gamedev_divs.getOrDefault(ZERO).compareTo(ZERO) > 0) {
            this._distribute_to_game_developers();
        } else if (this._platform_divs.getOrDefault(ZERO).compareTo(ZERO) > 0) {
            this._distribute_to_platform();
        } else {
            this._divs_dist_complete.set(true);
            return true;
        }
        return false;
    }

    /*
    Distributes the dividend according to set percentage for promotion
     */
    private void _distribute_to_promo_address() {
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        BigInteger amount = this._promo_divs.getOrDefault(ZERO);
        Address address = scoreAddresses._promo_score.getOrDefault(ZERO_ADDRESS);
        if (amount.compareTo(ZERO) > 0) {
            try {
                Context.transfer(address, amount);
                this.FundTransfer(
                        address.toString(), amount, "Dividends distribution to Promotion contract"
                );
                this._promo_divs.set(ZERO);
            } catch (Exception e) {
                Context.revert(
                        "Network problem while sending to game SCORE. " +
                                "Distribution of " + amount + " not sent to " + address + ". " +
                                "Will try again later. " +
                                "Exception: " + e.getMessage()
                );
            }
        }
    }

    /*
    Distributes the dividend according to set percentage for DAOFund
     */
    private void _distribute_to_daofund_address() {
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        BigInteger amount = this._daofund_divs.getOrDefault(ZERO);
        Address address = scoreAddresses._daofund_score.getOrDefault(ZERO_ADDRESS);
        if (amount.compareTo(ZERO) > 0) {
            try {
                this._daofund_divs.set(ZERO);
                this.FundTransfer(address.toString(), amount, "Dividends distribution to DAOFund contract");
                Context.transfer(address, amount);
            } catch (Exception e) {
                Context.revert(
                        TAG + ": Network problem while sending to game SCORE. Distribution of " + amount + " not sent to " + address + " " +
                                "Will try again later. Exception: " + e.getMessage()
                );
            }
        }
    }

    /*
    Distributes the dividends to game developers if only their game has made a positive excess i.e. total wager
    is greater than their total payout
     */
    private void _distribute_to_game_developers() {

        for (int i = 0; i < this._games_list.size(); i++) {
            Address game = this._games_list.get(i);
            if (containsInArrayDb(game, this._inhouse_games)) {
                //TODO: possible division by 0, infinte value
                BigInteger amount = this._dividend_percentage.get(1).multiply(this._games_excess.getOrDefault(game.toString(), ZERO)).divide(_100);
                //TODO: possible negative value, is that correct?
                this._remaining_gamedev_divs.set(this._remaining_gamedev_divs.getOrDefault(ZERO).subtract(amount));
                this._platform_divs.set(this._platform_divs.getOrDefault(ZERO).add(amount));
            } else {
                BigInteger amount = this._games_excess.getOrDefault(game.toString(), ZERO).multiply(this._dividend_percentage.get(1)).divide(_100);
                Address address = this._revshare_wallet_address.get(game.toString());
                if (amount.compareTo(ZERO) > 0) {
                    try {
                        Context.transfer(address, amount);

                        this.FundTransfer(
                                address.toString(),
                                amount,
                                "Dividends distribution to Game developer's wallet address"
                        );
                        //TODO: possible negative value, is that correct?
                        this._remaining_gamedev_divs.set(this._remaining_gamedev_divs.getOrDefault(ZERO).subtract(amount));
                    } catch (Exception e) {
                        Context.revert(
                                "Network problem while sending to revshare wallet address " +
                                        "Distribution of {amount} not sent to {address}. " +
                                        "Will try again later. " +
                                        "Exception: " + e.getMessage()
                        );
                    }
                }
            }
        }
        int gamesListSize = this._games_list.size();
        for (int i = 0; i < gamesListSize; i++) {
            Address game = this._games_list.pop();
            //TODO: verify that this is removing a element
            this._games_excess.set(game.toString(), null);
        }
        this._remaining_gamedev_divs.set(ZERO);
    }

    /*
    Distributes the dividends to platform/founder members.
     */
    private void _distribute_to_platform() {
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        BigInteger total_platform_tap = ZERO;
        for (int i = 0; i < this._blacklist_address.size(); i++) {
            String address = this._blacklist_address.get(i);
            Address address_from_str = Address.fromString(address);
            if (!address_from_str.isContract()) {
                BigInteger balanceOfAdd = callScore(BigInteger.class, scoreAddresses._token_score.get(), "balanceOf", address_from_str);
                total_platform_tap = total_platform_tap.add(balanceOfAdd);
            }
        }
        if (total_platform_tap.equals(ZERO)) {
            Context.revert(TAG + ": No tap found in founder's addresses");
        }
        BigInteger dividends = this._platform_divs.getOrDefault(ZERO);
        for (int i = 0; i < this._blacklist_address.size(); i++) {
            String address = this._blacklist_address.get(i);
            Address address_from_str = Address.fromString(address);
            BigInteger balance = callScore(BigInteger.class, scoreAddresses._token_score.get(), "balanceOf", address_from_str);
            if (!address_from_str.isContract() && total_platform_tap.compareTo(ZERO) > 0 && balance.compareTo(ZERO) > 0 && dividends.compareTo(ZERO) > 0) {
                BigInteger amount = balance.multiply(dividends).divide(total_platform_tap);
                dividends = dividends.subtract(amount);
                total_platform_tap = total_platform_tap.subtract(balance);
                try {
                    Context.transfer(address_from_str, amount);
                    this.FundTransfer(address, amount, "Dividends distribution to Platform/Founders address");
                } catch (Exception e) {
                    Context.revert(
                            "Network problem while sending to founder members address " +
                                    "Distribution of " + amount + " not sent to " + address + ". " +
                                    "Will try again later. " +
                                    "Exception: " + e.getMessage()
                    );
                }
            }
        }
        this._platform_divs.set(ZERO);
    }

    /*
    This function distributes the dividends to staked tap token holders.
     */
    public void _distribute_to_stake_holders() {
        TapDividendsTax tapDividendsTax = new TapDividendsTax();
        BigInteger count = this._batch_size.getOrDefault(ZERO);
        BigInteger length = BigInteger.valueOf(this._stake_holders.size());
        BigInteger start = this._stake_dist_index.getOrDefault(ZERO);
        BigInteger remaining_addresses = length.subtract(start);
        if (count.compareTo(remaining_addresses) > 0) {
            count = remaining_addresses;
        }
        BigInteger end = start.add(count);
        BigInteger dividend = this._remaining_tap_divs.getOrDefault(ZERO);
        BigInteger tokens_total = this._total_eligible_staked_tap_tokens.getOrDefault(ZERO);

        for (int i = start.intValue(); i < end.intValue(); i++) {
            String address = this._stake_holders.get(i);
            BigInteger holder_balance = this._stake_balances.get(address);
            if (holder_balance.compareTo(ZERO) > 0 && tokens_total.compareTo(ZERO) > 0) {
                BigInteger amount = dividend.multiply(holder_balance).divide(tokens_total);
                dividend = dividend.subtract(amount);
                tokens_total = tokens_total.subtract(holder_balance);

                Address addressKey = Address.fromString(address);
                String value = tapDividendsTax.tapDividendsClaimablePerDay.getOrDefault(addressKey, "");
                if (value.equals("")){
                    tapDividendsTax.tapDividendsClaimablePerDay.set(addressKey, ZERO.add(amount) + ",");
                } else {
                    tapDividendsTax.tapDividendsClaimablePerDay.set(addressKey, new BigInteger(value).add(amount) + ",");
                }
                BigInteger claimableDividends = tapDividendsTax.tapDividendsClaimable.getOrDefault(addressKey, ZERO);
                tapDividendsTax.tapDividendsClaimable.set(addressKey, claimableDividends.add(amount));
                tapDividendsTax.tapDividendsTaxable.set(addressKey, getTaxableAmount(addressKey));
            }
        }
        this._remaining_tap_divs.set(dividend);
        this._total_eligible_staked_tap_tokens.set(tokens_total);

        if (end.equals(length) || dividend.compareTo(ZERO) <= 0) {
            this._stake_dist_index.set(ZERO);
            this._remaining_tap_divs.set(ZERO);
        } else {
            this._stake_dist_index.set(start.add(count));
        }
    }

    /*
    Returns all the blacklisted addresses(rewards score address and devs team address)
    :return: List of blacklisted address
    :rtype: list
     */
    @External(readonly = true)
    public List<String> get_blacklist_addresses() {
        String[] address_list = new String[this._blacklist_address.size()];
        return arrayDBtoList(this._blacklist_address, address_list);
    }

    /*
    Removes the address from blacklist.
    Only owner can remove the blacklist address
    :param _address: Address to be removed from blacklist
    :type _address: :class:`iconservice.base.address.Address`
    :return:
     */
    @External
    public void remove_from_blacklist(String _address) {
        if (Context.getCaller().equals(Context.getOwner())) {
            if (!containsInArrayDb(_address, this._blacklist_address)) {
                Context.revert(TAG + ": " + _address + " not in blacklist address");
            }
            this.BlacklistAddress(_address, "Removed from blacklist");
            String top = this._blacklist_address.pop();
            if (!top.equals(_address)) {
                for (int i = 0; i < this._blacklist_address.size(); i++) {
                    if (this._blacklist_address.get(i) == _address) {
                        this._blacklist_address.set(i, top);
                    }
                }
            }
        }
    }

    /*
    The provided address is set as blacklist address and will be excluded from TAP dividends.
    Only the owner can set the blacklist address
    :param _address: Address to be included in the blacklist
    :type _address: :class:`iconservice.base.address.Address`
    :return:
     */
    @External
    public void set_blacklist_address(String _address) {
        if (Context.getCaller().equals(Context.getOwner())) {
            this.BlacklistAddress(_address, "Added to Blacklist");
            if (!containsInArrayDb(_address, this._blacklist_address)) {
                this._blacklist_address.add(_address);
            }
        }
    }

    /*
    Sets the inhouse games list. The dividend for game developers for these games will be sent to the
    platform/founders. Only owner can set the games as inhouse games.
    :param _score: Game address to be defined as in-house game.
    :type _score: :class:`iconservice.base.address.Address`
    :return:
     */
    @External
    public void set_inhouse_games(Address _score) {
        if (!_score.isContract()) {
            Context.revert(TAG + ": " + _score + "  should be a contract address");
        }
        if (Context.getCaller().equals(Context.getOwner())) {
            this.InhouseGames(_score, "Added as inhouse games");
            if (!containsInArrayDb(_score, this._inhouse_games)) {
                this._inhouse_games.add(_score);
            }
        }
    }

    /*
    Returns all the inhouse games developed by ICONBet team
    :return: Returns the list of inhouse games
    :rtype: list
     */
    @External(readonly = true)
    public List<Address> get_inhouse_games() {
        Address[] games_list = new Address[this._inhouse_games.size()];
        return arrayDBtoList(this._inhouse_games, games_list);
    }

    /*
    Remove the game address from inhouse game developers list. Only the owner can remove the game from inhouse
    games list.
    :param _score: Game address to be removed from inhouse games
    :type _score: :class:`iconservice.base.address.Address`
    :return:
     */
    @External
    public void remove_from_inhouse_games(Address _score) {
        if (!_score.isContract()) {
            Context.revert(TAG + " : " + _score + " is not a valid contract address");
        }
        if (Context.getCaller().equals(Context.getOwner())) {
            if (!containsInArrayDb(_score, this._inhouse_games)) {
                Context.revert(TAG + ": " + _score + " is not in inhouse games list");
            }
            this.InhouseGames(_score, "Removed from inhouse games list");
            Address top = this._inhouse_games.pop();
            if (!top.equals(_score)) {
                for (int i = 0; i < this._inhouse_games.size(); i++) {
                    if (this._inhouse_games.get(i).equals(_score)) {
                        this._inhouse_games.set(i, top);
                    }
                }
            }
        }
    }

    /*
    Updates the balances of tap holders in dividends distribution contract
    :return:
     */
    @SuppressWarnings({"unchecked"})
    private boolean _update_stake_balances() {
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        Map<String, BigInteger> stake_balances = callScore(Map.class, scoreAddresses._token_score.get(), "get_stake_updates");
        if (stake_balances.size() == 0) {
            return true;
        }

        Iterator<String> it = stake_balances.keySet().iterator();
        while (it.hasNext()) {
            String address = it.next();
            if (this._stake_holders_index.getOrDefault(address, ZERO).equals(ZERO)) {
                this._stake_holders.add(address);
                this._stake_holders_index.set(address, BigInteger.valueOf(this._stake_holders.size()));
            }
            this._stake_balances.set(address, stake_balances.get(address));
        }
        return false;
    }

    /*
    Updates the balances of tap holders in dividends distribution contract
    :return:
     */
    //TODO: not used
    @SuppressWarnings({"unchecked"})
    private boolean _update_balances() {
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        Map<String, BigInteger> tap_balances = (Map<String, BigInteger>) Context.call(scoreAddresses._token_score.get(), "get_balance_updates");

        if (tap_balances.size() == 0) {
            return true;
        }
        Iterator<String> it = tap_balances.keySet().iterator();
        while (it.hasNext()) {
            String address = it.next();
            if (!containsInArrayDb(address, this._blacklist_address)) {
                this._tap_balances.set(address, tap_balances.get(address));
            }
        }
        return false;
    }

    /*
    Sets the eligible tap holders i.e. except the blacklist addresses, updates the balances of tap holders and
    sets the total eligible tap tokens
    :return:
     */
    private void _set_total_tap() {
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        Context.println("looking into blacklist address");
        BigInteger total = ZERO;
        for (int i = 0; i < this._blacklist_address.size(); i++) {
            String address = this._blacklist_address.get(i);
            Address address_from_str = Address.fromString(address);
            BigInteger balance = callScore(BigInteger.class, scoreAddresses._token_score.get(), "balanceOf", address_from_str);
            total = total.add(balance);
        }
        BigInteger totalSupply = callScore(BigInteger.class, scoreAddresses._token_score.get(), "totalSupply");
        this._total_eligible_tap_tokens.set(totalSupply.subtract(total));
    }

    /*
    Sets the total staked tap tokens.
    :return:
     */
    private void _set_total_staked_tap() {
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        BigInteger stakedBalance = callScore(BigInteger.class, scoreAddresses._token_score.get(), "total_staked_balance");
        this._total_eligible_staked_tap_tokens.set(stakedBalance);
    }

    /*
    Takes list of games to receive excess from game authorization score and sets the games list.
    Sets the excess of those games.
    :return:
     */
    @SuppressWarnings({"unchecked"})
    private void _set_games() {
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        Map<String, String> gamesExcess = callScore(Map.class, scoreAddresses._game_auth_score.get(), "get_yesterdays_games_excess");

        BigInteger positive_excess = ZERO;

        Iterator<String> it = gamesExcess.keySet().iterator();
        while (it.hasNext()) {
            String game = it.next();
            Address game_address = Address.fromString(game);

            BigInteger gameEx = new BigInteger(gamesExcess.get(game));
            if (gameEx.compareTo(ZERO) > 0) {
                if (!containsInArrayDb(game_address, this._games_list)) {
                    this._games_list.add(game_address);
                }
                this._games_excess.set(game, gameEx);
                Address revshare = callScore(Address.class, scoreAddresses._game_auth_score.get(), "get_revshare_wallet_address", game_address);
                if (!this._revshare_wallet_address.getOrDefault(game, ZERO_ADDRESS).equals(revshare)) {
                    this._revshare_wallet_address.set(game, revshare);
                }
                positive_excess = positive_excess.add(gameEx);
            }
        }

        BigInteger game_developers_share = this._dividend_percentage.get(1).add(this._dividend_percentage.get(3));
        BigInteger game_developers_amount = game_developers_share.multiply(positive_excess).divide(_100);
        BigInteger tap_holders_amount = Context.getBalance(Context.getAddress()).subtract(game_developers_amount);

        this._remaining_gamedev_divs.set(
                this._dividend_percentage.get(1).multiply(game_developers_amount).divide(game_developers_share));
        this._platform_divs.set(this._dividend_percentage.get(3).multiply(game_developers_amount).divide(game_developers_share));
        if (tap_holders_amount.compareTo(ZERO) > 0) {
            BigInteger tap_holders_share = this._dividend_percentage.get(0).add(this._dividend_percentage.get(2));
            this._remaining_tap_divs.set(this._dividend_percentage.get(0).multiply(tap_holders_amount).divide(tap_holders_share));
            this._promo_divs.set(this._dividend_percentage.get(2).multiply(tap_holders_amount).multiply(tap_holders_share));
        } else {
            this._remaining_tap_divs.set(ZERO);
            this._promo_divs.set(ZERO);
        }
    }

    /*
    Set the dividends for different categories according to the improvement proposal
     */
    @SuppressWarnings({"unchecked"})
    private void _set_games_ip() {
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        Map<String, String> gamesExcess = callScore(Map.class, scoreAddresses._game_auth_score.get(), "get_yesterdays_games_excess");
        BigInteger third_party_excess = ZERO;
        BigInteger inhouse_excess = ZERO;

        Iterator<String> it = gamesExcess.keySet().iterator();
        while (it.hasNext()) {
            String game = it.next();
            Address game_address = Address.fromString(game);

            BigInteger gameEx = new BigInteger(gamesExcess.get(game));
            if (gameEx.compareTo(ZERO) > 0) {
                if (!containsInArrayDb(game_address, this._inhouse_games)) {
                    if (!containsInArrayDb(game_address, this._games_list)) {
                        this._games_list.add(game_address);
                    }
                    this._games_excess.set(game, gameEx);
                    Address revshare = callScore(Address.class, scoreAddresses._game_auth_score.get(), "get_revshare_wallet_address", game_address);
                    if (!this._revshare_wallet_address.getOrDefault(game, ZERO_ADDRESS).equals(revshare)) {
                        this._revshare_wallet_address.set(game, revshare);
                    }
                    third_party_excess = third_party_excess.add(gameEx);

                } else if (containsInArrayDb(game_address, this._inhouse_games)) {
                    inhouse_excess = inhouse_excess.add(gameEx);
                }
            }
        }

        BigInteger game_developers_amount = third_party_excess.multiply(_20).divide(_100);
        BigInteger daofund_amount = inhouse_excess.multiply(_20).divide(_100);
        BigInteger tap_holders_amount = Context.getBalance(Context.getAddress()).subtract(game_developers_amount).subtract(daofund_amount);

        this._remaining_gamedev_divs.set(game_developers_amount);
        this._daofund_divs.set(daofund_amount);
        this._platform_divs.set(ZERO);
        if (tap_holders_amount.compareTo(ZERO) > 0) {
            BigInteger tap_divs = tap_holders_amount.multiply(_80).divide(_90);
            this._remaining_tap_divs.set(tap_divs);
            this._promo_divs.set(tap_holders_amount.subtract(tap_divs));
        } else {
            this._remaining_tap_divs.set(ZERO);
            this._promo_divs.set(ZERO);
        }
    }

    @External(readonly = true)
    public BigInteger get_staked_tap_hold_length() {
        return BigInteger.valueOf(this._stake_holders.size());
    }

    @External(readonly = true)
    public Map<String, String> divs_share() {
        return Map.of(
                "tap", this._remaining_tap_divs.getOrDefault(ZERO).toString(),
                "wager", this._promo_divs.getOrDefault(ZERO).toString(),
                "gamedev", this._remaining_gamedev_divs.getOrDefault(ZERO).toString(),
                "platform", this._platform_divs.getOrDefault(ZERO).toString()
        );
    }

    @External
    public void set_divs_share(BigInteger _tap, BigInteger _promo, BigInteger _platform, BigInteger _gamedev) {
        if (!Context.getCaller().equals(Context.getOwner())) {
            Context.revert(TAG + ": This method is only available for the owner");
        }
        this._remaining_tap_divs.set(_tap);
        this._promo_divs.set(_promo);
        this._platform_divs.set(_platform);
        this._remaining_gamedev_divs.set(_gamedev);
    }

    @External
    public void toggle_divs_dist() {
        if (!Context.getCaller().equals(Context.getOwner())) {
            Context.revert(TAG + ": Only the owner can call this method");
        }
        if (this._divs_dist_complete.getOrDefault(false)) {
            this._divs_dist_complete.set(false);
        } else {
            this._divs_dist_complete.set(true);
        }
    }

    @External(readonly = true)
    public List<String> get_exception_address() {
        String[] list = new String[this._exception_address.size()];
        return arrayDBtoList(this._exception_address, list);
    }

    @External
    public void add_exception_address(Address _address) {
        if (!Context.getCaller().equals(Context.getOwner())) {
            Context.revert(TAG + ":  Only owner can add an exception address");
        }
        String str_address = _address.toString();
        if (!containsInArrayDb(str_address, this._exception_address)) {
            this._exception_address.add(str_address);
        }
    }

    @External
    public void remove_exception_address(Address _address) {
        if (!Context.getCaller().equals(Context.getOwner())) {
            Context.revert(TAG + ":  Only owner can remove an exception address");
        }

        String str_address = _address.toString();
        if (!containsInArrayDb(str_address, this._exception_address)) {
            Context.revert(TAG + ":  Address to remove not found in exception address list.");
        }

        this._stake_balances.set(str_address, ZERO);
        if (this._exception_address.size() > 0) {
            String _out = this._exception_address.get(this._exception_address.size() - 2);
            if (_out.equals(str_address)) {
                this._exception_address.pop();
            }
            for (int index = 0; index < this._exception_address.size() - 1; index++) {
                if (this._exception_address.get(index).equals(str_address)) {
                    this._exception_address.set(index, _out);
                    this._exception_address.pop();
                }
            }
        }
    }

    public void _set_tap_of_exception_address() {
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        System.out.println("exception size: " + this._exception_address.size());
        for (int idx = 0; idx < this._exception_address.size(); idx++) {
            String address = this._exception_address.get(idx);
            if (this._stake_holders_index.getOrDefault(address, ZERO).equals(ZERO)) {
                this._stake_holders.add(address);
                this._stake_holders_index.set(address, BigInteger.valueOf(this._stake_holders.size()));
            }
            Address add = Address.fromString(address);
            BigInteger tap_balance = callScore(BigInteger.class, scoreAddresses._token_score.get(), "balanceOf", add);
            BigInteger staked_balance = callScore(BigInteger.class, scoreAddresses._token_score.get(), "staked_balanceOf", add);
            this._stake_balances.set(address, tap_balance);
            this._total_eligible_staked_tap_tokens.set(this._total_eligible_staked_tap_tokens.getOrDefault(ZERO).add(tap_balance)
                    .subtract(staked_balance));
        }
    }

    @Payable
    public void fallback() {
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        if (Context.getCaller().equals(scoreAddresses._game_score.get())) {
            // Set the status of all divisions as False
            this._dividends_received.set(ONE);
            this.DivsReceived(this._total_divs.get(), this._batch_size.get());
        } else {
            Context.revert(TAG + ": Funds can only be accepted from the game contract.");
        }
    }

    @Payable
    @External
    public void add_funds() {
        if (!Context.getCaller().equals(Context.getOwner())) {
            Context.revert(TAG + ": Only owner can transfer the amount to dividends contract.");
        }
    }

    /**
     * return min <= value <= max
     */
    public boolean inBetween(BigInteger min, BigInteger value, BigInteger max) {
        return min.compareTo(value) <= 0 && value.compareTo(max) <= 0;
    }


    @External
    public void setTaxPercentage(int percentage) {
        TapDividendsTax tapDividendsTax = new TapDividendsTax();
        Context.require(Context.getCaller() == Context.getOwner(), TAG + ": This method is only available for the owner.");
        Context.require(0 <= percentage && percentage <= 100, TAG + ": Percentage must be between 0 and 100.");
        tapDividendsTax.tapDividendsTaxPercentage.set(BigInteger.valueOf(percentage));
    }

    @External(readonly = true)
    public BigInteger getTaxPercentage() {

        TapDividendsTax tapDividendsTax = new TapDividendsTax();
        return tapDividendsTax.tapDividendsTaxPercentage.get();
    }

    @External
    public void setNonTaxPeriod(int period) {
        Context.require(Context.getCaller() == Context.getOwner(), TAG + ": This method is only available for the owner.");
        TapDividendsTax tapDividendsTax = new TapDividendsTax();
        Context.require(period > 0, TAG + ": Percentage must be greater than 0.");
        tapDividendsTax.tapDividendsNonTaxPeriod.set(period);
    }

    @External(readonly = true)
    public int getNonTaxPeriod() {
        TapDividendsTax tapDividendsTax = new TapDividendsTax();
        return tapDividendsTax.tapDividendsNonTaxPeriod.get();
    }

    private BigInteger getActualClaimableAmount(Address owner) {
        TapDividendsTax tapDividendsTax = new TapDividendsTax();
        BigInteger claimableAmount = tapDividendsTax.tapDividendsClaimable.get(owner);
        BigInteger taxableAmount = tapDividendsTax.tapDividendsTaxable.get(owner);
        BigInteger tax = taxableAmount.multiply(tapDividendsTax.tapDividendsTaxPercentage.get());

        return claimableAmount.subtract(tax);
    }

    private BigInteger getTaxableAmount(Address userAddress) {
        TapDividendsTax tapDividendsTax = new TapDividendsTax();
        int nonTaxPeriod = tapDividendsTax.tapDividendsNonTaxPeriod.get();
        List<String> tapDividendsClaimablePerDay = splitString(tapDividendsTax.tapDividendsClaimablePerDay.get(userAddress));

        BigInteger taxableAmount = ZERO;
        int start = 0;

        int end = tapDividendsClaimablePerDay.size() - nonTaxPeriod;
        if (end > 0) {
            for (int i = start; i < end; i++) {
                taxableAmount = taxableAmount.add(new BigInteger(tapDividendsClaimablePerDay.get(i)));
            }
        }
        return taxableAmount;
    }

    @External(readonly = true)
    public Map<?, ?> getClaimableAmount(Address address) {
        TapDividendsTax tapDividendsTax = new TapDividendsTax();
        return Map.of("total", tapDividendsTax.tapDividendsClaimable.get(address),
                "taxable", tapDividendsTax.tapDividendsTaxable.get(address),
                "actual_claimable", getActualClaimableAmount(address));
    }

    @External
    public void claimRewards() {
        Address caller = Context.getCaller();
        ScoreAddresses scoreAddresses = new ScoreAddresses();
        Boolean hasWallet = callScore(Boolean.class, scoreAddresses.ibpnpScore.get(), "has_alternate_wallet", caller);
        Context.require(hasWallet, TAG + ": Player NFT Profile doesn't exists.");
        TapDividendsTax tapDividendsTax = new TapDividendsTax();
        BigInteger claimableAmount = tapDividendsTax.tapDividendsClaimable.get(caller);
        BigInteger taxableAmount = getTaxableAmount(caller);
        BigInteger tax = taxableAmount.multiply(tapDividendsTax.tapDividendsTaxPercentage.get());
        BigInteger actualClaimableAmount = claimableAmount.subtract(tax);

        tapDividendsTax.tapDividendsClaimablePerDay.set(caller, "");
        tapDividendsTax.tapDividendsClaimable.set(caller, ZERO);
        tapDividendsTax.tapDividendsTaxable.set(caller, ZERO);

        Address alternateAddress = callScore(Address.class, scoreAddresses.ibpnpScore.get(), "get_alternate_wallet_address", caller);

        BigInteger alternateClaimableAmount = tapDividendsTax.tapDividendsClaimable.get(alternateAddress);
        BigInteger alternateTaxableAmount = getTaxableAmount(alternateAddress);
        BigInteger alternateTax = alternateTaxableAmount.multiply(tapDividendsTax.tapDividendsTaxPercentage.get());
        BigInteger alternateActualClaimableAmount = alternateClaimableAmount.subtract(tax);

        tax = tax.add(alternateTax);
        actualClaimableAmount = actualClaimableAmount.add(alternateActualClaimableAmount);

        tapDividendsTax.tapDividendsClaimablePerDay.set(alternateAddress, "");
        tapDividendsTax.tapDividendsClaimable.set(alternateAddress, ZERO);
        tapDividendsTax.tapDividendsTaxable.set(alternateAddress, ZERO);

        if (actualClaimableAmount.compareTo(ZERO) > 0) {
            if (tax.compareTo(ZERO) > 0) {
                Address targetAddress = scoreAddresses._daofund_score.get();
                Context.transfer(targetAddress, tax);
                FundTransfer(String.valueOf(targetAddress), tax, "Dividends distribution to DAOFund as tax for late claim.");
            }
            Context.transfer(caller, actualClaimableAmount);
            FundTransfer(String.valueOf(caller), actualClaimableAmount, "Dividends distribution to tap holder");
        }
    }

    public <T> T callScore(Class<T> t, Address address, String method, Object... params) {
        return Context.call(t, address, method, params);
    }

    public void callScore(Address address, String method, Object... params) {
        Context.call(address, method, params);
    }

    public void callScore(BigInteger amount, Address address, String method, Object... params) {
        Context.call(amount, address, method, params);
    }

    /*
    methods used for tests only to be removed in production
     */

    @External(readonly = true)
    public BigInteger getEligibleStakedTapToken(){
        return _total_eligible_staked_tap_tokens.get();
    }

    @External(readonly = true)
    public BigInteger batchSize(){
        return this._batch_size.getOrDefault(ZERO);
    }

    @External(readonly = true)
    public BigInteger dividendsReceived(){
        return this._dividends_received.get();
    }

    @External
    public void set_divs_dist_complete(boolean status){
        Context.require(Context.getCaller().equals(Context.getOwner()), TAG + "Only owner can call this method.");
        this._divs_dist_complete.set(status);
    }

}

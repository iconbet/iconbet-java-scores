package com.iconbet.score.daolette;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import score.Address;
import score.ArrayDB;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;
import score.annotation.Keep;

public class Daolette {
    protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    private static final String TAG = "ICONbet Treasury";

    // Treasury minimum 2.5E+23, or 250,000 ICX.
    private static final BigInteger TREASURY_MINIMUM = new BigInteger("250000000000000000000000");

    private static final BigInteger U_SECONDS_DAY = BigInteger.valueOf(86400000000L); // Microseconds in a day.
    private static final BigInteger TX_MIN_BATCH_SIZE = BigInteger.valueOf(10);
    private static final BigInteger TX_MAX_BATCH_SIZE = BigInteger.valueOf(500);
    private static final BigInteger DIST_DURATION_PARAM = BigInteger.valueOf(50);  // Units of 1/Days
    private static final String _EXCESS = "house_excess";
    private static final String _EXCESS_TO_DISTRIBUTE = "excess_to_distribute";
    private static final String _TOTAL_DISTRIBUTED = "total_distributed";
    private static final String _TREASURY_MIN = "treasury_min";
    private static final String _DAY = "day";
    private static final String _SKIPPED_DAYS = "skipped_days";
    private static final String _DAILY_BET_COUNT = "daily_bet_count";
    private static final String _TOTAL_BET_COUNT = "total_bet_count";
    private static final String _YESTERDAYS_BET_COUNT = "yesterdays_bet_count";
    private static final String _TOKEN_SCORE = "token_score";
    private static final String _REWARDS_SCORE = "rewards_score";
    private static final String _DIVIDENDS_SCORE = "dividends_score";

    private static final String _VOTE = "vote";
    private static final String _VOTED = "voted";
    private static final String _YES_VOTES = "yes_votes";
    private static final String _NO_VOTES = "no_votes";
    private static final String _OPEN_TREASURY = "open_treasury";
    private static final String _GAME_AUTH_SCORE = "game_auth_score";

    private static final String _NEW_DIV_LIVE = "new_div_live";
    private static final String _TREASURY_BALANCE = "treasury_balance";

    private static final String _EXCESS_SMOOTHING_LIVE = "excess_smoothing_live";

    private static final String _DAOFUND_SCORE = "daofund_score";
    private static final String _YESTERDAYS_EXCESS = "yesterdays_excess";
    private static final String _DAOFUND_TO_DISTRIBUTE = "daofund_to_distribute";
    private static final String IBPNP_SCORE = "ibpnp_score";

    public static class GameData {
        @Keep
        public BigInteger game_amount_wagered;
        @Keep
        public BigInteger game_amount_won;
        @Keep
        public BigInteger game_amount_lost;
        @Keep
        public int game_bets_won;
        @Keep
        public int game_bets_lost;
        @Keep
        public BigInteger game_largest_bet;
        @Keep
        public BigInteger game_wager_level;
        @Keep
        public Address wallet_address;
        @Keep
        public String remarks;
        @Keep
        public BigInteger lastAmountWagered;
    }

    private final VarDB<BigInteger> _total_distributed = Context.newVarDB(_TOTAL_DISTRIBUTED, BigInteger.class);
    private final VarDB<BigInteger> _treasury_min = Context.newVarDB(_TREASURY_MIN, BigInteger.class);
    private final VarDB<BigInteger> _day = Context.newVarDB(_DAY, BigInteger.class);
    private final VarDB<BigInteger> _skipped_days = Context.newVarDB(_SKIPPED_DAYS, BigInteger.class);
    private final VarDB<BigInteger> _total_bet_count = Context.newVarDB(_TOTAL_BET_COUNT, BigInteger.class);
    private final VarDB<BigInteger> _daily_bet_count = Context.newVarDB(_DAILY_BET_COUNT, BigInteger.class);
    private final VarDB<BigInteger> _yesterdays_bet_count = Context.newVarDB(_YESTERDAYS_BET_COUNT, BigInteger.class);
    private final VarDB<Address> _token_score = Context.newVarDB(_TOKEN_SCORE, Address.class);
    private final VarDB<Address> _rewards_score = Context.newVarDB(_REWARDS_SCORE, Address.class);
    private final VarDB<Address> _dividends_score = Context.newVarDB(_DIVIDENDS_SCORE, Address.class);

    private final DictDB<String, String> _vote = Context.newDictDB(_VOTE, String.class);
    private final ArrayDB<Address> _voted = Context.newArrayDB(_VOTED, Address.class);
    private final VarDB<BigInteger> _yes_votes = Context.newVarDB(_YES_VOTES, BigInteger.class);
    private final VarDB<BigInteger> _no_votes = Context.newVarDB(_NO_VOTES, BigInteger.class);
    private final VarDB<Boolean> _open_treasury = Context.newVarDB(_OPEN_TREASURY, Boolean.class);
    private final VarDB<Address> _game_auth_score = Context.newVarDB(_GAME_AUTH_SCORE, Address.class);
    private final VarDB<BigInteger> _excess_to_distribute = Context.newVarDB(_EXCESS_TO_DISTRIBUTE, BigInteger.class);
    private final VarDB<BigInteger> _treasury_balance = Context.newVarDB(_TREASURY_BALANCE, BigInteger.class);
    private final VarDB<Boolean> _excess_smoothing_live = Context.newVarDB(_EXCESS_SMOOTHING_LIVE, Boolean.class);
    private final VarDB<Address> _daofund_score = Context.newVarDB(_DAOFUND_SCORE, Address.class);
    private final VarDB<BigInteger> _yesterdays_excess = Context.newVarDB(_YESTERDAYS_EXCESS, BigInteger.class);
    private final VarDB<BigInteger> _daofund_to_distirbute = Context.newVarDB(_DAOFUND_TO_DISTRIBUTE, BigInteger.class);
    private final VarDB<Address> ibpnpScore = Context.newVarDB(IBPNP_SCORE, Address.class);

    public Daolette() {
        if (_treasury_min.get() == null) {
            this._total_distributed.set(BigInteger.ZERO);
            this._treasury_min.set(TREASURY_MINIMUM);
            this._day.set(BigInteger.valueOf(Context.getTransactionTimestamp()).divide(U_SECONDS_DAY));
            this._skipped_days.set(BigInteger.ZERO);
            this._total_bet_count.set(BigInteger.ZERO);
            this._daily_bet_count.set(BigInteger.ZERO);
            this._yesterdays_bet_count.set(BigInteger.ZERO);
            this._yes_votes.set(BigInteger.ZERO);
            this._no_votes.set(BigInteger.ZERO);
            this._open_treasury.set(false);
            this._game_auth_score.set(ZERO_ADDRESS);
            this._excess_smoothing_live.set(false);
        }
    }

    private void validateOwner() {
        Context.require(Context.getCaller().equals(Context.getOwner()), TAG + ": Only owner can call this method.");
    }

    @External
    public void toggleExcessSmoothing() {
		/*
        Toggles the status of excess smoothing between true and false. If its true, it keeps the 10% of excess to be
        distributed to tap holders and wager war in the treasury itself making a positive start for next day. If false,
        the feature is disabled
        :return:
		 */
        validateOwner();
        this._excess_smoothing_live.set(!this._excess_smoothing_live.getOrDefault(Boolean.FALSE));
    }

    @External(readonly = true)
    public boolean get_excess_smoothing_status() {
		/*
        Status of excess smoothing.
        :return: Returns the boolean value representing the status of excess smoothing
		 */
        return this._excess_smoothing_live.get();
    }

    @External
    public void setTokenScore(Address score) {
		/*
        Sets the token score address. Only owner can set the address.
        :param score: Address of the token score
        :type score: :class:`iconservice.base.address.Address`
        :return:
		 */
        validateOwner();
        this._token_score.set(score);

    }

    @External(readonly = true)
    public Address get_token_score() {
		/*
        Returns the token score address
        :return: TAP token score address
        :rtype: :class:`iconservice.base.address.Address`
		 */
        return this._token_score.getOrDefault(null);
    }

    @External
    public void setIbpnpScore(Address score) {
		/*
        Sets the IBPNP score address. Only owner can set the address.
        :param score: Address of the IBPNP score
        :type score: :class:`iconservice.base.address.Address`
        :return:
		 */
        validateOwner();
        this.ibpnpScore.set(score);
    }

    @External(readonly = true)
    public Address get_ibpnp_score() {
		/*
        Returns the IBPNP score address
        :return: IBPNP token score address
        :rtype: :class:`iconservice.base.address.Address`
		 */
        return this.ibpnpScore.getOrDefault(null);
    }

    @External
    public void setRewardsScore(Address score) {
		/*
        Sets the rewards score address. Only owner can set the address.
        :param score: Address of the rewards score
        :type score: :class:`iconservice.base.address.Address`
        :return:
		 */
        validateOwner();
        this._rewards_score.set(score);
    }

    @External(readonly = true)
    public Address get_rewards_score() {
		/*
        Returns the rewards score address
        :return: Rewards score address
        :rtype: :class:`iconservice.base.address.Address`
		 */
        return this._rewards_score.getOrDefault(null);
    }

    @External
    public void setDividendsScore(Address score) {
		/*
        Sets the dividends score address. Only owner can set the address.
        :param score: Address of the dividends score address
        :type score: :class:`iconservice.base.address.Address`
        :return:
		 */
        validateOwner();
        this._dividends_score.set(score);

    }

    @External(readonly = true)
    public Address get_dividends_score() {
		/*
        Returns the dividends score address
        :return: Dividends score address
        :rtype: :class:`iconservice.base.address.Address`
		 */
        return this._dividends_score.getOrDefault(null);
    }

    @External
    public void setGameAuthScore(Address score) {
		/*
        Sets the game authorization score address. Only owner can set this address
        :param score: Address of the game authorization score
        :type score: :class:`iconservice.base.address.Address`
        :return:
		 */
        validateOwner();
        this._game_auth_score.set(score);

    }

    @External(readonly = true)
    public Address get_game_auth_score() {
		/*
        Returns the game authorization score address
        :return: Game authorization score address
        :rtype: :class:`iconservice.base.address.Address`
		 */
        return this._game_auth_score.getOrDefault(null);
    }

    @External(readonly = true)
    public Address get_daofund_score() {
        return this._daofund_score.getOrDefault(ZERO_ADDRESS);
    }

    @External
    public void setDaofundScore(Address score) {
        validateOwner();
        if (!score.isContract()) {
            Context.revert("TREASURY: Only contract address is accepted for DAOfund");
        }
        this._daofund_score.set(score);
    }

    @External(readonly = true)
    public boolean get_treasury_status() {
		/*
        Returns the status of treasury. If the treasury is to be dissolved it returns True
        :return: True if treasury is to be dissolved
        :rtype: bool
		 */
        return this._open_treasury.getOrDefault(false);
    }

    @External
    @Payable
    public void set_treasury() {
		/*
        Anyone can add amount to the treasury and increase the treasury minimum
        Receives the amount and updates the treasury minimum value.
        Can increase treasury minimum with multiples of 10,000 ICX
        :return:
		 */
        BigInteger value = Context.getValue();
        if (value.compareTo(pow(BigInteger.TEN, 22)) < 0) {
            Context.revert(TAG + " : set_treasury method doesnt accept ICX less than 10000 ICX");
        }
        if (value.mod(pow(BigInteger.TEN, 22)).compareTo(BigInteger.ZERO) != 0) {
            Context.revert(TAG + " : Set treasury error, Please send amount in multiples of 10,000 ICX");
        }

        BigInteger treasuryMinimum = this._treasury_min.get();
        this._treasury_min.set(treasuryMinimum.add(value));
        Context.println("Increasing treasury minimum by " + value + " to " + treasuryMinimum);
        this._open_treasury.set(false);
        this.FundReceived(Context.getCaller(), value, "Treasury minimum increased by " + value);
        Context.println(value + " was added to the treasury from address " + Context.getCaller() + " " + TAG);
    }

    @External(readonly = true)
    public BigInteger getTreasuryBalance() {
        return Context.getBalance(Context.getAddress());
    }

    /*
    Returns the reward pool of the ICONbet platform
    :return: Reward pool of the ICONbet platform
    :rtype: int
     */
    @External(readonly = true)
    public BigInteger get_excess() {
        //TODO: this could be negative looks like, is it ok?
        BigInteger excessToMinTreasury = this._treasury_balance.getOrDefault(BigInteger.ZERO).subtract(this._treasury_min.get());

        Address authScore = this._game_auth_score.get();
        if (!this._excess_smoothing_live.get()) {
            return excessToMinTreasury.subtract(Context.call(BigInteger.class, authScore, "get_excess"));
        } else {
            BigInteger thirdPartyGamesExcess = BigInteger.ZERO;
            @SuppressWarnings("unchecked")
            Map<String, String> gamesExcess = (Map<String, String>) Context.call(authScore, "get_todays_games_excess");

            for (Map.Entry<String, String> gameExcess : gamesExcess.entrySet()) {
                thirdPartyGamesExcess = thirdPartyGamesExcess.add(
                        BigInteger.ZERO.max(new BigInteger(gameExcess.getValue()))
                );
            }
            return excessToMinTreasury.subtract(thirdPartyGamesExcess.multiply(BigInteger.valueOf(20))).divide(BigInteger.valueOf(100));
        }
    }

    /*
    Returns the total distributed amount from the platform
    :return: Total distributed excess amount
    :rtype: int
     */
    @External(readonly = true)
    public BigInteger get_total_distributed() {
        return this._total_distributed.getOrDefault(BigInteger.ZERO);
    }

    /*
    Returns the total bets made till date
    :return: Total bets made till date
    :rtype: int
     */
    @External(readonly = true)
    public BigInteger get_total_bets() {
        return this._total_bet_count.getOrDefault(BigInteger.ZERO).add(this._daily_bet_count.getOrDefault(BigInteger.ZERO));
    }

    /*
    Returns the total bets of current day
    :return: Total bets of current day
    :rtype: int
     */
    @External(readonly = true)
    public BigInteger get_todays_bet_total() {
        return this._daily_bet_count.getOrDefault(BigInteger.ZERO);
    }

    /*
    Returns the treasury minimum value
    :return: Treasury minimum value
    :rtype: int
     */
    @External(readonly = true)
    public BigInteger get_treasury_min() {
        return this._treasury_min.getOrDefault(BigInteger.ZERO);
    }

    /*
    Returns the vote results of dissolving the treasury.
    :return: Vote result for treasury to be dissolved e.g. [0,0]
    :rtype: str
     */
    @External(readonly = true)
    public String get_vote_results() {
        return "[" + this._yes_votes.get() + "," + this._no_votes.get() + "]";
    }

    /*
    A function to return the owner of this score.
    :return: Owner address of this score
    :rtype: :class:`iconservice.base.address.Address`
     */
    @External(readonly = true)
    public Address get_score_owner() {
        return Context.getOwner();
    }

    /*
    Returns the number of skipped days. Days are skipped if the distribution is not completed in any previous day.
    :return: Number of skipped days
    :rtype: int
     */
    @External(readonly = true)
    public BigInteger get_skipped_days() {
        return this._skipped_days.get();
    }

    @External(readonly = true)
    public BigInteger get_yesterdays_excess() {
        return this._yesterdays_excess.getOrDefault(BigInteger.ZERO);
    }

    @External
    @Payable
    public void send_wager(BigInteger _amount) {
        if (Context.getValue().compareTo(_amount) != 0) {
            Context.revert("ICX sent and the amount in the parameters are not same");
        }
        this._take_wager(Context.getCaller(), _amount);
    }

    @External
    @Payable
    public void send_rake(BigInteger _wager, BigInteger _payout) {
        if (Context.getValue().compareTo(_wager.subtract(_payout)) != 0) {
            Context.revert("ICX sent and the amount in the parameters are not same");
        }
        this.take_rake(_wager, _payout);
    }

    /*
    Takes wager amount from approved games. The wager amounts are recorded in game authorization score. Checks if
    the day has been advanced. If the day has advanced the excess amount is transferred to distribution contract.
    :param _amount: Wager amount to be recorded for excess calculation
    :return:
     */
    @External
    public void take_wager(BigInteger _amount) {
        this._take_wager(Context.getCaller(), _amount);
    }

    /*
    Takes wager amount from approved games.
    :param _game_address: Address of the game
    :type _game_address: :class:`iconservice.base.address.Address`
    :param _amount: Wager amount
    :type _amount: int
    :return:
     */
    public void _take_wager(Address _game_address, BigInteger _amount) {
        if (_amount.compareTo(BigInteger.ZERO) <= 0) {
            Context.revert("Invalid bet amount " + _amount);
        }
        Address authScore = this._game_auth_score.get();
        Context.println("Game address: " + _game_address + " Auth score: " + authScore);
        String gameStatus = callScore(String.class, authScore, "get_game_status", _game_address);
        Context.println("gameStatus: " + gameStatus);
        if (!gameStatus.equals("gameApproved")) {
            Context.revert("Bet only accepted through approved games.");
        }
        Address userSender = Context.getOrigin();
        boolean hasIBPNPProfile = callScore(Boolean.class, ibpnpScore.get(), "hasIBPNPProfile", userSender);
        Context.require(hasIBPNPProfile, TAG + ": The sender " + userSender + " doesnot have an IBPNP profile.");

        if (this.dayAdvanced()) {
            Context.println("Day advanced is true");
            this.__check_for_dividends();
        }

        this._daily_bet_count.set(this._daily_bet_count.getOrDefault(BigInteger.ZERO).add(BigInteger.ONE));
        callScore(authScore, "accumulate_daily_wagers", _game_address, _amount);
        Context.println("Sending wager data to rewards score." + TAG);

        BigInteger days = this._day.get().subtract(this._skipped_days.getOrDefault(BigInteger.ZERO)).mod(BigInteger.TWO);
        callScore(this._rewards_score.get(), "accumulate_wagers", userSender.toString(), _amount, days);

        this._treasury_balance.set(Context.getBalance(Context.getAddress()));
        GameData gameData = new GameData();
        BigInteger value = Context.getValue();
        gameData.game_amount_lost = value;
        gameData.game_amount_wagered = value;
        gameData.game_amount_won = BigInteger.ZERO;
        gameData.game_bets_lost = 1;
        gameData.game_bets_won = 0;
        gameData.game_wager_level = BigInteger.ZERO;
        gameData.game_largest_bet = value;
        gameData.remarks = "take_wager";
        gameData.wallet_address = userSender;
        gameData.lastAmountWagered = value;
        addGameDataToIBPNP(gameData);
    }

    private void addGameDataToIBPNP(GameData gameData) {
        callScore(ibpnpScore.get(), "addGameData", gameData);
    }

    /*
    Takes wager amount and payout amount data from games which have their own treasury.
    :param _wager: Wager you want to record in GAS
    :param _payout: Payout you want to record
    :return:
     */
    @External
    public void take_rake(BigInteger _wager, BigInteger _payout) {

        if (_payout.compareTo(BigInteger.ZERO) <= 0) {
            Context.revert("Payout can't be zero");
        }
        Address caller = Context.getCaller();
        this._take_wager(caller, _wager);

        // dry run of wager_payout i.e. make payout without sending ICX
        Address authScore = this._game_auth_score.get();
        String gameStatus = callScore(String.class, authScore, "get_game_status", caller);

        if (!gameStatus.equals("gameApproved")) {
            Context.revert("Payouts can only be invoked by approved games.");
        }
        Context.println("Reach here???");
        Context.call(authScore, "accumulate_daily_payouts", caller, _payout);

        this._treasury_balance.set(Context.getBalance(Context.getAddress()));
    }

    /*
    Makes payout to the player of the approved games. Only the approved games can request payout.
    :param _payout: Payout to be made to the player
    :return:
     */
    @External
    public void wager_payout(BigInteger _payout) {
        this._wager_payout(Context.getCaller(), _payout);
    }

    /*
    Makes payout to the player of the approved games.
    :param _game_address: Address of the game requesting payout
    :type _game_address: :class:`iconservice.base.address.Address`
    :param _payout: Payout to be made to the player
    :type _payout: int
    :return:
     */
    public void _wager_payout(Address _game_address, BigInteger _payout) {

        if (_payout.compareTo(BigInteger.ZERO) <= 0) {
            Context.revert("Invalid payout amount requested " + _payout);
        }

        String gameStatus = callScore(String.class, this._game_auth_score.get(), "get_game_status", _game_address);

        if (!gameStatus.equals("gameApproved")) {
            Context.revert("Payouts can only be invoked by approved games.");
        }

        boolean accumulated = callScore(Boolean.class, this._game_auth_score.get(), "accumulate_daily_payouts", _game_address, _payout);

        if (accumulated) {
            Address caller = Context.getOrigin();
            Context.println("Trying to send to (" + caller + "): " + _payout + " . " + TAG);
            boolean hasIBPNPProfile = callScore(Boolean.class, ibpnpScore.get(), "hasIBPNPProfile", caller);
            Context.require(hasIBPNPProfile, TAG + ": The sender " + caller + " does not have an IBPNP profile.");
            Context.transfer(caller, _payout);
            this.FundTransfer(caller, _payout, "Player Winnings from " + _game_address + ".");
            Context.println("Sent winner (" + caller + ") " + _payout + "." + TAG);

            GameData gameData = new GameData();
            BigInteger value = Context.getValue();
            Context.println("wagered value: " + value);
            gameData.game_amount_lost = BigInteger.ZERO;
            gameData.game_amount_wagered = value;
            gameData.game_amount_won = _payout;
            gameData.game_bets_lost = 0;
            gameData.game_bets_won = 1;
            gameData.game_wager_level = BigInteger.ZERO;
            gameData.game_largest_bet = BigInteger.ZERO;
            gameData.remarks = "wager_payout";
            gameData.wallet_address = caller;
            gameData.lastAmountWagered = BigInteger.ZERO;
            addGameDataToIBPNP(gameData);

            this._treasury_balance.set(Context.getBalance(Context.getAddress()));
        }
    }

    /*
    Vote takes the votes from TAP holders to dissolve the treasury.
    :param option: Option to select for dissolving the treasury ("yes" | "no")
    :type option: str
    :return:
     */
    @External
    public void vote(String option) {

        List<String> op = List.of("yes", "no");

        if (!op.contains(option)) {
            Context.revert("Option must be one of either \"yes\" or \"no\".");
        }

        Address address = Context.getCaller();
        BigInteger balanceOwner = Context.call(BigInteger.class, this._token_score.get(), "balanceOf", address);

        if (!containsInArrayDb(address, this._voted)
                && balanceOwner.equals(BigInteger.ZERO)) {
            Context.revert("You must either own or be a previous owner of TAP tokens in order to cast a vote.");
        }
        this._vote.set(address.toString(), option);
        if (!containsInArrayDb(address, this._voted)) {
            this._voted.add(address);
            String message = "Recorded vote of " + address.toString();
            this.Vote(Context.getCaller(), option, message);
        } else {
            String message = address.toString() + " updated vote to " + option;
            this.Vote(address, option, message);
        }
        if (!this.vote_result()) {
            String vote_msg = "Overall Vote remains a 'No'.";
            this.Vote(address, option, vote_msg);
        } else {
            // In case the votes is passed, treasury is dissolved by sending all the balance to distribution contract.
            // Distribution contract will then distribute 80% to tap holders and 20% to founders.
            this._open_treasury.set(true);
            this._excess_to_distribute.set(Context.getBalance(Context.getAddress()));
            this.__check_for_dividends();
            String vote_msg = "Vote passed! Treasury balance forwarded to distribution contract.";
            this.Vote(address, option, vote_msg);
            this._treasury_min.set(BigInteger.ZERO);
        }
    }

    /*
    Returns the vote result of vote on dissolving the treasury
    :return: True if majority of votes are yes
    :rtype: bool
     */
    public boolean vote_result() {

        BigInteger yes = BigInteger.ONE;
        BigInteger no = BigInteger.ZERO;
        Address tapToken = this._token_score.get();
        int size = this._voted.size();
        for (int i = 0; i < size; i++) {
            Address address = this._voted.get(i);
            String vote = this._vote.get(address.toString());
            BigInteger balance = Context.call(BigInteger.class, tapToken, "balanceOf", address);
            if (vote.equals("yes")) {
                yes = yes.add(balance);
            } else {
                no = no.add(balance);
            }
        }
        this._yes_votes.set(yes);
        this._no_votes.set(no);
        BigInteger totalSupply = Context.call(BigInteger.class, tapToken, "totalSupply");
        BigInteger rewardsBalance = Context.call(BigInteger.class, tapToken, "balanceOf", this._rewards_score.get());

        return this._yes_votes.get().compareTo(
                totalSupply.subtract(rewardsBalance).divide(BigInteger.TWO)
        ) > 0;
    }

    /*
    Returns the batch size to be used for distribution according to the number of recipients. Minimum batch size is
    10 and maximum is 500.
    :param recip_count: Number of recipients
    :type recip_count: int
    :return: Batch size
    :rtype: int
     */
    @External(readonly = true)
    public BigInteger get_batch_size(BigInteger recip_count) {
        Context.println("In get_batch_size." + TAG);
        BigInteger yesterdaysCount = this._yesterdays_bet_count.get();
        if (yesterdaysCount.compareTo(BigInteger.ONE) < 0) {
            yesterdaysCount = BigInteger.ONE;
        }
        BigInteger size = DIST_DURATION_PARAM.multiply(recip_count).divide(yesterdaysCount);
        if (size.compareTo(TX_MIN_BATCH_SIZE) < 0) {
            size = TX_MIN_BATCH_SIZE;
        }
        if (size.compareTo(TX_MAX_BATCH_SIZE) > 0) {
            size = TX_MAX_BATCH_SIZE;
        }
        Context.println("Returning batch size of " + size + " - " + TAG);
        return size;
    }

    /*
    Checks if day has been advanced nad the TAP distribution as well as dividends distribution has been completed.
    If the day has advanced and the distribution has completed then the current day is updated, excess is recorded
    from game authorization score, total bet count is updated and the daily bet count is reset.
    :return: True if day has advanced and distribution has been completed for previous day
    :rtype: bool
     */
    @SuppressWarnings("unchecked")
    private boolean dayAdvanced() {
        Context.println("In __day_advanced method." + TAG);
        BigInteger blockTimestamp = BigInteger.valueOf(Context.getBlockTimestamp());
        BigInteger currentDay = blockTimestamp.divide(U_SECONDS_DAY);
        BigInteger advance = currentDay.subtract(this._day.get());
        if (advance.compareTo(BigInteger.ONE) < 0) {
            return false;
        } else {
            Context.println("reached in else in dayAdvanced");
            Boolean rewardsComplete = callScore(Boolean.class, this._rewards_score.get(), "rewards_dist_complete");
            Boolean dividendsComplete = callScore(Boolean.class, this._dividends_score.get(), "dividends_dist_complete");
            BigInteger skippedDays = this._skipped_days.getOrDefault(BigInteger.ZERO);
            if (!rewardsComplete || !dividendsComplete) {
                String rew = "";
                String div = "";
                if (!rewardsComplete) {
                    rew = " Rewards dist is not complete";
                }
                if (!dividendsComplete) {
                    div = " Dividends dist is not complete";
                }
                this._day.set(currentDay);
                this._skipped_days.set(skippedDays.add(advance));
                this.DayAdvance(this._day.get(), this._skipped_days.getOrDefault(BigInteger.ZERO), blockTimestamp,
                        "Skipping a day since " + rew + " " + div);
                return false;
            }
            // Set excess to distribute
            BigInteger excessToMinTreasury = this._treasury_balance.getOrDefault(BigInteger.ZERO).subtract(this._treasury_min.get());

            Address authScore = this._game_auth_score.get();
            BigInteger developersExcess = callScore(BigInteger.class, authScore, "record_excess");
//            10
//            etd = 10 + max(0, -,-)
//            etd = 10
            this._excess_to_distribute.set(developersExcess.add(BigInteger.ZERO.max(excessToMinTreasury.subtract(developersExcess))));

            if (this._excess_smoothing_live.get()) {
                Context.println("excess smoothing live is true");
                BigInteger thirdPartyGamesExcess = BigInteger.ZERO;
                Map<String, String> gamesExcess = callScore(Map.class, authScore, "get_yesterdays_games_excess");
//                tpga = 100
                for (Map.Entry<String, String> game : gamesExcess.entrySet()) {
                    thirdPartyGamesExcess = thirdPartyGamesExcess.add(
                            BigInteger.ZERO.max(new BigInteger(game.getValue()))
                    );
                }
//                tpd = 20
//                rp = 0
//                df = 0
//                etd = 20
//                ye = -
//                dftd = 0
                BigInteger thirdPartyDeveloper = thirdPartyGamesExcess.multiply(BigInteger.valueOf(20)).divide(BigInteger.valueOf(100));
                BigInteger rewardPool = BigInteger.ZERO.max(
                        excessToMinTreasury.subtract(thirdPartyDeveloper).multiply(BigInteger.valueOf(90))).divide(BigInteger.valueOf(100));
                BigInteger daofund = BigInteger.ZERO.max(
                        excessToMinTreasury.subtract(thirdPartyDeveloper).multiply(BigInteger.valueOf(5))
                ).divide(BigInteger.valueOf(100));
                this._excess_to_distribute.set(thirdPartyDeveloper.add(rewardPool));
                this._yesterdays_excess.set(excessToMinTreasury.subtract(thirdPartyDeveloper));
                this._daofund_to_distirbute.set(daofund);
            }

            if (advance.compareTo(BigInteger.ONE) > 0) {
                this._skipped_days.set(skippedDays.add(advance).subtract(BigInteger.ONE));
            }

            this._day.set(currentDay);
            this._total_bet_count.set(this._total_bet_count.getOrDefault(BigInteger.ZERO).add(this._daily_bet_count.getOrDefault(BigInteger.ZERO)));
            this._yesterdays_bet_count.set(this._daily_bet_count.getOrDefault(BigInteger.ZERO));
            this._daily_bet_count.set(BigInteger.ZERO);
            this.DayAdvance(this._day.get(), skippedDays, blockTimestamp, "Day advanced. Counts reset.");
            return true;
        }
    }

    /*
    If there is excess in the treasury, transfers to the distribution contract.
    :return:
     */
    public void __check_for_dividends() {
        BigInteger excess = this._excess_to_distribute.getOrDefault(BigInteger.ZERO);
        BigInteger daofundExcess = this._daofund_to_distirbute.getOrDefault(BigInteger.ZERO);

        Context.println("Found treasury excess of " + excess + ". " + TAG);
        if (excess.compareTo(BigInteger.ZERO) > 0) {
            Address dividendScore = this._dividends_score.get();
            Context.println("Trying to send to (" + dividendScore + "): " + excess + ". " + TAG);
            Context.transfer(dividendScore, excess);
            this.FundTransfer(dividendScore, excess, "Excess made by games");
            Context.println("Sent div score (" + dividendScore + ") " + excess + ". " + TAG);
            this._total_distributed.set(this._total_distributed.getOrDefault(BigInteger.ZERO).add(excess));
            this._excess_to_distribute.set(BigInteger.ZERO);

        }

        if (daofundExcess.compareTo(BigInteger.ZERO) > 0) {
            this._daofund_to_distirbute.set(BigInteger.ZERO);
            Address daofundScore = this._daofund_score.get();
            Context.transfer(daofundScore, daofundExcess);
            this.FundTransfer(daofundScore, daofundExcess, "Excess transerred to daofund");
        }

    }

    /*
    Takes a list of numbers in the form of a comma separated string and the user seed
    :param numbers: The numbers which are selected for the bet
    :type numbers: str
    :param user_seed: User seed/ Lucky phrase provided by user which is used in random number calculation
    :type user_seed: str
    :return:
     */


    /*
    Users can add to excess, excess added by this method will be only shared to tap holders and wager wars
    :return:
     */
    @Payable
    @External
    public void add_to_excess() {
        BigInteger value = Context.getValue();
        if (value.compareTo(BigInteger.ZERO) <= 0) {
            Context.revert("No amount added to excess");
        }
        this._treasury_balance.set(Context.getBalance(Context.getAddress()));
        this.FundReceived(Context.getCaller(), value, value + " added to excess");
    }

    @Payable
    public void fallback() {
        String gameStatus = Context.call(String.class, this._game_auth_score.get(), "get_game_status", Context.getCaller());
        if (!gameStatus.equals("gameApproved")) {
            Context.revert(
                    "This score accepts plain ICX through approved games and through set_treasury, add_to_excess method.");
        }
    }

    @Payable
    @External
    public void transfer_to_dividends() {
        validateOwner();
        Context.transfer(this._dividends_score.get(), Context.getValue());
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

    private static BigInteger pow(BigInteger base, int exponent) {
        BigInteger result = BigInteger.ONE;
        for (int i = 0; i < exponent; i++) {
            result = result.multiply(base);
        }
        return result;
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

//    todo remove non production code
    @External
    public void advanceDayManually() {
        Context.require(Context.getCaller().equals(Context.getOwner()), TAG + "Only owner can call this method");
        this._day.set(this._day.getOrDefault(BigInteger.ZERO).subtract(BigInteger.ONE));
    }

    @External
    public void set_day_debug() {
        Context.require(Context.getCaller().equals(Context.getOwner()), TAG + "Only owner can call this method");
        BigInteger currentDay = BigInteger.valueOf(Context.getBlockTimestamp()).divide(U_SECONDS_DAY);
        this._day.set(currentDay);
    }

    @External(readonly = true)
    public BigInteger get_day() {
        return this._day.get();
    }

    @External(readonly = true)
    public BigInteger getCurrentDay() {
        return BigInteger.valueOf(Context.getBlockTimestamp()).divide(U_SECONDS_DAY);

    }

    // EventLogs
    @EventLog(indexed = 2)
    public void FundTransfer(Address recipient, BigInteger amount, String note) {
    }

    @EventLog(indexed = 2)
    public void FundReceived(Address sender, BigInteger amount, String note) {
    }

    @EventLog(indexed = 3)
    public void DayAdvance(BigInteger day, BigInteger skipped, BigInteger block_time, String note) {
    }

    @EventLog(indexed = 2)
    public void Vote(Address _from, String _vote, String note) {
    }
}

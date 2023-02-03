package com.iconbet.score.authorization;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import com.iconbet.score.authorization.utils.Consts;
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
import scorex.util.ArrayList;

import static com.iconbet.score.authorization.utils.Consts.*;

import com.iconbet.score.authorization.db.ProposalData;

import static com.iconbet.score.authorization.db.ProposalData.ProposalAttributes;
import static com.iconbet.score.authorization.db.ProposalData.*;

import scorex.util.HashMap;

import static com.iconbet.score.authorization.utils.ArrayDBUtils.*;
import static com.iconbet.score.authorization.db.VoteActions.*;

public class Authorization {
    private final DictDB<Address, String> proposal_data = Context.newDictDB(PROPOSAL_DATA, String.class);
    private final DictDB<Address, String> status_data = Context.newDictDB(STATUS_DATA, String.class);
    private final DictDB<Address, Address> owner_data = Context.newDictDB(OWNER_DATA, Address.class);
    private final ArrayDB<Address> proposal_list = Context.newArrayDB(PROPOSAL_LIST, Address.class);
    private final BranchDB<BigInteger, DictDB<Address, BigInteger>> wagers = Context.newBranchDB(WAGERS, BigInteger.class);
    private final BranchDB<BigInteger, DictDB<Address, BigInteger>> payouts = Context.newBranchDB(PAYOUTS, BigInteger.class);

    private final VarDB<BigInteger> game_developers_share = Context.newVarDB(GAME_DEVELOPERS_SHARE, BigInteger.class);
    private final DictDB<Address, BigInteger> todays_games_excess = Context.newDictDB(TODAYS_GAMES_EXCESS, BigInteger.class);

    private final VarDB<BigInteger> new_div_changing_time = Context.newVarDB(NEW_DIV_CHANGING_TIME, BigInteger.class);
    private final BranchDB<BigInteger, DictDB<Address, BigInteger>> games_excess_history = Context.newBranchDB(GAMES_EXCESS_HISTORY, BigInteger.class);

    private final VarDB<Boolean> applyWatchDogMethod = Context.newVarDB(APPLY_WATCH_DOG_METHOD, Boolean.class);
    private final DictDB<Address, BigInteger> maximum_payouts = Context.newDictDB(MAXIMUM_PAYOUTS, BigInteger.class);
    private final VarDB<BigInteger> maximum_loss = Context.newVarDB(MAXIMUM_LOSS, BigInteger.class);

    private final VarDB<Boolean> _governance_enabled = Context.newVarDB(GOVERNANCE_ENABLED, Boolean.class);

    private final ArrayDB<String> proposalKeys = Context.newArrayDB(PROPOSAL_KEYS, String.class);
    private final DictDB<String, Integer> proposalKeysIndex = Context.newDictDB(PROPOSAL_KEY_INDEX, Integer.class);

    private final VarDB<BigInteger> _official_review_cost = Context.newVarDB(OFFICIAL_REVIEW + "_cost", BigInteger.class);
    private final DictDB<String, Address> _official_review_sponsors = Context.newDictDB(OFFICIAL_REVIEW + "_sponsors", Address.class);
    private final DictDB<String, BigInteger> _official_review_costs = Context.newDictDB(OFFICIAL_REVIEW + "_costs", BigInteger.class);

    private final VarDB<BigInteger> tapVoteDefinitionCriterion = Context.newVarDB(VOTE_DEFINITION_CRITERION, BigInteger.class);
    private final VarDB<Integer> quorum = Context.newVarDB(QUORUM, Integer.class);
    private final VarDB<BigInteger> _time_offset = Context.newVarDB(TIME_OFFSET, BigInteger.class);
    private final VarDB<BigInteger> voteDuration = Context.newVarDB(VOTE_DURATION, BigInteger.class);
    private final VarDB<Integer> maxActions = Context.newVarDB(MAX_ACTIONS, Integer.class);
    private final ArrayDB<String> gameNames = Context.newArrayDB(GAME_NAMES, String.class);
    private final DictDB<String, String> gameAddresses = Context.newDictDB(GAME_ADDRESSES, String.class);

    public Authorization() {
    }

    @External(readonly = true)
    public Address getOwner() {
        return Context.getOwner();
    }

    @External
    public void setTreasuryScore(Address scoreAddress) {
        validateOwnerScore(scoreAddress);
        SettersGetters settersGetters = new SettersGetters();
        settersGetters.treasuryScore.set(scoreAddress);
    }

    /**
     * Returns the roulette score address
     * :return: Address of the roulette score
     * :rtype: :class:`iconservice.base.address.Address
     ***/
    @External(readonly = true)
    public Address get_roulette_score() {
        SettersGetters settersGetters = new SettersGetters();
        return settersGetters.treasuryScore.get();
    }

    /***
     Sets the address of tap token score
     :param scoreAddress: Address of tap_token
     :type scoreAddress: :class:`iconservice.base.address.Address`
     :return:
     ***/
    @External
    public void setTapTokenScore(Address scoreAddress) {
        validateOwnerScore(scoreAddress);
        SettersGetters settersGetters = new SettersGetters();
        settersGetters.tapTokenScore.set(scoreAddress);
    }

    /***
     Returns the tap token score address
     :return: Address of the tap_token score
     :rtype: :class:`iconservice.base.address.Address`
     ***/
    @External(readonly = true)
    public Address get_tap_token_score() {
        SettersGetters settersGetters = new SettersGetters();
        return settersGetters.tapTokenScore.get();
    }

    /***
     Sets the address of dividend distribution/game score
     :param scoreAddress: Address of dividend_distribution
     :type scoreAddress: :class:`iconservice.base.address.Address`
     :return:
     ***/
    @External
    public void setDividendDistributionScore(Address scoreAddress) {
        validateOwnerScore(scoreAddress);
        SettersGetters settersGetters = new SettersGetters();
        settersGetters.dividendDistributionScore.set(scoreAddress);
    }

    /***
     Returns the dividend distribution score address
     :return: Address of the tap_token score
     :rtype: :class:`iconservice.base.address.Address`
     ***/
    @External(readonly = true)
    public Address get_dividend_distribution() {
        SettersGetters settersGetters = new SettersGetters();
        return settersGetters.dividendDistributionScore.get();
    }

    /***
     Sets the address of rewards/game score
     :param scoreAddress: Address of rewards
     :type scoreAddress: :class:`iconservice.base.address.Address`
     :return:
     ***/
    @External
    public void setRewardsScore(Address scoreAddress) {
        validateOwnerScore(scoreAddress);
        SettersGetters settersGetters = new SettersGetters();
        settersGetters.rewardsScore.set(scoreAddress);
    }

    /***
     Returns the rewards score address
     :return: Address of the rewards score
     :rtype: :class:`iconservice.base.address.Address`
     ***/
    @External(readonly = true)
    public Address get_rewards_score() {
        SettersGetters settersGetters = new SettersGetters();
        return settersGetters.rewardsScore.get();
    }

    /***
     Sets the address of uTAP Token score
     :param _scoreAddress: Address of uTAP Token
     :type _scoreAddress: :class:`iconservice.base.address.Address`
     :return:
     ***/
    @External
    public void set_utap_token_score(Address _scoreAddress) {
        validateOwnerScore(_scoreAddress);
        SettersGetters settersGetters = new SettersGetters();
        settersGetters.uTapTokenScore.set(_scoreAddress);
    }

    /***
     Returns the uTAP Token score address
     :return: Address of the uTAP Token score
     :rtype: :class:`iconservice.base.address.Address`
     ***/
    @External(readonly = true)
    public Address get_utap_token_score() {
        SettersGetters settersGetters = new SettersGetters();
        return settersGetters.uTapTokenScore.get();
    }

    /***
     Sets super admin. Super admin is also added in admins list. Only allowed
     by the contract owner.
     :param _super_admin: Address of super admin
     :type _super_admin: :class:`iconservice.base.address.Address`
     ***/
    @External
    public void set_super_admin(Address _super_admin) {
        validateOwner();
        SettersGetters settersGetters = new SettersGetters();
        settersGetters.superAdmin.set(_super_admin);
        if (!containsInArrayDb(_super_admin, settersGetters.adminList)) {
            settersGetters.adminList.add(_super_admin);
        }
    }

    /**
     * Return the super admin address
     * :return: Super admin wallet address
     * :rtype: :class:`iconservice.base.address.Address
     **/
    @External(readonly = true)
    public Address get_super_admin() {
        if (DEBUG) {
            Context.println(Context.getOrigin().toString() + " is getting super admin address." + TAG);
        }
        SettersGetters settersGetters = new SettersGetters();
        return settersGetters.superAdmin.get();
    }

    /**
     * Sets admin. Only allowed by the super admin.
     * :param _admin: Wallet address of admin
     * :type _admin: :class:`iconservice.base.address.Address`
     * :return:
     ***/
    @External
    public void set_admin(Address _admin) {
        validateSuperAdmin();
        SettersGetters settersGetters = new SettersGetters();
        Context.require(!containsInArrayDb(_admin, settersGetters.adminList), TAG + ": Already in admin list");
        settersGetters.adminList.add(_admin);
    }

    /***
     Returns all the admin list
     :return: List of admins
     :rtype: list
     ***/
    @External(readonly = true)
    public List<Address> get_admin() {
        if (DEBUG) {
            Context.println(Context.getOrigin().toString() + " is getting admin addresses." + TAG);
        }
        SettersGetters settersGetters = new SettersGetters();
        int size = settersGetters.adminList.size();
        Address[] admin_list = new Address[size];
        for (int i = 0; i < size; i++) {
            admin_list[i] = settersGetters.adminList.get(i);
        }
        return List.of(admin_list);
    }

    /***
     Removes admin from the admin arrayDB. Only called by the super admin
     :param _admin: Address of admin to be removed
     :type _admin: :class:`iconservice.base.address.Address`
     :return:
     ***/
    @External
    public void remove_admin(Address _admin) {
        validateSuperAdmin();
        SettersGetters settersGetters = new SettersGetters();
        ArrayDB<Address> adminList = settersGetters.adminList;
        Context.require(containsInArrayDb(_admin, adminList), TAG + "Invalid Address: Not in list");
        removeArrayItem(adminList, _admin);
        if (DEBUG) {
            Context.println(_admin.toString() + " has been removed from admin list." + TAG);
        }
    }

    private void validateOwner() {
        SettersGetters settersGetters = new SettersGetters();
        settersGetters.validateOwner();
    }

    private void validateSuperAdmin() {
        SettersGetters settersGetters = new SettersGetters();
        settersGetters.validateSuperAdmin();
    }

    private void validateAdmin() {
        SettersGetters settersGetters = new SettersGetters();
        settersGetters.validateAdmin();
    }

    private void validateOwnerScore(Address score) {
        SettersGetters settersGetters = new SettersGetters();
        settersGetters.validateOwnerScore(score);
    }


    /**
     * Sets the equivalent time of 00:00 UTC of dividend structure changing
     * date in microseconds timestamp.
     * :param timestamp: Timestamp of 00:00 UTC of dividend structure changing
     * date in microseconds timestamp
     * :type timestamp: int
     * :return:
     **/
    @External
    public void setNewDivChangingTime(BigInteger timestamp) {
        validateOwner();
        this.new_div_changing_time.set(timestamp);
        List<Address> approved_games_list = get_approved_games();
        for (Address address : approved_games_list) {
            this.todays_games_excess.set(address, null);
        }
    }

    /***
     Returns the new dividend changing time in microseconds timestamp.
     :return: New dividend changing time in timestamp
     :rtype: int
     ***/
    @External(readonly = true)
    public BigInteger get_new_div_changing_time() {
        return new_div_changing_time.get();

    }

    /**
     * Sets the sum of game developers as well as platform share
     * :param share: Sum of game_devs as well as platform share
     * :type share: int
     * :return:
     */
    @External
    public void setGameDevelopersShare(BigInteger share) {
        validateOwner();
        this.game_developers_share.set(share);
    }

    /**
     * Returns the sum of game developers and platform share.
     * :return: Sum of game developers share as well as platform share
     * :rtype: int
     ***/
    @External(readonly = true)
    public BigInteger get_game_developers_share() {
        return this.game_developers_share.getOrDefault(ZERO);
    }

    @External(readonly = true)
    public boolean getGovernanceEnabled() {
        return this._governance_enabled.getOrDefault(Boolean.FALSE);
    }

    /***
     Takes the proposal from new games who want to register in the ICONbet
     platform. The games need to submit game with a fee of 50 ICX as well as
     the game data needs to be a JSON string in the following format:
     {
     "name": ""(Name of the game, str),
     "scoreAddress": "", (User must submit a score address, the game can
     be completed or else the score can contain the
     boilerplate score required for ICONbet platform,
     Address)
     "minBet": , (minBet must be greater than 100000000000000000(0.1 ICX), int)
     "maxBet": , (maxBet in the game in loop, int)
     "houseEdge": "", (house edge of the game in percentage, str)
     "gameType": "", (Type of game, type should be either "Per wager
     settlement" or "Game defined interval settlement", str)
     "revShareMetadata": "" ,(data about how would you share your revenue)
     "revShareWalletAddress": "", (Wallet address in which you want to
     receive your percentage of the excess
     made by game)
     "linkProofPage": "" , (link of the page showing the game statistics)
     "gameUrlMainnet": "", (IP of the game in mainnet)
     "gameUrlTestnet": "", (IP of the game in testnet)
     }
     :param gamedata: JSON object containing the data of game in above format
     :type gamedata: str
     :return:
     ***/
    @Payable
    @External
    public void submitGameProposal(String gamedata) {
        Address sender = Context.getCaller();
        BigInteger fee = MULTIPLIER.multiply(new BigInteger("50"));
        JsonValue json = Json.parse(gamedata);
        if (!json.isObject()) {
            throw new IllegalArgumentException("gamedata parameter is not a json object");
        }

        JsonObject jsonObject = json.asObject();
        checkGameMetadata(jsonObject);

        String name = jsonObject.get("name").asString();
        Address game_address = Address.fromString(jsonObject.get("scoreAddress").asString());

        if (this._governance_enabled.getOrDefault(Boolean.FALSE)) {
            verifyConceptVotingSuccess(name);
            verifyConceptVotingProposer(name, sender);
            this.gameAddresses.set(name, game_address.toString());
            this.gameNames.add(name);
        } else {
            if (fee.compareTo(Context.getValue()) != 0) {
                Context.revert("50 ICX is required for submitting game proposal");
            }
        }
        Address scoreOwner = callScore(Address.class, game_address, "get_score_owner");

        Context.require(sender.equals(scoreOwner), TAG + "Owner is not matched");

        ProposalSubmitted(sender, game_address);

        Context.require(!containsInArrayDb(game_address, this.proposal_list), TAG + "Already listed scoreAddress in the proposal list.");
        this.proposal_list.add(game_address);

        /// question =?   self._owner_data[Address.from_string(metadata['scoreAddress'])] = self.msg.sender
        this.owner_data.set(game_address, sender);


        this.status_data.set(game_address, "waiting");
        this.proposal_data.set(game_address, gamedata);

        if (this.applyWatchDogMethod.getOrDefault(Boolean.FALSE)) {
            BigInteger maxPayout = new BigInteger(jsonObject.get("maxPayout").asString());
            this.maximum_payouts.set(game_address, maxPayout);
        }

    }

    /***
     Admin can change the game status according to its previous status.
     :param status: Status of the game.
     :type status: str
     :param scoreAddress: Score address of the game for which status is to be changed
     :type scoreAddress: :class:`iconservice.base.address.Address`
     :return:
     ***/
    @External
    public void setGameStatus(String status, Address scoreAddress) {
        Address sender = Context.getCaller();
        SettersGetters settersGetters = new SettersGetters();
        Context.require(settersGetters.get_admin().contains(sender), "Sender not an admin");
        Context.require(STATUS_TYPE.contains(status), "Invalid status");

        String statusScoreAddress = this.status_data.get(scoreAddress);
        if (status.equals("gameRejected") && !statusScoreAddress.equals("gameReady")) {
            Context.revert("This game cannot be rejected from state " + statusScoreAddress);
        }
        if (status.equals("gameApproved") && !(statusScoreAddress.equals("gameReady") || statusScoreAddress.equals("gameSuspended"))) {
            Context.revert("This game cannot be approved from state " + statusScoreAddress);
        }
        if (status.equals("gameSuspended") && !statusScoreAddress.equals("gameApproved")) {
            Context.revert("Only approved games may be suspended.");
        }
        if (status.equals("gameDeleted") && !statusScoreAddress.equals("gameSuspended")) {
            Context.revert("Only suspended games may be deleted.");
        }

        this.status_data.set(scoreAddress, status);

    }

    /***
     When the game developer has completed the code for SCORE, can set the
     address of the game as ready.
     :param _scoreAddress: Address of the Game which is to be made ready
     :type _scoreAddress: :class:`iconservice.base.address.Address`
     :return:
     ***/
    @External
    public void set_game_ready(Address _scoreAddress) {
        Address sender = Context.getCaller();
        Address owner = this.owner_data.get(_scoreAddress);

        if (!sender.equals(owner)) {
            Context.revert("Sender not the owner of SCORE ");
        }
        this.status_data.set(_scoreAddress, "gameReady");
    }

    /***
     Sanity checks for the game metadata
     :param metadata: JSON metadata of the game
     :type metadata: dict
     :return:
     ***/
    private void checkGameMetadata(JsonObject metadata) {

        //All fields should be provided
        for (String name : METADATA_FIELDS) {
            if (!metadata.contains(name)) {
                Context.revert("There is no " + name + " for the game");
            }
        }

        Context.println("is apply_watch_dog_method ? " + this.applyWatchDogMethod.get());

        if (this.applyWatchDogMethod.get()) {
            String maxPayoutStr = metadata.getString("maxPayout", "");
            if (!maxPayoutStr.isEmpty()) {
                BigInteger maxPayout = new BigInteger(maxPayoutStr);
                if (maxPayout.compareTo(POINT_1_ICX) < 0) {
                    Context.revert("max payout: " + maxPayout + " is less than 0.1 ICX");
                }
            } else {
                Context.revert("There is no maxPayout for the game");
            }
        }

        String nameStr = metadata.getString("name", "");
        if (nameStr.isEmpty()) {
            Context.revert("Game name cant be empty");
        }

        // check if scoreAddress is a valid contract address
        String scoreAddressStr = metadata.getString("scoreAddress", "");
        if (!scoreAddressStr.isEmpty()) {
            Address scoreAddress = Address.fromString(scoreAddressStr);
            if (!scoreAddress.isContract()) {
                Context.revert(scoreAddress.toString() + " is not a valid contract address");
            }
        } else {
            Context.revert(TAG + "The scoreAddress field is empty.");
        }

        // Check if minbet is within defined limit of 0.1 ICX
        String minBetStr = metadata.getString("minBet", "");
        Context.require(!minBetStr.isEmpty(), TAG + ": The minbet field is empty");
        BigInteger minBet = new BigInteger(minBetStr);
        Context.require(minBet.compareTo(POINT_1_ICX) >= 0, minBet + " is less than 0.1 ICX");

        // Check if proper game type is provided
        String gameType = metadata.getString("gameType", "");
        Context.require(!gameType.isEmpty(), TAG + ": The gameType field is empty");
        Context.require(GAME_TYPE.contains(gameType), TAG + ": Not a valid game type");

        // Check for revenue share wallet address
        String revwallet = metadata.get("revShareWalletAddress").asString();
        Address revWalletAddress = Address.fromString(revwallet);
        Context.require(!revWalletAddress.isContract(), TAG + ": Not a wallet address");
    }

    /***
     Accumulates daily wagers of the game. Updates the excess of the game.
     Only roulette score can call this function.
     :param game: Address of the game
     :type game: :class:`iconservice.base.address.Address`
     :param wager: Wager amount of the game
     :type wager: int
     :return:
     ***/
    @External
    public void accumulate_daily_wagers(Address game, BigInteger wager) {
        Address sender = Context.getCaller();
        SettersGetters settersGetters = new SettersGetters();
        if (!sender.equals(settersGetters.treasuryScore.get())) {
            Context.revert("Only roulette score can invoke this method.");
        }
        BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());
        BigInteger day = now.divide(U_SECONDS_DAY);

        DictDB<Address, BigInteger> wagersAtDay = wagers.at(day);
        BigInteger wagerValue = wagersAtDay.getOrDefault(game, ZERO);
        wagersAtDay.set(game, wager.add(wagerValue));

        Context.println("acumulated wager at " + day + " for game " + game + " is " + wagersAtDay.get(game));
        BigInteger newTime = new_div_changing_time.get();

        if (newTime != null && now.compareTo(newTime) >= 1) {
            BigInteger excess = todays_games_excess.getOrDefault(game, ZERO);
            todays_games_excess.set(game, excess.add(wager));
        }

    }

    /***
     Get daily wagers for a game in a particular day
     :param day: Index of the day for which wagers is to be returned,
     index=timestamp//(seconds in a day)
     :type day: int
     :return: Wagers for all games on that particular day
     :rtype: dict
     ***/
    @SuppressWarnings("unchecked")
    @External(readonly = true)
    public Map<String, String> get_daily_wagers(@Optional BigInteger day) {

        if (day == null) {
            day = BigInteger.ZERO;
        }

        if (day.compareTo(BigInteger.ONE) < 0) {
            BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());
            day.add(now.divide(U_SECONDS_DAY));
        }

        List<Address> approvedGames = this.get_approved_games();
        int size = approvedGames.size();
        Map.Entry<String, String>[] wagers = new Map.Entry[size];

        for (int i = 0; i < size; i++) {
            Address game = approvedGames.get(i);
            wagers[i] = Map.entry(game.toString(), String.valueOf(this.wagers.at(day).get(game)));
        }
        return Map.ofEntries(wagers);
    }

    /***
     Accumulates daily payouts of the game. Updates the excess of the game.
     Only roulette score can call this function.
     :param game: Address of the game
     :type game: :class:`iconservice.base.address.Address`
     :param payout: Payout amount of the game
     :type payout: int
     :return:
     ***/
    @External
    public boolean accumulate_daily_payouts(Address game, BigInteger payout) {
        SettersGetters settersGetters = new SettersGetters();
        Address treasury = settersGetters.treasuryScore.get();
        if (!Context.getCaller().equals(treasury)) {
            Context.revert("Only treasury score can invoke this method.");
        }
        BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());
        BigInteger day = now.divide(U_SECONDS_DAY);

        BigInteger gamePayoutAtDay = this.payouts.at(day).getOrDefault(game, ZERO);
        if (this.applyWatchDogMethod.getOrDefault(false)) {
            try {
                Context.println("apply watch dog enabled . " + TAG);
                BigInteger maxPayout = this.maximum_payouts.getOrDefault(game, ZERO);
                if (payout.compareTo(maxPayout) > 0) {
                    Context.revert(TAG + ": Preventing Overpayment. Requested payout: " + payout + ". MaxPayout for this game: " + maxPayout);
                }

                BigInteger payOutDay = gamePayoutAtDay;
                payOutDay = payOutDay.add(payout);
                BigInteger wagerDay = this.wagers.at(day).getOrDefault(game, ZERO);
                BigInteger incurred = payOutDay.subtract(wagerDay);
                Context.println("incurred payout: " + incurred);
                BigInteger maxLoss = this.maximum_loss.getOrDefault(ZERO);
                if (incurred.compareTo(maxLoss) >= 1) {
                    Context.revert("Limit loss. MaxLoss: " + maxLoss + ". Loss Incurred if payout: " + incurred.intValue() + " " + TAG);
                }

            } catch (Exception e) {
                Context.println("error thrown:" + e.getMessage());
                this.status_data.set(game, "gameSuspended");
                this.GameSuspended(game, e.getMessage());
                return false;
            }
        }

        payout = payout.add(gamePayoutAtDay);
        this.payouts.at(day).set(game, payout);
        Context.println("new payout:" + payout + "at day " + day + " ." + TAG);

        BigInteger newDivChangingTime = this.new_div_changing_time.getOrDefault(ZERO);
        if (newDivChangingTime.compareTo(ZERO) != 0 && now.compareTo(newDivChangingTime) >= 0) {
            BigInteger accumulate = this.todays_games_excess.getOrDefault(game, ZERO);
            this.todays_games_excess.set(game, accumulate.subtract(payout));
        }
        return true;
    }

    /***
     Get daily payouts for a game in a particular day
     :param day: Index of the day for which payouts is to be returned
     :type day: int
     :return: Payouts of the game in that particular day
     :rtype: int
     ***/
    @SuppressWarnings("unchecked")
    @External(readonly = true)
    public Map<String, String> get_daily_payouts(@Optional BigInteger day) {

        if (day == null) {
            day = BigInteger.ZERO;
        }

        if (day.compareTo(BigInteger.ONE) < 0) {
            BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());
            day = day.add(now.divide(U_SECONDS_DAY));
        }

        int size = this.get_approved_games().size();
        Map.Entry<String, String>[] payouts = new Map.Entry[size];

        for (int i = 0; i < size; i++) {
            Address game = this.get_approved_games().get(i);
            payouts[i] = Map.entry(game.toString(), String.valueOf(this.payouts.at(day).getOrDefault(game, ZERO)));
        }
        return Map.ofEntries(payouts);
    }

    /***
     Returns the metadata fields which the games need to submit while
     submitting proposal.
     :return: List of metadata fields
     :rtype: list
     ***/
    @External(readonly = true)
    public List<String> get_metadata_fields() {
        return METADATA_FIELDS;
    }

    /***
     Returns the proposal data of the game address
     :param _scoreAddress: Game address for which proposal data is to be fetched
     :type _scoreAddress: :class:`iconservice.base.address.Address`
     :return: JSON object of the proposal data of the game
     :rtype: str
     ***/
    @External(readonly = true)
    public String get_proposal_data(Address _scoreAddress) {
        return this.proposal_data.get(_scoreAddress);
    }

    /***
     Returns all the games' Address regardless of their status.
     :return: List of games' Address
     :rtype: list
     ***/
    @External(readonly = true)
    public List<Address> get_score_list() {

        int size = this.proposal_list.size();
        Address[] proposal_list = new Address[size];

        for (int i = 0; i < size; i++) {
            proposal_list[i] = this.proposal_list.get(i);
        }
        return List.of(proposal_list);
    }

    /***
     Returns all the approved games' Address
     :return: List of approved games
     :rtype: list
     ***/
    @External(readonly = true)
    public List<Address> get_approved_games() {
        int size = this.proposal_list.size();
        List<Address> proposalList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Address address = this.proposal_list.get(i);
            String gameApproved = this.status_data.get(address);
            if (gameApproved != null && gameApproved.equals("gameApproved")) {
                proposalList.add(address);
            }
        }
        return proposalList;
    }

    /***
     Returns all the approved games' names
     :return: List of approved games
     :rtype: list
     ***/
    @External(readonly = true)
    public List<String> get_approved_game_names() {
        List<Address> gameAddress = get_approved_games();
        List<String> approvedGames = new ArrayList<>();
        for (Address scoreAddress : gameAddress) {
            JsonValue json = Json.parse(this.proposal_data.get(scoreAddress));
            JsonObject jsonObject = json.asObject();
            approvedGames.add(jsonObject.getString("name", ""));
        }
        return approvedGames;
    }

    /**
     * Returns the revshare wallet address of the game
     * :param _scoreAddress: Address of the game for which revenue share wallet
     * address is to be fetched
     * :type _scoreAddress: :class:`iconservice.base.address.Address`
     * :return: Revenue share wallet address of the game
     * :rtype: :class:`iconservice.base.address.Address`
     ***/
    @External(readonly = true)
    public Address get_revshare_wallet_address(Address _scoreAddress) {

        String gamedata = this.proposal_data.get(_scoreAddress);
        JsonValue json = Json.parse(gamedata);
        if (!json.isObject()) {
            throw new IllegalArgumentException("metadata is Not a json object");
        }

        String revShareWalletAddressStr = json.asObject().get("revShareWalletAddress").asString();

        return Address.fromString(revShareWalletAddressStr);
    }


    /***
     Returns the available types of games.
     :return: List of types of games that the game owner can choose from
     :rtype: list
     ***/
    @External(readonly = true)
    public List<String> get_game_type() {
        return GAME_TYPE;
    }

    /***
     Returns the status of the game.
     :param _scoreAddress: Address of the game
     :type _scoreAddress: :class:`iconservice.base.address.Address`
     :return: Status of game
     :rtype: str
     ***/
    @External(readonly = true)
    public String get_game_status(Address _scoreAddress) {

        return this.status_data.get(_scoreAddress);

    }

    /***
     Returns the excess share of game developers and founders
     :return: Game developers share
     :rtype: int
     ***/
    @External(readonly = true)
    public BigInteger get_excess() {

        BigInteger positive_excess = BigInteger.ZERO;
        BigInteger game_developers_amount;

        List<Address> approvedGames = this.get_approved_games();
        for (Address game : approvedGames) {
            BigInteger game_excess = this.todays_games_excess.getOrDefault(game, ZERO);
            if (game_excess != null && game_excess.compareTo(BigInteger.ZERO) >= 0) {
                positive_excess = positive_excess.add(game_excess);
            }
        }

        game_developers_amount = this.game_developers_share.getOrDefault(ZERO).multiply(positive_excess);
        Context.println("game_developers_share amount was calculates as " + game_developers_amount);
        if (game_developers_amount.equals(ZERO)) {
            return ZERO;
        }
        game_developers_amount = game_developers_amount.divide(BigInteger.valueOf(100L));

        return game_developers_amount;
    }

    /***
     Roulette score calls this function if the day has been advanced. This
     function takes the snapshot of the excess made by the game till the
     advancement of day.
     :return: Sum of game developers amount
     :rtype: int
     ***/
    @External
    public BigInteger record_excess() {
        Context.println("In record excess method." + TAG);
        Address sender = Context.getCaller();

        if (!sender.equals(get_roulette_score())) {
            Context.revert("This method can only be called by Roulette score");
        }
        BigInteger positive_excess = BigInteger.ZERO;
        BigInteger game_developers_amount;
        BigInteger day = BigInteger.ZERO;

        BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());
        day = day.add(now.divide(U_SECONDS_DAY));

        List<Address> approvedGames = this.get_approved_games();
        int size = approvedGames.size();

        Context.println("number of approved games:" + size);
        for (int i = 0; i < size; i++) {
            Address game = approvedGames.get(i);
            if (game == null) {
                Context.println("warn - no address defined for index " + i);
                continue;
            }
            BigInteger game_excess = this.todays_games_excess.getOrDefault(game, ZERO);
            this.games_excess_history.at(day.subtract(BigInteger.ONE)).set(game, game_excess);
            if (game_excess != null && game_excess.compareTo(BigInteger.ZERO) >= 0) {
                positive_excess = positive_excess.add(game_excess);
                this.todays_games_excess.set(game, BigInteger.ZERO);
            }
        }
        game_developers_amount = this.game_developers_share.getOrDefault(ZERO).multiply(positive_excess);
        Context.println("game_developers_share amount rate is " + game_developers_amount);
        if (game_developers_amount.equals(ZERO)) {
            return ZERO;
        }
        game_developers_amount = game_developers_amount.divide(BigInteger.valueOf(100L));
        Context.println("game_developers_share final amount was calculates as " + game_developers_amount);

        return game_developers_amount;
    }


    /***
     Returns the todays excess of the game. The excess is reset to 0 if it
     remains positive at the end of the day.
     :return: Returns the excess of games at current time
     ***/
    @SuppressWarnings("unchecked")
    @External(readonly = true)
    public Map<String, String> get_todays_games_excess() {
        List<Address> approvedGames = this.get_approved_games();
        int size = approvedGames.size();
        Map.Entry<String, String>[] gamesExcess = new Map.Entry[size];

        for (int i = 0; i < size; i++) {
            Address game = approvedGames.get(i);
            gamesExcess[i] = Map.entry(game.toString(), String.valueOf(this.todays_games_excess.getOrDefault(game, ZERO)));
        }

        return Map.ofEntries(gamesExcess);
    }

// reached upto here

    /***
     Returns a dictionary with game addresses as keys and the excess as the
     values for the specified day.
     :return: Dictionary of games' address and excess of the games
     :rtype: dict
     ***/
    @SuppressWarnings("unchecked")
    @External(readonly = true)
    public Map<String, String> get_games_excess(@Optional BigInteger day) {

        if (day == null) {
            day = BigInteger.ZERO;
        }

        if (day.compareTo(BigInteger.ZERO) == 0) {
            return this.get_todays_games_excess();
        }
        if (day.compareTo(BigInteger.ZERO) < 0) {
            BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());
            day = day.add(now.divide(U_SECONDS_DAY));
        }
        List<Address> approvedGames = get_approved_games();
        int size = approvedGames.size();
        Map.Entry<String, String>[] games_excess = new Map.Entry[size];
        DictDB<Address, BigInteger> gameHistoryAtDay = this.games_excess_history.at(day);

        for (int i = 0; i < size; i++) {
            Address game = approvedGames.get(i);
            games_excess[i] = Map.entry(game.toString(), String.valueOf(gameHistoryAtDay.getOrDefault(game, ZERO)));
        }

        return Map.ofEntries(games_excess);
    }

    /***
     Returns the dictionary containing keys as games address and value as
     excess of the game of yesterday
     :return: Dictionary of games' address and excess of the games
     :rtype: dict
     ***/
    @External(readonly = true)
    public Map<String, String> get_yesterdays_games_excess() {
        return this.get_games_excess(BigInteger.ONE.negate());
    }


    @Payable
    public void fallback() {
    }

    @External
    public void setMaximumLoss(BigInteger maxLoss) {
        Context.println("Setting maxLoss of " + maxLoss.toString());
        validateAdmin();
        if (maxLoss.compareTo(POINT_1_ICX) < 0) { // 0.1 ICX = 10^18 * 0.1
            Context.revert("maxLoss is set to a value less than 0.1 ICX");
        }
        this.maximum_loss.set(maxLoss);
    }

    @External(readonly = true)
    public BigInteger get_maximum_loss() {
        return maximum_loss.get();
    }

    @External
    public void setMaximumPayout(Address game, BigInteger maxPayout) {
        validateAdmin();
        if (maxPayout.compareTo(POINT_1_ICX) < 0) { // 0.1 ICX = 10^18 * 0.1
            Context.revert(maxPayout.toString() + "is less than 0.1 ICX");
        }
        if (!containsInArrayDb(game, this.proposal_list)) {
            Context.revert("Game has not been submitted.");
        }
        this.maximum_payouts.set(game, maxPayout);
    }


    @External(readonly = true)
    public BigInteger get_maximum_payout(Address game) {
        return this.maximum_payouts.getOrDefault(game, ZERO);
    }

    @External
    public void toggleApplyWatchDogMethod() {
        validateAdmin();
        boolean old_watch_dog_status = this.applyWatchDogMethod.getOrDefault(Boolean.FALSE);
        if (!old_watch_dog_status) {
            //# All approved games must have minimum_payouts set before applying watch dog methods.
            List<Address> approvedGames = get_approved_games();

            for (Address game : approvedGames) {
                BigInteger maximum_payouts = this.maximum_payouts.getOrDefault(game, ZERO);
                if (maximum_payouts.compareTo(POINT_1_ICX) < 0) {
                    Context.revert("maxPayout of " + game.toString() + " is less than 0.1 ICX");
                }
            }
            BigInteger maximum_loss = this.maximum_loss.getOrDefault(ZERO);
            if (maximum_loss.compareTo(POINT_1_ICX) < 0) {
                Context.revert("maxLoss is set to a value less than 0.1 ICX");
            }
        }
        this.applyWatchDogMethod.set(!old_watch_dog_status);
    }

    @External(readonly = true)
    public boolean getWatchDogStatus() {
        return this.applyWatchDogMethod.get();
    }

    //    governance methods on TAP
    public void setMinimumStake(BigInteger amount) {
        validateAdmin();
        callScore(get_tap_token_score(), "set_minimum_stake", amount);
    }

    public void setUnstakingPeriod(BigInteger time) {
        validateAdmin();
        callScore(get_tap_token_score(), "set_unstaking_period", time);
    }

    public void setMaxLoop(@Optional int loops) {
        validateAdmin();
        callScore(get_tap_token_score(), "set_max_loop", loops);
    }

    public void removeFromBlacklistTap(Address _address) {
        validateAdmin();
        Context.call(get_tap_token_score(), "remove_from_blacklist", _address);
    }

    public void setBlacklistAddressTap(Address _address) {
        this.validateAdmin();
        Context.call(get_tap_token_score(), "set_blacklist_address", _address);
    }

    public void remove_from_locklist(Address _address) {
        validateAdmin();
        Context.call(get_tap_token_score(), "remove_from_locklist", _address);
    }

    public void setLocklistAddress(Address _address) {
        validateAdmin();
        Context.call(get_tap_token_score(), "set_locklist_address", _address);
    }

    public void removeFromWhitelist(Address _address) {
        validateAdmin();
        Context.call(get_tap_token_score(), "remove_from_whitelist", _address);
    }

    public void setWhitelistAddress(Address _address) {
        validateAdmin();
        Context.call(get_tap_token_score(), "set_whitelist_address", _address);
    }

    //    Governance methods on Dividend Distribution
    public void setDividendPercentage(int _tap, int _gamedev, int _promo, int _platform) {
        validateAdmin();
        Context.call(get_dividend_distribution(), "set_dividend_percentage", _tap, _gamedev, _promo, _platform);
    }

    public void setNonTaxPeriod(BigInteger period) {
        validateAdmin();
        Context.call(get_dividend_distribution(), "setNonTaxPeriod", period);
    }

    public void setTaxPercentage(int percentage) {
        validateAdmin();
        Context.call(get_dividend_distribution(), "setTaxPercentage", percentage);
    }

    public void removeFromBlacklistDividend(Address _address) {
        validateAdmin();
        Context.call(get_dividend_distribution(), "remove_from_blacklist", _address);
    }

    public void setBlacklistAddressDividend(Address _address) {
        validateAdmin();
        Context.call(get_dividend_distribution(), "set_blacklist_address", _address);
    }

    public void setInhouseGames(Address _score) {
        validateAdmin();
        Context.call(get_dividend_distribution(), "set_inhouse_games", _score);
    }

    public void removeFromInhouseGames(Address _score) {
        validateAdmin();
        Context.call(get_dividend_distribution(), "remove_from_inhouse_games", _score);
    }

    public void addExceptionAddress(Address _address) {
        validateAdmin();
        Context.call(get_dividend_distribution(), "add_exception_address", _address);
    }

    public void removeExceptionAddress(Address _address) {
        validateAdmin();
        Context.call(get_dividend_distribution(), "remove_exception_address", _address);
    }

    /*------------------------------------------------------------------------------------------------------------------
                                        Proposals and voting
     -----------------------------------------------------------------------------------------------------------------*/

    /***
     Sets the percentage of the total eligible TAP which must participate in a vote
     for a vote to be valid.

     :param quorum: percentage of the total eligible TAP required for a vote to be valid
     ***/
    public void setQuorum(int quorum) {
        Context.require(0 < quorum && quorum < 100, TAG + "Quorum must be between 0 and 100.");
        {
            validateAdmin();
            this.quorum.set(quorum);
        }
    }

    /***
     Returns the percentage of the total eligible TAP which must participate in a vote
     for a vote to be valid.
     ***/
    @External(readonly = true)
    public int getQuorum() {
        return this.quorum.get();
    }

    /***
     Sets the vote duration.

     :param duration: number of days a vote will be active once started
     ***/
    public void setVoteDuration(BigInteger duration) {
        validateAdmin();
        this.voteDuration.set(duration);
    }

    /***
     Returns the vote duration in days.
     ***/
    @External(readonly = true)
    public BigInteger getVoteDuration() {
        return this.voteDuration.get();
    }

    @External(readonly = true)
    public BigInteger getDay() {
        return callScore(BigInteger.class, get_tap_token_score(), "getDay");
    }

    /***
     Sets the maximum no of actions that can be done if the vote is successful.
     :param max_actions: maximum no of actions
     ***/
    public void setMaxActions(int max_actions) {
        validateAdmin();
        Context.require(0 < max_actions && max_actions <= 10, TAG + "Max Actions must be between 0 and 10.");
        this.maxActions.set(max_actions);
    }

    /***
     Returns the percentage of the total eligible TAP which must participate in a vote
     for a vote to be valid.
     ***/
    @External(readonly = true)
    public int getMaxActions() {
        return this.maxActions.get();
    }

    public void addToGameAddresses() {
        List<Address> approvedGames = get_approved_games();
        for (Address gameAddress : approvedGames) {
            String gameDataJsonString = get_proposal_data(gameAddress);
            JsonValue json = Json.parse(gameDataJsonString);
            String gameName = json.asObject().get("name").asString();
            this.gameNames.add(gameName);
            this.gameAddresses.set(gameName, gameAddress.toString());
        }
    }

    /***
     Method to start governance

     :param voteDuration: how many days the voting lasts
     ***/
    @External
    public void startGovernance(BigInteger voteDuration, BigInteger tapVoteDefinitionCriterion, int maxActions, int quorum) {

        Address tapScore = get_tap_token_score();
        boolean snapshotEnabled = callScore(Boolean.class, tapScore, "getSnapshotEnabled");
        Context.require(snapshotEnabled, TAG + ": Snapshot must be enabled in TAP Token to start governance.");
        BigInteger time_offset = callScore(BigInteger.class, tapScore, "getTimeOffset");
        this._time_offset.set(time_offset);

        setTAPVoteDefinitionCriterion(tapVoteDefinitionCriterion);
        setMaxActions(maxActions);
        setQuorum(quorum);
        setVoteDuration(voteDuration);
        addToGameAddresses();
        this._governance_enabled.set(true);
    }

    /***
     Sets the minimum percentage of TAP's total supply which a user must have staked
     in order to define a vote.

     :param percentage: percent represented in basis points
     ***/
    public void setTAPVoteDefinitionCriterion(BigInteger stake) {
        validateAdmin();
        Context.require(stake.compareTo(ZERO) > 0, TAG + ": Stake should be greater than zero.");
        this.tapVoteDefinitionCriterion.set(stake);
    }

    /***
     Returns the minimum percentage of TAP's total supply which a user must have staked
     in order to define a vote. Percentage is returned as basis points.
     ***/
    @External(readonly = true)
    public BigInteger getTAPVoteDefinitionCriterion() {
        return this.tapVoteDefinitionCriterion.get();
    }

    /***
     Defines a new vote and which actions are to be executed if it is successful.

     :param name: name of the vote
     :param description: description of the vote
     :param vote_start: day to start the vote
     :param snapshot: which day to use for the TAP stake snapshot
     :param actions: json string on the form: {'<action_1>': {<kwargs for action_1>},
     '<action_2>': {<kwargs_for_action_2>},..}
     ***/

    private BigInteger getStakedBalanceOfUser() {
        return callScore(BigInteger.class, get_tap_token_score(), "staked_balanceOf", Context.getCaller());
    }

    private BigInteger getTotalStakedBalance() {
        return callScore(BigInteger.class, get_tap_token_score(), "total_staked_balance");
    }

    public void defineVote(String dbName, String name, String description, BigInteger vote_start, BigInteger snapshot, @Optional String actions, @Optional String ipfsHash) {
        if (actions == null) {
            actions = "{}";
        }

        if (ipfsHash == null) {
            ipfsHash = "";
        }

//        db = self.get_proposal_db(dbName)
        Context.require(containsInList(dbName, Consts.dbName), TAG + "Invalid DB name");
        Context.require(description.length() < 500, TAG + ": Description must be less than or equal to 500 characters.");
        BigInteger day = getDay();
        Context.require(vote_start.compareTo(day) > 0, TAG + "Vote cannot start at or before the current day.");

        Context.require(snapshot.compareTo(day) >= 0 && snapshot.compareTo(vote_start) < 0, TAG + ":The reference snapshot must be in the range: [current_day (" + day + " startDay - 1 (" + vote_start.subtract(ONE) + ")].");
        String proposalPrefix = proposalPrefix(dbName, name);

        Context.require(proposalKeysIndex.getOrDefault(proposalPrefix, 0) == 0, TAG + "Poll name " + name + "has already been used.");

//        #Test TAP staking criterion
        BigInteger userStaked = getStakedBalanceOfUser();
        BigInteger voteDefineCriteria = this.tapVoteDefinitionCriterion.getOrDefault(ZERO);
        Context.require(userStaked.compareTo(voteDefineCriteria) > 0, TAG + ": TAP staked by the sender does not meet the minimum tap vote definition criterion which is: " + voteDefineCriteria);

        if (!actions.equals("")) {
            JsonValue json = Json.parse(actions);
            JsonObject jsonObject = json.asObject();
            Integer maxActions = this.maxActions.getOrDefault(0);
            Context.require(jsonObject.size() <= maxActions, TAG + ": Only " + maxActions + " actions are allowed");
            VoteDefined(name, Context.getCaller());
        }
        ProposalAttributes proposalAttributes = new ProposalAttributes();
        proposalAttributes.name = name;
        proposalAttributes.description = description;
        proposalAttributes.proposerAddress = Context.getCaller();
        proposalAttributes.quorum = BigInteger.valueOf(this.quorum.get()).multiply(EXA).divide(BigInteger.valueOf(100));
        proposalAttributes.majority = MAJORITY;
        proposalAttributes.snapshot = snapshot;
        proposalAttributes.start = vote_start;
        proposalAttributes.end = vote_start.add(this.voteDuration.get());
        proposalAttributes.actions = actions;
        proposalAttributes.ipfsHash = ipfsHash;

        ProposalData proposalData = new ProposalData();
        proposalData.createProposal(proposalAttributes, proposalPrefix);
        this.proposalKeys.add(proposalPrefix);
        this.proposalKeysIndex.set(proposalPrefix, this.proposalKeys.size());
        proposalData.setProposalCount(dbName, proposalData.getProposalCount(dbName) + 1);
        proposalData.dbProposalKeys.get(dbName).add(name);
    }

    private String proposalPrefix(String dbName, String proposalName) {
        return dbName + "|" + proposalName;
    }

    @External(readonly = true)
    public BigInteger totalTap(BigInteger _day) {
        return callScore(BigInteger.class, get_tap_token_score(), "totalStakedBalanceOFAt", _day);
    }

    private Map<String, Object> checkVote(String dbName, String name) {
        BigInteger _for;
        BigInteger _against;
        String proposalPrefix = proposalPrefix(dbName, name);
        Context.require(proposalKeysIndex.getOrDefault(proposalPrefix, 0) > 0, TAG + "Proposal not found");
        Context.require(containsInList(dbName, Consts.dbName), TAG + "Invalid DB name.");
        ProposalData proposalData = new ProposalData();
        Map<String, Object> vote_status = new HashMap<>();
        BigInteger total_tap = ZERO;
        BigInteger day = getDay();
        BigInteger voteSnapshot = proposalData.getVote_snapshot(proposalPrefix);
        if (day.compareTo(voteSnapshot) >= 0) {
            total_tap = totalTap(voteSnapshot);
        }
        if (total_tap.equals(ZERO)) {
            _for = ZERO;
            _against = ZERO;
        } else {
            BigInteger[] totalVoted = new BigInteger[]{proposalData.getTotalForVotes(proposalPrefix), proposalData.getTotalAgainstVotes(proposalPrefix)};
            _for = EXA.multiply(totalVoted[0]).divide(total_tap);
            Context.println("for: " + _for);
            _against = EXA.multiply(totalVoted[1]).divide(total_tap);
        }

        vote_status.put("name", proposalData.getName(proposalPrefix));
        vote_status.put("ipfs_hash", proposalData.getIpfs_hash(proposalPrefix));
        vote_status.put("proposer", proposalData.getProposer(proposalPrefix));
        vote_status.put("description", proposalData.getDescription(proposalPrefix));
        vote_status.put("majority", proposalData.getMajority(proposalPrefix));
        vote_status.put("vote snapshot", voteSnapshot);
        vote_status.put("start day", proposalData.getStart_snapshot(proposalPrefix));
        vote_status.put("end day", proposalData.getEnd_snapshot(proposalPrefix));
        vote_status.put("actions", proposalData.getActions(proposalPrefix));
        vote_status.put("quorum", proposalData.getQuorum(proposalPrefix));
        vote_status.put("for", _for);
        vote_status.put("against", _against);
        vote_status.put("for_voter_count", proposalData.getForVotersCount(proposalPrefix));
        vote_status.put("against_voter_count", proposalData.getAgainstVotersCount(proposalPrefix));
        String status = proposalData.getStatus(proposalPrefix);
        BigInteger majority = (BigInteger) vote_status.get("majority");

        if (status.equals(ACTIVE) && day.compareTo((BigInteger) vote_status.get("end day")) >= 0) {
            if (_for.add(_against).compareTo((BigInteger) vote_status.get("quorum")) < 0) {
                vote_status.put("status", NO_QUORUM);
            } else if (EXA.subtract(majority).multiply(_for).compareTo(majority.multiply(_against)) > 0) {
                vote_status.put("status", SUCCEEDED);
            } else {
                vote_status.put("status", DEFEATED);
            }
        } else {
            vote_status.put("status", status);
        }
        return vote_status;
    }

    private void verifyConceptVotingSuccess(String name) {
        Map<String, Object> voteInfo = checkGameConceptVote(name);
        Context.require(voteInfo.get("status").equals(SUCCEEDED), TAG + ": Concept Voting must be " + SUCCEEDED + ". Concept voting status: " + voteInfo.get("status"));
    }

    private void verifyConceptVotingProposer(String name, Address sender) {
        Map<String, Object> voteInfo = checkGameConceptVote(name);
        Context.require(voteInfo.get("proposer").equals(sender), TAG + ": Different address from the proposer of concept voting.");
    }

    private void verifyDetailsSubmission(String name) {
        verifyConceptVotingSuccess(name);
        Context.require(containsInArrayDb(name, this.gameNames), TAG + ": Details for " + name + " not submitted.");
    }

    private void verifyProposalStatus(String name, String status) {
        String gameAddress = this.gameAddresses.getOrDefault(name, "");
        Context.require(!gameAddress.equals(""), TAG + ": Game address of name '" + name + "' not found");
        String game_status = this.status_data.getOrDefault(Address.fromString(gameAddress), "");
        Context.require(game_status.equals(status), TAG + ": Game status must be `" + status + "`. Present status `" + game_status + "`.");
    }

    private void verify_approval_voting_success(String name) {
        Map<String, Object> vote_info = checkGameApprovalVote(name);
        Context.require(vote_info.get("status").equals(SUCCEEDED), TAG + ": Approval Voting must be " + SUCCEEDED + ". Approval voting status: " + vote_info.get("status"));
    }

    /***
     Cancels a vote, in case a mistake was made in its definition.
     ***/
    @External
    public void cancelVote(String db_name, String name) {
        String proposalPrefix = proposalPrefix(db_name, name);
        ProposalData proposalData = new ProposalData();
        Context.require(this.proposalKeysIndex.getOrDefault(proposalPrefix, 0) > 0, TAG + "There is no proposal with the name: " + name);

        Address owner = Context.getOwner();
        Address[] eligible_addresses = new Address[]{proposalData.getProposer(proposalPrefix), owner};

        Address caller = Context.getCaller();
        Context.require(containsInArray(caller, eligible_addresses), TAG + ": Only owner or proposer may call this method.");

        if (proposalData.getStart_snapshot(proposalPrefix).compareTo(getDay()) <= 0) {
            validateAdmin();
        }

        Context.require(proposalData.getStatus(proposalPrefix).equals(ACTIVE), TAG + ": Proposal can be cancelled only from active status.");

        proposalData.setActive(proposalPrefix, false);
        proposalData.setStatus(proposalPrefix, CANCELLED);
    }

    private int getProposalCount(String dbName) {
        ProposalData proposalData = new ProposalData();
        Context.require(containsInList(dbName, Consts.dbName), TAG + ": Invalid DB name.");
        return proposalData.dbProposalKeys.get(dbName).size();
    }

    private List<Map<String, Object>> getProposals(String dbName, @Optional int offset) {
        Context.require(containsInList(dbName, Consts.dbName), TAG + ": Invalid DB name");
        ProposalData proposalData = new ProposalData();
        List<Map<String, Object>> proposal_list = new ArrayList<>();
        int count = this.getProposalCount(dbName);
        int start = 0;
        if (count > 1) {
            start = Math.max(0, offset);
        }

        int batchSize = 5;

        int end = Math.min(start + batchSize, this.getProposalCount(dbName));
        ArrayDB<String> proposalKeys = proposalData.dbProposalKeys.get(dbName);
        for (int i = start; i < end; i++) {
            String proposalName = proposalKeys.get(i);
            Map<String, Object> proposal = checkVote(dbName, proposalName);
            proposal_list.add(proposal);
        }
        proposal_list.add(Map.of("count", proposalKeys.size()));
        return proposal_list;
    }

    /***
     Casts a vote in the named poll.
     ***/
    private void castVote(String dbName, String name, boolean vote) {

        Context.require(containsInList(dbName, Consts.dbName), TAG + "Invalid DB name");
        String proposalPrefix = proposalPrefix(dbName, name);

        ProposalData proposalData = new ProposalData();
        BigInteger start_snap = proposalData.getStart_snapshot(proposalPrefix);
        BigInteger end_snap = proposalData.getEnd_snapshot(proposalPrefix);
        Context.println("index: " + this.proposalKeysIndex.getOrDefault(proposalPrefix, 0));
        boolean dayCheck = start_snap.compareTo(getDay()) > 0 || getDay().compareTo(end_snap) > 0;
        if (this.proposalKeysIndex.getOrDefault(proposalPrefix, 0) <= 0 || dayCheck || !proposalData.getActive(proposalPrefix)) {
            Context.revert(TAG + "That is not an active poll.");
        }
        Address sender = Context.getCaller();
        BigInteger snapshot = proposalData.getVote_snapshot(proposalPrefix);

        BigInteger total_vote = callScore(BigInteger.class, get_tap_token_score(), "stakedBalanceOfAt", sender, snapshot);

        Context.require(total_vote.compareTo(ZERO) > 0, TAG + ": TAP tokens need to be staked");
        BigInteger[] prior_vote = new BigInteger[]{proposalData.getForVotesOfUser(proposalPrefix, sender), proposalData.getAgainstVotesOfUser(proposalPrefix, sender)};
        BigInteger total_for_votes = proposalData.getTotalForVotes(proposalPrefix);
        BigInteger total_against_votes = proposalData.getTotalAgainstVotes(proposalPrefix);
        int total_for_voters_count = proposalData.getForVotersCount(proposalPrefix);
        int total_against_voters_count = proposalData.getAgainstVotersCount(proposalPrefix);
        BigInteger total_for;
        BigInteger total_against;
        boolean priorVoteCheck = prior_vote[0].equals(ZERO) && prior_vote[1].equals(ZERO);
        if (vote) {
            proposalData.setForVotesOfUser(proposalPrefix, total_vote, sender);
            proposalData.setAgainstVotesOfUser(proposalPrefix, ZERO, sender);
            total_for = total_for_votes.add(total_vote).subtract(prior_vote[0]);
            total_against = total_against_votes.subtract(prior_vote[1]);
            if (priorVoteCheck) {
                proposalData.setForVotersCount(proposalPrefix, total_for_voters_count + 1);
            } else {
                if (prior_vote[1].compareTo(ZERO) > 0) {
                    proposalData.setAgainstVotersCount(proposalPrefix, total_against_voters_count - 1);
                    proposalData.setForVotersCount(proposalPrefix, total_for_voters_count + 1);
                }
            }
        } else {
            proposalData.setForVotesOfUser(proposalPrefix, ZERO, sender);
            proposalData.setAgainstVotesOfUser(proposalPrefix, total_vote, sender);
            total_for = total_for_votes.subtract(prior_vote[0]);

            total_against = total_against_votes.add(total_vote).subtract(prior_vote[1]);
            if (priorVoteCheck) {
                proposalData.setAgainstVotersCount(proposalPrefix, total_against_voters_count + 1);
            } else {
                if (prior_vote[0].compareTo(ZERO) > 0) {
                    proposalData.setAgainstVotersCount(proposalPrefix, total_against_voters_count + 1);
                    proposalData.setForVotersCount(proposalPrefix, total_for_voters_count - 1);
                }
            }
        }
        proposalData.setTotalForVotes(proposalPrefix, total_for);
        proposalData.setTotalAgainstVotes(proposalPrefix, total_against);
        VoteCast(name, vote, sender, total_vote, total_for, total_against);
    }

    private void executeVoteActions(String voteActions) {
        JsonValue jsonValue = Json.parse(voteActions);
        List<String> actions = jsonValue.asObject().names();
        for (String action : actions) {
            JsonObject params = jsonValue.asObject().get(action).asObject().get("params").asObject();
            execute(this, action, params);
        }
    }

    /***
     Submit a new governance Proposal and which actions are to be executed if it is successful.

     :param name: name of the vote
     :param description: description of the vote
     :param vote_start: day to start the vote
     :param snapshot: which day to use for the TAP stake snapshot
     :param actions: json string on the form: {'<action_1>': {<kwargs for action_1>},
     '<action_2>': {<kwargs_for_action_2>},..}
     ***/
    @External
    public void defineGovernanceVote(String name, String description, BigInteger vote_start, BigInteger snapshot, @Optional String actions, @Optional String ipfs_hash) {
        if (actions == null) {
            actions = "";
        }

        if (ipfs_hash == null) {
            ipfs_hash = "";
        }

        defineVote(GOVERNANCE, name, description, vote_start, snapshot, actions, ipfs_hash);
    }

    /***
     Submit a new game Proposal.

     :param name: name of the vote
     :param description: description of the vote
     :param ipfs_hash: IPFS Hash of the concept file
     :param vote_start: day to start the vote
     :param snapshot: which day to use for the TAP stake snapshot
     ***/
    @External
    public void defineGameConceptVote(String name, String description, String ipfs_hash, BigInteger vote_start, BigInteger snapshot) {
        if (containsInList(name, get_approved_game_names())) {
            Address scoreAddress = Address.fromString(this.gameAddresses.getOrDefault(name, ""));
            Context.revert(TAG + ": " + name + " is already approved and deployed at " + scoreAddress);
        }
        defineVote(NEW_GAME, name, description, vote_start, snapshot, "", ipfs_hash);
    }

    /***
     Defines approval voting for the game
     :param name: name of the vote
     :param description: description of the vote
     :param ipfs_hash: IPFS Hash of the docs for approval file
     :param vote_start: day to start the vote
     :param snapshot: which day to use for the TAP stake snapshot
     ***/
    @External
    public void defineGameApprovalVote(String name, String description, String ipfs_hash, BigInteger vote_start, BigInteger snapshot) {
        verifyConceptVotingProposer(name, Context.getCaller());
        verifyConceptVotingSuccess(name);
        verifyDetailsSubmission(name);
        verifyProposalStatus(name, "proposalApproved");
        defineVote(GAME_APPROVAL, name, description, vote_start, snapshot, "", ipfs_hash);
    }

    @External
    public void castGovernanceVote(String name, boolean vote) {
        castVote(GOVERNANCE, name, vote);
    }

    @External
    public void castGameConceptVote(String name, boolean vote) {
        castVote(NEW_GAME, name, vote);
    }

    @External
    public void castGameApprovalVote(String name, boolean vote) {
        castVote(GAME_APPROVAL, name, vote);
    }

    @External(readonly = true)
    public Map<String, Object> checkGovernanceVote(String name) {
        return checkVote(GOVERNANCE, name);
    }

    @External(readonly = true)
    public Map<String, Object> checkGameConceptVote(String name) {
        return checkVote(NEW_GAME, name);
    }

    @External(readonly = true)
    public Map<String, Object> checkGameApprovalVote(String name) {
        return checkVote(GAME_APPROVAL, name);
    }

    @External(readonly = true)
    public int getGovernanceProposalCount() {
        return getProposalCount(GOVERNANCE);
    }

    @External(readonly = true)
    public int getGameConceptProposalCount() {
        return getProposalCount(NEW_GAME);
    }

    @External(readonly = true)
    public int getGameApprovalProposalCount() {
        return getProposalCount(GAME_APPROVAL);
    }

    @External(readonly = true)
    public List<Map<String, Object>> getGovernanceProposals(@Optional int batch_size, @Optional int offset) {
        if (batch_size == 0 || batch_size > 5) {
            batch_size = 5;
        }

        return getProposals(GOVERNANCE, offset);
    }

    @External(readonly = true)
    public List<Map<String, Object>> getGameConceptProposals(@Optional int batch_size, @Optional int offset) {
        return getProposals(NEW_GAME, offset);
    }

    @External(readonly = true)
    public List<Map<String, Object>> getGameApprovalProposals(@Optional int batch_size, @Optional int offset) {
        return getProposals(GAME_APPROVAL, offset);
    }

    @External(readonly = true)
    public String getGameAddress(String name) {
        return this.gameAddresses.getOrDefault(name, "Game not found");
    }

    @External(readonly = true)
    public List<String> getGameAddresses() {
        List<String> game_addresses = new ArrayList<>();
        int size = proposalKeys.size();
        for (int i = 0; i < size; i++) {
            String gameName = this.proposalKeys.get(i);
            game_addresses.add(this.gameAddresses.get(gameName));
        }
        return game_addresses;
    }

    /***
     Evaluates a vote after the voting period is done. If the vote passed,
     any actions included in the proposal are executed. The vote definition fee
     is also refunded to the proposer if the vote passed.
     ***/
    @External
    public void evaluateGovernanceVote(String name) {
        String proposalPrefix = proposalPrefix(GOVERNANCE, name);
        Context.println("index " + this.proposalKeysIndex.getOrDefault(proposalPrefix, 0));
        Context.require(this.proposalKeysIndex.getOrDefault(proposalPrefix, 0) > 0, TAG + "" + ": Proposal with the name '" + name + "' is not found.");
        ProposalData proposalData = new ProposalData();
        BigInteger end_snap = proposalData.getEnd_snapshot(proposalPrefix);
        String actions = proposalData.getActions(proposalPrefix);
        BigInteger majority = proposalData.getMajority(proposalPrefix);

        Context.require(getDay().compareTo(end_snap) >= 0, "Governance: Voting period has not ended.");
        Context.require(proposalData.getActive(proposalPrefix), "This proposal is not active.");

        Map<String, Object> result = checkGovernanceVote(name);

        BigInteger forVote = (BigInteger) result.get("for");
        BigInteger againstVote = (BigInteger) result.get("against");

        if (forVote.add(againstVote).compareTo((BigInteger) result.get("quorum")) >= 0) {
            if (EXA.subtract(majority).multiply(forVote).compareTo(majority.multiply(againstVote)) > 0) {
                if (!actions.equals("")) {
                    try {
                        executeVoteActions(actions);
                        proposalData.setStatus(proposalPrefix, EXECUTED);
                    } catch (Exception e) {
                        Context.println(e.toString());
                        proposalData.setStatus(proposalPrefix, FAILED_EXECUTION);
                    }
                } else {
                    proposalData.setStatus(proposalPrefix, SUCCEEDED);
                }
            } else {
                proposalData.setStatus(proposalPrefix, DEFEATED);
            }
        } else {
            proposalData.setStatus(proposalPrefix, NO_QUORUM);
        }
        proposalData.setActive(proposalPrefix, false);
    }

    @External(readonly = true)
    public String getGameStatus(String name) {
        if (containsInArrayDb(name, this.gameNames)) {
            return this.status_data.get(Address.fromString(this.gameAddresses.get(name)));
        }
        Map<String, Object> voteInfo = checkGameConceptVote(name);
        return (String) voteInfo.get(STATUS);
    }

    @External
    public void updateGameStatus(String name, String status) {
        validateAdmin();
        String[] statusList = {"proposalRejected", "proposalApproved"};
        Context.require(containsInArray(status, statusList), TAG + ": Invalid Status");
        verifyProposalStatus(name, "waiting");
        Address gameAddress = Address.fromString(this.gameAddresses.get(name));
        setGameStatus(status, gameAddress);
    }

    @External
    public void setOfficialReviewCost(BigInteger officialReviewCost) {
        validateAdmin();
        Context.require(officialReviewCost.compareTo(ZERO) > 0, TAG + ": Official Review Cost must be greater than 0");
        this._official_review_cost.set(officialReviewCost);
    }

    private void startOfficialReview(Address _from, BigInteger _value, String name) {
        Address reviewSponsors = this._official_review_sponsors.get(name);
        Context.require(reviewSponsors == null, TAG + ": Official review for  " + name + " already initiated by " + reviewSponsors + " is not None");
        BigInteger officialReviewCost = this._official_review_cost.get();
        Context.require(_value.equals(officialReviewCost.multiply(MULTIPLIER)), TAG + ": " + officialReviewCost + " TAP is required to start official review.");
        verify_approval_voting_success(name);

        this._official_review_costs.set(name, _value);
        this._official_review_sponsors.set(name, _from);

        Address game_address = Address.fromString(this.gameAddresses.get(name));
        this.status_data.set(game_address, "gameReady");
    }

    @External
    public void tokenFallback(Address _from, BigInteger _value, @Optional byte[] _data) {
        Context.require(Context.getCaller().equals(get_tap_token_score()), TAG + " accepts only TAP token.");
        if (_data == null) {
            _data = "{}".getBytes();
        }
        String unpackedData = new String(_data);
        JsonObject reviewData = Json.parse(unpackedData).asObject();

        if (reviewData.get("method").asString().equals("_startOfficialReview")) {
            startOfficialReview(_from, _value, reviewData.get("params").asObject().get("game").asString());
        }
    }

    private void returnOfficialReviewAmount(String name) {
        Address sponsor_address = this._official_review_sponsors.get(name);
        BigInteger official_review_cost = this._official_review_costs.get(name);

        Context.call(get_tap_token_score(), "transfer", sponsor_address, official_review_cost);
    }


    @External
    public void approveGame(String name) {
        validateAdmin();
        verifyProposalStatus(name, "gameReady");
        returnOfficialReviewAmount(name);
        Address game_address = Address.fromString(this.gameAddresses.get(name));
        setGameStatus("gameApproved", game_address);
    }

    @External
    public void rejectGame(String name) {
        validateAdmin();
        verifyProposalStatus(name, "gameReady");
        returnOfficialReviewAmount(name);
        Address game_address = Address.fromString(this.gameAddresses.get(name));
        setGameStatus("gameRejected", game_address);
    }

    @External
    public void advanceDayManually() {
        Context.require(DEBUG, TAG + ": This method can only be called on DEBUG);not DEBUG");
        validateOwner();
        this._time_offset.set(this._time_offset.get().subtract(U_SECONDS_DAY));
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

    @EventLog(indexed = 2)
    public void VoteCast(String vote_name, boolean vote, Address voter, BigInteger stake, BigInteger total_for, BigInteger total_against) {
    }

    @EventLog(indexed = 1)
    public void VoteDefined(String vote_name, Address proposer) {
    }

    @EventLog(indexed = 2)
    public void FundTransfer(Address recipient, BigInteger amount, String note) {
    }

    @EventLog(indexed = 2)
    public void ProposalSubmitted(Address sender, Address scoreAddress) {
    }

    @EventLog(indexed = 1)
    public void GameSuspended(Address scoreAddress, String note) {
    }

    /*
    To be removed during production
     */
    @External(readonly = true)
    public Map<String, BigInteger> getTotalVotesByUser(Address sender, String proposal) {
        ProposalData proposalData = new ProposalData();
        String gameConceptPrefix = proposalPrefix(GOVERNANCE, proposal);
        Map<String, BigInteger> voteData = Map.of(
                "totalForVotes", proposalData.getTotalForVotes(gameConceptPrefix),
                "totalAgainstVotes", proposalData.getTotalAgainstVotes(gameConceptPrefix),
                "totalForVotesOfUser", proposalData.getForVotesOfUser(gameConceptPrefix, sender),
                "totalAgainstVotesOfUser", proposalData.getAgainstVotesOfUser(gameConceptPrefix, sender)
        );
        return voteData;
    }

    @External
    public void setVoteDuration() {
        validateOwner();
        this.voteDuration.set(ONE);
    }

    @External
    public void setVoteDurationOfProposals(String name, BigInteger duration) {
        validateOwner();
        ProposalData proposalData = new ProposalData();
        proposalData.setEndSnapshot(proposalPrefix(NEW_GAME, name), duration);
    }

    @External
    public void makeProposalActive(String name) {
        validateOwner();
        ProposalData proposalData = new ProposalData();
        proposalData.setActive(proposalPrefix(GOVERNANCE, name), true);
//        proposalData.setStatus(proposalPrefix(GOVERNANCE, name), ACTIVE);
    }
}
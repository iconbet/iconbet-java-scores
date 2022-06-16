package com.iconbet.score.authorization;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

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
import com.iconbet.score.authorization.SettersGetters;

public class Authorization {
    protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);

    public static final String TAG = "AUTHORIZATION";
    public static final boolean DEBUG = false;
    private static final BigInteger MULTIPLIER = new BigInteger("1000000000000000000");
    private static final BigInteger U_SECONDS_DAY = new BigInteger("86400000000"); // Microseconds in a day.

    private static final BigInteger _1_ICX = new BigInteger("100000000000000000"); // 0.1 ICX = 10^18 * 0.1

    private static List<String> METADATA_FIELDS = List.of("name", "scoreAddress", "minBet", "maxBet", "houseEdge",
            "gameType", "revShareMetadata", "revShareWalletAddress",
            "linkProofPage", "gameUrlMainnet", "gameUrlTestnet");

    private static List<String> GAME_TYPE = List.of("Per wager settlement", "Game defined interval settlement");

    private static List<String> STATUS_TYPE = List.of("waiting", "proposalApproved", "proposalRejected", "gameReady",
            "gameApproved", "gameRejected", "gameSuspended", "gameDeleted");

    private static final String ADMIN_LIST = "admin_list";
    private static final String SUPER_ADMIN = "super_admin";
    private static final String PROPOSAL_DATA = "proposal_data";
    private static final String PROPOSAL_LIST = "proposal_list";
    private static final String STATUS_DATA = "status_data";
    private static final String OWNER_DATA = "owner_data";
    private static final String ROULETTE_SCORE = "roulette_score";
    private static final String TAP_TOKEN_SCORE = "tap_token_score";
    private static final String DIVIDEND_DISTRIBUTION_SCORE = "dividend_distribution_score";
    private static final String REWARDS_SCORE = "rewards_score";
    private static final String UTAP_TOKEN_SCORE = "utap_token_score";
    private static final String DAY = "day";
    private static final String PAYOUTS = "payouts";
    private static final String WAGERS = "wagers";
    private static final String NEW_DIV_CHANGING_TIME = "new_div_changing_time";
    private static final String GAME_DEVELOPERS_SHARE = "game_developers_share";

    private static final String TODAYS_GAMES_EXCESS = "todays_games_excess";
    // dividends paid according to this excess
    private static final String GAMES_EXCESS_HISTORY = "games_excess_history";


    private static final String APPLY_WATCH_DOG_METHOD = "apply_watch_dog_method";
    private static final String MAXIMUM_PAYOUTS = "maximum_payouts";
    private static final String MAXIMUM_LOSS = "maximum_loss";

    private static final String GOVERNANCE_ENABLED = "governance_enabled";
    private static final String VOTE_DEFINITION_CRITERION = "vote_definition_criterion";
    private static final String QUORUM = "quorum";
    private static final String TIME_OFFSET = "time_offset";
    private static final String VOTE_DURATION = "vote_duration";
    private static final String MAX_ACTIONS = "max_actions";

    private static final String GAME_ADDRESSES = "game_addresses";
    private static final String PROPOSAL_KEYS = "proposal_keys";
    private static final String PROPOSAL_KEY_INDEX = PROPOSAL_KEYS + "_index";
    private static final String PROPOSAL_COUNT = "proposal_count";
    private static final String GAME_NAMES = "game_names";

    private static final String OFFICIAL_REVIEW = "official_review";

    private final VarDB<Address> roulette_score = Context.newVarDB(ROULETTE_SCORE, Address.class);
    private final VarDB<Address> tapTokenScore = Context.newVarDB(TAP_TOKEN_SCORE, Address.class);
    private final VarDB<Address> dividendDistributionScore = Context.newVarDB(DIVIDEND_DISTRIBUTION_SCORE, Address.class);
    private final VarDB<Address> rewardsScore = Context.newVarDB(REWARDS_SCORE, Address.class);
    private final VarDB<Address> uTapTokenScore = Context.newVarDB(UTAP_TOKEN_SCORE, Address.class);
    private final ArrayDB<Address> admin_list = Context.newArrayDB(ADMIN_LIST, Address.class);
    private final VarDB<Address> super_admin = Context.newVarDB(SUPER_ADMIN, Address.class);
    //  question=?  why adrress
    private final DictDB<Address, String> proposal_data = Context.newDictDB(PROPOSAL_DATA, String.class);
    private final DictDB<Address, String> status_data = Context.newDictDB(STATUS_DATA, String.class);
    private final DictDB<Address, Address> owner_data = Context.newDictDB(OWNER_DATA, Address.class);
    private final ArrayDB<Address> proposal_list = Context.newArrayDB(PROPOSAL_LIST, Address.class);
    private final VarDB<BigInteger> day = Context.newVarDB(DAY, BigInteger.class);
    private final BranchDB<BigInteger, DictDB<Address, BigInteger>> wagers = Context.newBranchDB(WAGERS, BigInteger.class);
    private final BranchDB<BigInteger, DictDB<Address, BigInteger>> payouts = Context.newBranchDB(PAYOUTS, BigInteger.class);

    private final VarDB<BigInteger> game_developers_share = Context.newVarDB(GAME_DEVELOPERS_SHARE, BigInteger.class);
    private final DictDB<Address, BigInteger> todays_games_excess = Context.newDictDB(TODAYS_GAMES_EXCESS, BigInteger.class);

    private final VarDB<BigInteger> new_div_changing_time = Context.newVarDB(NEW_DIV_CHANGING_TIME, BigInteger.class);
    private final BranchDB<BigInteger, DictDB<Address, BigInteger>> games_excess_history = Context.newBranchDB(GAMES_EXCESS_HISTORY, BigInteger.class);

    private final VarDB<Boolean> apply_watch_dog_method = Context.newVarDB(APPLY_WATCH_DOG_METHOD, Boolean.class);
    private final DictDB<Address, BigInteger> maximum_payouts = Context.newDictDB(MAXIMUM_PAYOUTS, BigInteger.class);
    private final VarDB<BigInteger> maximum_loss = Context.newVarDB(MAXIMUM_LOSS, BigInteger.class);

    private final VarDB<Boolean> _governance_enabled = Context.newVarDB(GOVERNANCE_ENABLED, Boolean.class);
    private final VarDB<Integer> _tap_vote_definition_criterion = Context.newVarDB(VOTE_DEFINITION_CRITERION, Integer.class);
    private final VarDB<Integer> _quorum = Context.newVarDB(QUORUM, Integer.class);
    private final VarDB<BigInteger> _time_offset = Context.newVarDB(TIME_OFFSET, BigInteger.class);
    private final VarDB<BigInteger> _vote_duration = Context.newVarDB(VOTE_DURATION, BigInteger.class);
    private final VarDB<Integer> _max_actions = Context.newVarDB(MAX_ACTIONS, Integer.class);

//	self._vote_execute = VoteActions(db, self)

    private final ArrayDB<String> _game_names = Context.newArrayDB(GAME_NAMES, String.class);
    private final DictDB<String, String> _game_addresses = Context.newDictDB(GAME_ADDRESSES, String.class);

    private final ArrayDB<String> proposalKeys = Context.newArrayDB(PROPOSAL_KEYS, String.class);
    private final DictDB<String, Integer> proposalKeysIndex = Context.newDictDB(PROPOSAL_KEY_INDEX, Integer.class);

    private final VarDB<BigInteger> _official_review_cost = Context.newVarDB(OFFICIAL_REVIEW + "_cost", BigInteger.class);
    private final DictDB<String, Address> _official_review_sponsors = Context.newDictDB(OFFICIAL_REVIEW + "_sponsors", Address.class);
    private final DictDB<String, BigInteger> _official_review_costs = Context.newDictDB(OFFICIAL_REVIEW + "_costs", BigInteger.class);

    private final ArrayDB<String> governanceProposalKeys = Context.newArrayDB(GOVERNANCE + "_proposal_keys", String.class);
    private final ArrayDB<String> newGameProposalKeys = Context.newArrayDB(NEW_GAME + "_proposal_keys", String.class);
    private final ArrayDB<String> gameApprovalProposalKeys = Context.newArrayDB(GAME_APPROVAL + "_proposal_keys", String.class);
    private final DictDB<String, Integer> proposalCount = Context.newDictDB(PROPOSAL_COUNT, Integer.class);

    private final Map<String, ArrayDB<String>> dbProposalKeys = Map.of(
            GOVERNANCE, this.governanceProposalKeys,
            NEW_GAME, this.newGameProposalKeys,
            GAME_APPROVAL, this.gameApprovalProposalKeys
    );

    public static class DBName {
        private static final String[] dbName = new String[]{GOVERNANCE, NEW_GAME, GAME_APPROVAL};
    }

    public Authorization(@Optional boolean _on_update_var) {

        if (_on_update_var) {
            Context.println("updating contract only");
            onUpdate();
            return;
        }

        if (DEBUG) {
            Context.println("In __init__." + TAG);
            Context.println("owner is " + Context.getOwner() + " " + TAG);
        }
        BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());
        day.set(now.divide(U_SECONDS_DAY));
        //TODO: should we define it as false by default?
        this.apply_watch_dog_method.set(false);

    }

    public void onUpdate() {
        Context.println("calling on update. " + TAG);
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

    private void validateOwner() {
        Context.require(Context.getCaller().equals(Context.getOwner()),
                TAG + ": Only owner can call this method");
    }

    private void validateSuperAdmin() {
        Context.require(Context.getCaller().equals(super_admin.get()),
                TAG + "Only super admin can call this method.");
    }

    private void validateAdmin() {
        Context.require(containsInArrayDb(Context.getCaller(), admin_list),
                TAG + "Only Admins can call this method");
    }

    private void validateOwnerScore(Address score) {
        validateOwner();
        Context.require(score.isContract(), TAG + ": The provided address is not a contract address 'cx....'");
    }

    /***
     A function to redefine the value of self.owner once it is possible.
     To be included through an update if it is added to IconService.

     Sets the value of self.owner to the score holding the game treasury.
     ***/
    @External
    public void untether() {
        //Context.getOrigin() - > txn.origin  - always wallet
        //Context.getCaller() - > sender
        //Context.getOrigin() - > owner
        if (!Context.getOrigin().equals(Context.getOwner()))
            Context.revert("Only the owner can call the untether method.");
    }


    /**
     * Sets the equivalent time of 00:00 UTC of dividend structure changing
     * date in microseconds timestamp.
     * :param _timestamp: Timestamp of 00:00 UTC of dividend structure changing
     * date in microseconds timestamp
     * :type _timestamp: int
     * :return:
     **/
    @External
    public void set_new_div_changing_time(BigInteger _timestamp) {

        if (Context.getCaller().equals(Context.getOwner())) {
            this.new_div_changing_time.set(_timestamp);
            List<Address> approved_games_list = get_approved_games();
            for (Address address : approved_games_list) {
                //TODO: verify if we are removing the value correctly here, should we set to zero?
                this.todays_games_excess.set(address, null);
            }
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
     * Sets the address of roulette/game score
     * :param _scoreAddress: Address of roulette
     * :type _scoreAddress: :class:`iconservice.base.address.Address`
     * :return:
     **/
    @External
    public void set_roulette_score(Address _scoreAddress) {
        //TODO: should we call address isContract()?? contract means score?
        validateOwnerScore(_scoreAddress);
        this.roulette_score.set(_scoreAddress);
    }

    /**
     * Returns the roulette score address
     * :return: Address of the roulette score
     * :rtype: :class:`iconservice.base.address.Address
     ***/
    @External(readonly = true)
    public Address get_roulette_score() {
        return this.roulette_score.get();
    }

    /***
     Sets the address of tap token score
     :param _scoreAddress: Address of tap_token
     :type _scoreAddress: :class:`iconservice.base.address.Address`
     :return:
     ***/
    @External
    public void set_tap_token_score(Address _scoreAddress) {
        validateOwnerScore(_scoreAddress);
        this.tapTokenScore.set(_scoreAddress);
    }

    /***
     Returns the tap token score address
     :return: Address of the tap_token score
     :rtype: :class:`iconservice.base.address.Address`
     ***/
    @External(readonly = true)
    public Address get_tap_token_score() {
        return this.tapTokenScore.get();
    }

    /***
     Sets the address of dividend distribution/game score
     :param _scoreAddress: Address of dividend_distribution
     :type _scoreAddress: :class:`iconservice.base.address.Address`
     :return:
     ***/
    @External
    public void set_dividend_distribution_score(Address _scoreAddress) {


        validateOwnerScore(_scoreAddress);
        this.dividendDistributionScore.set(_scoreAddress);
    }

    /***
     Returns the dividend distribution score address
     :return: Address of the tap_token score
     :rtype: :class:`iconservice.base.address.Address`
     ***/
    @External(readonly = true)
    public Address get_dividend_distribution() {

        return this.dividendDistributionScore.get();
    }

    /***
     Sets the address of rewards/game score
     :param _scoreAddress: Address of rewards
     :type _scoreAddress: :class:`iconservice.base.address.Address`
     :return:
     ***/
    @External
    public void set_rewards_score(Address _scoreAddress) {

        validateOwnerScore(_scoreAddress);
        this.rewardsScore.set(_scoreAddress);
    }

    /***
     Returns the rewards score address
     :return: Address of the rewards score
     :rtype: :class:`iconservice.base.address.Address`
     ***/
    @External(readonly = true)
    public Address get_rewards_score() {
        return this.rewardsScore.get();
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
        this.uTapTokenScore.set(_scoreAddress);
    }

    /***
     Returns the uTAP Token score address
     :return: Address of the uTAP Token score
     :rtype: :class:`iconservice.base.address.Address`
     ***/
    @External(readonly = true)
    public Address get_utap_token_score() {
        return this.uTapTokenScore.get();
    }

    /**
     * Sets the sum of game developers as well as platform share
     * :param _share: Sum of game_devs as well as platform share
     * :type _share: int
     * :return:
     */
    @External
    public void set_game_developers_share(BigInteger _share) {
        validateOwner();
        this.game_developers_share.set(_share);
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


    /***
     Sets super admin. Super admin is also added in admins list. Only allowed
     by the contract owner.
     :param _super_admin: Address of super admin
     :type _super_admin: :class:`iconservice.base.address.Address`
     ***/
    @External
    public void set_super_admin(Address _super_admin) {
        validateOwner();
        this.super_admin.set(_super_admin);
        this.admin_list.add(_super_admin);
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
        return this.super_admin.get();
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
        Context.require(!containsInArrayDb(_admin, admin_list), TAG + ": Already in admin list");
        this.admin_list.add(_admin);
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
        Address[] admin_list = new Address[this.admin_list.size()];
        for (int i = 0; i < this.admin_list.size(); i++) {
            admin_list[i] = this.admin_list.get(i);
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
        Context.require(containsInArrayDb(_admin, this.admin_list), TAG + "Invalid Address: Not in list");
        removeArrayItem(this.admin_list, _admin);
        if (DEBUG) {
            Context.println(_admin.toString() + " has been removed from admin list." + TAG);
        }
    }

    @External(readonly = true)
    public boolean getGovernanceEnabled() {
        return this._governance_enabled.get();
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
     :param _gamedata: JSON object containing the data of game in above format
     :type _gamedata: str
     :return:
     ***/
    @Payable
    @External
    public void submit_game_proposal(String _gamedata) {
        Address sender = Context.getCaller();
        BigInteger fee = MULTIPLIER.multiply(new BigInteger("50"));
        JsonValue json = Json.parse(_gamedata);
        if (!json.isObject()) {
            throw new IllegalArgumentException("_gamedata parameter is not a json object");
        }

        JsonObject jsonObject = json.asObject();
        _check_game_metadata(jsonObject);

        String name = jsonObject.get("name").asString();
        Address game_address = Address.fromString(jsonObject.get("scoreAddress").asString());

        if (this._governance_enabled.get()) {
            verify_concept_voting_success(name);
            verify_concept_voting_proposer(name, sender);
            this._game_addresses.set(name, game_address.toString());
            this._game_names.add(name);
        } else {
            if (fee.compareTo(Context.getValue()) != 0) {
                Context.revert("50 ICX is required for submitting game proposal");
            }
        }
        Address scoreOwner = (Address) Context.call(game_address, "get_score_owner");

        Context.require(sender.equals(scoreOwner), TAG + "Owner is not matched");

        ProposalSubmitted(sender, game_address);

        Context.require(!containsInArrayDb(game_address, this.proposal_list),
                TAG + "Already listed scoreAddress in the proposal list.");
        this.proposal_list.add(game_address);

        /// question =?   self._owner_data[Address.from_string(metadata['scoreAddress'])] = self.msg.sender
        this.owner_data.set(game_address, sender);


        this.status_data.set(game_address, "waiting");
        this.proposal_data.set(game_address, _gamedata);

        if (this.apply_watch_dog_method.get()) {
            BigInteger maxPayout = new BigInteger(jsonObject.get("maxPayout").asString());
            this.maximum_payouts.set(game_address, maxPayout);
        }

    }

    /***
     Admin can change the game status according to its previous status.
     :param _status: Status of the game.
     :type _status: str
     :param _scoreAddress: Score address of the game for which status is to be changed
     :type _scoreAddress: :class:`iconservice.base.address.Address`
     :return:
     ***/
    @External
    public void set_game_status(String _status, Address _scoreAddress) {
        Address sender = Context.getCaller();
        if (!this.get_admin().contains(sender)) {
            Context.revert("Sender not an admin");
        }
        if (!STATUS_TYPE.contains(_status)) {
            Context.revert("Invalid status");
        }
        String statusScoreAddress = this.status_data.get(_scoreAddress);
        if (_status.equals("gameRejected") && !statusScoreAddress.equals("gameReady")) {
            Context.revert("This game cannot be rejected from state " + statusScoreAddress);
        }
        if (_status.equals("gameApproved") && !(
                statusScoreAddress.equals("gameReady") || statusScoreAddress.equals("gameSuspended")
        )) {
            Context.revert("This game cannot be approved from state " + statusScoreAddress);
        }
        if (_status.equals("gameSuspended") && !statusScoreAddress.equals("gameApproved")) {
            Context.revert("Only approved games may be suspended.");
        }
        if (_status.equals("gameDeleted") && !statusScoreAddress.equals("gameSuspended")) {
            Context.revert("Only suspended games may be deleted.");
        }

        this.status_data.set(_scoreAddress, _status);

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
     :param _metadata: JSON metadata of the game
     :type _metadata: dict
     :return:
     ***/
    /// question=? Python  def _check_game_metadata(self, _metadata: dict) ?dict?
    /// TODO Cambiar a Map o Json
    public void _check_game_metadata(JsonObject _metadata) {

        //All fields should be provided
        for (String name : METADATA_FIELDS) {
            if (!_metadata.contains(name)) {
                Context.revert("There is no " + name + " for the game");
            }
        }

        Context.println("is apply_watch_dog_method ? " + this.apply_watch_dog_method.get());

        if (this.apply_watch_dog_method.get()) {
            String maxPayoutStr = _metadata.getString("maxPayout", "");
            if (!maxPayoutStr.isEmpty()) {
                BigInteger maxPayout = new BigInteger(maxPayoutStr);
                if (maxPayout.compareTo(_1_ICX) == -1) {
                    Context.revert(maxPayout.toString() + " is less than 0.1 ICX");
                }
            } else {
                Context.revert("There is no maxPayout for the game");
            }
        }

        //TODO: use getString(name, default value) to prevent null pointer exceptions
        // Check if name is empty
        String nameStr = _metadata.getString("name", "");
        if (nameStr.isEmpty()) {
            Context.revert("Game name cant be empty");
        }

        // check if scoreAddress is a valid contract address
        String scoreAddressStr = _metadata.getString("scoreAddress", "");
        if (!scoreAddressStr.isEmpty()) {
            Address scoreAddress = Address.fromString(scoreAddressStr);
            if (!scoreAddress.isContract()) {
                Context.revert(scoreAddress.toString() + " is not a valid contract address");
            }
        } else {
            Context.revert(TAG + "The scoreAddress field is empty.");
        }

        // Check if minbet is within defined limit of 0.1 ICX
        String minBetStr = _metadata.getString("minBet", "");
        Context.require(!minBetStr.isEmpty(), TAG + ": The minbet field is empty");
        BigInteger minBet = new BigInteger(minBetStr);
        Context.require(minBet.compareTo(_1_ICX) > 0, minBet + " is less than 0.1 ICX");

        // Check if proper game type is provided
        String gameType = _metadata.getString("gameType", "");
        Context.require(!gameType.isEmpty(), TAG + ": The gameType field is empty");
        Context.require(GAME_TYPE.contains(gameType), TAG + "Not a valid game type");

        // Check for revenue share wallet address
        String revwallet = _metadata.get("revShareWalletAddress").asString();
        Address revWalletAddress = Address.fromString(revwallet);
        Context.require(revWalletAddress.isContract(), TAG + "Not a wallet address");
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

        if (!sender.equals(this.roulette_score.get())) {
            Context.revert("Only roulette score can invoke this method.");
        }
        BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());
        BigInteger day = now.divide(U_SECONDS_DAY);

        BigInteger wagerValue = this.wagers.at(day).getOrDefault(game, ZERO);
        this.wagers.at(day).set(game, wager.add(wagerValue));

        Context.println("acumulated wager at " + day + " for game " + game + " is " + this.wagers.at(day).get(game));
        BigInteger newTime = this.new_div_changing_time.get();

        if (newTime != null && now.compareTo(newTime) >= 1) {
            BigInteger excess = this.todays_games_excess.getOrDefault(game, ZERO);
            this.todays_games_excess.set(game, excess.add(wager));
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
        Address roulette = this.roulette_score.get();
        if (!Context.getCaller().equals(roulette)) {
            Context.revert("Only roulette score can invoke this method.");
        }
        BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());
        BigInteger day = now.divide(U_SECONDS_DAY);

        if (this.apply_watch_dog_method.getOrDefault(false)) {
            try {
                Context.println("apply watch dog enabled . " + TAG);
                if (payout.compareTo(this.maximum_payouts.getOrDefault(game, ZERO)) == 1) {
                    Context.revert("Preventing Overpayment. Requested payout: " + payout.toString() +
                            ". MaxPayout for this game: " + this.maximum_payouts.get(game) +
                            ". " + TAG);
                }

                BigInteger payOutDay = this.payouts.at(day).getOrDefault(game, ZERO);
                payOutDay = payOutDay.add(payout);
                BigInteger wagerDay = this.wagers.at(day).getOrDefault(game, ZERO);
                BigInteger incurred = payOutDay.subtract(wagerDay);
                Context.println("incurred payout: " + incurred);
                if (incurred.compareTo(this.maximum_loss.getOrDefault(ZERO)) >= 1) {
                    Context.revert("Limit loss. MaxLoss: " + this.maximum_loss.getOrDefault(ZERO) + ". Loss Incurred if payout: " +
                            incurred.intValue() + " " + TAG);
                }

            } catch (Exception e) {
                Context.println("error thrown:" + e.getMessage());
                this.status_data.set(game, "gameSuspended");
                this.GameSuspended(game, e.getMessage());
                return false;
            }

        }

        BigInteger newPayOut = this.payouts.at(day).getOrDefault(game, ZERO);
        payout = payout.add(newPayOut);
        this.payouts.at(day).set(game, payout);
        Context.println("new payout:" + payout + "at day " + day + " ." + TAG);

        if (this.new_div_changing_time.getOrDefault(ZERO).compareTo(ZERO) != 0 &&
                day.compareTo(this.new_div_changing_time.get()) >= 1) {
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

        if (day.compareTo(BigInteger.ONE) == -1) {
            BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());
            day = day.add(now.divide(U_SECONDS_DAY));
        }

        Map.Entry<String, String>[] payouts = new Map.Entry[this.get_approved_games().size()];

        for (int i = 0; i < this.get_approved_games().size(); i++) {
            Address game = this.get_approved_games().get(i);
            payouts[i] = Map.entry(game.toString(), String.valueOf(this.payouts.at(day).get(game)));
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
    /// question def get_proposal_data(self, _scoreAddress: Address) -> str ? toString ?
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

        Address[] proposal_list = new Address[this.proposal_list.size()];

        for (int i = 0; i < this.proposal_list.size(); i++) {
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
        Address[] _proposal_list = new Address[this.proposal_list.size()];
        int j = 0;
        for (int i = 0; i < this.proposal_list.size(); i++) {
            Address address = this.proposal_list.get(i);
            String gameApproved = this.status_data.get(address);
            if (gameApproved != null && gameApproved.equals("gameApproved")) {
                _proposal_list[j] = address;
                j++;
            }
        }
        Address[] tmp = new Address[j];
        System.arraycopy(_proposal_list, 0, tmp, 0, j);
        return List.of(tmp);
    }

    /***
     Returns all the approved games' names
     :return: List of approved games
     :rtype: list
     ***/
    @External(readonly = true)
    public List<String> get_approved_game_names() {
        List<String> games_list = new ArrayList<>();
        for (int i = 0; i < this.proposal_list.size(); i++) {
            Address scoreAddress = this.proposal_list.get(i);
            if (this.status_data.getOrDefault(scoreAddress, "").equals("gameApproved")) {
                JsonValue json = Json.parse(this.proposal_data.get(scoreAddress));
                JsonObject jsonObject = json.asObject();
                games_list.add(jsonObject.getString("name", ""));
            }
        }
        return games_list;
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
        BigInteger game_developers_amount = BigInteger.ZERO;

        for (int i = 0; i < this.get_approved_games().size(); i++) {
            Address game = this.get_approved_games().get(i);
            BigInteger game_excess = this.todays_games_excess.get(game);
            if (game_excess != null &&
                    game_excess.compareTo(BigInteger.ZERO) >= 0) {
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

        if (!sender.equals(this.roulette_score.get())) {
            Context.revert("This method can only be called by Roulette score");
        }
        BigInteger positive_excess = BigInteger.ZERO;
        BigInteger game_developers_amount = BigInteger.ZERO;
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
            BigInteger game_excess = this.todays_games_excess.get(game);
            this.games_excess_history.at(day.subtract(BigInteger.ONE)).set(game, game_excess);
            if (game_excess != null &&
                    game_excess.compareTo(BigInteger.ZERO) >= 0) {
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
        Map.Entry<String, String>[] games_excess = new Map.Entry[this.get_approved_games().size()];

        for (int i = 0; i < this.get_approved_games().size(); i++) {
            Address game = this.get_approved_games().get(i);
            games_excess[i] = Map.entry(game.toString(), String.valueOf(this.todays_games_excess.get(game)));
        }

        return Map.ofEntries(games_excess);
    }


    /***
     Returns a dictionary with game addresses as keys and the excess as the
     values for the specified day.
     :return: Dictionary of games' address and excess of the games
     :rtype: dict
     ***/
    //TODO: we can chage this from String, String to String, BigInteger
    @SuppressWarnings("unchecked")
    @External(readonly = true)
    public Map<String, String> get_games_excess(@Optional BigInteger day) {

        if (day == null) {
            day = BigInteger.ZERO;
        }

        if (day.compareTo(BigInteger.ZERO) == 0) {
            return this.get_todays_games_excess();
        }
        if (day.compareTo(BigInteger.ZERO) == -1) {
            BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());
            day = day.add(now.divide(U_SECONDS_DAY));
        }
        Map.Entry<String, String>[] games_excess = new Map.Entry[this.get_approved_games().size()];
        for (int i = 0; i < this.get_approved_games().size(); i++) {
            Address game = this.get_approved_games().get(i);
            games_excess[i] = Map.entry(game.toString(), String.valueOf(this.games_excess_history.at(day).get(game)));
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
    public void set_maximum_loss(BigInteger maxLoss) {
        Context.println("Setting maxLoss of " + maxLoss.toString());
        if (maxLoss.compareTo(_1_ICX) == -1) { // 0.1 ICX = 10^18 * 0.1
            Context.revert("maxLoss is set to a value less than 0.1 ICX");
        }
        Address sender = Context.getCaller();
        if (!this.get_admin().contains(sender)) {
            Context.revert("Sender not an admin");
        }
        this.maximum_loss.set(maxLoss);
    }

    @External(readonly = true)
    public BigInteger get_maximum_loss() {

        return maximum_loss.get();
    }

    @External
    public void set_maximum_payout(Address game, BigInteger maxPayout) {
        if (maxPayout.compareTo(_1_ICX) < 0) { // 0.1 ICX = 10^18 * 0.1
            Context.revert(maxPayout.toString() + "is less than 0.1 ICX");
        }

        if (!containsInArrayDb(game, this.proposal_list)) {
            Context.revert("Game has not been submitted.");
        }
        Address sender = Context.getCaller();
        if (!this.get_admin().contains(sender)) {
            Context.revert("Sender not an admin");
        }
        this.maximum_payouts.set(game, maxPayout);
    }


    @External(readonly = true)
    public BigInteger get_maximum_payout(Address game) {

        if (!containsInArrayDb(game, this.proposal_list)) {
            Context.revert("Game has not been submitted.");
        }
        return this.maximum_payouts.get(game);
    }

    @External
    public void toggle_apply_watch_dog_method() {
        Address sender = Context.getCaller();
        if (!this.get_admin().contains(sender)) {
            Context.revert("Sender not an admin");
        }
        boolean old_watch_dog_status = this.apply_watch_dog_method.get();
        if (!old_watch_dog_status) {
            //# All approved games must have minimum_payouts set before applying watch dog methods.
            for (int i = 0; i < this.proposal_list.size(); i++) {
                Address scoreAddress = this.proposal_list.get(i);
                String scoreAddressValue = this.status_data.get(scoreAddress);
                if (scoreAddressValue.equals("gameApproved")) {
                    BigInteger maximum_payouts = this.maximum_payouts.get(scoreAddress);
                    if (maximum_payouts.compareTo(_1_ICX) == -1) {
                        Context.revert("maxPayout of " + scoreAddress.toString() + " is less than 0.1 ICX");
                    }
                }
            }
            BigInteger maximum_loss = this.maximum_loss.get();
            if (maximum_loss.compareTo(_1_ICX) == -1) {
                Context.revert("maxLoss is set to a value less than 0.1 ICX");
            }
        }

        this.apply_watch_dog_method.set(!old_watch_dog_status);
    }

    @External(readonly = true)
    public boolean get_apply_watch_dog_method() {
        return this.apply_watch_dog_method.get();
    }

    //    governance methods on TAP
    public void setMinimumStake(BigInteger amount) {
        validateAdmin();
        Context.call(tapTokenScore.get(), "set_minimum_stake", amount);
    }

    public void setUnstakingPeriod(BigInteger time) {
        validateAdmin();
        Context.call(tapTokenScore.get(), "set_unstaking_period", time);
    }

    public void setMaxLoop(@Optional int loops) {
        validateAdmin();
        Context.call(tapTokenScore.get(), "set_max_loop", loops);
    }

    public void remove_from_blacklist_tap(Address _address) {
        validateAdmin();
        Context.call(tapTokenScore.get(), "remove_from_blacklist", _address);
    }

    public void set_blacklist_address_tap(Address _address) {
        this.validateAdmin();
        Context.call(tapTokenScore.get(), "set_blacklist_address", _address);
    }

    public void remove_from_locklist(Address _address) {
        validateAdmin();
        Context.call(tapTokenScore.get(), "remove_from_locklist", _address);
    }

    public void set_locklist_address(Address _address) {
        validateAdmin();
        Context.call(tapTokenScore.get(), "set_locklist_address", _address);
    }

    public void remove_from_whitelist(Address _address) {
        validateAdmin();
        Context.call(tapTokenScore.get(), "remove_from_whitelist", _address);
    }

    public void set_whitelist_address(Address _address) {
        validateAdmin();
        Context.call(tapTokenScore.get(), "set_whitelist_address", _address);
    }

    //    Governance methods on Dividend Distribution
    public void set_dividend_percentage(int _tap, int _gamedev, int _promo, int _platform) {
        validateAdmin();
        Context.call(dividendDistributionScore.get(), "set_dividend_percentage", _tap, _gamedev, _promo, _platform);
    }

    public void set_non_tax_period(BigInteger period) {
        validateAdmin();
        Context.call(dividendDistributionScore.get(), "set_non_tax_period", period);
    }

    public void set_tax_percentage(int percentage) {
        validateAdmin();
        Context.call(dividendDistributionScore.get(), "set_tax_percentage", percentage);
    }

    public void remove_from_blacklist_dividend(Address _address) {
        validateAdmin();
        Context.call(dividendDistributionScore.get(), "remove_from_blacklist", _address);
    }

    public void set_blacklist_address_dividend(Address _address) {
        validateAdmin();
        Context.call(dividendDistributionScore.get(), "set_blacklist_address", _address);
    }

    public void set_inhouse_games(Address _score) {
        validateAdmin();
        Context.call(dividendDistributionScore.get(), "set_inhouse_games", _score);
    }

    public void remove_from_inhouse_games(Address _score) {
        validateAdmin();
        Context.call(dividendDistributionScore.get(), "remove_from_inhouse_games", _score);
    }

    public void add_exception_address(Address _address) {
        validateAdmin();
        Context.call(dividendDistributionScore.get(), "add_exception_address", _address);
    }

    public void remove_exception_address(Address _address) {
        validateAdmin();
        Context.call(dividendDistributionScore.get(), "remove_exception_address", _address);
    }

    /*------------------------------------------------------------------------------------------------------------------
                                        Proposals and voting
     -----------------------------------------------------------------------------------------------------------------*/

    /***
     Sets the percentage of the total eligible TAP which must participate in a vote
     for a vote to be valid.

     :param quorum: percentage of the total eligible TAP required for a vote to be valid
     ***/
    @External
    public void setQuorum(int quorum) {
        Context.require(0 < quorum && quorum < 100, TAG + "Quorum must be between 0 and 100.");
        {
            validateAdmin();
            this._quorum.set(quorum);
        }
    }

    /***
     Returns the percentage of the total eligible TAP which must participate in a vote
     for a vote to be valid.
     ***/
    @External(readonly = true)
    public int getQuorum() {
        return this._quorum.get();
    }

    /***
     Sets the vote duration.

     :param duration: number of days a vote will be active once started
     ***/
    public void setVoteDuration(BigInteger duration) {
        validateAdmin();
        this._vote_duration.set(duration);
    }

    /***
     Returns the vote duration in days.
     ***/
    @External(readonly = true)
    public BigInteger getVoteDuration() {
        return this._vote_duration.get();
    }

    @External(readonly = true)
    public BigInteger getDay() {
        return (BigInteger.valueOf(Context.getBlockTimestamp()).subtract(this._time_offset.get()).divide(U_SECONDS_DAY));
    }

    /***
     Sets the maximum no of actions that can be done if the vote is successful.
     :param max_actions: maximum no of actions
     ***/
    public void setMaxActions(int max_actions) {
        Context.require(0 < max_actions && max_actions < 10, TAG + "Max Actions must be between 0 and 10.");
        validateAdmin();
        this._max_actions.set(max_actions);
    }

    /***
     Returns the percentage of the total eligible TAP which must participate in a vote
     for a vote to be valid.
     ***/
    @External(readonly = true)
    public int getMaxActions() {
        return this._max_actions.get();
    }

    public void add_to_game_addresses() {
        List<Address> approvedGames = get_approved_games();
        for (int i = 0; i < approvedGames.size(); i++) {
            Address gameAddress = approvedGames.get(i);
            String gameDataJsonString = get_proposal_data(gameAddress);
            JsonValue json = Json.parse(gameDataJsonString);
            String gameName = json.asObject().get("name").asString();
            this._game_names.add(gameName);
            this._game_addresses.set(gameName, gameAddress.toString());
        }
    }

    /***
     Method to start governance

     :param vote_duration: how many days the voting lasts
     ***/
    @External
    public void startGovernance(BigInteger vote_duration, int tap_vote_definition_criterion, int max_actions, int quorum) {
        boolean snapshotEnabled = (Boolean) Context.call(tapTokenScore.get(), "get_snapshot_enabled");
        Context.require(snapshotEnabled, TAG + ": Snapshot must be enabled in TAP Token to start governance.");
        BigInteger time_offset = (BigInteger) Context.call(tapTokenScore.get(), "get_time_offset");
        this._time_offset.set(time_offset);

        setTAPVoteDefinitionCriterion(tap_vote_definition_criterion);
        setMaxActions(max_actions);
        setQuorum(quorum);
        setVoteDuration(vote_duration);
        add_to_game_addresses();
        this._governance_enabled.set(true);
    }

    /***
     Sets the minimum percentage of TAP's total supply which a user must have staked
     in order to define a vote.

     :param percentage: percent represented in basis points
     ***/
    public void setTAPVoteDefinitionCriterion(int percentage) {
        validateAdmin();
        Context.require(0 <= percentage && percentage <= 10000, TAG + "Basis point must be between 0 and 10000.");
        this._tap_vote_definition_criterion.set(percentage);
    }

    /***
     Returns the minimum percentage of TAP's total supply which a user must have staked
     in order to define a vote. Percentage is returned as basis points.
     ***/
    @External(readonly = true)
    public int getTAPVoteDefinitionCriterion() {
        return this._tap_vote_definition_criterion.get();
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

    public void define_vote(String db_name, String name, String description, BigInteger vote_start,
                            BigInteger snapshot, @Optional String actions, @Optional String ipfs_hash) {
        if (actions == null) {
            actions = "{}";
        }

        if (ipfs_hash == null) {
            ipfs_hash = "";
        }

//        db = self.get_proposal_db(db_name)
        Context.require(containsInList(db_name, dbName), TAG + "Invalid DB name");
        Context.require(description.length() < 500, TAG + ": Description must be less than or equal to 500 characters.");
        Context.require(vote_start.compareTo(this.getDay()) > 0, TAG + "Vote cannot start at or before the current day.");
        Context.require(snapshot.compareTo(getDay()) >= 0 && snapshot.compareTo(vote_start) < 0, TAG + ":The reference snapshot must be in the range: [current_day (" + getDay() + " startDay - 1 (" + vote_start.subtract(ONE) + ")].");

        Context.require(proposalKeysIndex.getOrDefault(name, 0) == 0, TAG + "Poll name " + name + "has already been used.");

//        #Test TAP staking criterion.
        BigInteger tap_total = (BigInteger) Context.call(tapTokenScore.get(), "totalSupply");

        BigInteger tap_in_rewards = (BigInteger) Context.call(tapTokenScore.get(), "balanceOf", this.rewardsScore.get());
        BigInteger total_tap_in_circulation = tap_total.subtract(tap_in_rewards);

        BigInteger user_staked = (BigInteger) Context.call(tapTokenScore.get(), "staked_balanceOf", Context.getCaller());
        BigInteger uTapTokenTradingContractBalance = (BigInteger) Context.call(uTapTokenScore.get(), "tradingTokenContractbalanceOf");
        BigInteger uTapTokenBalanceOfSender = (BigInteger) Context.call(uTapTokenScore.get(), "balanceOf", Context.getCaller());
        BigInteger uTapTotalSupply = (BigInteger) Context.call(tapTokenScore.get(), "totalSupply");
        user_staked = user_staked.add(uTapTokenTradingContractBalance.multiply(uTapTokenBalanceOfSender).divide(uTapTotalSupply));

        int tap_criterion = this._tap_vote_definition_criterion.get();
        Context.require(POINTS.multiply(user_staked).divide(total_tap_in_circulation).intValue() > tap_criterion, "User needs at least " + tap_criterion / 100 + "of total cirulating TAP supply staked to define a vote.");
        if (!actions.equals("")) {
            JsonValue json = Json.parse(actions);
            JsonObject jsonObject = json.asObject();
            Context.require(jsonObject.size() < this._max_actions.get(), TAG + ": Only " + this._max_actions.get() + "actions are allowed");
            VoteDefined(name, Context.getCaller());
        }
        ProposalAttributes proposalAttributes = new ProposalAttributes();
        proposalAttributes.name = name;
        proposalAttributes.description = description;
        proposalAttributes.proposerAddress = Context.getCaller();
        proposalAttributes.quorum = BigInteger.valueOf(this._quorum.get()).multiply(EXA).divide(BigInteger.valueOf(100));
        proposalAttributes.majority = MAJORITY;
        proposalAttributes.snapshot = snapshot;
        proposalAttributes.start = vote_start;
        proposalAttributes.end = vote_start.add(this._vote_duration.get());
        proposalAttributes.actions = actions;

        ProposalData proposalData = new ProposalData();
        String proposalPrefix = proposalPrefix(db_name, name);
        proposalData.createProposal(proposalAttributes, proposalPrefix);
        this.proposalKeys.add(name);
        this.proposalKeysIndex.set(name, this.proposalKeys.size());
        this.proposalCount.set(db_name, this.proposalCount.getOrDefault(db_name, 0) + 1);
        this.dbProposalKeys.get(db_name).add(name);
    }

    private String proposalPrefix(String dbName, String proposalName) {
        return dbName + "|" + proposalName;
    }

    @External(readonly = true)
    public BigInteger totalTap(int _day) {
        return (BigInteger) Context.call(tapTokenScore.get(), "totalStakedBalanceOfAt", _day);
    }

    private Map<String, Object> check_vote(String db_name, String name) {
        BigInteger _for;
        BigInteger _against;
        Context.require(proposalKeysIndex.getOrDefault(name, 0) > 0, TAG + "Proposal not found");
        Context.require(containsInList(db_name, dbName), TAG + "Invalid DB name.");
        String proposalPrefix = proposalPrefix(db_name, name);
        ProposalData proposalData = new ProposalData();
        BigInteger total_tap = proposalData.getVote_snapshot(proposalPrefix);
        if (total_tap.equals(ZERO)) {
            _for = ZERO;
            _against = ZERO;
        } else {
            BigInteger[] totalVoted = new BigInteger[]{proposalData.getTotalForVotes(proposalPrefix), proposalData.getTotalAgainstVotes(proposalPrefix)};
            _for = EXA.multiply(totalVoted[0].divide(total_tap));
            _against = EXA.multiply(totalVoted[1].divide(total_tap));
        }

        Map<String, Object> vote_status = new HashMap<>();
        vote_status.put("name", proposalData.getName(proposalPrefix));
        vote_status.put("proposer", proposalData.getProposer(proposalPrefix));
        vote_status.put("description", proposalData.getDescription(proposalPrefix));
        vote_status.put("majority", proposalData.getMajority(proposalPrefix));
        vote_status.put("vote snapshot", proposalData.getVote_snapshot(proposalPrefix));
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
        if (status.equals(ACTIVE) && getDay().compareTo((BigInteger) vote_status.get("end day")) >= 0) {
            if (((BigInteger) vote_status.get("for")).add((BigInteger) vote_status.get("against")).compareTo((BigInteger) vote_status.get("quorum")) < 0) {
                vote_status.put("status", NO_QUORUM);
            } else if (EXA.subtract(majority).multiply((BigInteger) vote_status.get("for")).compareTo(majority.multiply((BigInteger) vote_status.get("against"))) > 0) {
                vote_status.put("status", SUCCEEDED);
            } else {
                vote_status.put("status", DEFEATED);
            }
        } else {
            vote_status.put("status", status);
        }
        return vote_status;
    }

    private void verify_concept_voting_success(String name) {
        Map<String, Object> vote_info = checkGameConceptVote(name);
        Context.require(vote_info.get("status").equals(SUCCEEDED), TAG + ": Concept Voting must be " + SUCCEEDED + ". Concept voting status: " + vote_info.get("status"));
    }

    private void verify_concept_voting_proposer(String name, Address sender) {
        Map<String, Object> vote_info = checkGameConceptVote(name);
        Context.require(vote_info.get("proposer").equals(sender), TAG + ": Different address from the proposer of concept voting.");
    }

    private void verify_details_submission(String name) {
        verify_concept_voting_success(name);
        Context.require(containsInArrayDb(name, this._game_names), TAG + ": Details for " + name + " not submitted.");
    }

    private void verify_proposal_status(String name, String status) {
        String game_address = this._game_addresses.getOrDefault(name, "");
        Context.require(!game_address.equals(""), TAG + ": Game address of name '" + name + "' not found");
        String game_status = this.status_data.getOrDefault(Address.fromString(game_address), "");
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
    public void cancel_vote(String db_name, String name) {
        String proposalPrefix = proposalPrefix(db_name, name);
        ProposalData proposalData = new ProposalData();
        Context.require(this.proposalKeysIndex.getOrDefault(name, 0) > 0, TAG + "There is no proposal with the name: " + name);

        Address[] eligible_addresses = new Address[]{proposalData.getProposer(proposalPrefix), Context.getOwner()};

        Context.require(containsInArray(Context.getCaller(), eligible_addresses), TAG + ": Only owner or proposer may call this method.");

        Context.require(proposalData.getStart_snapshot(proposalPrefix).compareTo(getDay()) >= 0
                        && Context.getCaller().equals(Context.getOwner()),
                TAG + "Only owner can cancel a vote that has started.");

        Context.require(proposalData.getStatus(proposalPrefix).equals(ACTIVE), TAG + ": Proposal can be cancelled only from active status.");

        proposalData.setActive(proposalPrefix, false);
        proposalData.setStatus(proposalPrefix, CANCELLED);
    }

    private int get_proposal_count(String db_name) {
        DBName dbName = new DBName();
        Context.require(containsInArray(db_name, dbName.dbName), TAG + ": Invalid DB name.");
        return this.proposalCount.getOrDefault(db_name, 0);
    }

    private List<Map<String, Object>> get_proposals(String db_name, @Optional int batch_size, @Optional int offset) {
        DBName dbName = new DBName();
        Context.require(containsInArray(db_name, dbName.dbName), TAG + ": Invalid DB name");
        List<Map<String, Object>> proposal_list = new ArrayList<>();
        int start = Math.max(1, offset);
        int end = Math.min(start + batch_size, this.get_proposal_count(db_name));
        for (int i = start; i < end + 1; i++) {
            String proposalName = this.dbProposalKeys.get(db_name).get(i);
            Map<String, Object> proposal = check_vote(db_name, proposalName);
            proposal_list.add(proposal);
        }
        return proposal_list;
    }

    @EventLog(indexed = 2)
    public void VoteCast(String vote_name, boolean vote, Address voter, BigInteger stake, BigInteger total_for, BigInteger total_against) {
    }

    @EventLog(indexed = 1)
    public void VoteDefined(String vote_name, Address proposer) {
    }

    /***
     Casts a vote in the named poll.
     ***/
    private void cast_vote(String db_name, String name, boolean vote) {

        String proposalPrefix = proposalPrefix(db_name, name);
        Context.require(containsInArray(db_name, DBName.dbName), TAG + "Invalid DB name");

        ProposalData proposalData = new ProposalData();
        BigInteger start_snap = proposalData.getStart_snapshot(proposalPrefix);
        BigInteger end_snap = proposalData.getEnd_snapshot(proposalPrefix);
        if (this.proposalKeysIndex.getOrDefault(name, 0) <= 0 || start_snap.compareTo(getDay()) > 0 || getDay().compareTo(end_snap) > 0 || !proposalData.getActive(proposalPrefix)) {
            Context.revert(TAG + "That is not an active poll.");
        }
        Address sender = Context.getCaller();
        BigInteger snapshot = proposalData.getVote_snapshot(proposalPrefix);

        BigInteger total_vote = (BigInteger) Context.call(tapTokenScore.get(), "stakedBalanceOfAt", sender, snapshot);
        total_vote = total_vote.add(((BigInteger) Context.call(uTapTokenScore.get(), "tradingTokenContractbalanceOf"))
                .multiply((BigInteger) Context.call(uTapTokenScore.get(), "balanceOf", sender))
                .divide((BigInteger) Context.call(uTapTokenScore.get(), "totalSupply")));

        Context.require(total_vote.compareTo(ZERO) > 0, TAG + ": TAP tokens need to be staked");
        BigInteger[] prior_vote = new BigInteger[]{proposalData.getForVotesOfUser(proposalPrefix, sender), proposalData.getAgainstVotesOfUser(proposalPrefix, sender)};
        BigInteger total_for_votes = proposalData.getTotalForVotes(proposalPrefix);
        BigInteger total_against_votes = proposalData.getTotalAgainstVotes(proposalPrefix);
        int total_for_voters_count = proposalData.getForVotersCount(proposalPrefix);
        int total_against_voters_count = proposalData.getAgainstVotersCount(proposalPrefix);
        BigInteger total_for = ZERO;
        BigInteger total_against = ZERO;
        if (vote) {
            proposalData.setForVotesOfUser(proposalPrefix, total_vote, sender);
            proposalData.setAgainstVotesOfUser(proposalPrefix, ZERO, sender);
            total_for = total_for_votes.add(total_vote).subtract(prior_vote[0]);
            total_against = total_against_votes.subtract(prior_vote[1]);
            if (prior_vote[0].equals(ZERO) && prior_vote[1].equals(ZERO)) {
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
            if (prior_vote[0].equals(ZERO) && prior_vote[1].equals(ZERO)) {
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
        List<String> action = jsonValue.asObject().names();
        for (int i = 0; i < action.size(); i++) {
            JsonObject params = jsonValue.asObject().get(action.get(i)).asObject().get("params").asObject();
            execute(this, action.get(i), params);
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

        define_vote(GOVERNANCE, name, description, vote_start, snapshot, actions, ipfs_hash);
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
        if (containsInArray(name, get_approved_game_names().toArray(new String[0]))) {
            Address scoreAddress = Address.fromString(this._game_addresses.get(name));
            Context.revert(TAG + ": " + name + " is already approved and deployed at " + scoreAddress);
        }
        define_vote(NEW_GAME, name, description, vote_start, snapshot, "", ipfs_hash);
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
        verify_concept_voting_proposer(name, Context.getCaller());
        verify_concept_voting_success(name);
        verify_details_submission(name);
        verify_proposal_status(name, "proposalApproved");
        define_vote(GAME_APPROVAL, name, description, vote_start, snapshot, "", ipfs_hash);
    }

    @External
    public void castGovernanceVote(String name, boolean vote) {
        cast_vote(GOVERNANCE, name, vote);
    }

    @External
    public void castGameConceptVote(String name, boolean vote) {
        cast_vote(NEW_GAME, name, vote);
    }

    @External
    public void castGameApprovalVote(String name, boolean vote) {
        cast_vote(GAME_APPROVAL, name, vote);
    }

    @External(readonly = true)
    public Map<String, Object> checkGovernanceVote(String name) {
        return check_vote(GOVERNANCE, name);
    }

    @External(readonly = true)
    public Map<String, Object> checkGameConceptVote(String name) {
        return check_vote(NEW_GAME, name);
    }

    @External(readonly = true)
    public Map<String, Object> checkGameApprovalVote(String name) {
        return check_vote(GAME_APPROVAL, name);
    }

    //    TODO: proposal Index not needed as we have name as an identifier?
    @External(readonly = true)
    public int getGovernanceProposalCount() {
        return dbProposalKeys.get(GOVERNANCE).size();
    }

    @External(readonly = true)
    public int getGameConceptProposalCount() {
        return dbProposalKeys.get(NEW_GAME).size();
    }

    @External(readonly = true)
    public int getGameApprovalProposalCount() {
        return dbProposalKeys.get(GAME_APPROVAL).size();
    }

    @External(readonly = true)
    public List<Map<String, Object>> getGovernanceProposals(@Optional int batch_size, @Optional int offset) {
        if (batch_size == 0) {
            batch_size = 20;
        }
        if (offset == 0) {
            offset = 1;
        }

        return get_proposals(GOVERNANCE, batch_size, offset);
    }

    @External(readonly = true)
    public List<Map<String, Object>> getGameConceptProposals(@Optional int batch_size, @Optional int offset) {
        if (batch_size == 0) {
            batch_size = 20;
        }
        if (offset == 0) {
            offset = 1;
        }

        return get_proposals(NEW_GAME, batch_size, offset);
    }

    @External(readonly = true)
    public List<Map<String, Object>> getGameApprovalProposals(@Optional int batch_size, @Optional int offset) {
        if (batch_size == 0) {
            batch_size = 20;
        }
        if (offset == 0) {
            offset = 1;
        }

        return get_proposals(GAME_APPROVAL, batch_size, offset);
    }

    @External(readonly = true)
    public String getGameAddress(String name) {
        Context.require(containsInArrayDb(name, this.proposalKeys), TAG + ": Game address of the game with the " +
                "name '" + name + "' not found");
        return this._game_addresses.get(name);
    }

    @External(readonly = true)
    public List<String> getGameAddresses() {
        List<String> game_addresses = new ArrayList<>();
        for (int i = 0; i < proposalKeys.size(); i++) {
            String gameName = this.proposalKeys.get(i);
            game_addresses.add(this._game_addresses.get(gameName));
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

        Context.require(this.proposalKeysIndex.getOrDefault(name, 0) > 0, TAG + "" +
                ": Proposal with the name '" + name + "' is not found.");
        ProposalData proposalData = new ProposalData();
        BigInteger end_snap = proposalData.getEnd_snapshot(proposalPrefix);
        String actions = proposalData.getActions(proposalPrefix);
        BigInteger majority = proposalData.getMajority(proposalPrefix);

        Context.require(getDay().compareTo(end_snap) > 0, TAG + "Balanced Governance: Voting period has not ended.");
        Context.require(proposalData.getActive(proposalPrefix), TAG + "This proposal is not active.");

        Map<String, Object> result = checkGovernanceVote(name);
        if (((BigInteger) result.get("for")).add((BigInteger) result.get("against")).compareTo((BigInteger) result.get("quorum")) > 0) {
            if (EXA.subtract(majority).multiply((BigInteger) result.get("for")).compareTo(majority.multiply((BigInteger) result.get("against"))) > 0) {
                if (!actions.equals("")) {
                    try {
                        executeVoteActions(actions);
                        proposalData.setStatus(proposalPrefix, EXECUTED);
                    } catch (Exception e) {
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
        if (containsInArrayDb(name, this._game_names)) {
            return this.status_data.get(Address.fromString(this._game_addresses.get(name)));
        }
        Map<String, Object> vote_info = checkGameConceptVote(name);
        return (String) vote_info.get(STATUS);
    }

    @External
    public void approveGameProposal(String name) {
        validateAdmin();
        verify_proposal_status(name, "waiting");
        Address game_address = Address.fromString(this._game_addresses.get(name));
        set_game_status("proposalApproved", game_address);
    }

    @External
    public void rejectGameProposal(String name) {
        validateAdmin();
        verify_proposal_status(name, "waiting");
        Address game_address = Address.fromString(this._game_addresses.get(name));
        set_game_status("proposalRejected", game_address);
    }

    @External
    public void setOfficialReviewCost(BigInteger officialReviewCost) {
        validateAdmin();
        Context.require(officialReviewCost.compareTo(ZERO) > 0, TAG + ": Official Review Cost must be greater than 0");
        this._official_review_cost.set(officialReviewCost);
    }

    private void startOfficialReview(Address _from, BigInteger _value, String name) {
        Context.require(this._official_review_sponsors.get(name) == null, TAG + ": Official review for  " + name + " already initiated by " + this._official_review_sponsors.get(name) + " is not None");
        Context.require(_value.equals(this._official_review_cost.get().multiply(MULTIPLIER)), TAG + ": " + this._official_review_cost.get() + " TAP is required to start official review.");
        verify_approval_voting_success(name);

        this._official_review_costs.set(name, _value);
        this._official_review_sponsors.set(name, _from);

        Address game_address = Address.fromString(this._game_addresses.get(name));
        this.status_data.set(game_address, "gameReady");
    }

    @External
    public void tokenFallback(Address _from, BigInteger _value, @Optional byte[] _data) {
        Context.require(Context.getCaller().equals(tapTokenScore.get()), TAG + " accepts only TAP token.");
        if (_data == null) {
            _data = "{}".getBytes();
        }
        String unpackedData = new String(_data);
        JsonObject reviewData = Json.parse(unpackedData).asObject();

        if (reviewData.get("method").asString().equals("_startOfficialReview")) {
            startOfficialReview(_from, _value, reviewData.get("params").asObject().get("game").asString());
        }
    }

    private void return_official_review_amount(String name) {
        Address sponsor_address = this._official_review_sponsors.get(name);
        BigInteger official_review_cost = this._official_review_costs.get(name);

        Context.call(tapTokenScore.get(), "transfer", sponsor_address, official_review_cost);
    }


    @External
    public void approveGame(String name) {
        validateAdmin();
        verify_proposal_status(name, "gameReady");
        return_official_review_amount(name);
        Address game_address = Address.fromString(this._game_addresses.get(name));
        set_game_status("gameApproved", game_address);
    }

    @External
    public void rejectGame(String name) {
        validateAdmin();
        verify_proposal_status(name, "gameReady");
        return_official_review_amount(name);
        Address game_address = Address.fromString(this._game_addresses.get(name));
        set_game_status("gameRejected", game_address);
    }

    @External
    public void advance_day_manually() {
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
}
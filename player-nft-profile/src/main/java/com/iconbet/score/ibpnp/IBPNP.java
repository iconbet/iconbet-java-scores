package com.iconbet.score.ibpnp;

import com.iconloop.score.token.irc3.IRC3Basic;

import score.VarDB;
import score.ArrayDB;
import score.DictDB;
import score.Context;
import score.Address;
import score.annotation.EventLog;
import score.annotation.External;

import java.math.BigInteger;

import scorex.util.HashMap;

import java.util.List;
import java.util.Map;

import com.iconbet.score.ibpnp.db.WalletLinkData;
import com.iconbet.score.ibpnp.db.UserData;

import static com.iconbet.score.ibpnp.utils.ArrayUtils.*;
import static com.iconbet.score.ibpnp.utils.Constants.*;

public class IBPNP extends IRC3Basic {
    public static final String TAG = "IconBet Player NFT Profile";
    public static final BigInteger REQUEST_WITHDRAW_BLOCK_HEIGHT = new BigInteger("10");
    public static final BigInteger decimal = new BigInteger("1000000000000000000");
    private static final String userDBPrefix = "user_data";
    private static final String walletDBPrefix = "wallet_data";

    private final DictDB<String, String> username = Context.newDictDB("USERNAME", String.class);
    private final DictDB<Address, String> wallet_to_username = Context.newDictDB("WALLET_TO_USERNAME", String.class);

    private final DictDB<String, Integer> username_index = Context.newDictDB("USERNAME_INDEX", Integer.class);
    private final ArrayDB<String> username_list = Context.newArrayDB("USERNAME_LIST", String.class);

    private final DictDB<Address, Integer> user_wallet_index = Context.newDictDB("USER_WALLET_INDEX", Integer.class);

    private final VarDB<BigInteger> total_supply = Context.newVarDB("TOTAL_SUPPLY", BigInteger.class);
    private final ArrayDB<Address> user_wallets_list = Context.newArrayDB("USER_WALLETS_LIST", Address.class);

    private final DictDB<Address, BigInteger> last_amount_wagered = Context.newDictDB("LAST_AMOUNT_WAGERED", BigInteger.class);
    private final DictDB<Address, BigInteger> life_time_earning = Context.newDictDB("LIFE_TIME_EARNING", BigInteger.class);
    private final DictDB<Address, Address> request_link_ledger_wallets = Context.newDictDB("REQUEST_LINK_LEDGER_WALLETS", Address.class);
    private final DictDB<Address, String> linked_wallets = Context.newDictDB("LINK_LEDGER_WALLETS", String.class);
    private final DictDB<Address, Boolean> link_ledger_wallet_completion = Context.newDictDB("LINK_LEDGER_WALLET_COMPLETION", Boolean.class);
    private final DictDB<Address, String> requested_to_requesting = Context.newDictDB("REQUESTED_WALLET_TO_REQUESTING_WALLET", String.class);
    private final VarDB<Address> treasury_score = Context.newVarDB("GAME_SCORE_ADDRESS", Address.class);
    private final VarDB<Address> tapTokenScore = Context.newVarDB("TAP_TOKEN_SCORE", Address.class);
    private final VarDB<Address> rewardsScore = Context.newVarDB("REWARDS_SCORE", Address.class);

    public static class GameData {
        public BigInteger game_amount_wagered;
        public BigInteger game_amount_won;
        public BigInteger game_amount_lost;
        public int game_bets_won;
        public int game_bets_lost;
        public BigInteger game_largest_bet;
        public BigInteger game_wager_level;
        public Address wallet_address;
        public String remarks;
        public BigInteger lastAmountWagered;
    }

    public IBPNP(String _name, String _symbol, int update) {
        super(_name, _symbol);
        if (update == 0) {
            this.total_supply.set(BigInteger.ZERO);
        }
        else {
            on_update();
        }
    }

    private void on_update(){

    }


    @External
    public void setTreasuryScore(Address scoreAddress) {
        Context.require(Context.getCaller().equals(Context.getOwner()), TAG + ": Only owner can call this method");
        this.treasury_score.set(scoreAddress);
    }

    @External(readonly = true)
    public Address getTreasuryScore() {
        return this.treasury_score.get();
    }

    @External
    public void setTapTokenScore(Address scoreAddress) {
        Context.require(Context.getCaller().equals(Context.getOwner()), TAG + ": Only owner can call this method");
        this.tapTokenScore.set(scoreAddress);
    }

    @External(readonly = true)
    public Address getTapTokenScore() {
        return this.tapTokenScore.get();
    }

    @External
    public void setRewardsScore(Address scoreAddress) {
        Context.require(Context.getCaller().equals(Context.getOwner()), TAG + ": Only owner can call this method");
        this.rewardsScore.set(scoreAddress);
    }

    @External(readonly = true)
    public Address getRewardsScore() {
        return this.rewardsScore.get();
    }

    @Override
    @External
    public void transfer(Address _to, BigInteger _tokenId) {
        Context.revert(TAG + ": Transfer is not allowed.");
    }

    @Override
    @External
    public void transferFrom(Address _from, Address _to, BigInteger _tokenId) {
        Context.revert(TAG + ": Transfer is not allowed.");
    }

    @Override
    @External(readonly = true)
    public BigInteger tokenOfOwnerByIndex(Address _owner, int _index) {
        Context.revert(TAG + ": Method not found.");
        return BigInteger.ZERO;
    }

    @Override
    @External(readonly = true)
    public BigInteger tokenByIndex(int _index) {
        Context.revert(TAG + ": Method not found.");
        return BigInteger.ZERO;
    }

    @Override
    @External
    public void approve(Address _to, BigInteger _tokenId) {
        Context.revert(TAG + "Approvals is disabled.");
    }

        private void _set_username(Address address, String username) {
        _check_username_validity(username);
        String usernameWithoutSpace = username.replace(" ", "");
        String lowercase_name = usernameWithoutSpace.toLowerCase();
        if (_checkIfUsernamePresent(lowercase_name)) {
            Context.revert("This username is already taken.");
        }
        this.username.set(lowercase_name, address.toString());
        this.wallet_to_username.set(address, lowercase_name);
        this.username_list.add(lowercase_name);
        var index = this.username_list.size();
        this.username_index.set(lowercase_name, index);
    }

    private boolean _checkIfUsernamePresent(String username) {
        return this.username_index.getOrDefault(username, 0) > 0;
    }

    private String userDBPrefix(Address userAddress) {
        return userDBPrefix + "|" + userAddress.toString();
    }

    private String walletDBPrefix(Address userWallet) {
        return walletDBPrefix + "|" + userWallet.toString();
    }

    @External(readonly = true)
    public String getUsernameByWallet(Address address) {
        return this.wallet_to_username.get(address);
    }

    @External(readonly = true)
    public boolean hasIBPNPProfile(Address address){
        return _checkIfWalletPresent(address);
    }

    @External(readonly = true)
    public boolean isUsernameAlreadyPresent(String username){
        String usernameWithoutSpace = username.replace(" ", "");
        String _username = usernameWithoutSpace.toLowerCase();
        return _checkIfUsernamePresent(_username);
    }

    @External
    public void createIBPNP(String userName) {
        BigInteger tokenId = this.total_supply.get().add(BigInteger.ONE);
        this.total_supply.set(tokenId);
        Address owner = Context.getCaller();
        if (_checkIfWalletPresent(owner)) {
            Context.revert("This user already has an " + TAG);
        }
        super._mint(owner, tokenId);
        this.user_wallets_list.add(owner);
        int index = this.user_wallets_list.size();
        this.user_wallet_index.set(owner, index);
        _set_username(owner, userName);
        add_data_to_userdb(userName, owner, tokenId);
        CreatedIconBetNFTProfile("Created IconBet NFT profile of " + Context.getCaller().toString() + "with username " + userName);
    }

    @External
    public void addGameData(GameData gameData) {
        Context.require(Context.getCaller().equals(this.treasury_score.get()), "Only treasury score can call this method.");
        UserData userData = new UserData();
        String userPrefix = userDBPrefix(gameData.wallet_address);
        BigInteger amount_wagered = userData.getAmount_wagered(userPrefix);
        BigInteger amount_won = userData.getAmount_won(userPrefix);
        BigInteger amount_lost = userData.getAmount_lost(userPrefix);
        int bets_won = userData.getBets_won(userPrefix);
        int bets_lost = userData.getBets_lost(userPrefix);
        BigInteger largest_bet = userData.getLargest_bet(userPrefix);
        int wager_level = userData.getWager_level(userPrefix);

        if (gameData.remarks.equals("wager_payout")) {
            userData.setAmount_won(userPrefix, amount_won.add(gameData.game_amount_won));
            userData.setAmount_lost(userPrefix, amount_lost.subtract(userData.getLastAmountWagered(userPrefix)));
            userData.setBets_won(userPrefix, bets_won + gameData.game_bets_won);
            userData.setBets_lost(userPrefix, bets_lost - gameData.game_bets_won);
        }

        if (gameData.remarks.equals("take_wager")) {
            userData.setAmount_wagered(userPrefix, amount_wagered.add(gameData.game_amount_wagered));
            if (gameData.game_largest_bet.compareTo(largest_bet) > 0) {
                userData.setLargest_bet(userPrefix, gameData.game_largest_bet);
            }

            BigInteger amount_wagered1 = userData.getAmount_wagered(userPrefix);
            if (amount_wagered1.divide(decimal).compareTo(new BigInteger("1000000")) > 0) {
                int wager_level_1 = amount_wagered1.divide(new BigInteger("1000000")).intValue();
                userData.setWager_level(userPrefix, wager_level_1);
            }
            userData.setBets_lost(userPrefix, gameData.game_bets_lost + bets_lost);
            userData.setAmount_lost(userPrefix, gameData.game_amount_lost.add(amount_lost));
            userData.setLastAmountWagered(userPrefix, gameData.lastAmountWagered);
        }
        AddedGameData("Game data is added to " + Context.getOrigin().toString());
    }


    @External(readonly = true)
    public Map<String, Object> getUserData(Address address) {
        Map<String, Object> userData = get_user_data(address);
        BigInteger dailyEarning = callScore(BigInteger.class, rewardsScore.get(), "get_expected_rewards", address.toString());
        userData.put("daily_earning", dailyEarning);
        return userData;
    }

    @External(readonly = true)
    public Map<String, Object> getTokenDataOfLinkedAccounts(Address address) {
        Address linkedAccount = Address.fromString(get_alternate_wallet_address(address));
        return get_user_data(linkedAccount);
    }

    @External(readonly = true)
    public BigInteger getTotalUserProfiles(){
        return this.total_supply.get();
    }

    @External(readonly = true)
    public boolean has_alternate_wallet(Address _wallet) {
        String userPrefix = userDBPrefix(_wallet);
        UserData userData = new UserData();
        boolean hasAlternalte = false;
        if (!userData.getLinked_wallet(userPrefix).equals("")) {
            hasAlternalte = true;
        }
        return hasAlternalte;
    }

    @External(readonly = true)
    public String get_alternate_wallet_address(Address _wallet) {
        String userPrefix = userDBPrefix(_wallet);
        UserData userData = new UserData();
        return userData.getLinked_wallet(userPrefix);
    }

    @External(readonly = true)
    public String get_requesting_wallet_address(Address _wallet) {
        return this.requested_to_requesting.getOrDefault(_wallet, "");
    }

    @External(readonly = true)
    public boolean can_request_to_another_wallet(Address _wallet) {
        String walletPrefix = walletDBPrefix(_wallet);
        WalletLinkData walletLinkData = new WalletLinkData();
        boolean canRequest = false;
        List<String> statusList = List.of("_reject", "_unlinked", "");
        int hasReachedRequestingBlockheight = walletLinkData.getRequested_block(walletPrefix).add(REQUEST_WITHDRAW_BLOCK_HEIGHT).compareTo(BigInteger.valueOf(Context.getBlockHeight()));
        if (walletLinkData.getRequest_status(walletPrefix).equals("_pending") && hasReachedRequestingBlockheight < 0) {
            canRequest = true;
        } else if (containsInList(walletLinkData.getRequest_status(walletPrefix), statusList)) {
            canRequest = true;
        }
        return canRequest;
    }

    private void _add_data_to_wallet_link_data(Address requesting_address, Address requested_address, String response, String walletType) {
        WalletLinkData walletLinkData = new WalletLinkData();
        String walletPrefix = walletDBPrefix(requesting_address);
        walletLinkData.setRequested_wallet(walletPrefix, requested_address.toString());
        walletLinkData.setRequested_block(walletPrefix, BigInteger.valueOf(Context.getBlockHeight()));
        walletLinkData.setRequest_status(walletPrefix, response);
        walletLinkData.setWallet_type(walletPrefix, walletType);
    }

    @External
    public void requestLinkingWallet(Address _wallet, String _walletType) {
        Address sender = Context.getCaller();
        Context.require(!sender.equals(_wallet), "Can not request own account for linking.");
        Context.require(_checkIfWalletPresent(sender) && _checkIfWalletPresent(_wallet), "Both requesting and requested wallet should have an IBPNP profile. " + TAG);
        Context.require(containsInList(_walletType, walletType), TAG + ": The wallet type should be either " + ICONex + " or " + Ledger + " wallet");
        String senderPrefix = walletDBPrefix(sender);
        String requestedWalletPrefix = walletDBPrefix(_wallet);
        WalletLinkData walletLinkData = new WalletLinkData();
        Context.require(!walletLinkData.getRequest_status(senderPrefix).equals("_approve"), "The requesting wallet is already linked to another wallet.");
        Context.require(!walletLinkData.getRequest_status(requestedWalletPrefix).equals("_approve"), "The requesting wallet is already linked to another wallet.");

        if (walletLinkData.getRequest_status(senderPrefix).equals("_pending") && !can_request_to_another_wallet(sender)) {
            Context.revert(TAG + ": Cannot request the requested wallet before one day of requesting it.");
        }
        if (!walletLinkData.getRequested_wallet(senderPrefix).equals("")) {
            if (containsInList(walletLinkData.getRequest_status(senderPrefix), List.of("_reject", "_unlinked", ""))) {
                _add_data_to_wallet_link_data(sender, _wallet, "_pending", "");
                _add_data_to_wallet_link_data(_wallet, sender, "_pending", _walletType);
                this.requested_to_requesting.set(_wallet, sender.toString());
            } else if (walletLinkData.getRequest_status(senderPrefix).equals("_pending")) {
                _add_data_to_wallet_link_data(sender, _wallet, "_pending", "");
                _add_data_to_wallet_link_data(_wallet, sender, "_pending", _walletType);
                this.requested_to_requesting.set(_wallet, sender.toString());
            } else {
                Context.revert(TAG + ": Cannot request linking wallet before one day of requesting a wallet.");
            }
        }
        else {
            _add_data_to_wallet_link_data(sender, _wallet, "_pending", "");
            _add_data_to_wallet_link_data(_wallet, sender, "_pending", _walletType);
            this.requested_to_requesting.set(_wallet, sender.toString());
        }
        RequestToLinkWalletSent("Request to link wallet is sent to " + _wallet);
    }

    @External
    public void respondToLinkRequest(Address _wallet, String _response) {
        Address sender = Context.getCaller();
        String walletPrefix = walletDBPrefix(_wallet);
        String senderPrefix = walletDBPrefix(sender);
        String senderUserDataPrefix = userDBPrefix(sender);
        String walletUserDataPrefix = userDBPrefix(_wallet);
        WalletLinkData walletLinkData = new WalletLinkData();
        UserData userData = new UserData();
        Context.require(walletLinkData.getRequest_status(walletPrefix).equals("_pending"), TAG +
                "Can respond to the request for pending status only.");
        Context.require(userData.getLinked_wallet(walletUserDataPrefix).equals("") &&
                        userData.getLinked_wallet(senderUserDataPrefix).equals(""),
                TAG + ": The wallet is already linked to another wallet.");
        Context.require(requested_to_requesting.getOrDefault(sender, "").equals(_wallet.toString()), TAG + ": " + _wallet + " is not the requesting wallet");
        if (_response.equals("_approve")) {
            userData.setLinked_wallet(senderUserDataPrefix, _wallet.toString());
            userData.setLinked_wallet(walletUserDataPrefix, sender.toString());
            walletLinkData.setRequest_status(senderPrefix, _response);
            walletLinkData.setRequest_status(walletPrefix, _response);
            this.requested_to_requesting.set(sender, null);
        } else if (_response.equals("_reject")) {
            walletLinkData.setRequest_status(senderPrefix, _response);
            walletLinkData.setRequest_status(walletPrefix, _response);
            this.requested_to_requesting.set(sender, null);
        } else {
            Context.revert(TAG + ": Invalid response. Response should be either _approve or _reject.");
        }
        RespondedToLinkWalletRequest("Responded to link wallet.");
    }

    @External(readonly = true)
    public Map<String, Object> getLinkWalletStatus(Address address) {
        WalletLinkData walletLinkData = new WalletLinkData();
        String walletPrefix = walletDBPrefix(address);
        return Map.of("requested_wallet", walletLinkData.getRequested_wallet(walletPrefix),
                "requested_block", walletLinkData.getRequested_block(walletPrefix),
                "request_status", walletLinkData.getRequest_status(walletPrefix),
                "wallet_type", walletLinkData.getWallet_type(walletPrefix));
    }

    @External
    public void changeUsername(String newusername) {
        Address sender = Context.getCaller();
        Context.require(_checkIfWalletPresent(sender), "This sender does not have an IBPNP profile." + TAG);
        String newusername_without_space = newusername.replace(" ", "");
        String new_username = newusername_without_space.toLowerCase();
        String old_username = this.wallet_to_username.getOrDefault(sender, "None");
        Context.require(!old_username.equals(new_username), "Cannot change into the same username. " + TAG);
        _check_username_validity(newusername);
        Context.require(!_checkIfUsernamePresent(new_username), "This username is already taken.");
        UserData userData = new UserData();
        String userPrefix = userDBPrefix(sender);
        int old_index = this.username_index.get(old_username);
        this.username.set(old_username, null);
        this.username_index.set(old_username, null);
        this.wallet_to_username.set(sender, new_username);
        this.username_list.set(old_index - 1, new_username);
        this.username_index.set(new_username, old_index);
        this.username.set(new_username, sender.toString());
        userData.setUsername(userPrefix, new_username);
        ChangedUserName("The username of wallet " + sender + " is changed from " + old_username + " to " + new_username);
    }

    @External
    public void unlinkWallets() {
        Address sender = Context.getCaller();
        UserData userData = new UserData();
        String senderPrefix = userDBPrefix(sender);
        Context.require(!userData.getLinked_wallet(senderPrefix).equals(""), TAG + ": This wallet is not linked to any other wallet.");
        Address addressToBeUnlinked = Address.fromString(userData.getLinked_wallet(senderPrefix));
        String addressToBeUnlinkedPrefix = userDBPrefix(addressToBeUnlinked);

        String senderWalletPrefix = walletDBPrefix(sender);
        String addressToBeUnlinkedWalletPrefix = walletDBPrefix(addressToBeUnlinked);

        WalletLinkData walletLinkData = new WalletLinkData();
        Context.require(walletLinkData.getRequest_status(senderWalletPrefix).equals("_approve") &&
                walletLinkData.getRequest_status(addressToBeUnlinkedWalletPrefix).equals("_approve"), TAG + ": The wallets are not in approved status.");

        userData.setLinked_wallet(senderPrefix, "");
        userData.setLinked_wallet(addressToBeUnlinkedPrefix, "");

        walletLinkData.setRequest_status(senderWalletPrefix, "_unlinked");
        walletLinkData.setRequest_status(addressToBeUnlinkedWalletPrefix, "_unlinked");

        walletLinkData.setRequested_wallet(senderWalletPrefix, "");
        walletLinkData.setRequested_wallet(addressToBeUnlinkedWalletPrefix, "");

        WalletUnlinked("The wallets " + sender + " and " + addressToBeUnlinked + " are unlinked");
    }


    private void _check_username_validity(String username) {
        if (username.equals("")) {
            Context.revert("Username cannot be an empty string. " + TAG);
        }
        char first_letter = username.charAt(0);
        if (Character.isWhitespace(username.charAt(0)) || Character.isWhitespace(username.charAt(username.length() - 1))) {
            Context.revert("Username cannot start or end with a white space.");
        }
    }

    @External(readonly = true)
    public String getWalletByUsername(String username) {
        String username_without_space = username.replace(" ", "");
        String lowercase_username = username_without_space.toLowerCase();
        return this.username.get(lowercase_username);
    }

    @External(readonly = true)
    public List<String> getUsernames() {
        return arrayDBToList(this.username_list);
    }

    private boolean _checkIfWalletPresent(Address address) {
        return this.user_wallet_index.getOrDefault(address, 0) > 0;
    }

    private void add_data_to_userdb(String username, Address wallet_address, BigInteger tokenId) {
        String userPrefix = userDBPrefix(wallet_address);
        UserData userData = new UserData();
        userData.setUsername(userPrefix, username);
        userData.setWallet_address(userPrefix, wallet_address);
        userData.setToken_id(userPrefix, tokenId);
    }


    private Map<String, Object> get_user_data(Address address) {
        UserData userData = new UserData();
        String userPrefix = userDBPrefix(address);
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("username", userData.getUsername(userPrefix));
        tokenData.put("wallet_address", userData.getWallet_address(userPrefix));
        tokenData.put("tokenId", userData.getToken_id(userPrefix));
        tokenData.put("amount_wagered", userData.getAmount_wagered(userPrefix));
        tokenData.put("amount_won", userData.getAmount_won(userPrefix));
        tokenData.put("amount_lost", userData.getAmount_lost(userPrefix));
        tokenData.put("bets_won", userData.getBets_won(userPrefix));
        tokenData.put("bets_lost", userData.getBets_lost(userPrefix));
        tokenData.put("largest_bet", userData.getLargest_bet(userPrefix));
        tokenData.put("wager_level", userData.getWager_level(userPrefix));
        tokenData.put("linked_wallet", userData.getLinked_wallet(userPrefix));
        return tokenData;
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

    @EventLog(indexed = 1)
    public void CreatedIconBetNFTProfile(String note) {
    }

    @EventLog(indexed = 1)
    public void AddedGameData(String note) {
    }

    @EventLog
    public void RequestToLinkWalletSent(String note) {
    }

    @EventLog
    public void RespondedToLinkWalletRequest(String note) {
    }

    @EventLog
    public void ChangedUserName(String note) {
    }

    @EventLog
    public void WalletUnlinked(String note) {
    }
}


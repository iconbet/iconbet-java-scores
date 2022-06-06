package com.iconbet.score.ibpnp;

import com.iconloop.score.token.irc3.IRC3Basic;
import score.*;
import score.annotation.EventLog;
import score.annotation.External;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class IBPNP extends IRC3Basic {
    public static final String TAG = "IconBet Player NFT Profile";
    public static final BigInteger REQUEST_WITHDRAW_BLOCK_HEIGHT = new BigInteger("90");
    public static final BigInteger decimal = new BigInteger("1000000000000000000");

    private final DictDB<String, Address> username = Context.newDictDB("USERNAME", Address.class);
    private final DictDB<Address, String> wallet_to_username = Context.newDictDB("WALLET_TO_USERNAME", String.class);

    private final DictDB<String, BigInteger> username_index = Context.newDictDB("USERNAME_INDEX", BigInteger.class);
    private final ArrayDB<String> username_list = Context.newArrayDB("USERNAME_LIST", String.class);

    private final DictDB<Address, BigInteger> user_wallet_index = Context.newDictDB("USER_WALLET_INDEX", BigInteger.class);

    private final VarDB<BigInteger> total_supply = Context.newVarDB("TOTAL_SUPPLY", BigInteger.class);
    private final ArrayDB<Address> user_wallets_list = Context.newArrayDB("USER_WALLETS_LIST", Address.class);

    private final BranchDB<Address, DictDB<String, String>> user_data = Context.newBranchDB("USERDATA", String.class);
    private final BranchDB<Address, DictDB<String, String>> wallet_link_data = Context.newBranchDB("WALLET_LINK_DATA", String.class);

    private final DictDB<Address, BigInteger> last_amount_wagered = Context.newDictDB("LAST_AMOUNT_WAGERED", BigInteger.class);
    private final DictDB<Address, BigInteger> life_time_earning = Context.newDictDB("LIFE_TIME_EARNING", BigInteger.class);
    private final DictDB<Address, Address> request_link_ledger_wallets = Context.newDictDB("REQUEST_LINK_LEDGER_WALLETS", Address.class);
    private final DictDB<Address, Address> linked_wallets = Context.newDictDB("LINK_LEDGER_WALLETS", Address.class);
    private final DictDB<Address, Boolean> link_ledger_wallet_completion = Context.newDictDB("LINK_LEDGER_WALLET_COMPLETION", Boolean.class);
    private final DictDB<Address, Address> requested_to_requesting = Context.newDictDB("REQUESTED_WALLET_TO_REQUESTING_WALLET", Address.class);
    private final VarDB<Address> treasury_score = Context.newVarDB("GAME_SCORE_ADDRESS", Address.class);
    private final VarDB<Address> tapTokenScore = Context.newVarDB("TAP_TOKEN_SCORE", Address.class);
    private final VarDB<Address> rewardsScore = Context.newVarDB("REWARDS_SCORE", Address.class);

    public IBPNP(String _name, String _symbol) {
        super(_name, _symbol);
        this.total_supply.set(BigInteger.ZERO);
    }

    @External
    public void setTreasuryScore(Address scoreAddress) {
        Context.require(Context.getCaller().equals(Context.getOwner()), "Only owner can call this method " + TAG);
        this.treasury_score.set(scoreAddress);
    }

    @External(readonly = true)
    public Address getTreasuryScore() {
        return this.treasury_score.get();
    }

    @External
    public void setTapTokenScore(Address scoreAddress) {
        Context.require(Context.getCaller().equals(Context.getOwner()), "Only owner can call this method " + TAG);
        this.tapTokenScore.set(scoreAddress);
    }

    @External(readonly = true)
    public Address getTapTokenScore() {
        return this.tapTokenScore.get();
    }

    @External
    public void setRewardsScore(Address scoreAddress) {
        Context.require(Context.getCaller().equals(Context.getOwner()), "Only owner can call this method " + TAG);
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
        Context.revert(TAG + ": Transfer is not allowed");
    }

    @External
    public void createIBPNP(String user_name) {
        BigInteger tokenId = this.total_supply.get().add(BigInteger.ONE);
        this.total_supply.set(tokenId);
        Address owner = Context.getCaller();
        if (_checkIfWalletPresent(owner)) {
            Context.revert("This user already has an " + TAG);
        }
        super._mint(owner, tokenId);
        this.user_wallets_list.add(owner);
        var index = this.username_list.size();
        this.user_wallet_index.set(owner, BigInteger.valueOf(index));
//        String username = _set_username(owner, user_name);
        _set_username(owner, user_name);
        add_data_to_userdb(user_name, owner, tokenId);
        CreatedIconBetNFTProfile("Created IconBet NFT profile of " + Context.getCaller().toString() + "with username " + user_name);
    }

    private void _set_username(Address address, String username) {
        _check_username_validity(username);
        String usernameWithoutSpace = username.replace(" ", "");
        String lowercase_name = usernameWithoutSpace.toLowerCase();
        if (checkIfUsernamePresent(lowercase_name)) {
            Context.revert("This username is already taken.");
        }
        this.username.set(lowercase_name, address);
        this.wallet_to_username.set(address, lowercase_name);
        this.username_list.add(lowercase_name);
        var index = this.username_list.size();
        this.username_index.set(lowercase_name, BigInteger.valueOf(index));
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
    public String getUsernameByWallet(Address address) {
        return this.wallet_to_username.get(address);
    }

    @External(readonly = true)
    public Address getWalletByUsername(String username) {
        String username_without_space = username.replace(" ", "");
        String lowercase_username = username_without_space.toLowerCase();
        return this.username.get(lowercase_username);
    }

    @External(readonly = true)
    public List<String> getUsernames(int startIndex, int endIndex) {
        String[] usernames = new String[this.username_list.size()];
        return arrayDBtoList(this.username_list, usernames);
    }

    private boolean checkIfUsernamePresent(String username) {
        if (this.username_list.size() == 0) {
            return false;
        } else {
            for (int i = 0; i < this.username_list.size(); i++) {
                if (username.equals(this.username_list.get(i))) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean _checkIfWalletPresent(Address address) {
        if (this.user_wallets_list.size() == 0) {
            return false;
        } else {
            for (int i = 0; i < this.user_wallets_list.size(); i++) {
                if (address.equals(this.user_wallets_list.get(i))) {
                    return true;
                }
            }
            return false;
        }
    }

    @External(readonly = true)
    public boolean checkIfWalletPresent(Address address) {
        return _checkIfWalletPresent(address);
    }

    private void add_data_to_userdb(String username, Address wallet_address, BigInteger tokenId) {
        this.user_data.at(wallet_address).set("username", username);
        this.user_data.at(wallet_address).set("wallet_address", wallet_address.toString());
        this.user_data.at(wallet_address).set("tokenId", tokenId.toString());
    }

    @External
    public void addGameData(BigInteger game_amount_wagered, BigInteger game_amount_won,
                            BigInteger game_amount_lost, BigInteger game_bets_won,
                            BigInteger game_bets_lost, BigInteger game_largest_bet,
                            BigInteger game_wager_level, Address wallet_address, String remarks) {
        Context.require(Context.getCaller().equals(this.treasury_score.get()), "Only treasury score can call this method.");
        BigInteger amount_wagered = new BigInteger(this.user_data.at(wallet_address).getOrDefault("amount_wagered", "0"));
        BigInteger amount_won = new BigInteger(this.user_data.at(wallet_address).getOrDefault("amount_won", "0"));
        BigInteger amount_lost = new BigInteger(this.user_data.at(wallet_address).getOrDefault("amount_lost", "0"));
        BigInteger bets_won = new BigInteger(this.user_data.at(wallet_address).getOrDefault("bets_won", "0"));
        BigInteger bets_lost = new BigInteger(this.user_data.at(wallet_address).getOrDefault("bets_lost", "0"));
        BigInteger largest_bet = new BigInteger(this.user_data.at(wallet_address).getOrDefault("largest_bet", "0"));
        BigInteger wager_level = new BigInteger(this.user_data.at(wallet_address).getOrDefault("wager_level", "0"));

        if (remarks.equals("wager_payout")) {
            this.user_data.at(wallet_address).set("amount_won", game_amount_won.add(amount_won).toString());
            this.user_data.at(wallet_address).set("amount_lost", amount_lost.subtract(game_amount_wagered).toString());
            this.user_data.at(wallet_address).set("bets_won", game_bets_won.add(bets_won).toString());
            this.user_data.at(wallet_address).set("bets_lost", bets_lost.subtract(game_bets_won).toString());
        }

        if (remarks.equals("take_wager")) {
            this.user_data.at(wallet_address).set("amount_wagered", game_amount_wagered.add(amount_wagered).toString());
            int compare_largest_bet = game_largest_bet.compareTo(largest_bet);
            if (compare_largest_bet == 1) {
                this.user_data.at(wallet_address).set("largest_bet", game_largest_bet.toString());
            }
            BigInteger amount_wagered1 = new BigInteger(this.user_data.at(wallet_address).getOrDefault("amount_wagered", "0"));
            if (amount_wagered1.divide(decimal).compareTo(new BigInteger("1000000")) == 1) {
                BigInteger wager_level_1 = amount_wagered1.divide(new BigInteger("1000000"));
                this.user_data.at(wallet_address).set("wager_level", wager_level_1.add(BigInteger.ONE).toString());
            }
            this.user_data.at(wallet_address).set("bets_lost", game_bets_lost.add(bets_lost).toString());
            this.user_data.at(wallet_address).set("amount_lost", game_amount_lost.add(amount_lost).toString());
        }
        AddedGameData("Game data is added to " + Context.getOrigin().toString());
    }

    private Map<String, String> get_user_data(Address address) {
        return Map.ofEntries(Map.entry("username", this.user_data.at(address).getOrDefault("username", "Not found")),
                Map.entry("wallet_address", this.user_data.at(address).getOrDefault("wallet_address", "Not found")),
                Map.entry("tokenId", this.user_data.at(address).getOrDefault("tokenId", "Not found")),
                Map.entry("amount_wagered", this.user_data.at(address).getOrDefault("amount_wagered", "0")),
                Map.entry("amount_won", this.user_data.at(address).getOrDefault("amount_won", "0")),
                Map.entry("amount_lost", this.user_data.at(address).getOrDefault("amount_lost", "0")),
                Map.entry("bets_won", this.user_data.at(address).getOrDefault("bets_won", "0")),
                Map.entry("bets_lost", this.user_data.at(address).getOrDefault("bets_lost", "0")),
                Map.entry("largest_bet", this.user_data.at(address).getOrDefault("largest_bet", "0")),
                Map.entry("wager_level", this.user_data.at(address).getOrDefault("wager_level", "1")),
                Map.entry("linked_wallet", this.user_data.at(address).getOrDefault("linked_wallet", "None"))
        );
    }


    @External(readonly = true)
    public Map<String, String> getUserData(Address address) {
        return get_user_data(address);
    }

    public <T> List<T> arrayDBtoList(ArrayDB<T> arraydb, T[] list) {
        for (int i = 0; i < arraydb.size(); i++) {
            list[i] = arraydb.get(i);
        }
        return List.of(list);
    }

    @External
    public void requestLinkingWallet(Address _wallet) {
        Address sender = Context.getCaller();
        Context.require(!sender.equals(_wallet), "Can not request own account for linking.");
        Context.require(_checkIfWalletPresent(sender) && _checkIfWalletPresent(_wallet), "Both requesting and requested wallet should hae an IBPNP profile. " + TAG);
        Context.require(!this.wallet_link_data.at(sender).getOrDefault("request_status", "None").equals("_approve"), "The requesting wallet is already linked to another wallet.");
        String requested_wallet = this.wallet_link_data.at(sender).getOrDefault("requested_wallet", "None");
        if (!requested_wallet.equals("None")) {
            if (this.wallet_link_data.at(sender).getOrDefault("request_status", "None").equals("_reject")) {
                this._add_data_to_wallet_link_data(sender, _wallet, "_pending");
            } else if (this.wallet_link_data.at(sender).getOrDefault("request_status", "None").equals("_unlinked")) {
                this._add_data_to_wallet_link_data(sender, _wallet, "_pending");
            } else if (this.wallet_link_data.at(sender).getOrDefault("request_status", "None").equals("_pending") &&
                    new BigInteger(this.wallet_link_data.at(sender).getOrDefault("requested_block", "0")).
                            add(REQUEST_WITHDRAW_BLOCK_HEIGHT).compareTo(BigInteger.valueOf(Context.getBlockHeight())) == -1) {
                this._add_data_to_wallet_link_data(sender, _wallet, "_pending");
            } else {
                Context.revert("Cannot request linking wallet again before 1 day after requesting a wallet. " + TAG);
            }
        } else {
            this._add_data_to_wallet_link_data(sender, _wallet, "_pending");
        }
        RequestToLinkWalletSent("Request to link wallet is sent to " + _wallet.toString());
    }

    private void _add_data_to_wallet_link_data(Address requesting_address, Address requested_address, String response) {
        this.wallet_link_data.at(requesting_address).set("requested_wallet", requested_address.toString());
        this.wallet_link_data.at(requesting_address).set("requested_block", String.valueOf(Context.getBlockHeight()));
        this.wallet_link_data.at(requesting_address).set("request_status", response);
    }

    @External
    public void respondToLinkRequest(Address _wallet, String _response) {
        Address sender = Context.getCaller();
        Context.require(this.wallet_link_data.at(_wallet).getOrDefault("request_status", "None")
                .equals("_pending"), "The request status of the requesting wallet should be pending. " + TAG);
        Context.require(this.user_data.at(sender).getOrDefault("linked_wallet", "None")
                        .equals("None") && this.user_data.at(_wallet).getOrDefault("linked_wallet", "None")
                        .equals("None"),
                "The requesting or requested wallet is already to linked to another account.");

        if (_response.equals("_approve")) {
            this.user_data.at(_wallet).set("linked_wallet", sender.toString());
            this.user_data.at(sender).set("linked_wallet", _wallet.toString());
            this.wallet_link_data.at(_wallet).set("request_status", "_approve");
            _add_data_to_wallet_link_data(sender, _wallet, "_approve");
        } else if (_response.equals("_reject")) {
            this.wallet_link_data.at(_wallet).set("request_status", "_reject");
        } else {
            Context.revert("Invalid response to link wallet request. " + TAG);
        }
        RespondedToLinkWalletRequest("Responded to link wallet request from " + _wallet.toString());
    }

    @External(readonly = true)
    public Map<String, String> getLinkWalletStatus(Address address) {
        return Map.of("requested_wallet", this.wallet_link_data.at(address).getOrDefault("requested_wallet", "None"),
                "requested_block", this.wallet_link_data.at(address).getOrDefault("requested_block", "None"),
                "request_status", this.wallet_link_data.at(address).getOrDefault("request_status", "None"));
    }

    @External
    public void changeUsername(String newusername) {
        String newusername_without_space = newusername.replace(" ", "");
        String new_username = newusername_without_space.toLowerCase();
        Address sender = Context.getCaller();
        Context.require(checkIfWalletPresent(sender), "This sender does not have an IBPNP profile." + TAG);
        String old_username = this.wallet_to_username.getOrDefault(sender, "None");
        Context.require(!old_username.equals(new_username), "Cannot change into the same username. " + TAG);
        _check_username_validity(newusername);
        Context.require(!checkIfUsernamePresent(new_username), "This username is already taken.");
        BigInteger old_index = this.username_index.get(old_username);
        this.username.set(old_username, null);
        this.username_index.set(old_username, null);
        this.wallet_to_username.set(sender, new_username);
        this.username_list.add(new_username);
        this.username_index.set(new_username, BigInteger.valueOf(username_list.size()));
        this.username.set(new_username, sender);
        this.user_data.at(sender).set("username", new_username);
        ChangedUserName("The username of wallet " + sender.toString() + " is changed from " + old_username + " to " + new_username);
    }

    @External
    public void unlinkWallets() {
        Address sender = Context.getCaller();
        Context.require(!this.user_data.at(sender).getOrDefault("linked_wallet", "None").equals("None"), "The wallet is not linked to any other wallets. " + TAG);
        Address linked_wallet = Address.fromString(this.user_data.at(sender).getOrDefault("linked_wallet", "None"));
        Context.require(this.wallet_link_data.at(sender).getOrDefault("request_status", "None").equals("_approve") && this.wallet_link_data.at(linked_wallet).getOrDefault("request_status", "None").equals("_approve"), "The wallets are not in approved status " + TAG);
        this.user_data.at(sender).set("linked_wallet", null);
        this.user_data.at(linked_wallet).set("linked_wallet", null);
        this.wallet_link_data.at(sender).set("request_status", "_unlinked");
        this.wallet_link_data.at(linked_wallet).set("request_status", "_unlinked");
        WalletUnlinked("The wallet " + sender.toString() + " is unlinked with " + linked_wallet.toString() + ".");
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


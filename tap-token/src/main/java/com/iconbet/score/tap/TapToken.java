package com.iconbet.score.tap;

import static java.math.BigInteger.ZERO;
import static java.math.BigInteger.TEN;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.iconloop.score.token.irc2.IRC2;

import score.Address;
import score.ArrayDB;
import score.BranchDB;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;


import static com.iconbet.score.tap.utils.Constants.*;

public class TapToken implements IRC2 {

    public static class ArrayDbToMigrate {
        public static String BLACKLIST = "blacklist";
        public static String WHITELIST = "whitelist";
        public static String LOCKLIST = "locklist";
    }

    public static class StakedTAPTokenSnapshots {
        public Address address;
        public BigInteger amount;
        public BigInteger day;
    }

    public static class TotalStakedTAPTokenSnapshots {
        public BigInteger amount;
        public BigInteger day;
    }

    protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);

    public static final String TAG = "TapToken";

    private final VarDB<BigInteger> totalSupply = Context.newVarDB(TOTAL_SUPPLY, BigInteger.class);
    //this variable is defined as int in the icon samples
    private final VarDB<BigInteger> decimals = Context.newVarDB(DECIMALS, BigInteger.class);
    private final ArrayDB<Address> addresses = Context.newArrayDB(ADDRESSES, Address.class);

    private final DictDB<Address, BigInteger> balances = Context.newDictDB(BALANCES, BigInteger.class);

    private final ArrayDB<Address> evenDayChanges = Context.newArrayDB(EVEN_DAY_CHANGES, Address.class);
    private final ArrayDB<Address> oddDayChanges = Context.newArrayDB(ODD_DAY_CHANGES, Address.class);

    private final List<ArrayDB<Address>> changes = List.of(evenDayChanges, oddDayChanges);

    private final VarDB<Integer> maxLoop = Context.newVarDB(MAX_LOOPS, Integer.class);
    private final VarDB<BigInteger> indexUpdateBalance = Context.newVarDB(INDEX_UPDATE_BALANCE, BigInteger.class);
    private final VarDB<BigInteger> indexAddressChanges = Context.newVarDB(INDEX_ADDRESS_CHANGES, BigInteger.class);

    private final VarDB<BigInteger> balanceUpdateDb = Context.newVarDB(BALANCE_UPDATE_DB, BigInteger.class);
    private final VarDB<Integer> addressUpdateDb = Context.newVarDB(ADDRESS_UPDATE_DB, Integer.class);

    private final VarDB<Address> dividendsScore = Context.newVarDB(DIVIDENDS_SCORE, Address.class);
    private final VarDB<Address> governanceScore = Context.newVarDB(GOVERNANCE_SCORE, Address.class);
    private final ArrayDB<Address> blacklistAddress = Context.newArrayDB(BLACKLIST_ADDRESS, Address.class);

    //TODO : Example 2) Two-depth dict (test_dict2[‘key1’][‘key2’]):
    //lets dig into how it is used self._STAKED_BALANCES, db, value_type=int, depth=2
    //verify if BranchDB can support multiples Dicdb keys like thousands, else we will need to go back to old impl or even a tricky data structure impl
    //let's check also if this is a valid convertion from Two-depth dict py to branch db java class when updating from py to java
    private final BranchDB<Address, DictDB<Integer, BigInteger>> stakedBalances = Context.newBranchDB(STAKED_BALANCES, BigInteger.class);
    private final VarDB<BigInteger> minimumStake = Context.newVarDB(MINIMUM_STAKE, BigInteger.class);
    private final VarDB<BigInteger> unstakingPeriod = Context.newVarDB(UNSTAKING_PERIOD, BigInteger.class);
    private final VarDB<BigInteger> totalStakedBalance = Context.newVarDB(TOTAL_STAKED_BALANCE, BigInteger.class);

    private final ArrayDB<Address> evenDayStakeChanges = Context.newArrayDB(EVEN_DAY_STAKE_CHANGES, Address.class);
    private final ArrayDB<Address> oddDayStakeChanges = Context.newArrayDB(ODD_DAY_STAKE_CHANGES, Address.class);

    private final List<ArrayDB<Address>> stakeChanges = List.of(evenDayStakeChanges, oddDayStakeChanges);

    private final VarDB<BigInteger> indexUpdateStake = Context.newVarDB(INDEX_UPDATE_STAKE, BigInteger.class);
    private final VarDB<BigInteger> indexStakeAddressChanges = Context.newVarDB(INDEX_STAKE_ADDRESS_CHANGES, BigInteger.class);

    // To choose between even and odd DBs
    private final VarDB<BigInteger> stakeUpdateDb = Context.newVarDB(STAKE_UPDATE_DB, BigInteger.class);

    //very tricky tricky and not usable var, it just store indexes for an array.
    private final VarDB<Integer> stakeAddressUpdateDb = Context.newVarDB(STAKE_ADDRESS_UPDATE_DB, Integer.class);

    private final VarDB<Boolean> stakingEnabled = Context.newVarDB(STAKING_ENABLED, Boolean.class);
    private final VarDB<Boolean> switchDivsToStakedTapEnabled = Context.newVarDB(SWITCH_DIVS_TO_STAKED_TAP_ENABLED, Boolean.class);

    // Pausing and locklist, whitelist implementations
    private final VarDB<Boolean> paused = Context.newVarDB(PAUSED, Boolean.class);
    private final ArrayDB<Address> pauseWhitelist = Context.newArrayDB(PAUSE_WHITELIST, Address.class);
    private final ArrayDB<Address> locklist = Context.newArrayDB(LOCKLIST, Address.class);

    public TapToken(BigInteger _initialSupply, BigInteger _decimals) {
        //we mimic on_update py feature, updating java score will call <init> (constructor) method


        if (this.totalSupply.get() == null) {
            if (_initialSupply == null || _initialSupply.compareTo(ZERO) < 0) {
                Context.revert("Initial supply cannot be less than zero");
            }

            if (_decimals == null || _decimals.compareTo(ZERO) < 0) {
                Context.revert("Decimals cannot be less than zero");
            }

            BigInteger totalSupply = _initialSupply.multiply( pow( TEN , _decimals.intValue()) );
            Context.println(TAG+" : total_supply "+ totalSupply );

            this.totalSupply.set(totalSupply);
            this.decimals.set(_decimals);
            this.balances.set(Context.getOwner(), totalSupply);
            this.addresses.add(Context.getOwner());
        }
    }

    @Override
    @EventLog(indexed = 3)
    public void Transfer(Address _from, Address _to, BigInteger _value, byte[] _data) {
    }

    @EventLog(indexed = 1)
    protected void LocklistAddress(Address address, String note) {
    }

    @EventLog(indexed = 1)
    protected void WhitelistAddress(Address address, String note) {
    }

    @EventLog(indexed = 1)
    protected void BlacklistAddress(Address address, String note) {
    }

    @EventLog(indexed = 2)
    public void TapStaked(Address address, BigInteger _value, String note) {
    }

    @Override
    @External(readonly = true)
    public String name() {
        return TAG;
    }

    @Override
    @External(readonly = true)
    public String symbol() {
        return "TAP";
    }

    @Override
    @External(readonly = true)
    public BigInteger decimals() {
        return this.decimals.getOrDefault(ZERO);
    }

    @Override
    @External(readonly = true)
    public BigInteger totalSupply() {
        return this.totalSupply.getOrDefault(ZERO);
    }

    @Override
    @External(readonly = true)
    public BigInteger balanceOf(Address _owner) {
        return this.balances.getOrDefault(_owner, ZERO);
    }

    @External(readonly = true)
    public BigInteger available_balance_of(Address _owner) {
        var detailBalance = details_balanceOf(_owner);
        if (detailBalance.containsKey("Available balance")) {
            return detailBalance.get("Available balance");
        } else {
            return ZERO;
        }
    }

    @External(readonly = true)
    public BigInteger staked_balanceOf(Address _owner) {
        return this.stakedBalances.at(_owner).getOrDefault(Status.STAKED, ZERO);
    }

    @External(readonly = true)
    public BigInteger unstaked_balanceOf(Address _owner) {
        Map<String, BigInteger> detailBalance = details_balanceOf(_owner);
        if (detailBalance.containsKey("Unstaking balance")) {
            return detailBalance.get("Unstaking balance");
        } else {
            return ZERO;
        }
    }

    @External(readonly = true)
    public BigInteger total_staked_balance() {
        return this.totalStakedBalance.getOrDefault(ZERO);
    }

    @External(readonly = true)
    public boolean staking_enabled() {
        return this.stakingEnabled.getOrDefault(false);
    }

    @External(readonly = true)
    public boolean switch_divs_to_staked_tap_enabled() {
        return this.switchDivsToStakedTapEnabled.getOrDefault(false);
    }

    @External(readonly = true)
    public boolean getPaused() {
        return this.paused.getOrDefault(false);
    }

    @External(readonly = true)
    public Map<String, BigInteger> details_balanceOf(Address _owner) {

        //Context.getBlockTimestamp() -- > self.now()
        BigInteger currUnstaked = ZERO;
        DictDB<Integer, BigInteger> sb = this.stakedBalances.at(_owner);
        if (sb.getOrDefault(Status.UNSTAKING_PERIOD, ZERO).compareTo(BigInteger.valueOf(Context.getBlockTimestamp())) < 0) {
            currUnstaked = sb.getOrDefault(Status.UNSTAKING, ZERO);
        }

        BigInteger availableBalance;
        if (this.firstTime(_owner)) {
            availableBalance = this.balanceOf(_owner);
        } else {
            availableBalance = sb.getOrDefault(Status.AVAILABLE, ZERO);
        }

        //TODO: possible negative value scenario in py?
        BigInteger unstakingAmount = sb.getOrDefault(Status.UNSTAKING, ZERO);
        if (unstakingAmount.compareTo(ZERO) > 0) {
            unstakingAmount = unstakingAmount.subtract(currUnstaked);
        }

        BigInteger unstakingTime = ZERO;
        if (!unstakingAmount.equals(ZERO)) {
            unstakingTime = sb.getOrDefault(Status.UNSTAKING_PERIOD, ZERO);
        }

        return Map.of(
                "Total balance", this.balances.getOrDefault(_owner, ZERO),
                "Available balance", availableBalance.add(currUnstaked),
                "Staked balance", sb.getOrDefault(Status.STAKED, ZERO),
                "Unstaking balance", unstakingAmount,
                "Unstaking time (in microseconds)", unstakingTime);

    }

    private boolean firstTime(Address from) {
        DictDB<Integer, BigInteger> sb = this.stakedBalances.at(from);
        return
                ZERO.equals(sb.getOrDefault(Status.AVAILABLE, ZERO))
                        && ZERO.equals(sb.getOrDefault(Status.STAKED, ZERO))
                        && ZERO.equals(sb.getOrDefault(Status.UNSTAKING, ZERO))
                        && this.balances.getOrDefault(from, ZERO).compareTo(ZERO) != 0;
    }

    private void checkFirstTime(Address from) {
        //If first time copy the balance to available staked balances
        if (this.firstTime(from)) {
            this.stakedBalances.at(from).set(Status.AVAILABLE, this.balances.getOrDefault(from, ZERO));
        }
    }

    private void stakingEnabledOnly() {
        boolean enabled = this.stakingEnabled.getOrDefault(false);
        Context.println("staking enabled? : " + enabled);
        if (!enabled) {
            Context.revert("Staking must first be enabled.");
        }
    }

    private void switchDivsToStakedTapEnabledOnly() {
        if (!this.switchDivsToStakedTapEnabled.getOrDefault(false)) {
            Context.revert("Switching to dividends for staked tap has to be enabled.");
        }
    }

    @External
    public void toggle_staking_enabled() {
        this.ownerOnly();
        this.stakingEnabled.set(!this.stakingEnabled.getOrDefault(false));
        Context.println("enabled staking?: " + this.stakingEnabled.get());
    }

    @External
    public void toggle_switch_divs_to_staked_tap_enabled() {
        this.ownerOnly();
        this.switchDivsToStakedTapEnabled.set(!this.switchDivsToStakedTapEnabled.getOrDefault(false));
    }

    @External
    public void togglePaused() {
        this.ownerOnly();
        this.paused.set(!this.paused.getOrDefault(false));
    }

    private void notContract(Address address){
        Context.require(!address.isContract(), TAG + ": Cannot call this method by a contract");
    }

    @External
    public void stake(BigInteger _value) {
        this.stakingEnabledOnly();

        Address from = Context.getCaller();
        notContract(from);

        if (_value.compareTo(ZERO) < 0) {
            Context.revert("Staked TAP value can't be less than zero");
        }

        if (_value.compareTo(
                this.balances.getOrDefault(from, ZERO)) > 0) {
            Context.revert("Out of TAP balance");
        }

        if (_value.compareTo(this.minimumStake.getOrDefault(ZERO)) < 0
                && _value.compareTo(ZERO) != 0) {
            Context.revert("Staked TAP must be greater than the minimum stake amount and non zero");
        }
        this.checkFirstTime(from);
        // Check if the unstaking period has already been reached.
        this.makeAvailable(from);

        if (containsInArrayDb(from, locklist)) {
            Context.revert("Locked address not permitted to stake.");
        }

        DictDB<Integer, BigInteger> sb = this.stakedBalances.at(from);
        BigInteger oldStake = sb.getOrDefault(Status.STAKED, ZERO).add(sb.getOrDefault(Status.UNSTAKING, ZERO));
        //big integer is immutable, not need this next line
        BigInteger newStake = _value;

        BigInteger stakeIncrement = _value.subtract(sb.getOrDefault(Status.STAKED, ZERO));
        BigInteger unstakeAmount = ZERO;
        if (newStake.compareTo(oldStake) > 0) {
            BigInteger offset = newStake.subtract(oldStake);
            sb.set(Status.AVAILABLE, sb.getOrDefault(Status.AVAILABLE, ZERO).subtract(offset));
        } else {
            unstakeAmount = oldStake.subtract(newStake);
        }

        sb.set(Status.STAKED, _value);
        sb.set(Status.UNSTAKING, unstakeAmount);
        sb.set(Status.UNSTAKING_PERIOD, BigInteger.valueOf(Context.getBlockTimestamp())
                .add(this.unstakingPeriod.getOrDefault(ZERO)));
        this.totalStakedBalance.set(this.totalStakedBalance.getOrDefault(ZERO).add(stakeIncrement));

        ArrayDB<Address> stakeAddressChanges = this.stakeChanges.get(this.stakeAddressUpdateDb.getOrDefault(0));
        stakeAddressChanges.add(from);

        checkMigration();

        Snapshot snapshot = new Snapshot();
        if (snapshot._enable_snapshots.getOrDefault(Boolean.FALSE)) {
            updateSnapshotForAddress(Context.getCaller(), _value);
            updateTotalStakedSnapshot(this.totalStakedBalance.getOrDefault(ZERO));
        }
        TapStaked(Context.getCaller(), _value, _value + " TAP token is staked by " + Context.getCaller());
    }

    @Override
    @External
    public void transfer(Address _to, BigInteger _value, @Optional byte[] _data) {
        //TODO: review all the loops that are use for searching
        LinearComplexityMigration linearComplexityMigration = new LinearComplexityMigration();
        if (this.paused.getOrDefault(Boolean.FALSE)) {
            if (linearComplexityMigration.linear_complexity_migration_complete.getOrDefault(ArrayDbToMigrate.WHITELIST, Boolean.FALSE)) {
                if (linearComplexityMigration._pause_whitelist_index.getOrDefault(Context.getCaller(), 0) == 0) {
                    Context.revert("TAP token transfers are paused");
                }
            } else {
                if (containsInArrayDb(Context.getCaller(), this.pauseWhitelist)) {
                    Context.revert("TAP token transfers are paused");
                }
            }
        }
        if (linearComplexityMigration.linear_complexity_migration_complete.getOrDefault(ArrayDbToMigrate.LOCKLIST, Boolean.FALSE)) {
            if (linearComplexityMigration._locklist_index.getOrDefault(Context.getCaller(), 0) > 0) {
                Context.revert("Transfer of TAP has been locked for this address.");
            }
        } else {
            if (containsInArrayDb(Context.getCaller(), this.locklist)) {
                Context.revert("Transfer of TAP has been locked for this address.");
            }
        }

        if (_data == null) {
            _data = "None".getBytes();
        }
        _transfer(Context.getCaller(), _to, _value, _data);
    }

    private void _transfer(Address from, Address to, BigInteger value, byte[] data) {

        // Checks the sending value and balance.
        if (value == null || value.compareTo(ZERO) < 0) {
            Context.revert("Transferring value cannot be less than zero");
        }

        BigInteger balanceFrom = this.balances.getOrDefault(from, ZERO);
        if (balanceFrom.compareTo(value) < 0) {
            Context.revert("Out of balance");
        }

        this.checkFirstTime(from);
        this.checkFirstTime(to);
        this.makeAvailable(to);
        this.makeAvailable(from);

        DictDB<Integer, BigInteger> sbFrom = this.stakedBalances.at(from);
        DictDB<Integer, BigInteger> sbTo = this.stakedBalances.at(to);

        if (sbFrom.getOrDefault(Status.AVAILABLE, ZERO).compareTo(value) < 0) {
            Context.revert("Out of available balance");
        }

        this.balances.set(from, balanceFrom.subtract(value));

        BigInteger balanceTo = this.balances.getOrDefault(to, ZERO);
        this.balances.set(to, balanceTo.add(value));
        Context.println("new balance of 'to' ( " + to + "): " + this.balances.get(to));

        sbFrom.set(Status.AVAILABLE, sbFrom.getOrDefault(Status.AVAILABLE, ZERO).subtract(value));
        sbTo.set(Status.AVAILABLE, sbTo.getOrDefault(Status.AVAILABLE, ZERO).add(value));

        if (!containsInArrayDb(to, this.addresses)) {
            this.addresses.add(to);
        }

        if (to.isContract()) {
            // If the recipient is SCORE,
            //   then calls `tokenFallback` to hand over control.
            Context.call(to, "tokenFallback", from, value, data);
        }

        // Emits an event log `Transfer`
        Context.println("Emit an event log Transfer from " + from + " to " + to);
        this.Transfer(from, to, value, data);
        LinearComplexityMigration linearComplexityMigration = new LinearComplexityMigration();
        if (!this.switchDivsToStakedTapEnabled.getOrDefault(false)) {
            ArrayDB<Address> addressChanges = this.changes.get(this.addressUpdateDb.getOrDefault(0));
            if (linearComplexityMigration.linear_complexity_migration_complete.getOrDefault(ArrayDbToMigrate.BLACKLIST, Boolean.FALSE)) {
                if (linearComplexityMigration._blacklist_address_index.getOrDefault(from, 0) == 0) {
                    addressChanges.add(from);
                    linearComplexityMigration._blacklist_address_index.set(from, addressChanges.size());
                }
                if (linearComplexityMigration._blacklist_address_index.getOrDefault(to, 0) == 0) {
                    addressChanges.add(to);
                    linearComplexityMigration._blacklist_address_index.set(to, addressChanges.size());
                }
            } else {
                if (!containsInArrayDb(from, this.blacklistAddress)) {
                    addressChanges.add(from);
                }
                if (!containsInArrayDb(to, this.blacklistAddress)) {
                    addressChanges.add(to);
                }
            }
        }
        Context.println("Transfer({" + from + "}, {" + to + "}, {" + value + "}, {" + data + ")" + TAG);
        checkMigration();
    }

    private void ownerOnly() {
        //this method works for first time call, test this scenario
        if (!Context.getCaller().equals(Context.getOwner())) {
            Context.revert("Only owner can call this method");
        }
    }

    private void governanceOnly() {
        Context.require(Context.getCaller().equals(this.governanceScore.get()), TAG + ": Only Governance Score can call this method.");
    }

    private void ownerOrGovernanceOnly(){
        Address caller = Context.getCaller();
        Context.require(caller.equals(Context.getOwner()) || caller.equals(get_authorization_score()), TAG + ": Only owner or Governance Score can call this method");
    }

    private void dividendsOnly() {
        if (!Context.getCaller().equals(this.dividendsScore.getOrDefault(ZERO_ADDRESS))) {
            Context.revert("This method can only be called by the dividends distribution contract");
        }
    }

    private void makeAvailable(Address from) {
        // Check if the unstaking period has already been reached.

        DictDB<Integer, BigInteger> sb = this.stakedBalances.at(from);

        if (sb.getOrDefault(Status.UNSTAKING_PERIOD, ZERO).compareTo(BigInteger.valueOf(Context.getBlockTimestamp())) <= 0) {
            BigInteger currUnstaked = sb.getOrDefault(Status.UNSTAKING, ZERO);
            sb.set(Status.UNSTAKING, ZERO);
            sb.set(Status.AVAILABLE, sb.getOrDefault(Status.AVAILABLE, ZERO).add(currUnstaked));
        }
    }

    @External
    public void set_minimum_stake(BigInteger _amount) {
		/*
        Set the minimum stake amount
        :param _amount: Minimum amount of stake needed.
		 */
        governanceOnly();
        if (_amount == null || _amount.compareTo(ZERO) < 0) {
            Context.revert("Amount cannot be less than zero");
        }

        BigInteger totalAmount = _amount.multiply(pow(TEN, this.decimals.get().intValue()));
        this.minimumStake.set(totalAmount);
    }


    /*
    Set the minimum staking period
    :param _time: Staking time period in days.
     */
    @External
    public void set_unstaking_period(BigInteger _time) {

        governanceOnly();
        if (_time == null || _time.compareTo(ZERO) < 0) {
            Context.revert("Time cannot be negative.");
        }
        BigInteger totalTime = _time.multiply(DAY_TO_MICROSECOND);  // convert days to microseconds
        this.unstakingPeriod.set(totalTime);
    }

    /*
    Set the maximum number a for loop can run for any operation
    :param _loops: Maximum number of for loops allowed
    :return:
     */
    @External
    public void set_max_loop(@Optional int _loops) {
        governanceOnly();
        if (_loops == 0) {
            _loops = 100;
        }
        this.maxLoop.set(_loops);
    }

    /*
    Returns the minimum stake amount
     */
    @External(readonly = true)
    public BigInteger get_minimum_stake() {
        return this.minimumStake.get();
    }

    /*
    Returns the minimum staking period in days
     */
    @External(readonly = true)
    public BigInteger get_unstaking_period() {
        BigInteger timeInMicroseconds = this.unstakingPeriod.get();
        return timeInMicroseconds.divide(DAY_TO_MICROSECOND);
    }

    /*
    Returns the maximum number of for loops allowed in the score
    :return:
     */
    @External(readonly = true)
    public int get_max_loop() {
        return this.maxLoop.get();
    }

    /*
    Sets the dividends score address. The function can only be invoked by score owner.
    :param _score: Score address of the dividends contract
    :type _score: :class:`iconservice.base.address.Address`
     */
    @External
    public void set_dividends_score(Address _score) {
        this.ownerOnly();
        this.dividendsScore.set(_score);
    }

    @External
    public void set_authorization_score(Address _score) {
        this.ownerOnly();
        this.governanceScore.set(_score);
    }


    /*
    Returns the roulette score address.
   :return: Address of the roulette score
   :rtype: :class:`iconservice.base.address.Address`
     */
    @External(readonly = true)
    public Address get_dividends_score() {
        return this.dividendsScore.get();
    }

    @External(readonly = true)
    public Address get_authorization_score() {
        return this.governanceScore.get();
    }

    /*
    Clears the array db storing yesterday's changes
    :return: True if the array has been emptied
     */
    @External
    public boolean clear_yesterdays_changes() {
        this.dividendsOnly();
        int yesterday = (this.addressUpdateDb.getOrDefault(0) + 1) % 2;
        ArrayDB<Address> yesterdaysChanges = this.changes.get(yesterday);
        int lengthList = yesterdaysChanges.size();
        if (lengthList == 0) {
            return true;
        }

        int loopCount = Math.min(lengthList, this.maxLoop.getOrDefault(0));
        for (int i = 0; i < loopCount; i++) {
            yesterdaysChanges.pop();
        }

        return yesterdaysChanges.size() <= 0;
    }

    /*
    Returns all the blacklisted addresses(rewards score address and devs team address)
    :return: List of blacklisted address
    :rtype: list
     */
    @External(readonly = true)
    public List<Address> get_blacklist_addresses() {

        return arrayDbToList(this.blacklistAddress);
    }

    /*
    Removes the address from blacklist.
    Only owner can remove the blacklist address
    :param _address: Address to be removed from blacklist
    :type _address: :class:`iconservice.base.address.Address`
    :return:
     */
    @External
    public void remove_from_blacklist(Address _address) {
        LinearComplexityMigration linearComplexityMigration = new LinearComplexityMigration();
        governanceOnly();
        if (linearComplexityMigration.linear_complexity_migration_complete.getOrDefault(ArrayDbToMigrate.BLACKLIST, Boolean.FALSE)) {
            if (linearComplexityMigration._blacklist_address_index.getOrDefault(_address, 0) == 0) {
                Context.revert(_address + " not in blacklist address");
            }
        } else {
            if (!containsInArrayDb(_address, this.blacklistAddress)) {
                //TODO: check if toString produces a s;tring representation or a java object string
                Context.revert(_address + " not in blacklist address");
            }
        }
        this.BlacklistAddress(_address, "Removed from blacklist");
        Address top = this.blacklistAddress.pop();

        if (linearComplexityMigration.linear_complexity_migration_complete.getOrDefault(ArrayDbToMigrate.BLACKLIST, Boolean.FALSE)) {
            int i = linearComplexityMigration._blacklist_address_index.getOrDefault(_address, 0);
            this.blacklistAddress.set(i, top);
            linearComplexityMigration._blacklist_address_index.set(top, i);
            linearComplexityMigration._blacklist_address_index.set(_address, 0);
        } else {
            if (!top.equals(_address)) {
                for (int i = 0; i < this.blacklistAddress.size(); i++) {
                    if (this.blacklistAddress.get(i).equals(_address)) {
                        this.blacklistAddress.set(i, top);
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
    public void set_blacklist_address(Address _address) {
        ownerOrGovernanceOnly();
        LinearComplexityMigration linearComplexityMigration = new LinearComplexityMigration();
        this.BlacklistAddress(_address, "Added to Blacklist");
        if (linearComplexityMigration.linear_complexity_migration_complete.getOrDefault(ArrayDbToMigrate.BLACKLIST, Boolean.FALSE)) {
            if (linearComplexityMigration._blacklist_address_index.getOrDefault(_address, 0) == 0) {
                this.blacklistAddress.add(_address);
                linearComplexityMigration._blacklist_address_index.set(_address, this.blacklistAddress.size());
            }
        } else {
            if (!containsInArrayDb(_address, this.blacklistAddress)) {
                this.blacklistAddress.add(_address);
            }
        }
    }

    /*
    Switches the day when the distribution has to be started
    :return:
     */
    @External
    public void switch_address_update_db() {
        this.dividendsOnly();
        int newDay = (this.addressUpdateDb.getOrDefault(0) + 1) % 2;
        this.addressUpdateDb.set(newDay);
        ArrayDB<Address> addressChanges = this.changes.get(newDay);
        this.indexAddressChanges.set(BigInteger.valueOf(addressChanges.size()));
    }

    /*
    Returns the updated addresses. Returns empty dictionary if the updates has
    completed
    :return: Dictionary contains the addresses. Maximum number of addresses
    and balances returned is defined by the max_loop
     */
    @External
    public Map<String, BigInteger> get_stake_updates() {
        this.dividendsOnly();
        this.stakingEnabledOnly();
        this.switchDivsToStakedTapEnabledOnly();

        ArrayDB<Address> stakeChanges = this.stakeChanges.get(this.stakeUpdateDb.getOrDefault(ZERO).intValue());
        int lengthList = stakeChanges.size();

        int start = this.indexUpdateStake.getOrDefault(ZERO).intValue();
        if (start == lengthList) {
            if (this.stakeUpdateDb.getOrDefault(ZERO).intValue() !=
                    this.stakeAddressUpdateDb.getOrDefault(0)) {
                this.stakeUpdateDb.set(BigInteger.valueOf(this.stakeAddressUpdateDb.getOrDefault(0)));
                this.indexUpdateStake.set(this.indexStakeAddressChanges.getOrDefault(ZERO));
            }
            return Map.of();
        }
        int end = Math.min(start + this.maxLoop.getOrDefault(0), lengthList);

        @SuppressWarnings("unchecked")
        Map.Entry<String, BigInteger>[] entries = new Map.Entry[end - start];
        //TODO: validate this logic
        int j = 0;
        for (int i = start; i < end; i++) {
            Address stakeChangesAtIndex = stakeChanges.get(i);
            entries[j] = Map.entry(stakeChangesAtIndex.toString(), this.staked_balanceOf(stakeChangesAtIndex));
            j++;
        }
        this.indexUpdateStake.set(BigInteger.valueOf(end));
        Context.println("Stake updates: " + Map.ofEntries(entries));
        return Map.ofEntries(entries);
    }

    @External
    public boolean clear_yesterdays_stake_changes() {
        this.stakingEnabledOnly();
        this.switchDivsToStakedTapEnabledOnly();
        this.dividendsOnly();
        int yesterday = (this.stakeAddressUpdateDb.getOrDefault(0) + 1) % 2;
        ArrayDB<Address> yesterdaysChanges = this.stakeChanges.get(yesterday);
        int lengthList = yesterdaysChanges.size();
        if (lengthList == 0) {
            return true;
        }
        int loopCount = Math.min(lengthList, this.maxLoop.getOrDefault(0));
        for (int i = 0; i < loopCount; i++) {
            yesterdaysChanges.pop();
        }
        return yesterdaysChanges.size() <= 0;
    }

    @External
    public void switch_stake_update_db() {
        this.dividendsOnly();
        this.stakingEnabledOnly();
        this.switchDivsToStakedTapEnabledOnly();

        int newDay = (this.stakeAddressUpdateDb.getOrDefault(0) + 1) % 2;
        this.stakeAddressUpdateDb.set(newDay);
        ArrayDB<Address> stakeChanges = this.stakeChanges.get(newDay);
        this.indexStakeAddressChanges.set(BigInteger.valueOf(stakeChanges.size()));
    }

    /*
    Returns all locked addresses.
    :return: List of locked addresses
    :rtype: list
     */
    @External(readonly = true)
    public List<Address> get_locklist_addresses() {

        return arrayDbToList(this.locklist);
    }

    /*
    Removes the address from the locklist.
    Only owner can remove the locklist address
    :param _address: Address to be removed from locklist
    :type _address: :class:`iconservice.base.address.Address`
    :return:
     */
    @External
    public void remove_from_locklist(Address _address) {
        governanceOnly();
        LinearComplexityMigration linearComplexityMigration = new LinearComplexityMigration();
        if (linearComplexityMigration.linear_complexity_migration_complete.getOrDefault(ArrayDbToMigrate.LOCKLIST, Boolean.FALSE)) {
            if (linearComplexityMigration._locklist_index.getOrDefault(_address, 0) == 0) {
                Context.revert(_address + " not in locklist address");
            }
        } else {
            if (!containsInArrayDb(_address, this.locklist)) {
                //TODO: check if toString produces a s;tring representation or a java object string
                Context.revert(_address + " not in locklist address");
            }
        }
        this.LocklistAddress(_address, "Removed from Locklist");
        Address top = this.locklist.pop();
        if (linearComplexityMigration.linear_complexity_migration_complete.getOrDefault(ArrayDbToMigrate.LOCKLIST, Boolean.FALSE)) {
            int i = linearComplexityMigration._locklist_index.getOrDefault(_address, 0);
            this.locklist.set(i, top);
            linearComplexityMigration._locklist_index.set(top, i);
            linearComplexityMigration._locklist_index.set(_address, 0);
        } else {
            if (!top.equals(_address)) {
                for (int i = 0; i < this.locklist.size(); i++) {
                    if (this.locklist.get(i).equals(_address)) {
                        this.locklist.set(i, top);
                    }
                }
            }
        }
    }

    /*
    Add address to list of addresses that cannot transfer TAP.
    Only the owner can set the locklist address
    :param _address: Address to be included in the locklist
    :type _address: :class:`iconservice.base.address.Address`
    :return:
     */
    @External
    public void set_locklist_address(Address _address) {
        ownerOrGovernanceOnly();
        this.stakingEnabledOnly();
        LinearComplexityMigration linearComplexityMigration = new LinearComplexityMigration();

        this.LocklistAddress(_address, "Added to Locklist");
        if (linearComplexityMigration.linear_complexity_migration_complete.getOrDefault(ArrayDbToMigrate.LOCKLIST, Boolean.FALSE)) {
            if (linearComplexityMigration._locklist_index.getOrDefault(_address, 0) == 0) {
                this.locklist.add(_address);
                linearComplexityMigration._locklist_index.set(_address, this.locklist.size());
            }
        } else {
            if (!containsInArrayDb(_address, this.locklist)) {
                this.locklist.add(_address);
            }
        }

        // Unstake TAP of locklist address

        BigInteger stakedBalance = this.stakedBalances.at(_address).getOrDefault(Status.STAKED, ZERO);
        if (stakedBalance.compareTo(ZERO) > 0) {
            // Check if the unstaking period has already been reached.
            this.makeAvailable(_address);
            DictDB<Integer, BigInteger> sb = this.stakedBalances.at(_address);
            sb.set(Status.STAKED, ZERO);
            sb.set(Status.UNSTAKING, sb.get(Status.UNSTAKING).add(stakedBalance));
            sb.set(Status.UNSTAKING_PERIOD, this.unstakingPeriod.get().add(BigInteger.valueOf(Context.getBlockTimestamp())));
            this.totalStakedBalance.set(this.totalStakedBalance.getOrDefault(ZERO).subtract(stakedBalance));
            ArrayDB<Address> stakeAddressChanges = this.stakeChanges.get(this.stakeAddressUpdateDb.get());
            stakeAddressChanges.add(_address);
        }
    }

    /*
    Returns all addresses whitelisted during pause.
    :return: List of whitelisted addresses
    :rtype: list
     */
    @External(readonly = true)
    public List<Address> get_whitelist_addresses() {

        return arrayDbToList(this.pauseWhitelist);
    }

    /*
    Removes the address from whitelist.
    Only owner can remove the whitelist address
    :param _address: Address to be removed from whitelist
    :type _address: :class:`iconservice.base.address.Address`
    :return:
     */
    @External
    public void remove_from_whitelist(Address _address) {
        governanceOnly();
        LinearComplexityMigration linearComplexityMigration = new LinearComplexityMigration();
        if (linearComplexityMigration.linear_complexity_migration_complete.getOrDefault(ArrayDbToMigrate.WHITELIST, Boolean.FALSE)) {
            if (linearComplexityMigration._pause_whitelist_index.getOrDefault(_address, 0) == 0) {
                Context.revert(_address + " not in whitelist address");
            }
        } else {
            if (!containsInArrayDb(_address, this.pauseWhitelist)) {
                Context.revert(_address + " not in whitelist address");
            }
        }
        this.WhitelistAddress(_address, "Removed from whitelist");
        Address top = this.pauseWhitelist.pop();
        if (linearComplexityMigration.linear_complexity_migration_complete.getOrDefault(ArrayDbToMigrate.WHITELIST, Boolean.FALSE)) {
            int i = linearComplexityMigration._pause_whitelist_index.get(_address);
            this.pauseWhitelist.set(i, top);
            linearComplexityMigration._pause_whitelist_index.set(top, i);
            linearComplexityMigration._pause_whitelist_index.set(_address, 0);
        } else {
            if (!top.equals(_address)) {
                for (int i = 0; i < this.pauseWhitelist.size(); i++) {
                    if (this.pauseWhitelist.get(i).equals(_address)) {
                        this.pauseWhitelist.set(i, top);
                    }
                }
            }
        }
    }

    /*
    Add address to list of addresses exempt from transfer pause.
    Only the owner can set the whitelist address
    :param _address: Address to be included in the whitelist
    :type _address: :class:`iconservice.base.address.Address`
    :return:
     */
    @External
    public void set_whitelist_address(Address _address) {
        ownerOrGovernanceOnly();
        this.WhitelistAddress(_address, "Added to Pause Whitelist");
        LinearComplexityMigration linearComplexityMigration = new LinearComplexityMigration();
        if (linearComplexityMigration.linear_complexity_migration_complete.getOrDefault(ArrayDbToMigrate.WHITELIST, Boolean.FALSE)) {
            if (linearComplexityMigration._pause_whitelist_index.getOrDefault(_address, 0) == 0) {
                this.pauseWhitelist.add(_address);
                linearComplexityMigration._pause_whitelist_index.set(_address, this.pauseWhitelist.size());
            }
        } else {
            if (!containsInArrayDb(_address, this.pauseWhitelist)) {
                this.pauseWhitelist.add(_address);
            }
        }
    }

    @External
    public void startLinearComplexityMigration() {
        this.ownerOnly();
        LinearComplexityMigration linearComplexityMigration = new LinearComplexityMigration();
        linearComplexityMigration.linear_complexity_migration_start.set(Boolean.TRUE);
    }

    private void migrateFromLinearComplexity(ArrayDB<Address> fromArray, DictDB<Address, Integer> toDict, String arrayName) {
        int count = this.maxLoop.get();
        int length = fromArray.size();
        LinearComplexityMigration linearComplexityMigration = new LinearComplexityMigration();

        int start = linearComplexityMigration.linear_complexity_migration_index.get(arrayName);
        int remainingAddresses = length - start;
        if (count > remainingAddresses) {
            count = remainingAddresses;
        }
        int end = start + count;
        Context.println("Migrating " + arrayName + ":: start: " + start + " end: " + end);
        for (int i = start; i < end; i++) {
            Address address = fromArray.get(i);
            if (toDict.getOrDefault(address, 0) == 0) {
                toDict.set(address, i + 1);
            }
        }
        if (end == length) {
            linearComplexityMigration.linear_complexity_migration_complete.set(arrayName, Boolean.TRUE);
        } else {
            linearComplexityMigration.linear_complexity_migration_index.set(arrayName, start + count);
        }
    }

    private void checkMigration() {
        LinearComplexityMigration linearComplexityMigration = new LinearComplexityMigration();
        if (linearComplexityMigration.linear_complexity_migration_start.getOrDefault(Boolean.FALSE)) {
            if (!linearComplexityMigration.linear_complexity_migration_complete.getOrDefault(ArrayDbToMigrate.LOCKLIST, Boolean.FALSE)) {
                migrateFromLinearComplexity(this.locklist, linearComplexityMigration._locklist_index, ArrayDbToMigrate.LOCKLIST);
            }
            if (!linearComplexityMigration.linear_complexity_migration_complete.getOrDefault(ArrayDbToMigrate.BLACKLIST, Boolean.FALSE)) {
                migrateFromLinearComplexity(this.blacklistAddress, linearComplexityMigration._blacklist_address_index, ArrayDbToMigrate.BLACKLIST);
            }
            if (!linearComplexityMigration.linear_complexity_migration_complete.getOrDefault(ArrayDbToMigrate.WHITELIST, Boolean.FALSE)) {
                migrateFromLinearComplexity(this.pauseWhitelist, linearComplexityMigration._pause_whitelist_index, ArrayDbToMigrate.WHITELIST);
            }
        }
    }

    @External
    public void toggleEnableSnapshot() {
        ownerOnly();
        Snapshot snapshot = new Snapshot();
        if (!snapshot._enable_snapshots.getOrDefault(Boolean.FALSE) && snapshot._time_offset.getOrDefault(ZERO).equals(ZERO)) {
            setTimeOffset();
        }
        snapshot._enable_snapshots.set(!snapshot._enable_snapshots.getOrDefault(Boolean.FALSE));
    }

    @External(readonly = true)
    public boolean getSnapshotEnabled() {
        Snapshot snapshot = new Snapshot();
        return snapshot._enable_snapshots.get();
    }

    private void setTimeOffset() {

        Snapshot snapshot = new Snapshot();
        snapshot._time_offset.set(BigInteger.valueOf(Context.getBlockTimestamp()));
    }

    @External(readonly = true)
    public BigInteger getTimeOffset() {
        Snapshot snapshot = new Snapshot();
        return snapshot._time_offset.get();
    }

    @External(readonly = true)
    public BigInteger getDay() {
        Snapshot snapshot = new Snapshot();
        return BigInteger.valueOf(Context.getBlockTimestamp()).subtract(snapshot._time_offset.get()).divide(DAY_TO_MICROSECOND);
    }

    private void updateSnapshotForAddress(Address _account, BigInteger _amount) {
        Snapshot snapshot = new Snapshot();
        if (snapshot._time_offset.get().equals(ZERO)) {
            setTimeOffset();
        }
        BigInteger current_id = getDay();
        int totalSnapshotsTaken = snapshot._total_snapshots.getOrDefault(_account, 0);
        Context.println("totalSnapshotsTaken: " + totalSnapshotsTaken);
        if (totalSnapshotsTaken > 0 && snapshot._stake_snapshots.at(_account).at(totalSnapshotsTaken - 1).getOrDefault(IDS, ZERO).equals(ZERO)) {
            snapshot._stake_snapshots.at(_account).at(totalSnapshotsTaken - 1).set(AMOUNT, _amount);
        } else {
            snapshot._stake_snapshots.at(_account).at(totalSnapshotsTaken).set(IDS, current_id);
            snapshot._stake_snapshots.at(_account).at(totalSnapshotsTaken).set(AMOUNT, _amount);
            snapshot._total_snapshots.set(_account, totalSnapshotsTaken + 1);
        }
    }

    private void updateTotalStakedSnapshot(BigInteger _amount) {
        Snapshot snapshot = new Snapshot();
        if (snapshot._time_offset.get().equals(ZERO)) {
            setTimeOffset();
        }
        BigInteger current_id = getDay();
        int totalSnapshotsTaken = snapshot._total_staked_snapshot_count.getOrDefault(0);

        if (totalSnapshotsTaken > 0 && (snapshot._total_staked_snapshot.at(totalSnapshotsTaken - 1).getOrDefault(IDS, ZERO)).equals(current_id)) {
            snapshot._total_staked_snapshot.at(totalSnapshotsTaken - 1).set(AMOUNT, _amount);
        } else {
            snapshot._total_staked_snapshot.at(totalSnapshotsTaken).set(IDS, current_id);
            snapshot._total_staked_snapshot.at(totalSnapshotsTaken).set(AMOUNT, _amount);
            snapshot._total_staked_snapshot_count.set(totalSnapshotsTaken + 1);
        }
    }

    @External(readonly = true)
    public BigInteger stakedBalanceOfAt(Address _account, BigInteger _day) {
        BigInteger current_day = this.getDay();
        if (_day.compareTo(current_day) > 0) {
            Context.revert(TAG + ": Asked _day is greater than the current day.");
        }
        Snapshot snapshot = new Snapshot();
        int totalSnapshotsTaken = snapshot._total_snapshots.getOrDefault(_account, 0);
        if (totalSnapshotsTaken == 0) {
            return ZERO;
        }

        if ((snapshot._stake_snapshots.at(_account).at(totalSnapshotsTaken - 1).getOrDefault(IDS, ZERO)).compareTo(_day) <= 0) {
            return (BigInteger) snapshot._stake_snapshots.at(_account).at(totalSnapshotsTaken - 1).getOrDefault(AMOUNT, ZERO);
        }

        if ((snapshot._stake_snapshots.at(_account).at(0).getOrDefault(IDS, ZERO)).compareTo(_day) > 0) {
            return ZERO;
        }
        int low = 0;
        int high = totalSnapshotsTaken - 1;
        while (high > low) {
            int mid = high - (high - low) / 2;
            DictDB<String, BigInteger> mid_value = snapshot._stake_snapshots.at(_account).at(mid);
            if ((mid_value.get(IDS)).compareTo(_day) == 0) {
                return mid_value.get(AMOUNT);
            } else if ((mid_value.get(IDS)).compareTo(_day) < 0) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return snapshot._stake_snapshots.at(_account).at(low).getOrDefault(AMOUNT, ZERO);
    }

    @External(readonly = true)
    public BigInteger totalStakedBalanceOFAt(BigInteger _day) {
        BigInteger current_day = getDay();
        if (_day.compareTo(current_day) > 0) {
            Context.revert(TAG + ": Asked _day is greater than the current day.");
        }
        Snapshot snapshot = new Snapshot();
        int totalSnapshotsTaken = snapshot._total_staked_snapshot_count.getOrDefault(0);
        if (totalSnapshotsTaken == 0) {
            return ZERO;
        }
        if ((snapshot._total_staked_snapshot.at(totalSnapshotsTaken - 1).getOrDefault(IDS, ZERO)).compareTo(_day) <= 0) {
            return snapshot._total_staked_snapshot.at(totalSnapshotsTaken - 1).getOrDefault(AMOUNT, ZERO);
        }

        if ((snapshot._total_staked_snapshot.at(0).getOrDefault(IDS, ZERO)).compareTo(_day) > 0) {
            return ZERO;
        }

        int low = 0;
        int high = totalSnapshotsTaken - 1;
        while (high > low) {
            int mid = high - (high - low) / 2;
            DictDB<String, BigInteger> mid_value = snapshot._total_staked_snapshot.at(mid);
            if ((mid_value.get(IDS)).compareTo(_day) == 0) {
                return mid_value.get(AMOUNT);
            } else if ((mid_value.get(IDS)).compareTo(_day) < 0) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return snapshot._total_staked_snapshot.at(low).getOrDefault(AMOUNT, ZERO);
    }

    @External
    public void loadTAPStakeSnapshot(StakedTAPTokenSnapshots[] _data) {
        ownerOnly();
        Snapshot snapshot = new Snapshot();
        if (snapshot._time_offset.get().equals(ZERO)) {
            setTimeOffset();
        }
        for (StakedTAPTokenSnapshots stake : _data) {
            BigInteger current_id = stake.day;
            if (current_id.compareTo(this.getDay()) < 0) {
                Address _account = stake.address;
                int length = snapshot._total_snapshots.getOrDefault(_account, 0);
                snapshot._stake_snapshots.at(_account).at(length).set(IDS, current_id);
                snapshot._stake_snapshots.at(_account).at(length).set(AMOUNT, stake.amount);
                snapshot._total_snapshots.set(_account, snapshot._total_snapshots.getOrDefault(_account, 0) + 1);
            }
        }
    }

    @External
    public void loadTotalStakeSnapshot(TotalStakedTAPTokenSnapshots[] _data) {
        ownerOnly();
        Snapshot snapshot = new Snapshot();
        if (snapshot._time_offset.get().equals(ZERO)) {
            setTimeOffset();
        }
        for (TotalStakedTAPTokenSnapshots _id : _data) {
            BigInteger current_id = _id.day;
            if (current_id.compareTo(this.getDay()) < 0) {
                BigInteger amount = _id.amount;
                int length = snapshot._total_staked_snapshot_count.getOrDefault(0);
                snapshot._total_staked_snapshot.at(length).set(IDS, current_id);
                snapshot._total_staked_snapshot.at(length).set(AMOUNT, amount);
                snapshot._total_staked_snapshot_count.set(snapshot._total_staked_snapshot_count.getOrDefault(0) + 1);
            }
        }
    }

    @External
    public void advanceDayManually() {
        ownerOnly();
        Snapshot snapshot = new Snapshot();
        snapshot._time_offset.set(snapshot._time_offset.getOrDefault(ZERO).subtract(DAY_TO_MICROSECOND));
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

    // BigInteger#pow() is not implemented in the shadow BigInteger.
    // we need to use our implementation for that.
    private static BigInteger pow(BigInteger base, int exponent) {
        BigInteger result = BigInteger.ONE;
        for (int i = 0; i < exponent; i++) {
            result = result.multiply(base);
        }
        return result;
    }

    private <T> List<T> arrayDbToList(ArrayDB<T> arraydb) {
        @SuppressWarnings("unchecked")
        T[] addressList = (T[]) new Object[arraydb.size()];

        for (int i = 0; i < arraydb.size(); i++) {
            addressList[i] = arraydb.get(i);
        }
        return List.of(addressList);
    }
}

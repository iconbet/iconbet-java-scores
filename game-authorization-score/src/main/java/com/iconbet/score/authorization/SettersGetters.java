package com.iconbet.score.authorization;

import score.Address;
import score.ArrayDB;
import score.Context;
import score.VarDB;
import score.annotation.External;

import java.util.List;

import static com.iconbet.score.authorization.utils.ArrayDBUtils.containsInArrayDb;
import static com.iconbet.score.authorization.utils.ArrayDBUtils.removeArrayItem;
import static com.iconbet.score.authorization.utils.Consts.*;

public class SettersGetters {

    public final VarDB<Address> roulette_score = Context.newVarDB(ROULETTE_SCORE, Address.class);
    public final VarDB<Address> tapTokenScore = Context.newVarDB(TAP_TOKEN_SCORE, Address.class);
    public final VarDB<Address> dividendDistributionScore = Context.newVarDB(DIVIDEND_DISTRIBUTION_SCORE, Address.class);
    public final VarDB<Address> rewardsScore = Context.newVarDB(REWARDS_SCORE, Address.class);
    public final VarDB<Address> uTapTokenScore = Context.newVarDB(UTAP_TOKEN_SCORE, Address.class);
    public final ArrayDB<Address> admin_list = Context.newArrayDB(ADMIN_LIST, Address.class);
    public final VarDB<Address> super_admin = Context.newVarDB(SUPER_ADMIN, Address.class);

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

    public void validateOwner() {
        Context.require(Context.getCaller().equals(Context.getOwner()),
                TAG + ": Only owner can call this method");
    }

    public void validateSuperAdmin() {
        Context.require(Context.getCaller().equals(super_admin.get()),
                TAG + "Only super admin can call this method.");
    }

    public void validateAdmin() {
        Context.require(containsInArrayDb(Context.getCaller(), admin_list),
                TAG + "Only Admins can call this method");
    }

    public void validateOwnerScore(Address score) {
        validateOwner();
        Context.require(score.isContract(), TAG + ": The provided address is not a contract address 'cx....'");
    }

    public Address getCaller(){
        return Context.getCaller();
    }



}

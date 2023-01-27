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

    public final VarDB<Address> treasuryScore = Context.newVarDB(ROULETTE_SCORE, Address.class);
    public final VarDB<Address> tapTokenScore = Context.newVarDB(TAP_TOKEN_SCORE, Address.class);
    public final VarDB<Address> dividendDistributionScore = Context.newVarDB(DIVIDEND_DISTRIBUTION_SCORE, Address.class);
    public final VarDB<Address> rewardsScore = Context.newVarDB(REWARDS_SCORE, Address.class);
    public final VarDB<Address> uTapTokenScore = Context.newVarDB(UTAP_TOKEN_SCORE, Address.class);
    public final ArrayDB<Address> adminList = Context.newArrayDB(ADMIN_LIST, Address.class);
    public final VarDB<Address> superAdmin = Context.newVarDB(SUPER_ADMIN, Address.class);

    /**
     * Sets the address of roulette/game score
     * :param scoreAddress: Address of roulette
     * :type scoreAddress: :class:`iconservice.base.address.Address`
     * :return:
     **/
    @External
    public void setTreasuryScore(Address scoreAddress) {
        //TODO: should we call address isContract()?? contract means score?
        validateOwnerScore(scoreAddress);
        this.treasuryScore.set(scoreAddress);
    }

    /**
     * Returns the roulette score address
     * :return: Address of the roulette score
     * :rtype: :class:`iconservice.base.address.Address
     ***/
    @External(readonly = true)
    public Address get_roulette_score() {
        return this.treasuryScore.get();
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
        this.tapTokenScore.set(scoreAddress);
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
     :param scoreAddress: Address of dividend_distribution
     :type scoreAddress: :class:`iconservice.base.address.Address`
     :return:
     ***/
    @External
    public void setDividendDistributionScore(Address scoreAddress) {
        validateOwnerScore(scoreAddress);
        this.dividendDistributionScore.set(scoreAddress);
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
     :param scoreAddress: Address of rewards
     :type scoreAddress: :class:`iconservice.base.address.Address`
     :return:
     ***/
    @External
    public void setRewardsScore(Address scoreAddress) {

        validateOwnerScore(scoreAddress);
        this.rewardsScore.set(scoreAddress);
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
     Sets super admin. Super admin is also added in admins list. Only allowed
     by the contract owner.
     :param superAdmin: Address of super admin
     :type superAdmin: :class:`iconservice.base.address.Address`
     ***/
    @External
    public void setSuperAdmin(Address superAdmin) {
        validateOwner();
        this.superAdmin.set(superAdmin);
        this.adminList.add(superAdmin);
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
        return this.superAdmin.get();
    }

    /**
     * Sets admin. Only allowed by the super admin.
     * :param admin: Wallet address of admin
     * :type admin: :class:`iconservice.base.address.Address`
     * :return:
     ***/
    @External
    public void setAdmin(Address admin) {
        validateSuperAdmin();
        Context.require(!containsInArrayDb(admin, adminList), TAG + ": Already in admin list");
        this.adminList.add(admin);
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
        Address[] admin_list = new Address[this.adminList.size()];
        for (int i = 0; i < this.adminList.size(); i++) {
            admin_list[i] = this.adminList.get(i);
        }
        return List.of(admin_list);
    }

    /***
     Removes admin from the admin arrayDB. Only called by the super admin
     :param admin: Address of admin to be removed
     :type admin: :class:`iconservice.base.address.Address`
     :return:
     ***/
    @External
    public void removeAdmin(Address admin) {
        validateSuperAdmin();
        Context.require(containsInArrayDb(admin, this.adminList), TAG + "Invalid Address: Not in list");
        removeArrayItem(this.adminList, admin);
        if (DEBUG) {
            Context.println(admin.toString() + " has been removed from admin list." + TAG);
        }
    }

    public void validateOwner() {
        Context.require(Context.getCaller().equals(Context.getOwner()),
                TAG + ": Only owner can call this method");
    }

    public void validateSuperAdmin() {
        Context.require(Context.getCaller().equals(superAdmin.get()),
                TAG + ": Only super admin can call this method.");
    }

    public void validateAdmin() {
        Context.require(containsInArrayDb(Context.getCaller(), adminList),
                TAG + "Only Admins can call this method");
    }

    public void validateOwnerScore(Address score) {
        validateOwner();
        Context.require(score.isContract(), TAG + ": The provided address is not a contract address 'cx....'");
    }
}

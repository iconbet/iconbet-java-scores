package com.iconbet.score.dividend;

import score.Address;
import score.Context;
import score.DictDB;
import score.VarDB;

import java.math.BigInteger;

import static com.iconbet.score.dividend.utils.Constants._TAP_DIVIDENDS;

public class TapDividendsTax {
    public final VarDB<Integer> tapDividendsNonTaxPeriod = Context.newVarDB(_TAP_DIVIDENDS + "_non_tax_period", Integer.class);
    public final VarDB<BigInteger> tapDividendsTaxPercentage = Context.newVarDB(_TAP_DIVIDENDS + "_tax_percentage", BigInteger.class);
    public final DictDB<Address, BigInteger> tapDividendsLastClaim = Context.newDictDB(_TAP_DIVIDENDS + "_last_claim", BigInteger.class);
    public final DictDB<Address, BigInteger> tapDividendsClaimable = Context.newDictDB(_TAP_DIVIDENDS + "_claimable", BigInteger.class);
    public final DictDB<Address, BigInteger> tapDividendsTaxable = Context.newDictDB(_TAP_DIVIDENDS + "_taxable", BigInteger.class);
    public final DictDB<Address, String> tapDividendsClaimablePerDay = Context.newDictDB(_TAP_DIVIDENDS + "_claimable_per_day", String.class);
}

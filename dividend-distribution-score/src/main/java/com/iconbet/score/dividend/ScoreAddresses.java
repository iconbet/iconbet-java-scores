package com.iconbet.score.dividend;

import score.Address;
import score.Context;
import score.VarDB;

import static com.iconbet.score.dividend.utils.Constants.*;
import static com.iconbet.score.dividend.utils.Constants._IBPNP_SCORE;

public class ScoreAddresses {
    public final VarDB<Address> _token_score = Context.newVarDB(_TOKEN_SCORE, Address.class);
    public final VarDB<Address> treasuryScore = Context.newVarDB(_GAME_SCORE, Address.class);
    public final VarDB<Address> _promo_score = Context.newVarDB(_PROMO_SCORE, Address.class);
    public final VarDB<Address> _game_auth_score = Context.newVarDB(_GAME_AUTH_SCORE, Address.class);
    public final VarDB<Address> _daofund_score = Context.newVarDB(_DAOFUND_SCORE, Address.class);
    public final VarDB<Address> ibpnpScore = Context.newVarDB(_IBPNP_SCORE, Address.class);
}

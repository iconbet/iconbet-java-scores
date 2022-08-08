package com.iconbet.score.tap;

import score.*;

import java.math.BigInteger;

import static com.iconbet.score.tap.utils.Constants.*;
import static com.iconbet.score.tap.utils.Constants._ENABLE_SNAPSHOTS;

public class Snapshot {
    public final VarDB<BigInteger> _time_offset = Context.newVarDB(_TIME_OFFSET, BigInteger.class);

    //        [address][snapshot_id]["ids" || "amount"]
    public final BranchDB<Address, BranchDB<Integer, DictDB<String, BigInteger>>> _stake_snapshots = Context.newBranchDB(_STAKE_SNAPSHOTS, BigInteger.class);
    //        [address] = total_number_of_snapshots_taken
    public final DictDB<Address, Integer> _total_snapshots = Context.newDictDB(_TOTAL_SNAPSHOTS, Integer.class);

    //        [snapshot_id]["ids" || "amount"]
    public final BranchDB<Integer, DictDB<String, BigInteger>> _total_staked_snapshot = Context.newBranchDB(_TOTAL_STAKED_SNAPSHOT, BigInteger.class);
    public final VarDB<Integer> _total_staked_snapshot_count = Context.newVarDB(_TOTAL_STAKED_SNAPSHOT_COUNT, Integer.class);

    public final VarDB<Boolean> _enable_snapshots = Context.newVarDB(_ENABLE_SNAPSHOTS, Boolean.class);
}

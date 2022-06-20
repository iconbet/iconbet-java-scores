package com.iconbet.score.tap;

import score.Address;
import score.Context;
import score.DictDB;
import score.VarDB;

import static com.iconbet.score.tap.utils.Constants.*;
import static com.iconbet.score.tap.utils.Constants.LOCKLIST;

public class LinearComplexityMigration {
    public final VarDB<Boolean> linear_complexity_migration_start = Context.newVarDB(_LINEAR_COMPLEXITY_MIGRATION + "_start", Boolean.class);
    public final DictDB<String, Boolean> linear_complexity_migration_complete = Context.newDictDB(_LINEAR_COMPLEXITY_MIGRATION + "_complete", Boolean.class);
    public final DictDB<String, Integer> linear_complexity_migration_index = Context.newDictDB(_LINEAR_COMPLEXITY_MIGRATION + "_index", Integer.class);

    public final DictDB<Address, Integer> _pause_whitelist_index = Context.newDictDB(PAUSE_WHITELIST + "_index", Integer.class);
    public final DictDB<Address, Integer> _blacklist_address_index = Context.newDictDB(BLACKLIST_ADDRESS + "_index", Integer.class);
    public final DictDB<Address, Integer> _locklist_index = Context.newDictDB(LOCKLIST + "_index", Integer.class);

}

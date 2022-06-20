package com.iconbet.score.ibpnp;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import score.Address;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.security.SecureRandom;

public class IBPNPTest extends TestBase {
    private static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    private static final Address treasuryScore = Address.fromString("cx0000000000000000000000000000000000000001");
    private static final Address rewardsScore = Address.fromString("cx0000000000000000000000000000000000000002");

    private static final String TAG = "IconBet Player NFT Profile";

    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account testingAccount = sm.createAccount();

    private Score IBPNPScore;
    private final SecureRandom secureRandom = new SecureRandom();

    IBPNP scoreSpy;

    @BeforeEach
    public void setup() throws Exception{
        IBPNPScore = sm.deploy(owner, IBPNP.class, TAG, "IBPNP");
        IBPNP instance = (IBPNP) IBPNPScore.getInstance();
        scoreSpy = spy(instance);
        IBPNPScore.setInstance(scoreSpy);
    }

    @Test
    void name(){
        assertEquals(TAG, IBPNPScore.call("name"));
    }

    @Test
    void symbol(){
        assertEquals("IBPNP", IBPNPScore.call("symbol"));
    }

}

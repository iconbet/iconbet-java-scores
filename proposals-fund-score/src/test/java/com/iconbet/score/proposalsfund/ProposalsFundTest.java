package com.iconbet.score.proposalsfund;

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
import score.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

public class ProposalsFundTest extends TestBase{
    private static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
    private static final Address treasuryScore = Address.fromString("cx0000000000000000000000000000000000000001");
    private static final Address rewardsScore = Address.fromString("cx0000000000000000000000000000000000000002");
    private static final Address tapTokenScore = Address.fromString("cx0000000000000000000000000000000000000003");


    private static final String TAG = "ICONBet ProposalsFund";

    private static final ServiceManager sm = getServiceManager();
    private static final Account owner = sm.createAccount();
    private static final Account testingAccount = sm.createAccount();
    private static final Account testingAccount1 = sm.createAccount();

    public static final BigInteger decimal = new BigInteger("1000000000000000000");

    private Score ProposalsFundScore;
    private final SecureRandom secureRandom = new SecureRandom();

    ProposalsFund scoreSpy;

    @BeforeEach
    public void setup() throws Exception {
        ProposalsFundScore = sm.deploy(owner, ProposalsFund.class);
        ProposalsFund instance = (ProposalsFund) ProposalsFundScore.getInstance();
        scoreSpy = spy(instance);
        ProposalsFundScore.setInstance(scoreSpy);
    }

    @Test
    void name(){
        assertEquals(TAG, ProposalsFundScore.call("name"));
    }
}

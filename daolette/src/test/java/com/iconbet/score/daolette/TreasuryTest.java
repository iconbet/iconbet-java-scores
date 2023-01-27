//package com.iconbet.score.daolette;
//import com.eclipsesource.json.Json;
//import com.eclipsesource.json.JsonObject;
//import com.eclipsesource.json.JsonValue;
//import com.iconloop.score.test.Account;
//import com.iconloop.score.test.Score;
//import com.iconloop.score.test.ServiceManager;
//import com.iconloop.score.test.TestBase;
//import com.sun.management.DiagnosticCommandMBean;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.function.Executable;
//import org.mockito.stubbing.Answer;
//import score.Address;
//import org.mockito.MockedStatic;
//import org.mockito.Mockito;
//
//import static org.mockito.Mockito.*;
//
//import score.Context;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//
//import java.math.BigInteger;
//import java.nio.channels.MulticastChannel;
//import java.security.SecureRandom;
//import java.util.List;
//import java.util.Map;
//public class TreasuryTest extends TestBase{
//    private static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
//    private static final Address daoFund = Address.fromString("cx0000000000000000000000000000000000000001");
//    private static final Address gameScore = Address.fromString("cx0000000000000000000000000000000000000002");
//    private static final Address tapToken = Address.fromString("cx0000000000000000000000000000000000000003");
//    private static final Address gameAuth = Address.fromString("cx0000000000000000000000000000000000000004");
//    private static final Address dividends = Address.fromString("cx0000000000000000000000000000000000000005");
//    private static final Address utap = Address.fromString("cx0000000000000000000000000000000000000006");
//    private static final Address rewards = Address.fromString("cx0000000000000000000000000000000000000006");
//
//    private static final Address dice = Address.fromString("cx0000000000000000000000000000000000000007");
//    private static final Address roulette = Address.fromString("cx0000000000000000000000000000000000000008");
//    private static final Address blackjack = Address.fromString("cx0000000000000000000000000000000000000009");
//    public static final String TAG = "AUTHORIZATION";
//
//    private static final ServiceManager sm = getServiceManager();
//    private static final Account owner = sm.createAccount();
//    private static final Account testingAccount = sm.createAccount();
//    private static final Account testingAccount1 = sm.createAccount();
//    private static final Account tapStakeUser1 = sm.createAccount();
//    private static final Account tapStakeUser2 = sm.createAccount();
//    private static final Account tapStakeUser3 = sm.createAccount();
//    private static final Account tapStakeUser4 = sm.createAccount();
//    private static final Account revshareWallet = sm.createAccount();
//    private static final Account notRevshareWallet = sm.createAccount();
//    public static final BigInteger decimal = new BigInteger("1000000000000000000");
//    private Score treasury;
//    private final SecureRandom secureRandom = new SecureRandom();
//    private static MockedStatic<Context> contextMock;
//
//
//    Daolette scoreSpy;
//
//    @BeforeEach
//    public void setup() throws Exception {
//        treasury = sm.deploy(owner, Daolette.class);
//        Daolette instance = (Daolette) treasury.getInstance();
//        scoreSpy = spy(instance);
//        treasury.setInstance(scoreSpy);
//        long currentTime = System.currentTimeMillis() / 1000L;
//        sm.getBlock().increase(currentTime / 2);
//        contextMock.reset();
//    }
//
//    @BeforeAll
//    public static void init(){
//        contextMock = Mockito.mockStatic(Context.class, CALLS_REAL_METHODS);
//    }
//
//
//}

package com.iconbet.score.daodice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;
import score.Context;

class DaoDiceTest {

	public static final Integer _68134 = 68134;
	public static final Double _681_34 = 681.34;

	@Test
	void testMainBetLimit() {

		//max bet amount 572.45
		//_treasury_min starts from 2.5E+23, or 250,000 ICX
		//gap will always be between 1 and 96
		//250000000000000000000000
		//125000000000000000000000
		//10457334076965988000000
		//5559454586372131000      = 5.5 ICX
		//100000000000000000       = 0.1 ICX
		//j  13211,009174311926605504
		//j2 13209 264097220183755540
		//p  13209,264097220180000000
		//p2 13209 264097220179787776
		BigInteger _treasury_min = new BigInteger("250000000000000000000000");
		BigInteger gap = BigInteger.valueOf(96);
		BigInteger main_bet_limit = 
				_treasury_min.multiply(BigInteger.valueOf(3)).multiply(gap)
				.multiply(BigInteger.valueOf(100)).divide( BigInteger.valueOf((long) (2 * 100* (_68134 - _681_34* gap.intValue()) )));

		System.out.println(main_bet_limit);
		assertEquals(new BigInteger("13209264097220183755540"), main_bet_limit);
	}

	@Test
	void testGapLimitTest() {

		BigInteger upper = BigInteger.valueOf(0);
		BigInteger lower = BigInteger.valueOf(0);
		BigInteger _95 = BigInteger.valueOf(95);

		BigInteger gapResult = upper.subtract(lower);
		boolean condition = BigInteger.ZERO.compareTo(gapResult) <= 0 && gapResult.compareTo(_95) <= 0;
		System.out.println(condition);
		assertEquals(Boolean.TRUE, condition,"Invalid gap. Choose upper and lower values such that gap is between 0 to 95");

	}
}

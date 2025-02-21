package org.folio.common.pf;

import java.util.function.Function;
import java.util.function.Predicate;

import org.jeasy.random.api.Randomizer;
import org.jeasy.random.randomizers.range.IntegerRangeRandomizer;

class TestData {

  private static final Randomizer<Integer> negativeRandom = new IntegerRangeRandomizer(Integer.MIN_VALUE, -1);
  private static final Randomizer<Integer> positiveRandom = new IntegerRangeRandomizer(1, Integer.MAX_VALUE);

  static final Predicate<Integer> positiveInt = positiveInt();
  static final Function<Integer, String> fPositiveIntMsg = fIntToString("Positive: %d");
  static final Predicate<Integer> negativeInt = negativeInt();
  static final Function<Integer, String> fNegativeIntMsg = fIntToString("Negative: %d");
  static final Function<Integer, Integer> fNegate = i -> -i;


  static final String OTHERWISE_RESULT = "OTHERWISE";
  static final Function<Integer, String> FUNC_OTHERWISE = integer -> OTHERWISE_RESULT;

  private TestData() {
  }

  static Integer[] intsOfEveryKind() {
    return new Integer[] {negativeRandom.getRandomValue(), 0 , positiveRandom.getRandomValue()};
  }

  private static Predicate<Integer> positiveInt() {
    return i -> i > 0;
  }

  private static Predicate<Integer> negativeInt() {
    return i -> i < 0;
  }

  private static Function<Integer, String> fIntToString(String format) {
    return i -> String.format(format, i);
  }

}

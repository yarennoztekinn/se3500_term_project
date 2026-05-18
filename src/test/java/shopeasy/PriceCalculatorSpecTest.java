package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 1 – Specification-Based Testing (Chapter 2)
 *
 * <p>Target class: {@link PriceCalculator}
 *
 * <p>Your goal is to test {@code PriceCalculator.calculate(basePrice, discountRate, taxRate)}
 * using the domain testing technique from Chapter 2:
 * <ol>
 *   <li>Identify equivalence partitions for each input dimension.</li>
 *   <li>Identify boundary values between partitions (on-point / off-point).</li>
 *   <li>Write at least 10 meaningful test cases that cover both partitions and boundaries.</li>
 *   <li>Use {@code @ParameterizedTest} with {@code @CsvSource} for tests that share structure.</li>
 *   <li>Add a comment above each test method explaining which partition or boundary it covers.</li>
 * </ol>
 *
 * <h3>Input dimensions to consider</h3>
 * <ul>
 *   <li><b>basePrice</b>  – zero, positive, very large</li>
 *   <li><b>discountRate</b> – 0 (no discount), (0,100) typical, 100 (full discount)</li>
 *   <li><b>taxRate</b>    – 0 (no tax), (0,100) typical, 100 (100% tax)</li>
 * </ul>
 */
class PriceCalculatorSpecTest {

    private PriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
    }

    // -----------------------------------------------------------------------
    // Specification-based tests for PriceCalculator.calculate(basePrice, discountRate, taxRate)
    // -----------------------------------------------------------------------

    /** Partition: zero base price — result must always be 0 regardless of discount and tax. */
    @Test
    void zeroBasePriceReturnsZero() {
        assertThat(calculator.calculate(0.0, 50.0, 20.0)).isCloseTo(0.0, within(0.001));
        assertThat(calculator.calculate(0.0, 0.0, 100.0)).isCloseTo(0.0, within(0.001));
    }

    /** Boundary: discountRate at lower bound (0%) — no discount is applied. */
    @Test
    void discountRateZeroMeansNoDiscount() {
        double result = calculator.calculate(100.0, 0.0, 10.0);
        assertThat(result).isCloseTo(110.0, within(0.001));
    }

    /** Boundary: discountRate at upper bound (100%) — full discount reduces base price to 0 before tax. */
    @Test
    void discountRateHundredMeansFullDiscount() {
        double result = calculator.calculate(100.0, 100.0, 50.0);
        assertThat(result).isCloseTo(0.0, within(0.001));
    }

    /** Boundary: taxRate at lower bound (0%) — no tax is added. */
    @Test
    void taxRateZeroMeansNoTax() {
        double result = calculator.calculate(150.0, 10.0, 0.0);
        assertThat(result).isCloseTo(135.0, within(0.001));
    }

    /** Boundary: taxRate at upper bound (100%) — tax doubles the discounted price. */
    @Test
    void taxRateHundredDoublesDiscountedPrice() {
        double result = calculator.calculate(100.0, 20.0, 100.0);
        assertThat(result).isCloseTo(160.0, within(0.001));
    }

    /** Partition: typical valid values exercise the formula across the normal input range. */
    @ParameterizedTest(name = "base={0}, discount={1}%, tax={2}% => expected={3}")
    @CsvSource({
        "100.0, 10.0, 20.0, 108.0",
        "200.0,  0.0, 10.0, 220.0",
        "50.0,  25.0,  8.0, 40.5"
    })
    void typicalValidValueCombinations(double basePrice, double discountRate, double taxRate, double expected) {
        assertThat(calculator.calculate(basePrice, discountRate, taxRate)).isCloseTo(expected, within(0.001));
    }

    /** Boundary: basePrice at the lowest valid positive point above zero. */
    @Test
    void smallPositiveBasePriceCalculatesCorrectly() {
        double result = calculator.calculate(0.01, 10.0, 5.0);
        assertThat(result).isCloseTo(0.00945, within(0.00001));
    }

    /** Invalid partition: negative basePrice is outside the valid domain and produces a negative result. */
    @ParameterizedTest(name = "base={0}, discount={1}%, tax={2}% => expected={3}")
    @CsvSource({
        "-1.0, 0.0, 0.0, -1.0",
        "-10.0, 50.0, 20.0, -6.0"
    })
    void invalidNegativeBasePrice(double basePrice, double discountRate, double taxRate, double expected) {
        assertThat(calculator.calculate(basePrice, discountRate, taxRate)).isCloseTo(expected, within(0.001));
    }

    /** Invalid partition: discountRate above 100% yields a negative discounted amount. */
    @Test
    void invalidDiscountRateAboveHundredProducesNegativeDiscountedPrice() {
        double result = calculator.calculate(100.0, 110.0, 10.0);
        assertThat(result).isCloseTo(-11.0, within(0.001));
    }

    /** Invalid partition: taxRate above 100% grows the discounted amount beyond normal range. */
    @Test
    void invalidTaxRateAboveHundredProducesOverchargedPrice() {
        double result = calculator.calculate(100.0, 0.0, 150.0);
        assertThat(result).isCloseTo(250.0, within(0.001));
    }

}

package shopeasy;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 4 – Property-Based Testing (Chapter 5)
 *
 * <p>Target classes: {@link PriceCalculator}, {@link ShoppingCart}
 *
 * <p>Using jqwik, define and test at least <strong>3 distinct properties</strong>.
 * You must use at least one custom {@code @Provide} method.
 *
 * <h3>Suggested properties (you may use these or design your own)</h3>
 * <ul>
 *   <li><b>Monotonicity</b> – For any fixed base and tax, increasing the discount
 *       rate never increases the final price.</li>
 *   <li><b>Identity</b> – A 0% discount and 0% tax returns exactly the base price.</li>
 *   <li><b>Boundedness</b> – The result is always &gt;= 0.</li>
 *   <li><b>Cart commutativity</b> – Adding product A then B yields the same total
 *       as adding B then A.</li>
 *   <li><b>Discount transitivity</b> – Applying a 10% then another 10% discount via
 *       {@code applyDiscount} is equivalent to a single call with the compounded rate
 *       (think carefully: is this actually true for this implementation?).</li>
 * </ul>
 *
 * <h3>For each property, include a comment that answers:</h3>
 * <ol>
 *   <li>What does this property mean in plain English?</li>
 *   <li>What class of bugs would this property catch?</li>
 * </ol>
 *
 * <h3>If jqwik finds a failing case</h3>
 * Do not just fix the test. Investigate the root cause and explain it in your
 * reflection report (include the counterexample jqwik printed).
 */
class ShopEasyPropertyTest {

    /**
     * Property: The final price is always non-negative.
     * Bug class caught: arithmetic or formula errors that produce a negative result
     *                   when discount and tax are applied to a valid base price.
     */
    @Property
    void finalPriceIsNeverNegative(
            @ForAll @DoubleRange(min = 0, max = 10_000) double base,
            @ForAll @DoubleRange(min = 0, max = 100) double discount,
            @ForAll @DoubleRange(min = 0, max = 100) double tax) {

        PriceCalculator calc = new PriceCalculator();
        double result = calc.calculate(base, discount, tax);

        assertThat(result).isGreaterThanOrEqualTo(0.0);
    }

    /**
     * Property: Zero discount and zero tax returns exactly the original base price.
     * Bug class caught: incorrect formula or misordered discount/tax application.
     */
    @Property
    void zeroDiscountAndTaxReturnsBasePrice(
            @ForAll @DoubleRange(min = 0, max = 10_000) double base) {

        PriceCalculator calc = new PriceCalculator();
        double result = calc.calculate(base, 0.0, 0.0);

        assertThat(result).isCloseTo(base, within(0.0001));
    }

    /**
     * Property: Increasing discount rate never increases the final price.
     * Bug class caught: wrong discount sign or incorrect discount weighting.
     */
    @Property
    void discountMonotonicity(
            @ForAll @DoubleRange(min = 0, max = 10_000) double base,
            @ForAll @DoubleRange(min = 0, max = 100) double tax,
            @ForAll @DoubleRange(min = 0, max = 100) double discount1,
            @ForAll @DoubleRange(min = 0, max = 100) double discount2) {

        Assume.that(discount1 <= discount2);

        PriceCalculator calc = new PriceCalculator();
        double price1 = calc.calculate(base, discount1, tax);
        double price2 = calc.calculate(base, discount2, tax);

        assertThat(price2).isLessThanOrEqualTo(price1);
    }

    /**
     * Property: Adding two distinct products in either order yields the same cart total.
     * Bug class caught: stateful ordering bugs in ShoppingCart.addItem or total calculation.
     */
    @Property
    void cartAdditionIsCommutative(
            @ForAll("validProducts") Product first,
            @ForAll("validProducts") Product second,
            @ForAll @IntRange(min = 1, max = 10) int quantity1,
            @ForAll @IntRange(min = 1, max = 10) int quantity2) {

        Assume.that(!first.getId().equals(second.getId()));

        ShoppingCart cartA = new ShoppingCart();
        cartA.addItem(first, quantity1);
        cartA.addItem(second, quantity2);

        ShoppingCart cartB = new ShoppingCart();
        cartB.addItem(second, quantity2);
        cartB.addItem(first, quantity1);

        assertThat(cartA.total()).isCloseTo(cartB.total(), within(0.0001));
    }

    /**
     * Generates valid products for property-based cart testing.
     */
    @Provide
    Arbitrary<Product> validProducts() {
        Arbitrary<String> ids = Arbitraries.strings()
                .withCharRange('A', 'Z')
                .ofMinLength(1)
                .ofMaxLength(5);

        Arbitrary<String> names = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(10);

        Arbitrary<Double> prices = Arbitraries.doubles()
                .between(0.01, 500.0);

        return Combinators.combine(ids, names, prices)
                .as((id, name, price) -> new Product("P-" + id, name, price, 100));
    }
}

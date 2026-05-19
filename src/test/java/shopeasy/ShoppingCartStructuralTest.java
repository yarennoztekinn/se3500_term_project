package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 2 – Structural Testing &amp; Code Coverage (Chapter 3)
 *
 * <p>Target class: {@link ShoppingCart}
 *
 * <h3>Workflow</h3>
 * <ol>
 *   <li>Write an initial test suite based on the specification (Javadoc of ShoppingCart).</li>
 *   <li>Run {@code mvn test} to generate the JaCoCo report:
 *       <pre>  target/site/jacoco/index.html</pre></li>
 *   <li>Open the report, navigate to {@code ShoppingCart}, and identify uncovered branches.</li>
 *   <li>Add tests specifically to cover those branches until branch coverage &gt;= 80%.</li>
 *   <li>Take a screenshot of the final JaCoCo summary and put it in {@code report/jacoco-screenshot.png}.</li>
 * </ol>
 *
 * <h3>Branches to think about</h3>
 * <ul>
 *   <li>{@code addItem}: product already in cart vs. new product</li>
 *   <li>{@code removeItem}: product found vs. not found in cart</li>
 *   <li>{@code updateQuantity}: product found vs. not found, quantity valid vs. invalid</li>
 *   <li>{@code applyDiscount}: zero discount, positive discount</li>
 *   <li>{@code total}: empty cart vs. non-empty cart</li>
 * </ul>
 *
 * <h3>Bonus (PIT Mutation Testing)</h3>
 * Run: {@code mvn org.pitest:pitest-maven:mutationCoverage}
 * <br>Examine the HTML report in {@code target/pit-reports/}. Find two surviving mutants,
 * explain why each survived, and describe a test that would kill it. Add this analysis
 * to your reflection report.
 */
class ShoppingCartStructuralTest {

    private ShoppingCart cart;
    private Product apple;
    private Product banana;

    @BeforeEach
    void setUp() {
        cart   = new ShoppingCart();
        apple  = new Product("P001", "Apple",  1.50, 100);
        banana = new Product("P002", "Banana", 0.80, 50);
    }

    // -----------------------------------------------------------------------
    // addItem() — branch coverage: new product vs. existing product
    // -----------------------------------------------------------------------

    /** Branch: addItem — product not yet in cart (new line added) */
    @Test
    void addItemNewProduct() {
        cart.addItem(apple, 5);
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.total()).isCloseTo(7.50, within(0.001));
    }

    /** Branch: addItem — product already in cart (quantity combined) */
    @Test
    void addItemExistingProductCombinesQuantity() {
        cart.addItem(apple, 3);
        cart.addItem(apple, 2);
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
        assertThat(cart.total()).isCloseTo(7.50, within(0.001));
    }

    /** Branch: addItem — multiple different products in cart */
    @Test
    void addItemMultipleDifferentProducts() {
        cart.addItem(apple, 2);
        cart.addItem(banana, 3);
        assertThat(cart.itemCount()).isEqualTo(2);
        assertThat(cart.total()).isCloseTo(3.0 + 2.40, within(0.001));
    }

    // -----------------------------------------------------------------------
    // removeItem() — branch coverage: product found vs. not found
    // -----------------------------------------------------------------------

    /** Branch: removeItem — product exists in cart (removed successfully) */
    @Test
    void removeItemExistingProduct() {
        cart.addItem(apple, 5);
        cart.addItem(banana, 3);
        cart.removeItem("P001");
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.getItems().get(0).getProduct().getId()).isEqualTo("P002");
    }

    /** Branch: removeItem — product not in cart (no effect, no exception) */
    @Test
    void removeItemNonExistentProductDoesNothing() {
        cart.addItem(apple, 5);
        cart.removeItem("P999");
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.total()).isCloseTo(7.50, within(0.001));
    }

    /** Branch: removeItem — empty cart remains empty */
    @Test
    void removeItemFromEmptyCartDoesNothing() {
        cart.removeItem("P001");
        assertThat(cart.itemCount()).isEqualTo(0);
    }

    // -----------------------------------------------------------------------
    // updateQuantity() — branch coverage: valid, invalid, found, not found
    // -----------------------------------------------------------------------

    /** Branch: updateQuantity — valid product and quantity (success) */
    @Test
    void updateQuantityValidUpdate() {
        cart.addItem(apple, 5);
        cart.updateQuantity("P001", 10);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(10);
        assertThat(cart.total()).isCloseTo(15.0, within(0.001));
    }

    /** Branch: updateQuantity — quantity is zero (throws exception) */
    @Test
    void updateQuantityZeroThrowsException() {
        cart.addItem(apple, 5);
        assertThatThrownBy(() -> cart.updateQuantity("P001", 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Quantity must be > 0");
    }

    /** Branch: updateQuantity — negative quantity (throws exception) */
    @Test
    void updateQuantityNegativeThrowsException() {
        cart.addItem(apple, 5);
        assertThatThrownBy(() -> cart.updateQuantity("P001", -3))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Quantity must be > 0");
    }

    /** Branch: updateQuantity — product not found in cart (throws exception) */
    @Test
    void updateQuantityProductNotFoundThrowsException() {
        cart.addItem(apple, 5);
        assertThatThrownBy(() -> cart.updateQuantity("P999", 10))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Product not found in cart");
    }

    /** Branch: updateQuantity — empty cart, product not found (throws exception) */
    @Test
    void updateQuantityEmptyCartThrowsException() {
        assertThatThrownBy(() -> cart.updateQuantity("P001", 5))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // -----------------------------------------------------------------------
    // applyDiscount() — branch coverage: 0%, positive, 100%
    // -----------------------------------------------------------------------

    /** Branch: applyDiscount — zero discount (returns original total) */
    @Test
    void applyDiscountZeroPercentReturnsOriginalTotal() {
        cart.addItem(apple, 10);
        double result = cart.applyDiscount(0.0);
        assertThat(result).isCloseTo(15.0, within(0.001));
    }

    /** Branch: applyDiscount — positive discount (reduces total) */
    @Test
    void applyDiscountPositivePercentReducesTotal() {
        cart.addItem(apple, 10);
        double result = cart.applyDiscount(50.0);
        assertThat(result).isCloseTo(7.50, within(0.001));
    }

    /** Branch: applyDiscount — 100% discount (returns zero) */
    @Test
    void applyDiscountHundredPercentReturnsZero() {
        cart.addItem(apple, 10);
        double result = cart.applyDiscount(100.0);
        assertThat(result).isCloseTo(0.0, within(0.001));
    }

    /** Branch: applyDiscount — discount on empty cart (returns zero) */
    @Test
    void applyDiscountEmptyCartReturnsZero() {
        double result = cart.applyDiscount(50.0);
        assertThat(result).isCloseTo(0.0, within(0.001));
    }

    // -----------------------------------------------------------------------
    // total() — branch coverage: empty cart vs. non-empty cart
    // -----------------------------------------------------------------------

    /** Branch: total — empty cart (returns 0) */
    @Test
    void totalEmptyCartReturnsZero() {
        assertThat(cart.total()).isCloseTo(0.0, within(0.001));
    }

    /** Branch: total — single item (sum one line) */
    @Test
    void totalSingleItem() {
        cart.addItem(apple, 3);
        assertThat(cart.total()).isCloseTo(4.50, within(0.001));
    }

    /** Branch: total — multiple items (sum all lines) */
    @Test
    void totalMultipleItems() {
        cart.addItem(apple, 2);
        cart.addItem(banana, 5);
        assertThat(cart.total()).isCloseTo(3.0 + 4.0, within(0.001));
    }

    // -----------------------------------------------------------------------
    // itemCount() — branch coverage: empty vs. non-empty
    // -----------------------------------------------------------------------

    /** Branch: itemCount — empty cart */
    @Test
    void itemCountEmptyCart() {
        assertThat(cart.itemCount()).isEqualTo(0);
    }

    /** Branch: itemCount — non-empty cart */
    @Test
    void itemCountNonEmptyCart() {
        cart.addItem(apple, 1);
        cart.addItem(banana, 2);
        assertThat(cart.itemCount()).isEqualTo(2);
    }

    // -----------------------------------------------------------------------
    // clear() — branch coverage: clearing non-empty cart
    // -----------------------------------------------------------------------

    /** Branch: clear — empties a non-empty cart */
    @Test
    void clearRemovesAllItems() {
        cart.addItem(apple, 5);
        cart.addItem(banana, 3);
        cart.clear();
        assertThat(cart.itemCount()).isEqualTo(0);
        assertThat(cart.total()).isCloseTo(0.0, within(0.001));
    }

    /** Branch: clear — calling on empty cart is safe */
    @Test
    void clearEmptyCartIsIdempotent() {
        cart.clear();
        assertThat(cart.itemCount()).isEqualTo(0);
    }

    // -----------------------------------------------------------------------
    // getItems() — returns unmodifiable list
    // -----------------------------------------------------------------------

    /** Branch: getItems — returns unmodifiable list (cannot modify returned list) */
    @Test
    void getItemsReturnsUnmodifiableList() {
        cart.addItem(apple, 5);
        var items = cart.getItems();
        assertThatThrownBy(() -> items.add(new CartItem(banana, 1)))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    // -----------------------------------------------------------------------
    // Integration and edge cases
    // -----------------------------------------------------------------------

    /** Integration: add → discount → clear → add flow */
    @Test
    void complexFlowAddDiscountClearAdd() {
        cart.addItem(apple, 10);
        assertThat(cart.applyDiscount(20.0)).isCloseTo(12.0, within(0.001));
        cart.clear();
        assertThat(cart.itemCount()).isEqualTo(0);
        cart.addItem(banana, 5);
        assertThat(cart.total()).isCloseTo(4.0, within(0.001));
    }

    /** Edge case: discount does not persist (multiple calls yield same result) */
    @Test
    void discountDoesNotPersistBetweenCalls() {
        cart.addItem(apple, 10);
        double first = cart.applyDiscount(50.0);
        double second = cart.applyDiscount(50.0);
        assertThat(first).isCloseTo(second, within(0.001));
        assertThat(cart.total()).isCloseTo(15.0, within(0.001));
    }

}

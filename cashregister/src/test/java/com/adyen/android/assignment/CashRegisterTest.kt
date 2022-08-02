package com.adyen.android.assignment

import com.adyen.android.assignment.money.Bill
import com.adyen.android.assignment.money.Change
import com.adyen.android.assignment.money.Coin
import org.junit.Assert.*
import org.junit.Test

class CashRegisterTest {

    /**
     *  Exception test cases
     *  These test cases return exception against various inputs
     */
    @Test(expected = CashRegister.TransactionException::class)
    fun `when amount paid is less than product price, it should throw exception`() {
        // Arrange
        val productPrice = Change.max().total
        val amountPaid = Change()
        amountPaid.add(Coin.ONE_CENT, 1)
        val exceptionResponse = CashRegister.Errors.PAID_LESS_AMOUNT.message

        // init cash register
        val cashRegister = CashRegister(Change())

        // Act
        val result = cashRegister.performTransaction(productPrice, amountPaid)

        // Assert
        assertEquals(exceptionResponse, result)
    }

    @Test(expected = CashRegister.TransactionException::class)
    fun `when no change is available in cents, it should throw exception`() {
        // Arrange
        val productPrice = Coin.TWENTY_CENT.minorValue.toLong()
        val amountPaid = Change()
        amountPaid.add(Coin.TWO_EURO, 1)
        val exceptionResponse = CashRegister.Errors.NO_CHANGE_AVAILABLE.message

        // init cash register
        val changeAvailable = Change()
        for(billValues in Bill.values()){
            changeAvailable.add(billValues, 1)
        }
        val cashRegister = CashRegister(changeAvailable)

        // Act
        val result = cashRegister.performTransaction(productPrice, amountPaid)

        // Assert
        assertEquals(exceptionResponse, result)
    }

    /**
     * 'No change' returned test cases
     * These test cases return no change against various inputs
     */

    @Test
    fun `when product price is same as paid price (coin), it should return no change`() {
        // Arrange
        val productPrice = Coin.TWO_EURO.minorValue.toLong()
        val amountPaid = Change()
        amountPaid.add(Coin.TWO_EURO, 1)
        val expectedResult = Change.none()

        // init cash register
        val cashRegister = CashRegister(Change())

        // Act
        val result = cashRegister.performTransaction(productPrice, amountPaid)

        // Assert
        assertEquals(expectedResult, result)
    }

    @Test
    fun `when product price and paid price are fully paid (bill), it should return no change`() {
        // Arrange
        val productPrice = Bill.FIVE_EURO.minorValue.toLong()
        val amountPaid = Change()
        amountPaid.add(Bill.FIVE_EURO, 1)

        val expectedResult = Change()

        // init cash register
        val changeAvailable = Change()
        for(billValues in Coin.values()){
            changeAvailable.add(billValues, 1)
        }
        for(billValues in Bill.values()){
            changeAvailable.add(billValues, 1)
        }
        val cashRegister = CashRegister(changeAvailable)

        // Act
        val result = cashRegister.performTransaction(productPrice, amountPaid)

        // Assert
        assertEquals(expectedResult, result)
    }

    /**
     * Return correct change test cases
     * These test cases return correct change amount from the cash register
     */
    @Test
    fun `when product price and paid price is in coins, it should return correct change in coins`() {
        // Arrange
        val productPrice = Coin.FIFTY_CENT.minorValue.toLong()
        val amountPaid = Change()
        amountPaid.add(Coin.TWO_EURO, 1)

        val expectedResult = Change()
        expectedResult.add(Coin.ONE_EURO, 1)
        expectedResult.add(Coin.FIFTY_CENT, 1)

        // init cash register
        val changeAvailable = Change()
        for(billValues in Coin.values()){
            changeAvailable.add(billValues, 1)
        }
        val cashRegister = CashRegister(changeAvailable)

        // Act
        val result = cashRegister.performTransaction(productPrice, amountPaid)

        // Assert
        assertEquals(expectedResult, result)
    }



    @Test
    fun `when product price and paid price are in bills, it should return correct change in coins`() {
        // Arrange
        val productPrice = Bill.TWO_HUNDRED_EURO.minorValue.toLong()
        val amountPaid = Change()
        amountPaid.add(Bill.FIVE_HUNDRED_EURO, 1)

        val expectedResult = Change()
        expectedResult.add(Bill.TWO_HUNDRED_EURO, 1)
        expectedResult.add(Bill.ONE_HUNDRED_EURO, 1)

        // init cash register
        val changeAvailable = Change()
        for(billValues in Coin.values()){
            changeAvailable.add(billValues, 1)
        }
        for(billValues in Bill.values()){
            changeAvailable.add(billValues, 1)
        }
        val cashRegister = CashRegister(changeAvailable)

        // Act
        val result = cashRegister.performTransaction(productPrice, amountPaid)

        // Assert
        assertEquals(expectedResult, result)
    }

    @Test
    fun `when change is only 1 in coins, it should return 1 cent`() {
        // Arrange
        val productPrice = Coin.NINE_CENT.minorValue.toLong()
        val amountPaid = Change()
        amountPaid.add(Coin.TEN_CENT, 1)

        val expectedResult = Change()
        expectedResult.add(Coin.ONE_CENT, 1)

        // init cash register
        val changeAvailable = Change()
        for(coinValues in Coin.values()){
            changeAvailable.add(coinValues, 1)
        }
        val cashRegister = CashRegister(changeAvailable)

        // Act
        val result = cashRegister.performTransaction(productPrice, amountPaid)

        // Assert
        assertEquals(expectedResult, result)
    }

    /**
     * Multiple coin/bill test cases
     * These test cases return correct change when same bill or same coins are more than 1
     */
    @Test
    fun `when amountPaid has multiple coins, it should return correct change in coins`() {
        // Arrange
        val productPrice = Coin.FIFTY_CENT.minorValue.toLong()
        val amountPaid = Change()
        amountPaid.add(Coin.TWO_EURO, 1)

        val expectedResult = Change()
        expectedResult.add(Coin.ONE_EURO, 1)
        expectedResult.add(Coin.FIFTY_CENT, 1)

        // init cash register
        val changeAvailable = Change()
        for(coinValues in Coin.values()){
            changeAvailable.add(coinValues, 5)
        }
        val cashRegister = CashRegister(changeAvailable)

        // Act
        val result = cashRegister.performTransaction(productPrice, amountPaid)

        // Assert
        assertEquals(expectedResult, result)
    }

    @Test
    fun `when amountPaid has multiple bills, it should return correct change in bills`() {
        // Arrange
        val productPrice = Bill.TWENTY_EURO.minorValue.toLong()
        val amountPaid = Change()
        amountPaid.add(Bill.TWENTY_EURO, 3)

        val expectedResult = Change()
        expectedResult.add(Bill.TWENTY_EURO, 2)

        // init cash register
        val changeAvailable = Change()
        for(billValues in Bill.values()){
            changeAvailable.add(billValues, 5)
        }
        val cashRegister = CashRegister(changeAvailable)

        // Act
        val result = cashRegister.performTransaction(productPrice, amountPaid)

        // Assert
        assertEquals(expectedResult, result)
    }

    @Test
    fun `when cash register has multiple bill amount, it should return correct change in bills`() {
        // Arrange
        val productPrice = Bill.TWO_HUNDRED_EURO.minorValue.toLong()
        val amountPaid = Change()
        amountPaid.add(Bill.FIVE_HUNDRED_EURO, 1)

        val expectedResult = Change()
        expectedResult.add(Bill.TWO_HUNDRED_EURO, 1)
        expectedResult.add(Bill.ONE_HUNDRED_EURO, 1)

        // init cash register
        val changeAvailable = Change()
        for(billValues in Bill.values()){
            changeAvailable.add(billValues, 5)
        }
        val cashRegister = CashRegister(changeAvailable)

        // Act
        val result = cashRegister.performTransaction(productPrice, amountPaid)

        // Assert
        assertEquals(expectedResult, result)
    }

}

package com.adyen.android.assignment

import com.adyen.android.assignment.money.Change
import kotlin.math.abs

/**
 * The CashRegister class holds the logic for performing transactions.
 *
 * @param change The change that the CashRegister is holding.
 */
class CashRegister(private val change: Change) {
    /**
     * We can define various error messages inside these enums and can use them from the test class and this class as well
     *
     * @param message This message param is used to define error messages in the enum.
     */
    enum class Errors(val message: String){
        PAID_LESS_AMOUNT("You paid less than the product price. Please give full or more payment."),
        NO_CHANGE_AVAILABLE("Sorry, we don't have any change available at the moment.")
    }

    /**
     * Performs a transaction for a product/products with a certain price and a given amount.
     *
     * @param productPrice The price of the product(s).
     * @param amountPaid The amount paid by the shopper.
     *
     * @return the change for the transaction.
     *
     * @throws TransactionException If the transaction cannot be performed.
     */
    fun performTransaction(productPrice: Long, amountPaid: Change): Change {
        if(productPrice > amountPaid.total){
            throw TransactionException(Errors.PAID_LESS_AMOUNT.message)
        }else{
            return payChangeBackToCustomer(productPrice, amountPaid)
        }
    }

    /**
     * Computes and returns the change for the amount received.
     *
     * @param productPrice The price of the product(s).
     * @param amountPaid The amount paid by the shopper.
     *
     * @return returns minimum change to customer, if no change then return none.
     *
     * @throws TransactionException If change is not available, it throws the exception.
     */

    private fun payChangeBackToCustomer(productPrice: Long, amountPaid: Change) : Change {
        val payBackChange = Change.none()
        val returningAmount = abs(productPrice - amountPaid.total)
        var payBackAmount = returningAmount
        if(returningAmount == 0L){
            return Change.none()
        }else {
            val availableCashFromRegister = change.getElements()
            val reversedAvailableCashFromRegister = availableCashFromRegister.reversed()
            if(returningAmount < availableCashFromRegister.first().minorValue){
                // returning amount is very less. cash register has amount more than this.
                throw TransactionException(Errors.NO_CHANGE_AVAILABLE.message)
            }
            for(availableCash in reversedAvailableCashFromRegister){
                if(returningAmount > availableCash.minorValue){
                    if(payBackAmount == 0L){
                        return payBackChange
                    } else if(payBackAmount < 0){
                        throw TransactionException(Errors.NO_CHANGE_AVAILABLE.message)
                    }

                    // if cash register has more than 1 bill against this amount
                    if(change.getCount(availableCash) > 1){
                        // total available amount of the current bill/ coin
                        val totalCashWithCount = availableCash.minorValue * change.getCount(availableCash)
                        // we will subtract this amount to check if it exceeds the limit of change or not
                        val tempCountAmount = payBackAmount - totalCashWithCount
                        if(tempCountAmount > 0){
                            // we will subtract this amount from the `payBackAmount`
                            payBackAmount -= totalCashWithCount
                            payBackChange.add(availableCash,1)
                        }else{
                            // we will iterate the count and keep on subtracting the amount from the payBackAmount
                            for(countIncrement in 1..change.getCount(availableCash)){
                                val tempAmountAfterSubtractingAvailableCash = payBackAmount - availableCash.minorValue
                                if(tempAmountAfterSubtractingAvailableCash > 0){
                                    // if after subtracting, there is still space that we can subtract more, we subtract from payBackAmount
                                    payBackAmount -= availableCash.minorValue
                                    payBackChange.add(availableCash, 1)
                                }else{
                                    // if subtracted amount is 0, then it means we got the change, otherwise we go to the next element.
                                    if(tempAmountAfterSubtractingAvailableCash == 0L){
                                        payBackAmount -= availableCash.minorValue
                                        payBackChange.add(availableCash, 1)
                                        return payBackChange
                                    }
                                    break
                                }
                            }
                        }
                    } else{
                        // if cash register has only 1 bill against this amount
                        payBackAmount -= availableCash.minorValue
                        payBackChange.add(availableCash,1)
                    }
                }
                // if returning amount is equal to available cash
                if(returningAmount == availableCash.minorValue.toLong()){
                    payBackChange.add(availableCash,1)
                    return payBackChange
                }
            }
        }
        return Change.none()
    }

    /**
     * Transaction Exception class to throw exception in case of failed transaction
     * In case of no change is available, we are using this to throw the exception
     */
    class TransactionException(message: String, cause: Throwable? = null) : Exception(message, cause)
}

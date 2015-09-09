package com.gradlevending;

import java.util.EnumMap;
import java.util.Map;

import org.junit.*;

import com.gradlevending.VendingMachine;
import com.gradlevending.VendingMachine.InsufficentPaymentException;
import com.gradlevending.VendingMachine.Money;
import com.gradlevending.VendingMachine.NotEnoughChangeException;
import com.gradlevending.VendingMachine.NotPaidException;

import static org.junit.Assert.*;
import static com.gradlevending.VendingMachine.*;

public class VendingMachineTest {

	private VendingMachine createWellFilledMachine() {
		VendingMachine machine = new VendingMachine();
		for (Money type : Money.values()) {
			machine.recharge(type, 10);
		}
		return machine;
	}

	@Test
	public void init() {
		VendingMachine machine = new VendingMachine();
		assertEquals(0, machine.getAvailableAmount(Money.FIFTY_DOLLAR));
	}

	@Test
	public void recharge() {
		VendingMachine machine = new VendingMachine();
		machine.recharge(Money.FIVE_HUNDRED_DOLLAR, 5);
		machine.recharge(Money.FIVE_HUNDRED_DOLLAR, 2);
		assertEquals(7, machine.getAvailableAmount(Money.FIVE_HUNDRED_DOLLAR));
	}

	@Test
	public void buyATicket() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = createWellFilledMachine();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Money.TEN_DOLLAR);
		Map<Money, Integer> change = machine.buy();
		EnumMap<Money, Integer> expMoney = new EnumMap<Money, Integer>(Money.class);
		expMoney.put(Money.TWO_DOLLAR, 2);
		expMoney.put(Money.FIFTY_CENT, 1);
		expMoney.put(Money.TEN_CENT, 1);
		assertEquals(expMoney, change);
		EnumMap<Ticket, Integer> expTickets = new EnumMap<Ticket, Integer>(Ticket.class);
		expTickets.put(Ticket.INNER_ZONES, 1);
		assertEquals(expTickets, machine.takeTickets());
		
		assertEquals(11, machine.getAvailableAmount(Money.TEN_DOLLAR));
		assertEquals(8, machine.getAvailableAmount(Money.TWO_DOLLAR));
		assertEquals(9, machine.getAvailableAmount(Money.FIFTY_CENT));
		assertEquals(9, machine.getAvailableAmount(Money.TEN_CENT));
	}
	
	@Test
	public void twoTransactions() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = createWellFilledMachine();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Money.TEN_DOLLAR);
		machine.buy();
		machine.takeTickets();
		machine.selectTicket(Ticket.ALL_ZONES);
		machine.insertMoney(Money.TEN_DOLLAR);
		machine.insertMoney(Money.TEN_CENT);
		machine.insertMoney(Money.TEN_CENT);
		machine.insertMoney(Money.TEN_CENT);
		Map<Money, Integer> change = machine.buy();
		EnumMap<Money, Integer> expMoney = new EnumMap<Money, Integer>(Money.class);
		assertEquals(expMoney, change);
		EnumMap<Ticket, Integer> expTickets = new EnumMap<Ticket, Integer>(Ticket.class);
		expTickets.put(Ticket.ALL_ZONES, 1);
		assertEquals(expTickets, machine.takeTickets());
	}	
	
	@Test(expected=NotPaidException.class)
	public void notPaid() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = createWellFilledMachine();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Money.TEN_DOLLAR);
		machine.takeTickets();
	}
	
	@Test
	public void transactionCanceled() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = createWellFilledMachine();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Money.TEN_DOLLAR);
		Map<Money, Integer> refund = machine.cancel();
		EnumMap<Money, Integer> expMoney = new EnumMap<Money, Integer>(Money.class);
		expMoney.put(Money.TEN_DOLLAR, 1);
		assertEquals(expMoney, refund);
		assertEquals(0, machine.getCurrentPrice());
		assertEquals(0, machine.getPaidSum());
	}
	
	@Test(expected=NotEnoughChangeException.class)
	public void notEnoughChange() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = new VendingMachine();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Money.TEN_DOLLAR);
		machine.buy();
	}
	
	@Test
	public void notEnoughChangeDoesNotAlterAvailableMoney() 
					throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = new VendingMachine();
		machine.recharge(Money.TWENTY_CENT, 1);
		machine.recharge(Money.TEN_CENT, 2);
		machine.selectTicket(Ticket.MINI_TICKET);
		machine.insertMoney(Money.TWO_DOLLAR);
		try {
			machine.buy();
			fail("Exception expected");
		} catch (NotEnoughChangeException ignored) {
			// The purpose of this test it to check the post exception state
		}
		assertEquals(1, machine.getAvailableAmount(Money.TWO_DOLLAR));
		assertEquals(1, machine.getAvailableAmount(Money.TWENTY_CENT));
		assertEquals(2, machine.getAvailableAmount(Money.TEN_CENT));
	}	
		
	@Test(expected=IllegalStateException.class)
	public void finishedTransactionCanceled() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = createWellFilledMachine();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Money.TEN_DOLLAR);
		machine.buy();
		machine.cancel();
	}
	
	@Test(expected=IllegalStateException.class)
	public void addedTicketAfterTransactionFinished() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = createWellFilledMachine();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Money.TEN_DOLLAR);
		machine.buy();
		machine.selectTicket(Ticket.ALL_ZONES);
		machine.takeTickets();
	}
	
	@Test(expected=IllegalStateException.class)
	public void insertedMoneyAfterTransactionFinished() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = createWellFilledMachine();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Money.TEN_DOLLAR);
		machine.buy();
		machine.insertMoney(Money.TEN_DOLLAR);
		machine.takeTickets();
	}
	
	@Test(expected=NotPaidException.class)
	public void twoTransactionsLastNotPaid() 
					throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = createWellFilledMachine();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Money.TEN_DOLLAR);
		machine.buy();
		machine.takeTickets();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.takeTickets();
	}	
	
	@Test(expected=InsufficentPaymentException.class)
	public void notEnoughMoneyForTicket() throws NotEnoughChangeException, InsufficentPaymentException {
		VendingMachine machine = createWellFilledMachine();
		machine.selectTicket(Ticket.INNER_ZONES);
		machine.insertMoney(Money.ONE_DOLLAR);
		machine.buy();
	}	
}

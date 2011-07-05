package br.com.buzzo.moneypig.db.restriction;

import br.com.buzzo.moneypig.db.FetchDefinition;
import br.com.buzzo.moneypig.db.FetchRestriction;
import br.com.buzzo.moneypig.db.ent.Expense;
import br.com.buzzo.moneypig.db.ent.ExpenseRepository;
import br.com.buzzo.moneypig.db.ent.ExpenseRepository.SortExpenseListType;

public class ExpenseOrderRestriction implements FetchRestriction<Expense> {
    private static final long         serialVersionUID = 1L;

    private final SortExpenseListType sort;

    public ExpenseOrderRestriction(final SortExpenseListType sort) {
        this.sort = sort;
    }

    @Override
    public void restrict(final FetchDefinition definition) {
        if (SortExpenseListType.LAST_CREATED_FIRST.equals(this.sort)) {
            definition.setOrderBy(ExpenseRepository.COLUMN_DATE);
            definition.sortDesc();
        } else {
            throw new IllegalArgumentException("Not implemented sort for [" + this.sort + "]");
        }
    }
}

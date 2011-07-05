package br.com.buzzo.moneypig.db.restriction;

import br.com.buzzo.moneypig.db.FetchDefinition;
import br.com.buzzo.moneypig.db.FetchRestriction;
import br.com.buzzo.moneypig.db.ent.Expense;

public class ExpenseLimitRestriction implements FetchRestriction<Expense> {
    private static final long serialVersionUID = 1L;

    private final int         begin;
    private final int         size;

    public ExpenseLimitRestriction(final int begin, final int size) {
        this.begin = begin;
        this.size = size;

    }

    @Override
    public void restrict(final FetchDefinition definition) {
        definition.setLimitBegin(this.begin);
        definition.setLimitSize(this.size);
    }
}

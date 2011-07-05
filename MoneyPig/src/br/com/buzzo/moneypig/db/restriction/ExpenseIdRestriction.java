package br.com.buzzo.moneypig.db.restriction;

import android.provider.BaseColumns;
import br.com.buzzo.moneypig.db.FetchDefinition;
import br.com.buzzo.moneypig.db.FetchRestriction;
import br.com.buzzo.moneypig.db.ent.Expense;

public class ExpenseIdRestriction implements FetchRestriction<Expense> {
    private static final long serialVersionUID = 1L;

    private final long        id;

    public ExpenseIdRestriction(final long id) {
        this.id = id;
    }

    @Override
    public void restrict(final FetchDefinition definition) {
        definition.addWhere(BaseColumns._ID + "=?");
        definition.addWhereArg(String.valueOf(this.id));
    }
}

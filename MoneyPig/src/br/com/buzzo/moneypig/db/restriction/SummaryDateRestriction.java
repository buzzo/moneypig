package br.com.buzzo.moneypig.db.restriction;

import br.com.buzzo.moneypig.db.FetchDefinition;
import br.com.buzzo.moneypig.db.FetchRestriction;
import br.com.buzzo.moneypig.db.ent.ExpenseRepository;
import br.com.buzzo.moneypig.db.ent.Summary;

public class SummaryDateRestriction implements FetchRestriction<Summary> {
    private static final long serialVersionUID = 1L;

    private final Long        begin;
    private final Long        end;
    private final boolean     equal;

    public SummaryDateRestriction(final Long begin, final Long end, final boolean includeEqual) {
        this.begin = begin;
        this.end = end;
        this.equal = includeEqual;
    }

    @Override
    public void restrict(final FetchDefinition definition) {
        String eq = "";
        if (this.equal) {
            eq = "=";
        }
        if (this.begin != null) {
            definition.addWhere(ExpenseRepository.TABLE + "." + ExpenseRepository.COLUMN_DATE + " >" + eq + " ? ");
            definition.addWhereArg(String.valueOf(this.begin));
        }
        if (this.end != null) {
            definition.addWhere(ExpenseRepository.TABLE + "." + ExpenseRepository.COLUMN_DATE + " <" + eq + " ? ");
            definition.addWhereArg(String.valueOf(this.end));
        }
    }
}

package br.com.buzzo.moneypig.db.restriction;

import android.provider.BaseColumns;
import br.com.buzzo.moneypig.db.FetchDefinition;
import br.com.buzzo.moneypig.db.FetchRestriction;
import br.com.buzzo.moneypig.db.ent.Label;

public class LabelIdRestriction implements FetchRestriction<Label> {
    private static final long serialVersionUID = 1L;

    private final int         id;

    public LabelIdRestriction(final int id) {
        this.id = id;
    }

    @Override
    public void restrict(final FetchDefinition definition) {
        definition.addWhere(BaseColumns._ID + "=?");
        definition.addWhereArg(String.valueOf(this.id));
    }
}

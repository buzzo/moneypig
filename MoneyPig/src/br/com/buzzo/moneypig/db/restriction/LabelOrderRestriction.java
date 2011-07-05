package br.com.buzzo.moneypig.db.restriction;

import br.com.buzzo.moneypig.db.FetchDefinition;
import br.com.buzzo.moneypig.db.FetchRestriction;
import br.com.buzzo.moneypig.db.ent.Label;
import br.com.buzzo.moneypig.db.ent.LabelRepository;
import br.com.buzzo.moneypig.db.ent.LabelRepository.SortLabelListType;

public class LabelOrderRestriction implements FetchRestriction<Label> {
    private static final long       serialVersionUID = 1L;

    private final SortLabelListType sort;

    public LabelOrderRestriction(final SortLabelListType sort) {
        this.sort = sort;
    }

    @Override
    public void restrict(final FetchDefinition definition) {
        if (SortLabelListType.ALPHABETIC.equals(this.sort)) {
            definition.setOrderBy(LabelRepository.COLUMN_NAME);
            definition.sortAsc();
        } else if (SortLabelListType.LAST_CREATED_FIRST.equals(this.sort)) {
            definition.setOrderBy(LabelRepository.COLUMN_CREATED);
            definition.sortDesc();
        } else {
            throw new IllegalArgumentException("Not implemented sort for [" + this.sort + "]");
        }
    }
}

package br.com.buzzo.moneypig.db.ent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;

public final class Summary {
    private final List<Label> labels = new ArrayList<Label>();
    private final float       sum;

    public Summary(final float sum, final Label... labels) {
        this.sum = sum;
        if (labels != null) {
            this.labels.addAll(Arrays.asList(labels));
        }
    }

    public List<Label> getLabels() {
        return this.labels;
    }

    public float getSum() {
        return this.sum;
    }

    public static SummaryRepository repository(final Context ctx) {
        return new SummaryRepository(ctx);
    }
}

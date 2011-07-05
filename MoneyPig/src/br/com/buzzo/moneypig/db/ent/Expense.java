package br.com.buzzo.moneypig.db.ent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import br.com.buzzo.moneypig.db.DomainBean;
import br.com.buzzo.moneypig.db.exception.EntityAlreadyExistsException;
import br.com.buzzo.moneypig.db.exception.InvalidEntityUpdateException;

public class Expense implements DomainBean<Expense> {
    public static final double           MAX_VAL = 1000000D;
    public static final SimpleDateFormat SDF;
    static {
        SDF = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    private final ExpenseMode            mode;
    private Date                         date;
    private double                       value;
    private String                       descr;
    private int                          id;
    private final List<Label>            labels  = new ArrayList<Label>();

    public Expense(final ExpenseMode mode, final Date date, final double value, final String descr) {
        validateValue(value);
        validateDescription(descr);
        this.mode = mode;
        this.date = date;
        this.value = value;
        this.descr = descr;
    }

    Expense(final int id, final ExpenseMode mode, final Date date, final double value, final String descr) {
        validateValue(value);
        validateDescription(descr);
        this.id = id;
        this.mode = mode;
        this.date = date;
        this.value = value;
        this.descr = descr;
    }

    public void addLabel(final Label label) {
        this.labels.add(label);
    }

    @Override
    public void delete(final Context ctx) {
        Expense.repository(ctx).delete(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Expense other = (Expense) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public Date getDate() {
        return this.date;
    }

    public String getDescr() {
        return this.descr;
    }

    public int getId() {
        return this.id;
    }

    public List<Label> getLabels() {
        return this.labels;
    }

    public ExpenseMode getMode() {
        return this.mode;
    }

    public double getValue() {
        return this.value;
    }

    @Override
    public int hashCode() {
        final int prime = 37;
        int result = 13;
        result = prime * result + this.id;
        return result;
    }

    @Override
    public void persist(final Context ctx) throws EntityAlreadyExistsException {
        Expense.repository(ctx).persist(this);
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public void setDescr(final String descr) {
        validateDescription(descr);
        this.descr = descr;
    }

    public void setValue(final double value) {
        validateValue(value);
        this.value = value;
    }

    @Override
    public void update(final Context ctx) throws InvalidEntityUpdateException {
        Expense.repository(ctx).update(this);
    }

    private void validateDescription(final String descr) {
        if (descr.length() > 64) {
            throw new IllegalArgumentException("Description must not have length higher than 64.");
        }
    }

    private void validateValue(final double value) {
        if (Math.abs(value) != value) {
            throw new IllegalArgumentException("Value must not be negative");
        }
        if (value > Expense.MAX_VAL) {
            throw new IllegalArgumentException("Value must not be higher than " + Expense.MAX_VAL);
        }
    }

    public static ExpenseRepository repository(final Context ctx) {
        return new ExpenseRepository(ctx);
    }

    public static enum ExpenseMode {
        INCOME(0), OUTCOME(1);

        private final int code;

        private ExpenseMode(final int code) {
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }

        public static ExpenseMode getByCode(final int code) {
            for (final ExpenseMode mode : ExpenseMode.values()) {
                if (mode.getCode() == code) {
                    return mode;
                }
            }
            throw new IllegalArgumentException();
        }
    }
}

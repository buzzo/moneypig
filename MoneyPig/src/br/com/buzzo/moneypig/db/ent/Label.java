package br.com.buzzo.moneypig.db.ent;

import java.util.Date;

import android.content.Context;
import br.com.buzzo.moneypig.db.DomainBean;
import br.com.buzzo.moneypig.db.exception.EntityAlreadyExistsException;
import br.com.buzzo.moneypig.db.exception.InvalidEntityUpdateException;

public class Label implements DomainBean<Label> {
    private final String name;
    private int          id;
    private final Date   created;
    private double       goal;

    public Label(final String labelName) {
        this.name = labelName;
        this.created = new Date();
    }

    public Label(final String labelName, final double goal) {
        validateGoal(goal);
        this.name = labelName;
        this.created = new Date();
        this.goal = goal;
    }

    Label(final int id, final String labelName, final Date created, final double goal) {
        validateGoal(goal);
        this.id = id;
        this.name = labelName;
        this.created = created;
        this.goal = goal;
    }

    @Override
    public void delete(final Context ctx) {
        Label.repository(ctx).delete(this);
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
        final Label other = (Label) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public Date getCreated() {
        return this.created;
    }

    public double getGoal() {
        return this.goal;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.id;
        return result;
    }

    @Override
    public void persist(final Context ctx) throws EntityAlreadyExistsException {
        Label.repository(ctx).persist(this);
    }

    public void setGoal(final double goal) {
        validateGoal(goal);
        this.goal = goal;
    }

    @Override
    public void update(final Context ctx) throws InvalidEntityUpdateException {
        Label.repository(ctx).update(this);
    }

    private void validateGoal(final double value) {
        if (Math.abs(value) != value) {
            throw new IllegalArgumentException("Value must not be negative");
        }
        if (value > Expense.MAX_VAL) {
            throw new IllegalArgumentException("Value must not be higher than " + Expense.MAX_VAL);
        }
    }

    public static LabelRepository repository(final Context ctx) {
        return new LabelRepository(ctx);
    }

}

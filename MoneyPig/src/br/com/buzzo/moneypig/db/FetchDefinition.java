package br.com.buzzo.moneypig.db;

import java.util.ArrayList;
import java.util.List;

public class FetchDefinition {
    private final List<String> columns  = new ArrayList<String>();
    private final List<String> where    = new ArrayList<String>();
    private final List<String> whereArg = new ArrayList<String>();
    private String             order;
    private SortOrder          sort     = SortOrder.DESC;
    private Integer            limitBegin;
    private Integer            limitSize;

    public void addColumn(final String name) {
        this.columns.add(name);
    }

    public void addWhere(final String clausule) {
        this.where.add(clausule);
    }

    public void addWhereArg(final String argument) {
        this.whereArg.add(argument);
    }

    public String[] getColumns() {
        return this.columns.toArray(new String[] {});
    }

    public String getLimit() {
        if (this.limitBegin == null || this.limitSize == null) {
            return null;
        }
        return this.limitBegin + " , " + this.limitSize;
    }

    public String getSortOrder() {
        if (this.order != null) {
            return this.order + " " + this.sort.getValue();
        } else {
            return null;
        }
    }

    public String getWhere() {
        final StringBuilder sb = new StringBuilder("1=1");
        for (final String w : this.where) {
            sb.append(" AND ").append(w);
        }
        return sb.toString();
    }

    public String[] getWhereArgs() {
        return this.whereArg.toArray(new String[] {});
    }

    public void setLimitBegin(final int limitBegin) {
        this.limitBegin = limitBegin;
    }

    public void setLimitSize(final int limitSize) {
        this.limitSize = limitSize;
    }

    public void setOrderBy(final String order) {
        this.order = order;
    }

    public void sortAsc() {
        this.sort = SortOrder.ASC;
    }

    public void sortDesc() {
        this.sort = SortOrder.DESC;
    }

    public static enum SortOrder {
        ASC("ASC"), DESC("DESC");

        private final String val;

        private SortOrder(final String value) {
            this.val = value;
        }

        public String getValue() {
            return this.val;
        }
    }

}

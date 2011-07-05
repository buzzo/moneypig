package br.com.buzzo.moneypig.db;

import java.io.Serializable;

public interface FetchRestriction<R> extends Serializable {

    void restrict(FetchDefinition definition);
}

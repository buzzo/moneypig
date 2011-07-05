package br.com.buzzo.moneypig.db;

import java.util.List;

public interface DomainRepository<R> {

    List<R> list(final FetchRestriction<R>... restrictions);
}

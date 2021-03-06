/* Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.reactive.mutiny.impl;

import io.smallrye.mutiny.Uni;
import org.hibernate.HibernateException;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.reactive.mutiny.Mutiny;
import org.hibernate.reactive.pool.ReactiveConnectionPool;
import org.hibernate.reactive.session.impl.ReactiveCriteriaBuilderImpl;
import org.hibernate.reactive.session.impl.ReactiveSessionImpl;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Implementation of {@link Mutiny.SessionFactory}.
 * <p>
 * Obtained by calling {@link org.hibernate.SessionFactory#unwrap(Class)}.
 */
public class MutinySessionFactoryImpl implements Mutiny.SessionFactory {

	private SessionFactoryImpl delegate;

	public MutinySessionFactoryImpl(SessionFactoryImpl delegate) {
		this.delegate = delegate;
	}

	@Override
	public Mutiny.Session createSession() {
		ReactiveConnectionPool pool = delegate.getServiceRegistry()
				.getService(ReactiveConnectionPool.class);
		return new MutinySessionImpl(
				new ReactiveSessionImpl(
						delegate,
						new SessionFactoryImpl.SessionBuilderImpl<>(delegate),
						pool.getProxyConnection()
				)
		);
	}

	@Override
	public Uni<Mutiny.Session> openSession() throws HibernateException {
		ReactiveConnectionPool pool = delegate.getServiceRegistry()
				.getService(ReactiveConnectionPool.class);
		return Uni.createFrom().completionStage( pool.getConnection() )
				.map( reactiveConnection -> new ReactiveSessionImpl(
						delegate,
						new SessionFactoryImpl.SessionBuilderImpl<>(delegate),
						reactiveConnection
				) )
				.map( MutinySessionImpl::new );
	}

	@Override
	public <T> Uni<T> withSession(Function<Mutiny.Session, Uni<T>> work) {
		return openSession().flatMap(
				session -> work.apply( session ).onTermination().invoke( session::close )
		);
	}

	@Override
	public <T> Uni<T> withTransaction(BiFunction<Mutiny.Session, Mutiny.Transaction, Uni<T>> work) {
		return withSession( (s) -> s.withTransaction( (t) -> work.apply(s, t) ) );
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		return new ReactiveCriteriaBuilderImpl( delegate );
	}

	@Override
	public Metamodel getMetamodel() {
		return delegate.getMetamodel();
	}

	@Override
	public void close() {
		delegate.close();
	}

	@Override
	public boolean isOpen() {
		return delegate.isOpen();
	}
}

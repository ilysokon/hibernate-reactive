package org.hibernate.reactive.stage;

import org.hibernate.SessionBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.engine.query.spi.QueryPlanCache;
import org.hibernate.reactive.engine.spi.RxSessionBuilderImplementor;

/**
 * Factory for {@link RxSession reactive sessions}.
 *
 * @see org.hibernate.reactive.engine.impl.RxSessionFactoryImpl
 */
public interface RxSessionFactory extends SessionFactory {

	/**
	 * Obtain a new {@link RxSession reactive session}, the
	 * main interaction point between the user's program and
	 * Hibernate Reactive.
	 */
	RxSession openRxSession();

	QueryPlanCache getQueryPlanCache();

	@Override
	RxSessionBuilderImplementor withOptions();

	/**
	 * A Hibernate {@link SessionBuilder} allowing specification
	 * of options for a {@link RxSession reactive session}.
	 */
	//TODO: Hibernate Reactive-specific options go here
	interface RxSessionBuilder<T extends RxSessionBuilder> extends SessionBuilder<T> {
		/**
		 * Obtain a new {@link RxSession reactive session}
		 * with these options.
		 */
		RxSession openRxSession();
	}
}
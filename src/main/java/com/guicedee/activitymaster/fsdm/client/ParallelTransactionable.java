package com.guicedee.activitymaster.fsdm.client;

import com.guicedee.client.IGuiceContext;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.function.Function;

public interface ParallelTransactionable
{
  static Uni<Void> runWithNewSession(Function<Mutiny.Session, Uni<Void>> op)
  {
    Mutiny.SessionFactory sessionFactory = IGuiceContext.get(Mutiny.SessionFactory.class);
    return sessionFactory.withTransaction((s, tx) -> op.apply(s));
  }

  static Uni<Void> runWithNewStatelessSession(Function<Mutiny.StatelessSession, Uni<Void>> op)
  {
    Mutiny.SessionFactory sessionFactory = IGuiceContext.get(Mutiny.SessionFactory.class);
    return sessionFactory.withStatelessTransaction((s, tx) -> op.apply(s));
  }

}

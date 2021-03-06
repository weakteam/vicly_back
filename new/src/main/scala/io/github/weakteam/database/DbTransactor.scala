package io.github.weakteam.database

import cats.effect.{Async, Blocker, ContextShift, Resource}
import cats.~>
import doobie.free.connection.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import io.github.weakteam.config.AppConfig.DatabaseConfig

object DbTransactor {

  def resource[F[_]: Async: ContextShift](config: DatabaseConfig): Resource[F, ConnectionIO ~> F] = {
    import config._

    val ceSize = threadPoolSize.value min 32
    for {
      ce <- ExecutionContexts.fixedThreadPool[F](ceSize)
      te <- ExecutionContexts.cachedThreadPool[F]
      blocker = Blocker.liftExecutionContext(te)
      xa <- HikariTransactor.newHikariTransactor(driver.value, url.value, user.value, password, ce, blocker)
      _ <- Resource.liftF(xa.configure { ds =>
            Async[F].delay {
              ds.setMaximumPoolSize(threadPoolSize.value)
              maxLifetime.foreach(v => ds.setMaxLifetime(v.value))
              connTimeout.foreach(v => ds.setConnectionTimeout(v.value))
            }
          })
    } yield xa.trans
  }
}

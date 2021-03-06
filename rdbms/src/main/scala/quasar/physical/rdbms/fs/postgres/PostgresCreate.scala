/*
 * Copyright 2014–2017 SlamData Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package quasar.physical.rdbms.fs.postgres

import slamdata.Predef._
import quasar.physical.rdbms.fs.RdbmsCreate
import quasar.physical.rdbms.common.{CustomSchema, DefaultSchema, TablePath}

import doobie.syntax.string._
import doobie.free.connection.ConnectionIO
import doobie.util.fragment.Fragment
import scalaz.syntax.monad._
import scalaz.syntax.show._

trait PostgresCreate extends RdbmsCreate {

  override def createSchema(schema: CustomSchema): ConnectionIO[Unit] = {
    (fr"CREATE SCHEMA IF NOT EXISTS" ++ Fragment.const(schema.shows)).update.run.void
  }

  override def createTable(tablePath: TablePath): ConnectionIO[Unit] = {
    val createSchemaQuery: ConnectionIO[Unit] = tablePath.schema match {
      case DefaultSchema => ().point[ConnectionIO]
      case c: CustomSchema => createSchema(c)
    }

    (createSchemaQuery *> (fr"CREATE TABLE IF NOT EXISTS" ++ Fragment.const(tablePath.shows) ++ fr"(data json NOT NULL)").update.run)
      .void
  }
}

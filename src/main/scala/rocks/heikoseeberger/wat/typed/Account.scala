/*
 * Copyright 2018 Heiko Seeberger
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

package rocks.heikoseeberger.wat
package typed

import akka.actor.typed.Behavior

/**
  * A typed actor with an immutable behavior, i.e. one that doesn't close over mutable state.
  */
object Account {

  // TODO Adapt untyped protocol to Akka Typed!

  final case object GetBalance
  final case class Balance(balance: Long)

  final case class Deposit(amount: Long)
  final case object Deposited

  final case class Withdraw(amount: Long)
  final case class InsufficientBalance(balance: Long)
  final case object Withdrawn

  def apply(balance: Long = 0): Behavior[Nothing] =
    ???
}

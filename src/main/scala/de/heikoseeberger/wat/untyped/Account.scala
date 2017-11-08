/*
 * Copyright 2017 Heiko Seeberger
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

package de.heikoseeberger.wat
package untyped

import akka.actor.{ Actor, Props }

object Account {

  final case object GetBalance
  final case class Balance(balance: Long)

  final case class Deposit(amount: Long)
  final case object Deposited

  final case class Withdraw(amount: Long)
  final case class InsufficientBalance(balance: Long)
  final case object Withdrawn

  def apply(initialBalance: Long = 0): Props =
    Props(new Account(initialBalance))
}

/**
  * An untyped actor changing its behavior to reflect state changes.
  */
final class Account(initialBalance: Long) extends Actor {
  import Account._

  override def receive = account(initialBalance)

  private def account(balance: Long): Receive = {
    case GetBalance =>
      sender() ! Balance(balance)

    case Deposit(amount) =>
      sender() ! Deposited
      context.become(account(balance + amount))

    case Withdraw(amount) if balance < amount =>
      sender() ! InsufficientBalance

    case Withdraw(amount) =>
      sender() ! Withdrawn
      context.become(account(balance - amount))
  }
}

/**
  * An untyped actor using a mutable variable to reflect state changes.
  */
final class MutAccount(initialBalance: Long) extends Actor {
  import Account._

  private var balance = initialBalance

  override def receive = {
    case GetBalance =>
      sender() ! Balance(balance)

    case Deposit(amount) =>
      balance += amount
      sender() ! Deposited

    case Withdraw(amount) if amount > balance =>
      sender() ! InsufficientBalance

    case Withdraw(amount) =>
      balance -= amount
      sender() ! Withdrawn
  }
}

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

import akka.actor.Props
import akka.testkit.TestProbe
import org.scalatest.{ Matchers, WordSpec }

final class AccountSpec extends BaseAccountSpec {
  override protected def accountProps(initialBalance: Long) = Account(initialBalance)
}

final class MutAccountSpec extends BaseAccountSpec {
  override protected def accountProps(initialBalance: Long) = Props(new MutAccount(initialBalance))
}

sealed trait BaseAccountSpec extends WordSpec with Matchers with BaseUntypedAkkaSpec {
  import Account._

  "Creating an Account and sending GetBalance" should {
    "result in a Balance respose with the initial balance" in {
      val sender             = TestProbe()
      implicit val senderRef = sender.ref

      val account = system.actorOf(accountProps(42))
      account ! GetBalance
      sender.expectMsg(Balance(42))
    }
  }

  "Sending Deposit to an Account" should {
    "result in a Deposited response an increased balance" in {
      val sender             = TestProbe()
      implicit val senderRef = sender.ref

      val account = system.actorOf(accountProps())
      account ! Deposit(42)
      sender.expectMsg(Deposited)
      account ! GetBalance
      sender.expectMsg(Balance(42))
    }
  }

  "Sending Withdraw to an Account" should {
    "(if insufficient balance) result in an InsufficientBalance response and an unchanged balance" in {
      val sender             = TestProbe()
      implicit val senderRef = sender.ref

      val account = system.actorOf(accountProps(42))
      account ! Withdraw(43)
      sender.expectMsg(InsufficientBalance)
      account ! GetBalance
      sender.expectMsg(Balance(42))
    }

    "(if sufficient balance) result in a Withdrawn response and a decrased balance" in {
      val sender             = TestProbe()
      implicit val senderRef = sender.ref

      val account = system.actorOf(accountProps(42))
      account ! Withdraw(42)
      sender.expectMsg(Withdrawn)
      account ! GetBalance
      sender.expectMsg(Balance(0))
    }
  }

  protected def accountProps(initialBalance: Long = 0): Props
}

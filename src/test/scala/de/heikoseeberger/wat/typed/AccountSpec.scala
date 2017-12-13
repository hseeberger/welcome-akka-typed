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
package typed

import akka.typed.Behavior
import akka.typed.testkit.{ EffectfulActorContext, Inbox }
import org.scalatest.{ Matchers, WordSpec }

final class AccountSpec extends WordSpec with Matchers {
  import Account._

  "Creating an Account and sending GetBalance" should {
    "result in a Balance respose with the initial balance" in {
      def verify(account: Behavior[Command]) = {
        val context = new EffectfulActorContext("get-balance", account, 42, null)

        val inbox = Inbox[Balance]("get-balance")
        context.run(GetBalance(inbox.ref))
        inbox.receiveMsg() shouldBe Balance(42)

        context.currentBehavior
      }

      val next = verify(Account(42))
      verify(next) // Evidence that handling GetBalance yields same behavior
    }
  }

  "Sending Deposit to an Account" should {
    "result in a Deposited response and an increased balance" in {
      val context = new EffectfulActorContext("get-balance", Account(), 42, null)

      val depositedInbox = Inbox[Deposited.type]("get-balance")
      context.run(Deposit(42, depositedInbox.ref))
      depositedInbox.receiveMsg() shouldBe Deposited

      val balanceInbox = Inbox[Balance]("get-balance")
      context.run(GetBalance(balanceInbox.ref))
      balanceInbox.receiveMsg() shouldBe Balance(42)
    }
  }

  "Sending Withdraw to an Account" should {
    "(if insufficient balance) result in an InsufficientBalance response and an unchanged balance" in {
      val context = new EffectfulActorContext("withdraw-insufficient", Account(42), 42, null)

      val inbox = Inbox[WithdrawReply]("withdraw-insufficient")
      context.run(Withdraw(43, inbox.ref))
      inbox.receiveMsg() shouldBe InsufficientBalance(42)

      val balanceInbox = Inbox[Balance]("get-balance")
      context.run(GetBalance(balanceInbox.ref))
      balanceInbox.receiveMsg() shouldBe Balance(42)
    }

    "(if sufficient balance) result in a Withdrawn response and a decrased balance" in {
      val context = new EffectfulActorContext("withdraw-sufficient", Account(42), 42, null)

      val withdrawReplyInbox = Inbox[WithdrawReply]("withdraw-sufficient")
      context.run(Withdraw(42, withdrawReplyInbox.ref))
      withdrawReplyInbox.receiveMsg() shouldBe Withdrawn

      val balanceInbox = Inbox[Balance]("get-balance")
      context.run(GetBalance(balanceInbox.ref))
      balanceInbox.receiveMsg() shouldBe Balance(0)
    }
  }
}

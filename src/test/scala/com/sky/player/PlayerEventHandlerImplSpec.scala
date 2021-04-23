package com.sky.player

import com.sky.bookmarking._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, _}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, MustMatchers, WordSpec}
import org.scalatest.PrivateMethodTester._

import java.time.OffsetDateTime

class PlayerEventHandlerImplSpec extends WordSpec with MustMatchers with BeforeAndAfterEach{

  override def beforeEach(): Unit ={
    reset(mockService)
  }

  private val uid1 = "userId"
  private val cid1 = "content1"
  private val cid2 = "content2"

  private val started = "Started"
  private val stopped = "Stopped"
  private val unknown = "Unknown"

  private val bookmark1 = Bookmark(uid1, cid1, 1, OffsetDateTime.now().minusDays(15))
  private val bookmark2 = Bookmark(uid1, cid1, 2, OffsetDateTime.now().minusDays(5))
  private val bookmark3 = Bookmark(uid1, cid1, 3, OffsetDateTime.now())
  private val bookmark6 = Bookmark(uid1, cid2, 6, OffsetDateTime.now())
  private val bookmark4 = Bookmark(uid1, cid1, 100, OffsetDateTime.now().minusDays(31))
  private val bookmark5 = Bookmark(uid1, cid1, 5, OffsetDateTime.now().minusDays(30))

  val mockService: BookmarkingService = MockitoSugar.mock[BookmarkingService]

  val eventHandler:PlayerEventHandler = new PlayerEventHandlerImpl(mockService, uid1)

  "PlayerEventHandler" when {

    "Started event" must {

      "Play from beginning if no bookmark found" in {
        when(mockService.get(any())).thenReturn(Right(Seq.empty))
        val result = eventHandler.handleEvent(started, cid1, 0)
        result mustBe Play(cid1, 0)
      }

      "Play from last stored stream position for one bookmark" in {
        when(mockService.get(any())).thenReturn(Right(List(bookmark1)))
        val result = eventHandler.handleEvent(started, cid1, 1)
        result mustBe Play(cid1, 1)
      }

      "Play from last stored stream position for correct bookmark for requested content id" in {
        when(mockService.get(any())).thenReturn(Right(List(bookmark1, bookmark6)))
        val result = eventHandler.handleEvent(started, cid1, 1)
        result mustBe Play(cid1, 1)
      }

      "Play from last stored stream position for multiple bookmarks" in {
        when(mockService.get(any())).thenReturn(Right(List(bookmark1, bookmark3, bookmark2)))
        val result = eventHandler.handleEvent(started, cid1, 3)
        result mustBe Play(cid1, 3)
      }

      "Play from last stored stream position for multiple bookmarks with expired" in {
        when(mockService.get(any())).thenReturn(Right(List(bookmark1, bookmark4, bookmark3, bookmark2)))
        val result = eventHandler.handleEvent(started, cid1, 3)
        result mustBe Play(cid1, 3)
      }

      "Play from beginning if bookmark is expired" in {
        when(mockService.get(any())).thenReturn(Right(List(bookmark4)))
        val result = eventHandler.handleEvent(started, cid1, 0 )
        result mustBe Play(cid1, 0)
      }

      "Play from beginning if ServiceUnavailable" in {
        when(mockService.get(any())).thenReturn(Left(ServiceUnavailable))
        val result = eventHandler.handleEvent(started, cid1, 0 )
        result mustBe Play(cid1, 0)
      }

      "Stop the player if unexpected error occurred" in {
        when(mockService.get(any())).thenReturn(Left(GenericError))
        val result = eventHandler.handleEvent(started, cid1, 0 )
        result mustBe Stop
      }

    }

    "Stopped event" must {

      "return stop and successfully publish event" in {
        when(mockService.put(any())).thenReturn(Right(Done))
        val result = eventHandler.handleEvent(stopped, cid1, 0 )
        result mustBe Stop
      }

      "Stop the player if unexpected error occurred" in {
        when(mockService.put(any())).thenReturn(Left(ServiceUnavailable))
        val result = eventHandler.handleEvent(stopped, cid1, 0 )
        result mustBe Stop
      }
    }

    "Unknown event" must {

      "do nothing" in {
        val result = eventHandler.handleEvent(unknown, cid1, 0 )
        result mustBe Ignored
      }
    }

    "notOlderThan30Days" must {

      "return true if today's bookmark" in {
        val theFunction = PrivateMethod[Boolean]('notOlderThan30Days)
        val result = eventHandler invokePrivate theFunction(bookmark3)
        result mustBe true
      }

      "return true if new bookmark" in {
        val theFunction = PrivateMethod[Boolean]('notOlderThan30Days)
        val result = eventHandler invokePrivate theFunction(bookmark1)
        result mustBe true
      }

      "return false if 30 days older" in {
        val theFunction = PrivateMethod[Boolean]('notOlderThan30Days)
        val result = eventHandler invokePrivate theFunction(bookmark5)
        result mustBe false
      }

      "return false if older than 30 days" in {
        val theFunction = PrivateMethod[Boolean]('notOlderThan30Days)
        val result = eventHandler invokePrivate theFunction(bookmark4)
        result mustBe false
      }
    }

    "sortBookmarks" must {

      "return same order if ordered" in {
        val theFunction = PrivateMethod[Seq[Bookmark]]('sortBookmarks)
        val result = eventHandler invokePrivate theFunction(Seq(bookmark2, bookmark1))
        result mustBe Seq(bookmark2, bookmark1)
      }

      "return correct order if not ordered" in {
        val theFunction = PrivateMethod[Seq[Bookmark]]('sortBookmarks)
        val result = eventHandler invokePrivate theFunction(Seq(bookmark1, bookmark2))
        result mustBe Seq(bookmark2, bookmark1)
      }

      "return correct order if not ordered and multiple" in {
        val theFunction = PrivateMethod[Seq[Bookmark]]('sortBookmarks)
        val result = eventHandler invokePrivate theFunction(Seq(bookmark5, bookmark1, bookmark2, bookmark4, bookmark3))
        result mustBe Seq(bookmark3, bookmark2, bookmark1, bookmark5, bookmark4)
      }
    }
  }
}

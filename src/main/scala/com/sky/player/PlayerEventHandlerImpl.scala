package com.sky.player

import com.google.inject.Inject
import com.sky.bookmarking.{Bookmark, BookmarkingService, GenericError, ServiceUnavailable}
import com.sky.player.Event.{Started, Stopped}

import java.time.OffsetDateTime
import java.util.logging.{Level, Logger}

class PlayerEventHandlerImpl@Inject()(service: BookmarkingService, userId: String) extends PlayerEventHandler {

  private val logger: Logger = Logger.getLogger(this.getClass.getName)
  private val filterDays: Int = 30
  private val defaultPosition: Int = 0

  override def handleEvent(eventType: String, contentId: String, streamPosition: Int): Action = eventType match {

    case Started => mayBeValidBookMark(contentId)

    case Stopped => publishEvent(contentId, streamPosition)

    case _ =>
      logger.log(Level.SEVERE, s"invalid event type received for userId: $userId & contentId: $contentId")
      Ignored
  }

  private def mayBeValidBookMark(contentId: String): Action = {

    service.get(userId) match {

      case Right(bookmarks) =>
        findLatestBookmark(contentId, bookmarks).headOption match {

          case Some(validBookmark) =>
            Play(contentId, validBookmark.streamPosition)

          case None =>
            logger.log(Level.INFO, s"No existing bookmark found for userId: $userId & contentId: $contentId")
            Play(contentId, defaultPosition)
        }

      case Left(ServiceUnavailable) =>
        logger.log(Level.WARNING, s"received ServiceUnavailable error while calling bookmarks for userId: $userId & contentId: $contentId")
        Play(contentId, defaultPosition)

      case Left(GenericError) =>
        logger.log(Level.SEVERE, s"received GenericError while calling bookmarks for userId: $userId & contentId: $contentId")
        Stop
    }
  }

  private def publishEvent(contentId: String, streamPosition: Int): Action = {

    service.put(Bookmark(userId, contentId, streamPosition)) match {
      case Right(_) => Stop

      case Left(error) =>
        logger.log(Level.SEVERE, s"received GenericError while storing bookmarks for userId: $userId & contentId: $contentId and error was: $error")
        Stop
    }
  }

  private def findLatestBookmark(contentId: String, bookmarks: Seq[Bookmark]): Seq[Bookmark] = {

    val filteredBookmarks = bookmarks.filter(_.contentId == contentId).filter(notOlderThan30Days)

    sortBookmarks(filteredBookmarks)
  }

  private def notOlderThan30Days(bookmark: Bookmark): Boolean = bookmark.timestamp.isAfter(OffsetDateTime.now().minusDays(filterDays))

  private def sortBookmarks(bookmarks: Seq[Bookmark]): Seq[Bookmark] = bookmarks.sortBy(_.timestamp)(Ordering[OffsetDateTime].reverse)

}
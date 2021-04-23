package com.sky.bookmarking

import java.time.OffsetDateTime

/**
 **
 * @param userId a unique identifier which represents a user
 * @param contentId a unique identifier which represents a content (TV shows, Movie, Commercials)
 * @param streamPosition is an offset to the beginning of the content in seconds
 * @param timestamp date and time when the bookmark was created.
 */
case class Bookmark(
  userId: String,
  contentId: String,
  streamPosition: Int,
  timestamp: OffsetDateTime = OffsetDateTime.now()
)

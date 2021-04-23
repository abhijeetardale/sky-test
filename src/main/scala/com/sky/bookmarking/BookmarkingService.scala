package com.sky.bookmarking

trait BookmarkingService {

  /**
   * Stores a given [[Bookmark]]
   **
  @param bookmark
   * @return store result
   */

  def put(bookmark: Bookmark): Either[Error,Done]
  /**
   * Retrieves all [[Bookmark]]s for a given user
   **

  @param userId a unique identifier which represents a user
   * @return bookmarks
   */
  def get(userId: String): Either[Error, Seq[Bookmark]]

}

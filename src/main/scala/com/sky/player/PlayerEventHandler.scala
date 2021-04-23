package com.sky.player

trait PlayerEventHandler {

  /**
   * @param eventType should handle ‘Started‘, ‘Stopped‘
   * @param contentId a unique identifier which represents a content (TV shows, Movie, Commercials)
   * @param streamPosition is an offset to the beginning of the content in seconds
   * @return next action for the player
   */
  def handleEvent(eventType: String, contentId: String, streamPosition: Int): Action

}

package com.sky.player

sealed trait Action

case class Play(contentId: String, streamPosition: Int) extends Action

case object Stop extends Action

case object Ignored extends Action

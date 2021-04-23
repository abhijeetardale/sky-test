package com.sky.bookmarking

sealed trait Done

case object Done extends Done

sealed trait Error extends Throwable

case object ServiceUnavailable extends Error

case object GenericError extends Error

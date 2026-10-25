package pe.edu.upeu.pharmamobile.data.local

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun epochMillisNow(): Long = Clock.System.now().toEpochMilliseconds()

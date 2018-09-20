package com.example.test.project.listeners

import band.mawi.android.bluetooth.model.motion.MotionPacket

interface MotionActionListener {
  fun onMotionChanged(motionPacket: MotionPacket)
}
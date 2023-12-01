package org.ruang.cmd

trait Display[T] {
  def show(text: T): Unit
}

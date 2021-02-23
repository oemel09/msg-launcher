package de.oemel09.msglauncher.ui

interface OnItemDragListener {

    fun onItemDismiss(position: Int)
    fun onItemMove(fromPosition: Int, toPosition: Int)
}

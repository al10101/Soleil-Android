package com.al10101.android.soleil.nodes

open class Node {

    var parent: Node? = null
    var children: MutableList<ChildNode> = mutableListOf()

    fun add(childNode: ChildNode) {
        children.add(childNode)
        childNode.parent = this
    }

    fun remove(childNode: ChildNode) {
        for (child in childNode.children) {
            child.parent = this
            children.add(child)
        }
        val index = children.indexOf(
            children.find { it == childNode } ?: return
        )
        children.removeAt(index)
        childNode.parent = null
        childNode.children = mutableListOf()
    }

}
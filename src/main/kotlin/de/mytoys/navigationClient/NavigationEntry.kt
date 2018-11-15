package de.mytoys.navigationClient

/**
 * Created by fabian on 12.11.18.
 */
class NavigationEntry(var type: String, var label: String, var children: List<NavigationEntry>, var url: String) {
}
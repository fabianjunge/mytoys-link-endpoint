package de.mytoys.navigationClient

/**
 * Created by fabian on 12.11.18.
 */
interface NavigationApiInterface {
    fun GetRootNavigationEntries() : List<NavigationEntry>
}
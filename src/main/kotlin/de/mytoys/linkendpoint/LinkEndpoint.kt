package de.mytoys.linkendpoint

import de.mytoys.navigationClient.NavigationEntry
import de.mytoys.navigationClient.impl.NavigationApiClient
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMethod
import java.util.*

/**
 * Created by fabian on 12.11.18.
 */
@RestController
class LinkEndpoint {

    var navigationApiClient = NavigationApiClient("https://mytoysiostestcase1.herokuapp.com") // todo: pass url via ENV in
    var logger = LoggerFactory.getLogger(LinkEndpoint::class.java)

    @RequestMapping("/links", method = arrayOf(RequestMethod.GET))
    fun GetLinks(@RequestParam(value = "parent", required = false) parentLabel: String?,
                 @RequestParam(value = "sort", required = false) sortTerm: String?) : ResponseEntity<List<Link>> {

        var entries: List<NavigationEntry> = listOf()

        try {
            entries = navigationApiClient.GetRootNavigationEntries()
        }
        catch (e: Exception) {
            logger.error(e.toString())
            return ResponseEntity<List<Link>>(listOf(), null, HttpStatus.INTERNAL_SERVER_ERROR)
        }

        if(parentLabel.isNullOrBlank()) {
            val links = extractLinksFormNavigationEntry(entries)
            logger.info("Return all ${links.size} links")
            return ResponseEntity<List<Link>>(links, null, HttpStatus.OK)
        }

        val parentNavigationEntry = getSectionOrNodeByLabel(entries, parentLabel.orEmpty())
        if(parentNavigationEntry == null) {
            // Q: Think about HTTP-response-headers and -codes (e.g. there is no item whose label matches parentLabel).
            // A: I don't really know if 404 Not Found is here really meaningful. The endpoint works fine and an empty array is
            // a valid response. I would use 404 if a "detail" resource like /user/$username isn't available.
            return ResponseEntity<List<Link>>(listOf(), null, HttpStatus.OK)
        }

        var links = parentNavigationEntry.getLinks(parentNavigationEntry)

        if(!sortTerm.isNullOrBlank()) {
            links = sortLinks(sortTerm.orEmpty(), links)
        }

        logger.info("Return ${links.size} links for parentLabel \"$parentLabel\"")
        return ResponseEntity<List<Link>>(links, null, HttpStatus.OK)
    }

    fun sortLinks(sortTerm: String, links: List<Link>) : List<Link> {
        val comparators = mutableListOf<Comparator<Link>>()

        for(key in sortTerm.split(",")) {
            // The value of this parameter is a comma-separated list of sort keys
            var sortKey = key

            if(sortKey.contains(":").not()) {
                sortKey += ":asc" // asc is default case
            }

            when(sortKey) {
                "label:asc" -> comparators.add(compareBy({ it.label }))
                "label:desc" -> comparators.add(compareByDescending({ it.label }))
                "url:asc" -> comparators.add(compareBy({ it.url }))
                "url:desc" -> comparators.add(compareByDescending({ it.url }))
            }
        }

        if(comparators.isEmpty()) {
            return links
        }

        // Get first comparator and removed from list
        var comparator = comparators.first()
        comparators.remove(comparators.first())

        // Chain all other comparators behind the first comparator
        while(comparators.isEmpty().not()) {
            comparator = comparator.then(comparators.first())
            comparators.remove(comparators.first())
        }

        return links.sortedWith(comparator)
    }

    // Return first section or node that match the label
    fun getSectionOrNodeByLabel(entries: List<NavigationEntry>, label: String) : NavigationEntry? {
        entries.forEach {
            if (it.label == label) {
                return it
            }

            if(it.isLink().not() && it.children.isNotEmpty()) {
                val nodeOrSection = getSectionOrNodeByLabel(it.children, label)

                if(nodeOrSection != null) {
                    return nodeOrSection
                }
            }
        }

        return null
    }

    fun extractLinksFormNavigationEntry(entries: List<NavigationEntry>) : List<Link> {
        val links = mutableListOf<Link>()
        entries.forEach {
            val entriesFrom = it.getLinks()
            logger.info("Extracted ${entriesFrom.size} links for ${it.label}")

            links.addAll(entriesFrom)
        }

        return links
    }
}

fun  NavigationEntry.getLinks(parent: NavigationEntry? = null) : List<Link> {
    val links = mutableListOf<Link>()

    if(this.isLink().not()) {
        val childLinks = mutableListOf<Link>()

        this.children.forEach {

            if(it.isLink()) {
                childLinks.add(Link(it.label, it.url))
            } else {
                childLinks.addAll(it.getLinks())
            }
        }

        if(this != parent) {
            // Do not add queried parent to label
            childLinks.forEach({link ->
                link.label = "${this.label} - ${link.label}"
            })
        }

        return childLinks

    } else {
        links.addAll(this.getLinks())
    }

    return links
}

fun NavigationEntry.isLink() : Boolean {
    return this.type == "link" || this.type == "external-link"
}
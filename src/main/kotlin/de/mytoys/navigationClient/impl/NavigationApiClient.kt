package de.mytoys.navigationClient.impl

import org.slf4j.LoggerFactory
import com.google.gson.Gson
import de.mytoys.navigationClient.NavigationApiInterface
import de.mytoys.navigationClient.NavigationEntry
import khttp.get

/**
 * Created by fabian on 12.11.18.
 */
class NavigationApiClient(var baseUrl: String) : NavigationApiInterface {

    var logger = LoggerFactory.getLogger(NavigationApiClient::class.java)

    fun getApiAuthKey() : String {
        // use static api key
        return "hz7JPdKK069Ui1TRxxd1k8BQcocSVDkj219DVzzD"
    }

    override fun GetRootNavigationEntries() : List<NavigationEntry>  {
        val requestUrl = baseUrl + "/api/navigation"

        logger.info("Get navigation entries from $requestUrl")
        val response = get(requestUrl, headers=mapOf("x-api-key" to getApiAuthKey()))

        if(response.statusCode != 200) {
            throw Exception("Request on $requestUrl returned StatusCode ${response.statusCode}; expected 200")
        }

        // Use Gson for lazy parsing JSON to POJO
        val entries = Gson().fromJson(response.jsonObject.get("navigationEntries").toString(), Array<NavigationEntry>::class.java)

        logger.info("Got ${entries.size} main navigation entries")

        // todo: Maybe caching result for a shot time? Looks very static...

        return entries.toList()
    }


}
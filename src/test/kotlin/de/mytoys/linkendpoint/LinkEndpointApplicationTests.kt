package de.mytoys.linkendpoint

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.MockitoAnnotations
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders


@RunWith(SpringRunner::class)
@SpringBootTest
class LinkEndpointApplicationTests {
	lateinit var mvc: MockMvc

	@InjectMocks
	lateinit var endpoint: LinkEndpoint

	@Before
	fun setup() {
		MockitoAnnotations.initMocks(this);

		mvc = MockMvcBuilders.standaloneSetup(endpoint).build()
	}

	@Test
	fun testSorting() {
		val linksUnsorted = listOf<Link>(
				Link("3", ""),
				Link("1", ""),
				Link("4", ""),
				Link("2", "a"),
				Link("2", "b"),
				Link("2", "c")
		)

		val sortedLinksAscDefault = endpoint.sortLinks("label", linksUnsorted)

		assert(sortedLinksAscDefault.first().label == "1")
		assert(sortedLinksAscDefault.last().label == "4")

		val sortedLinksAsc = endpoint.sortLinks("label:asc", linksUnsorted)

		assert(sortedLinksAsc.first().label == "1")
		assert(sortedLinksAsc.last().label == "4")

		val sortedLinksDesc = endpoint.sortLinks("label:desc", linksUnsorted)

		assert(sortedLinksDesc.first().label == "4")
		assert(sortedLinksDesc.last().label == "1")

		val sortedLinksAscWithUrlAsc = endpoint.sortLinks("label:asc,url:asc", linksUnsorted)
		assert(sortedLinksAscWithUrlAsc.first().label == "1")
		assert(sortedLinksAscWithUrlAsc.get(1).url == "a")
		assert(sortedLinksAscWithUrlAsc.get(2).url == "b")
		assert(sortedLinksAscWithUrlAsc.get(3).url == "c")
		assert(sortedLinksAscDefault.last().label == "4")

		val sortedLinksAscWithUrlDesc = endpoint.sortLinks("label:asc,url:desc", linksUnsorted)
		assert(sortedLinksAscWithUrlDesc.first().label == "1")
		assert(sortedLinksAscWithUrlDesc.get(1).url == "c")
		assert(sortedLinksAscWithUrlDesc.get(2).url == "b")
		assert(sortedLinksAscWithUrlDesc.get(3).url == "a")
		assert(sortedLinksAscWithUrlDesc.last().label == "4")
	}

	@Test
	fun testEndpointStatusOk() {
		mvc.perform(MockMvcRequestBuilders.get("/links")).andExpect(MockMvcResultMatchers.status().isOk)
	}

	@Test
	fun testCaseForAlterSection() {
		val result = """
		[
		{
			"label": "Baby & Kleinkind - 0-6 Monate",
			"url": "http://www.mytoys.de/0-6-months/"
		},
		{
			"label": "Baby & Kleinkind - 7-12 Monate",
			"url": "http://www.mytoys.de/7-12-months/"
		},
		{
			"label": "Baby & Kleinkind - 13-24 Monate",
			"url": "http://www.mytoys.de/13-24-months/"
		},
		{
			"label": "Kindergarten - 2-3 Jahre",
			"url": "http://www.mytoys.de/24-47-months/"
		},
		{
			"label": "Kindergarten - 4-5 Jahre",
			"url": "http://www.mytoys.de/48-71-months/"
		},
		{
			"label": "Grundschule - 6-7 Jahre",
			"url": "http://www.mytoys.de/72-95-months/"
		},
		{
			"label": "Grundschule - 8-9 Jahre",
			"url": "http://www.mytoys.de/96-119-months/"
		},
		{
			"label": "Teenager - 10-12 Jahre",
			"url": "http://www.mytoys.de/120-155-months/"
		},
		{
			"label": "Teenager - Über 12 Jahre",
			"url": "http://www.mytoys.de/over-156-months/"
		}
		]
		"""

		mvc.perform(MockMvcRequestBuilders.get("/links")
				.param("parent", "Alter")).andExpect(MockMvcResultMatchers.content().json(result))
	}

	@Test
	fun testCaseForBabyUndKleinkindSection() {
		val result = """
		[
			{
				"label": "0-6 Monate",
				"url": "http://www.mytoys.de/0-6-months/"
			},
			{
				"label": "7-12 Monate",
				"url": "http://www.mytoys.de/7-12-months/"
			},
			{
				"label": "13-24 Monate",
				"url": "http://www.mytoys.de/13-24-months/"
			}
		]
		"""

		mvc.perform(MockMvcRequestBuilders.get("/links")
				.param("parent", "Baby & Kleinkind")).andExpect(MockMvcResultMatchers.content().json(result))
	}

	@Test
	fun testCaseForParentNodeOrSectionThatNotExist() {
		val result = """
		[

		]
		"""

		mvc.perform(MockMvcRequestBuilders.get("/links")
				.param("parent", "Scheibenkleister123!§$%&&/()=")).andExpect(MockMvcResultMatchers.content().json(result))
	}

}

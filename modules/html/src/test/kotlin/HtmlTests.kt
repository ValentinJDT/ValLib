import fr.valentinjdt.lib.html.body
import fr.valentinjdt.lib.html.html
import kotlin.test.Test
import kotlin.test.assertTrue

class HtmlTests {

    @Test
    fun testHtmlRendering() {
        val htmlContent = "<html lang=\"en\"><body><h1>Hello, World!</h1></body></html>"

        val html = html {
            set("lang", "en")
            "body" {
                "h1" {
                    +"Hello, World!"
                }
            }
        }.toString()

        assertTrue(htmlContent.contains(html), "HTML content should contain the expected header")
    }

    @Test
    fun testHtmlParsing() {
        val htmlContent = html {
            "body" {
                +"<div class='test'>This is a test</div>"
            }
        }.toString()

        assertTrue(htmlContent.contains("class='test'"), "HTML content should contain the expected class attribute")
    }

    @Test
    fun testTagProperties() {
        val tag = html {
            set("id", "main")
            className = "container"
            set("data-role", "main-content")
        }

        val htmlString = tag.toString()
        assertTrue(htmlString.contains("id=\"main\"") && tag.get("id").equals("main"), "HTML should contain id attribute")
        assertTrue(htmlString.contains("class=\"container\"") && tag.get("class").equals("container"), "HTML should contain class attribute")
        assertTrue(htmlString.contains("data-role=\"main-content\"") && tag.get("data-role").equals("main-content"), "HTML should contain data-role attribute")
    }

    @Test
    fun testNestedTags() {
        val tag = html {
            "head" {
                "title" {
                    +"Page Title"
                }
                "meta" {
                    set("charset", "UTF-8")
                }
            }
            "body" {
                "div" {
                    className = "container"
                    "p" {
                        +"Paragraph content"
                    }
                    "ul" {
                        "li" { +"Item 1" }
                        "li" { +"Item 2" }
                        "li" { +"Item 3" }
                    }
                }
            }
        }

        val htmlString = tag.toString()
        assertTrue(htmlString.contains("<title>Page Title</title>"), "HTML should contain title tag")
        assertTrue(htmlString.contains("<meta charset=\"UTF-8\"/>"), "HTML should contain meta tag")
        assertTrue(htmlString.contains("<div class=\"container\">"), "HTML should contain div with class")
        assertTrue(htmlString.contains("<p>Paragraph content</p>"), "HTML should contain paragraph")
        assertTrue(htmlString.contains("<li>Item 1</li>"), "HTML should contain list items")
    }

    @Test
    fun testEmptyTags() {
        val tag = html {
            "img" {
                set("src", "image.jpg")
                set("alt", "An image")
            }
            "input" {
                set("type", "text")
                set("placeholder", "Enter text")
            }
            "br" {}
        }

        val htmlString = tag.toString()
        println(htmlString)
        assertTrue(htmlString.contains("<img src=\"image.jpg\" alt=\"An image\"/>"), "Self-closing tags should be properly rendered")
        assertTrue(htmlString.contains("<input type=\"text\" placeholder=\"Enter text\"/>"), "Self-closing input tag should be properly rendered")
        assertTrue(htmlString.contains("<br/>"), "Empty br tag should be properly rendered")
    }

    @Test
    fun testClassNameProperty() {
        val tag = html {
            "div" {
                className = "container main"
                "p" {
                    className = "text bold"
                    +"Text content"
                }
            }
        }

        val htmlString = tag.toString()
        assertTrue(htmlString.contains("<div class=\"container main\">"), "HTML should contain div with multiple classes")
        assertTrue(htmlString.contains("<p class=\"text bold\">"), "HTML should contain p with multiple classes")
    }

    @Test
    fun testHtmlAndBodyConvenienceClasses() {
        val htmlTag = html {
            set("lang", "fr")
        }

        val bodyTag = body {
            className = "main-body"
            "div" {
                +"Content"
            }
        }

        assertTrue(htmlTag.toString().startsWith("<html"), "HTML tag should be properly rendered")
        assertTrue(bodyTag.toString().startsWith("<body"), "Body tag should be properly rendered")
        assertTrue(htmlTag.toString().contains("lang=\"fr\""), "HTML tag should contain lang attribute")
        assertTrue(bodyTag.toString().contains("class=\"main-body\""), "Body tag should contain class attribute")
    }

    @Test
    fun testCamelCaseTagConversion() {
        val tag = html {
            "mainContainer" {
                +"This should be converted to main-container"
            }
            "customButton" {
                set("id", "btnSubmit")
                +"Click me"
            }
        }

        val htmlString = tag.toString()
        assertTrue(htmlString.contains("<main-container>"), "CamelCase should be converted to kebab-case")
        assertTrue(htmlString.contains("<custom-button"), "CamelCase should be converted to kebab-case")
    }
}
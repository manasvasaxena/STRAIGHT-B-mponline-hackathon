package com.learnquest.mp.markdown

import org.commonmark.Extension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

/** CommonMark/GFM renderer based on Mora's reader implementation. */
object MarkdownRenderer {
    private val extensions: List<Extension> = listOf(
        TablesExtension.create(),
        StrikethroughExtension.create(),
        TaskListItemsExtension.create(),
    )

    private val parser = Parser.builder()
        .extensions(extensions)
        .build()

    private val htmlRenderer = HtmlRenderer.builder()
        .extensions(extensions)
        .escapeHtml(true)
        .sanitizeUrls(true)
        .build()

    fun render(markdown: String, isHindi: Boolean): String {
        val body = htmlRenderer.render(parser.parse(markdown))
        val language = if (isHindi) "hi" else "en"
        return """
            <!doctype html>
            <html lang="$language">
            <head>
              <meta charset="utf-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover" />
              <style>
                :root { color-scheme: light; }
                * { box-sizing: border-box; }
                html, body { margin: 0; padding: 0; background: #FFFFFF; }
                body {
                  color: #1E293B;
                  font-family: system-ui, -apple-system, BlinkMacSystemFont,
                    "Noto Sans Devanagari", "Noto Sans", "Segoe UI", sans-serif;
                  font-size: 16px;
                  line-height: 1.72;
                  overflow-wrap: anywhere;
                  padding: 20px 18px 72px;
                  -webkit-font-smoothing: antialiased;
                  text-rendering: optimizeLegibility;
                }
                #write { width: 100%; max-width: 760px; margin: 0 auto; }
                h1, h2, h3, h4, h5, h6 {
                  color: #0F172A;
                  line-height: 1.3;
                  font-weight: 700;
                  letter-spacing: -0.01em;
                  overflow-wrap: anywhere;
                }
                h1 { font-size: 1.75em; margin: 0.25em 0 0.75em; color: #B45309; border-bottom: 2px solid #E2E8F0; padding-bottom: 8px; }
                h2 { font-size: 1.42em; margin: 1.6em 0 0.65em; border-bottom: 1px solid #F1F5F9; padding-bottom: 4px; }
                h3 { font-size: 1.18em; margin: 1.4em 0 0.5em; }
                h4, h5, h6 { font-size: 1.05em; margin: 1.2em 0 0.45em; }
                p { margin: 0 0 1em; }
                a { color: #2563EB; text-decoration-thickness: 1px; text-underline-offset: 0.18em; }
                strong { color: #0F172A; font-weight: 750; }
                em { color: #334155; }
                del { color: #64748B; }
                hr { border: 0; border-top: 1px solid #CBD5E1; margin: 2em 0; }
                ul, ol { padding-left: 1.45em; margin: 0.35em 0 1.05em; }
                li { margin: 0.28em 0; padding-left: 0.12em; }
                li > p { margin: 0.3em 0; }
                input[type="checkbox"] { accent-color: #D97706; transform: scale(1.08); margin-right: 0.5em; }
                blockquote {
                  margin: 1.25em 0;
                  padding: 0.1em 0 0.1em 1em;
                  border-left: 4px solid #F59E0B;
                  background: #FFFBEB;
                  color: #92400E;
                }
                blockquote > :last-child { margin-bottom: 0; }
                code {
                  font-family: ui-monospace, "SFMono-Regular", Consolas, "Liberation Mono", monospace;
                  font-size: 0.88em;
                  background: #F1F5F9;
                  border: 1px solid #E2E8F0;
                  border-radius: 5px;
                  padding: 0.13em 0.35em;
                  overflow-wrap: anywhere;
                }
                pre {
                  margin: 1.35em 0;
                  padding: 15px 16px;
                  overflow-x: auto;
                  background: #F8FAFC;
                  border: 1px solid #CBD5E1;
                  border-radius: 12px;
                  line-height: 1.55;
                  -webkit-overflow-scrolling: touch;
                }
                pre code { background: transparent; border: 0; padding: 0; font-size: 0.86em; }
                img { display: block; max-width: 100%; height: auto; margin: 1.4em auto; border-radius: 12px; }
                table {
                  display: block;
                  width: 100%;
                  overflow-x: auto;
                  border-collapse: collapse;
                  margin: 1.35em 0;
                  -webkit-overflow-scrolling: touch;
                }
                th, td {
                  min-width: 112px;
                  padding: 10px 12px;
                  border: 1px solid #CBD5E1;
                  text-align: left;
                  vertical-align: top;
                }
                th { background: #F1F5F9; font-weight: 700; }
                @media (min-width: 720px) {
                  body { padding-left: 34px; padding-right: 34px; }
                }
              </style>
            </head>
            <body><main id="write">$body</main></body>
            </html>
        """.trimIndent()
    }
}

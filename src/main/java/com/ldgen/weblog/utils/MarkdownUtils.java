package com.ldgen.weblog.utils;

import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.List;

/**
 * Markdown 工具类
 */
public final class MarkdownUtils {

    private static final List<Extension> EXTENSIONS = List.of(TablesExtension.create());

    private static final Parser PARSER = Parser.builder()
            .extensions(EXTENSIONS)
            .build();

    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder()
            .extensions(EXTENSIONS)
            .softbreak("<br />")
            .build();

    private MarkdownUtils() {
    }

    /**
     * 将 Markdown 转成 HTML
     *
     * @param markdown Markdown 正文
     * @return HTML 内容
     */
    public static String markdownToHtml(String markdown) {
        String safeMarkdown = markdown == null ? "" : markdown;
        Node document = PARSER.parse(safeMarkdown);
        return HTML_RENDERER.render(document);
    }

    /**
     * 将 Markdown 提取为纯文本
     *
     * @param markdown Markdown 正文
     * @return 纯文本
     */
    public static String markdownToPlainText(String markdown) {
        String html = markdownToHtml(markdown);
        return html.replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replaceAll("\\s+", " ")
                .trim();
    }
}

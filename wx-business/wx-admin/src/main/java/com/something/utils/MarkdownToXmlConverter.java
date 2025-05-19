package com.something.utils;

import java.util.ArrayList;
import java.util.List;

public class MarkdownToXmlConverter {
    public static String convert(String markdown) {
        StringBuilder result = new StringBuilder();
        List<String> paragraphLines = new ArrayList<>();

        String[] lines = markdown.split("\n");
        for (int i = 0; i <= lines.length; i++) {
            String line = (i < lines.length) ? lines[i] : "";

            // 处理段落
            if (isParagraphLine(line)) {
                paragraphLines.add(line.trim());
                continue;
            }

            // 提交段落
            if (!paragraphLines.isEmpty()) {
                flushParagraph(result, paragraphLines);
            }

            // 处理列表项
            if (isListItem(line)) {
                int indentLevel = getIndentLevel(line);
                String content = line.trim().substring(2).trim(); // 去掉 * 和空格

                // 根据缩进层级选择符号
                String bullet = indentLevel == 1 ? "●" : "✦";

                // 添加列表项
                String processedContent = processInlineElements(content);
                result.append(bullet).append(" ").append(processedContent).append("\n\n");

            } else if (!line.trim().isEmpty()) {
                // 其他普通段落
                String processedLine = processInlineElements(line.trim());
                result.append(processedLine).append("\n\n");
            }
        }

        return result.toString().trim();
    }

    private static boolean isParagraphLine(String line) {
        return !line.trim().isEmpty() && !line.trim().startsWith("*") && !line.trim().startsWith("#");
    }

    private static boolean isListItem(String line) {
        return line.trim().startsWith("*");
    }

    private static int getIndentLevel(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') count++;
            else break;
        }
        return count / 4 + 1; // 每 4 个空格为一个缩进层级
    }

    private static void flushParagraph(StringBuilder result, List<String> lines) {
        StringBuilder combined = new StringBuilder();
        for (String line : lines) {
            combined.append(line).append(" ");
        }
        String content = processInlineElements(combined.toString().trim());
        result.append(content).append("\n\n");
        lines.clear();
    }

    private static String processInlineElements(String text) {
        // 替换加粗 **...**
        text = text.replaceAll("\\*\\*(.+?)\\*\\*", "**$1**");
        return text;
    }
}
